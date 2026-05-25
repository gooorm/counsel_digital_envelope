package people;
import java.security.NoSuchAlgorithmException;

import envelope.DigitalEnvelope;
import envelope.DigitalSignature;

public class Client extends Person {

    public Client(String name, KeyPair keyPair) {
        super(name, keyPair);
    }

    // 내담자는 동의서에 서명하는 행위를 가짐
    public DigitalEnvelope.SignedDocument signConsent(String text) throws Exception {
        byte[] plainText = text.getBytes();
        byte[] signature = DigitalSignature.sign(plainText, getPrivateKey());
        return new DigitalEnvelope.SignedDocument(signature, plainText, getPublicKey());
    }
}
