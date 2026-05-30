package envelope;

import javax.crypto.*;
import java.io.*;
import java.security.*;

public final class DigitalEnvelope {

    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    private DigitalEnvelope() {
    }

    // -------------------------------------------------------------------------

    public static class SignedDocument implements Serializable {

        private static final long serialVersionUID = 1L;

        private final byte[] signature;
        private final byte[] plainText;
        private final PublicKey senderPubKey;
        
        public SignedDocument(byte[] signature, byte[] plainText, PublicKey senderPubKey) {
            this.signature = signature.clone();
            this.plainText = plainText.clone();
            this.senderPubKey = senderPubKey;
        }

        public byte[] getSignature() {
            return signature.clone();
        }

        public byte[] getPlainText() {
            return plainText.clone();
        }

        public PublicKey getSenderPubKey() {
            return senderPubKey;
        }
    }

    // -------------------------------------------------------------------------

    public static class EnvelopeContent implements Serializable {

        private static final long serialVersionUID = 1L;

        private final byte[] encryptedData; // AES로 암호화된 SignedDocument
        private final byte[] encryptedData2; // AES로 암호화된 SignedDocument
        private final byte[] sealedKey;     // SecretKey 객체를 RSA로 암호화한 전자봉투

        public EnvelopeContent(byte[] encryptedData, byte[] sealedKey) {
            this.encryptedData = encryptedData.clone();
            this.encryptedData2 = new byte[0];
            this.sealedKey = sealedKey.clone();
        }
        
        public EnvelopeContent(byte[] encryptedData, byte[] encryptedData2, byte[] sealedKey) {
            this.encryptedData = encryptedData.clone();
            this.encryptedData2 = encryptedData2.clone();
            this.sealedKey = sealedKey.clone();
        }

        public byte[] getEncryptedData() {
            return encryptedData.clone();
        }
        
        public byte[] getEncryptedData2() {
            return encryptedData2.clone();
        }
        
        public byte[] getSealedKey() {
            return sealedKey.clone();
        }
      
    }

    // -------------------------------------------------------------------------
    // 암/복호화 (작은 데이터 전용 — 큰 데이터는 별도 스트리밍 처리 필요)
    // -------------------------------------------------------------------------

    private static byte[] encrypt(byte[] rawData, Key key, String algorithm)
            throws NoSuchAlgorithmException, InvalidKeyException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(rawData);
    }

    private static byte[] decrypt(byte[] encryptedData, Key key, String algorithm)
            throws NoSuchAlgorithmException, InvalidKeyException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    // -------------------------------------------------------------------------
    // SecretKey ↔ byte[] 변환 : getEncoded/SecretKeySpec 대신 스트림 직렬화 사용
    // -------------------------------------------------------------------------

    // SecretKey 객체를 ObjectOutputStream으로 직렬화 → byte[]
    private static byte[] keyToBytes(SecretKey secretKey) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(secretKey);
            return baos.toByteArray();
        }
    }

    // byte[]를 ObjectInputStream으로 역직렬화 → SecretKey 객체
    private static SecretKey bytesToKey(byte[] keyBytes)
            throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(keyBytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (SecretKey) ois.readObject();
        }
    }

    // -------------------------------------------------------------------------
    // SignedDocument ↔ byte[] 변환
    // -------------------------------------------------------------------------

    public static byte[] signedToBytes(SignedDocument signed) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(signed);
            return baos.toByteArray();
        }
    }

    public static SignedDocument bytesToSigned(byte[] signedBytes)
            throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(signedBytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (SignedDocument) ois.readObject();
        }
    }

    // -------------------------------------------------------------------------
    // 전자봉투 봉인(seal) / 개봉(open)
    // -------------------------------------------------------------------------

    public static EnvelopeContent seal(byte[] signedBytes, byte[] signedBytes2, PublicKey receiverPub)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, IOException {

        SecretKey secretKey = KeyManager.generateAESKey();
        byte[] encryptedData = encrypt(signedBytes, secretKey, AES_TRANSFORMATION);
        byte[] encryptedData2 = encrypt(signedBytes2, secretKey, AES_TRANSFORMATION);

        byte[] keyBytes = keyToBytes(secretKey);
        byte[] sealedKey = encrypt(keyBytes, receiverPub, RSA_TRANSFORMATION);

        return new EnvelopeContent(encryptedData, encryptedData2, sealedKey);
    }
    
    public static EnvelopeContent seal(byte[] signedBytes, PublicKey receiverPub)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, IOException {

        SecretKey secretKey = KeyManager.generateAESKey();
        byte[] encryptedData = encrypt(signedBytes, secretKey, AES_TRANSFORMATION);

        byte[] keyBytes = keyToBytes(secretKey);
        byte[] sealedKey = encrypt(keyBytes, receiverPub, RSA_TRANSFORMATION);

        return new EnvelopeContent(encryptedData, sealedKey);
    }

    // 첫 번째 암호화 데이터 개봉 (의사의 상담기록)
    public static byte[] open(EnvelopeContent envelope, PrivateKey receiverPriv)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, IOException, ClassNotFoundException {
 
        return open(envelope, receiverPriv, 1);
    }
 
    // [추가] index로 꺼낼 데이터를 선택
    // index == 1 : 첫 번째 데이터 (의사의 서명 + 상담기록)
    // index == 2 : 두 번째 데이터 (환자의 동의서)
    public static byte[] open(EnvelopeContent envelope, PrivateKey receiverPriv, int index)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, IOException, ClassNotFoundException {
 
        byte[] keyBytes = decrypt(envelope.getSealedKey(), receiverPriv, RSA_TRANSFORMATION);
        SecretKey secretKey = bytesToKey(keyBytes);
 
        byte[] target;
        if (index == 1) {
            target = envelope.getEncryptedData();   // 의사의 서명 + 상담기록
        } else if (index == 2) {
            target = envelope.getEncryptedData2();  // 환자의 동의서
        } else {
            throw new IllegalArgumentException("[open] 유효하지 않은 index: " + index + " (1=의사 서명 및 상담기록, 2=환자 동의서)");
        }
        return decrypt(target, secretKey, AES_TRANSFORMATION);
    }
}
