package envelope;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

public class DigitalEnvelope {

    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    // 데이터 묶음
    public static class SignedDocument implements Serializable {

        private static final long serialVersionUID = 1L;

        private final byte[] signature;       // 전자서명
        private final byte[] plainText;       // 평문 (원문)
        private final PublicKey senderPubKey; // 송신자의 공개키

        public SignedDocument(byte[] signature, byte[] plainText, PublicKey senderPubKey) {
            this.signature = signature;
            this.plainText = plainText;
            this.senderPubKey = senderPubKey;
        }

        public byte[] getSignature()       { return signature; }
        public byte[] getPlainText()       { return plainText; }
        public PublicKey getSenderPubKey() { return senderPubKey; }
    }


    //암호화된 데이터 묶음
    public static class EnvelopeContent implements Serializable {

        private static final long serialVersionUID = 1L;

        private final byte[] encryptedData; // 암호화된 SignedDocument
        private final byte[] sealedKey;     // AES 비밀키를 수신자의 공개키로 비대칭 암호화한 전자봉투

        public EnvelopeContent(byte[] encryptedData, byte[] sealedKey) {
            this.encryptedData = encryptedData;
            this.sealedKey = sealedKey;
        }

        public byte[] getEncryptedData() { return encryptedData; }
        public byte[] getSealedKey()     { return sealedKey; }
    }


    // (전자서명 + 평문 + 송신자 공개키) 묶음을 직렬화하여 byte[]로 변환
    public static byte[] serialize(SignedDocument doc) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(doc);
            return baos.toByteArray();
        }
    }

    // 전자봉투 생성 (송신자가 AES 비밀키를 직접 지정하는 버전)
    public static EnvelopeContent seal(byte[] serialized, PublicKey receiverPub, SecretKey sessionKey)
            throws GeneralSecurityException {
        byte[] encryptedData = encrypt(serialized, sessionKey, AES_TRANSFORMATION);
        byte[] sealedKey = encrypt(sessionKey.getEncoded(), receiverPub, RSA_TRANSFORMATION);
        return new EnvelopeContent(encryptedData, sealedKey);
    }

    // 전자봉투 생성 (AES 비밀키를 내부에서 자동 생성하는 오버로드)
    public static EnvelopeContent seal(byte[] serialized, PublicKey receiverPub) throws GeneralSecurityException {
        SecretKey sessionKey = KeyGenerator.getInstance("AES").generateKey();
        return seal(serialized, receiverPub, sessionKey);
    }

    // 대칭/비대칭 공통 암호화 헬퍼 (seal 내부 전용)
    private static byte[] encrypt(byte[] data, Key key, String transformation) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }


    // 전자봉투를 열어 본문을 복호화한 byte[]로 반환
    public static byte[] open(EnvelopeContent envelope, PrivateKey receiverPriv) throws GeneralSecurityException {
        byte[] keyBytes = decrypt(envelope.getSealedKey(), receiverPriv, RSA_TRANSFORMATION);
        SecretKey sessionKey = new SecretKeySpec(keyBytes, "AES");
        return decrypt(envelope.getEncryptedData(), sessionKey, AES_TRANSFORMATION);
    }

    // 복호화한 byte[]를 SignedDocument 객체로 복원
    public static SignedDocument deserialize(byte[] serialized) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serialized))) {
            return (SignedDocument) ois.readObject();
        }
    }

    // 대칭/비대칭 공통 복호화 헬퍼 (open 내부 전용)
    private static byte[] decrypt(byte[] data, Key key, String transformation) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }
}
