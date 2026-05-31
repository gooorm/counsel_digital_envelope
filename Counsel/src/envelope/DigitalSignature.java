package envelope;

import java.security.*;


public final class DigitalSignature {

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private DigitalSignature() {}

    // 전자서명 생성: 평문의 해시값을 송신자의 사설키로 암호화
    // → 송신자만이 생성할 수 있으므로 부인 방지가 보장됨
    public static byte[] sign(byte[] plainText, PrivateKey senderPriv)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        if (plainText == null || plainText.length == 0) {
            throw new IllegalArgumentException("plainText는 null이거나 비어 있을 수 없습니다.");
        }
        if (senderPriv == null) {
            throw new IllegalArgumentException("senderPriv는 null일 수 없습니다.");
        }

        Signature signer = Signature.getInstance(SIGNATURE_ALGORITHM);
        signer.initSign(senderPriv);
        signer.update(plainText);
        return signer.sign();
    }

    // 전자서명 검증: 송신자의 공개키로 서명을 복호화하여 얻은 해시값과 평문을 직접 해시한 값을 비교
    // → 검증에 필요한 것: 평문, 전자서명, 송신자의 공개키
    public static boolean verify(byte[] plainText, byte[] signature, PublicKey senderPub)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        if (plainText == null || plainText.length == 0) {
            throw new IllegalArgumentException("plainText는 null이거나 비어 있을 수 없습니다.");
        }
        if (signature == null || signature.length == 0) {
            throw new IllegalArgumentException("signature는 null이거나 비어 있을 수 없습니다.");
        }
        if (senderPub == null) {
            throw new IllegalArgumentException("senderPub은 null일 수 없습니다.");
        }

        Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
        verifier.initVerify(senderPub);
        verifier.update(plainText);
        return verifier.verify(signature);
    }
}
