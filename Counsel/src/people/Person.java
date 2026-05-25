package people;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

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

    public boolean verifySignature(SignedDocument document, Person sender) throws Exception {
        boolean verified = DigitalSignature.verify(
                document.getPlainText(),
                document.getSignature(),
                sender.getPublicKey()
        );
        
        if (verified) {
            System.out.println(sender.getName() + "의 서명자 정보가 일치합니다.");
        } else {
        	System.out.println(sender.getName() + "의 서명자 정보가 일치하지 않습니다.");
        }
        
        return verified;
    }
}
