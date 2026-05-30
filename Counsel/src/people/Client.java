package people;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import envelope.DigitalEnvelope.SignedDocument;
import envelope.DigitalSignature;

public class Client extends Person {

    public Client(String name, KeyPair keyPair) {
        super(name, keyPair);
    }

    // 내담자는 동의서에 서명하는 행위를 가짐
    public SignedDocument signConsent(String text) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        byte[] plainText = text.getBytes();
        byte[] signature = DigitalSignature.sign(plainText, getPrivateKey());
        return new SignedDocument(signature, plainText, getPublicKey());
    }
}
