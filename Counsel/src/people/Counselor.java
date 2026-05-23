package people;
import java.security.NoSuchAlgorithmException;

import envelope.DigitalEnvelope;
import envelope.DigitalSignature;

public class Counselor extends Person {
    private byte[] medicalRecord;

    public Counselor(String name) throws NoSuchAlgorithmException {
        super(name);
    }

    public void setMedicalRecord(String record) {
        this.medicalRecord = record.getBytes();
    }

    // 내담자에게 동의서를 받고 검증하는 행위
    public void receiveAndVerifyConsent(DigitalEnvelope.SignedDocument consent, Person client) throws Exception {
        verifySignature(consent, client);
        System.out.println(getName() + ": 내담자의 제3자 제공 동의서를 확인했습니다.");
    }

    // 상담 기록을 서명하고 전자봉투로 포장하여 반환하는 행위
    public DigitalEnvelope.EnvelopeContent sealRecordFor(Person receiver) throws Exception {
        if (medicalRecord == null) throw new IllegalStateException("상담 기록이 없습니다.");

        byte[] signature = DigitalSignature.sign(medicalRecord, getPrivateKey());
        DigitalEnvelope.SignedDocument confirmedDoc = new DigitalEnvelope.SignedDocument(signature, medicalRecord, getPublicKey());

        byte[] serialized = DigitalEnvelope.serialize(confirmedDoc);
        return DigitalEnvelope.seal(serialized, receiver.getPublicKey());
    }
}