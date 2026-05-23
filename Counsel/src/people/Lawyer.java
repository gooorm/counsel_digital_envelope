package people;
import java.security.NoSuchAlgorithmException;

import envelope.DigitalEnvelope;

public class Lawyer extends Person {

    public Lawyer(String name) throws NoSuchAlgorithmException {
        super(name);
    }

    // 제출받은 전자봉투를 개봉하고 서명을 검증하는 행위
    public void receiveAndVerifyEvidence(DigitalEnvelope.EnvelopeContent envelope, Person sender) throws Exception {
        byte[] opened = DigitalEnvelope.open(envelope, getPrivateKey());
        DigitalEnvelope.SignedDocument deserialized = DigitalEnvelope.deserialize(opened);

        verifySignature(deserialized, sender);
        System.out.println(getName() + ": 제출된 증거(기록)를 안전하게 확인했습니다.");
    }
}