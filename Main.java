package project.version0523;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main {
    public static void main(String[] args) throws GeneralSecurityException, IOException, ClassNotFoundException {

        Person patient = new Person("Patient");
        Person doctor = new Person("Doctor");
        Person lawyer = new Person("Lawyer");

        //XX년 XX월 XX일의 상담 기록...
        byte[] record = "점순이가 너네 집엔 봄감자가 없냐고 괴롭혀서 환자가 힘들어했다".getBytes();
        doctor.setRecord(record);

        //환자는 점순이를 고소하기로 했다.
        //환자의 상담기록 제 3자 제공 동의
        byte[] consent = "제 상담 기록을 제 3자에게 제공하는 것을 동의합니다.".getBytes();
        byte[] signed = DigitalSignature.sign(consent, patient.getPrivateKey());
        DigitalEnvelope.SignedDocument signedDocument = new DigitalEnvelope.SignedDocument(signed, consent, patient.getPublicKey());
        patient.sendTo(doctor, signedDocument);

        //의사는 환자 본인이 동의한 게 맞는지 확인한다.
        DigitalEnvelope.SignedDocument received = doctor.getSignedDocument();
        boolean verified = DigitalSignature.verify(received.getPlainText(), received.getSignature(), received.getSenderPubKey());

        if (verified) {
            System.out.println("서명자 정보가 일치합니다.");
        } else {
            throw new RuntimeException("서명자 정보가 일치하지 않습니다.");
        }

        //의사는 서명해서 법원에 제출한다
        byte[] confirm = DigitalSignature.sign(doctor.getRecord(), doctor.getPrivateKey());
        DigitalEnvelope.SignedDocument confirmedDoc = new DigitalEnvelope.SignedDocument(confirm, doctor.getRecord(), doctor.getPublicKey());
        byte[] serialized = DigitalEnvelope.serialize(confirmedDoc);
        DigitalEnvelope.EnvelopeContent envelope = DigitalEnvelope.seal(serialized, lawyer.getPublicKey());
        doctor.sendTo(lawyer, envelope);

        //법원은 수신하고 검증
        DigitalEnvelope.EnvelopeContent received2 = lawyer.getEnvelopeContent();
        byte[] opened = DigitalEnvelope.open(received2, lawyer.getPrivateKey());
        DigitalEnvelope.SignedDocument deserialized = DigitalEnvelope.deserialize(opened);
        verified = DigitalSignature.verify(deserialized.getPlainText(), deserialized.getSignature(), deserialized.getSenderPubKey());
        if (verified) {
            System.out.println("서명자 정보가 일치합니다.");
        } else {
            throw new RuntimeException("서명자 정보가 일치하지 않습니다.");
        }
    }
}
