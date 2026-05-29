package people;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;

import envelope.DigitalEnvelope.SignedDocument;
import envelope.DigitalSignature;

public abstract class Person {
    private final String name;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public Person(String name, KeyPair keyPair) {
        this.name = name;
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }

    public String getName() {
        return name;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    protected PrivateKey getPrivateKey() {
        return privateKey;
    }

    public boolean verifySignature(SignedDocument document, PublicKey publicKey, String name) {
        boolean verified = false;
		try {
			verified = DigitalSignature.verify(
			        document.getPlainText(),
			        document.getSignature(),
			        publicKey
			);
			return verified;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
        
        return verified;
    }
}
