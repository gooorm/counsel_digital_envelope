package project.version0523;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Person {
    private final String name;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    private byte[] record;
    private DigitalEnvelope.SignedDocument signedDocument;
    private DigitalEnvelope.EnvelopeContent envelopeContent;

    public Person(String name) throws NoSuchAlgorithmException {
        this.name = name;
        KeyPair keyPair = KeyManager.generateRSAKeyPair();  // 한 번만!
        this.publicKey  = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }

    public Person(String name, PublicKey publicKey, PrivateKey privateKey) throws NoSuchAlgorithmException {
        this.name = name;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getName() {
        return name;
    }
    public PublicKey getPublicKey() {
        return publicKey;
    }
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public byte[] getRecord() {
        return record;
    }

    public void setRecord(byte[] record) {
        this.record = record;
    }

    public void setEnvelopeContent(DigitalEnvelope.EnvelopeContent envelopeContent) {
        this.envelopeContent = envelopeContent;
    }

    public DigitalEnvelope.EnvelopeContent getEnvelopeContent() {
        return envelopeContent;
    }

    public void setSignedDocument(DigitalEnvelope.SignedDocument signedDocument) {
        this.signedDocument = signedDocument;
    }

    public DigitalEnvelope.SignedDocument getSignedDocument() {
        return signedDocument;
    }

    public void sendTo(Person person, DigitalEnvelope.SignedDocument signed) {
        person.setSignedDocument(signed);
    }

    public void sendTo(Person person, DigitalEnvelope.EnvelopeContent envelope) {
        person.setEnvelopeContent(envelope);
    }
}
