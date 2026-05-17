package envelope;

import java.security.*;

public class DigitalSignature {

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    // 전자서명 생성: 평문의 해시값을 송신자의 사설키로 암호화
    // → 송신자만이 생성할 수 있으므로 부인 방지가 보장됨
    public byte[] sign(byte[] plainText, PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature signer = Signature.getInstance(SIGNATURE_ALGORITHM);
        signer.initSign(privateKey);
        signer.update(plainText);
        return signer.sign();
    }

    // 전자서명 검증: 송신자의 공개키로 서명을 복호화하여 얻은 해시값과 평문을 직접 해시한 값을 비교
    // → 검증에 필요한 것: 평문, 전자서명, 송신자의 공개키
    public boolean verify(byte[] plainText, byte[] signature, PublicKey publicKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
        verifier.initVerify(publicKey);
        verifier.update(plainText);
        return verifier.verify(signature);
    }
}

