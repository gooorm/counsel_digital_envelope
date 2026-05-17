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

    // Cipher.getInstance()에 전달하는 변환 문자열 (알고리즘/운영모드/패딩)
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    // ===================== 내부 데이터 클래스 =====================
    //
    // SignedDocument  : 전자서명 + 평문 + 송신자 공개키의 묶음
    //                   → combine()의 입력 / split()의 반환값
    //
    // EnvelopeContent : 대칭 암호화된 SignedDocument + 비대칭 암호화된 비밀키 (전자 봉투)
    //                   → seal()의 반환값 / open()의 입력
    //
    // 호출 측 import 예시:
    //   import envelope.DigitalEnvelope.SignedDocument;
    //   import envelope.DigitalEnvelope.EnvelopeContent;
    // ============================================================

    public static class SignedDocument implements Serializable {

        private static final long serialVersionUID = 1L;

        private final byte[] signature;       // 송신자의 사설키로 생성한 전자서명
        private final byte[] plainText;       // 평문 (원문)
        private final PublicKey senderPubKey; // 수신자가 서명 검증에 사용할 송신자의 공개키

        public SignedDocument(byte[] signature, byte[] plainText, PublicKey senderPubKey) {
            this.signature = signature;
            this.plainText = plainText;
            this.senderPubKey = senderPubKey;
        }

        public byte[] getSignature()       { return signature; }
        public byte[] getPlainText()       { return plainText; }
        public PublicKey getSenderPubKey() { return senderPubKey; }
    }

    public static class EnvelopeContent implements Serializable {

        private static final long serialVersionUID = 1L;

        private final byte[] encryptedData; // SignedDocument 묶음을 AES 비밀키로 대칭 암호화한 본문
        private final byte[] sealedKey;     // AES 비밀키를 수신자의 공개키로 비대칭 암호화한 전자봉투

        public EnvelopeContent(byte[] encryptedData, byte[] sealedKey) {
            this.encryptedData = encryptedData;
            this.sealedKey = sealedKey;
        }

        public byte[] getEncryptedData() { return encryptedData; }
        public byte[] getSealedKey()     { return sealedKey; }
    }

    // ========================= 송신자 로직 =========================
    //
    // 단계:
    //   1. SignedDocument 묶음을 직렬화하여 byte[]로 만든다           → serialize()
    //   2. byte[]를 AES 비밀키로 암호화하여 본문(encryptedData)을 만든다     ┐
    //   3. AES 비밀키를 수신자 공개키로 암호화하여 전자봉투(sealedKey)를 만든다 ├ seal()
    //   4. 본문과 봉투를 EnvelopeContent로 묶어 수신자에게 전송              ┘
    //
    // 호출 측 사용 예시:
    //   SignedDocument doc = new SignedDocument(sig, plainText, alicePub);
    //   byte[] serialized = DigitalEnvelope.serialize(doc);
    //   EnvelopeContent envelope = DigitalEnvelope.seal(serialized, bobPub);
    // ==============================================================

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
        // 1) 묶인 데이터를 AES 비밀키로 대칭 암호화 → 본문
        byte[] encryptedData = encrypt(serialized, sessionKey, AES_TRANSFORMATION);

        // 2) AES 비밀키 자체를 수신자의 공개키로 비대칭 암호화 → 봉투
        byte[] sealedKey = encrypt(sessionKey.getEncoded(), receiverPub, RSA_TRANSFORMATION);

        // 3) 본문과 봉투를 묶어 반환
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

    // ========================= 수신자 로직 =========================
    //
    // 단계:
    //   1. 전자봉투(sealedKey)를 수신자의 사설키로 복호화하여 AES 비밀키를 얻는다  ┐
    //   2. AES 비밀키로 본문(encryptedData)을 복호화하여 byte[]를 얻는다       ┴ open()
    //   3. byte[]를 역직렬화하여 SignedDocument 객체로 복원한다              → deserialize()
    //   4. SignedDocument의 senderPubKey로 전자서명을 검증한다 (DigitalSignature.verify)
    //
    // 호출 측 사용 예시:
    //   byte[] received = DigitalEnvelope.open(envelope, bobPriv);
    //   SignedDocument doc = DigitalEnvelope.deserialize(received);
    //   boolean ok = signature.verify(doc.getPlainText(), doc.getSignature(), doc.getSenderPubKey());
    // ==============================================================

    // 전자봉투를 열어 본문을 복호화한 byte[]로 반환
    public static byte[] open(EnvelopeContent envelope, PrivateKey receiverPriv) throws GeneralSecurityException {
        // 1) 봉투(sealedKey)를 수신자의 사설키로 복호화 → AES 비밀키 획득
        byte[] keyBytes = decrypt(envelope.getSealedKey(), receiverPriv, RSA_TRANSFORMATION);
        SecretKey sessionKey = new SecretKeySpec(keyBytes, "AES");

        // 2) 본문(encryptedData)을 AES 비밀키로 복호화
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

