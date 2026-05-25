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

		boolean verified = verifySignature(deserialized, sender);
		if (verified) {
			System.out.println(getName() + ": 제출된 증거를 안전하게 확인했습니다.");
		}
		else {
			System.out.println(getName() + ": 제출된 증거 확인에 실패하였습니다.");
		}
	}
}