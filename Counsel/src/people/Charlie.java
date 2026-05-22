package people;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import envelope.DigitalEnvelope;
import envelope.DigitalEnvelope.EnvelopeContent;
import envelope.DigitalEnvelope.SignedDocument;
import envelope.DigitalSignature;
import envelope.KeyManager;

public class Charlie extends Person {

	public Charlie() {
		super();
		KeyManager km = new KeyManager();
		KeyPair kp;
		try {
			kp = km.generateRSAKeyPair();
			this.publicKey = kp.getPublic();
			this.privateKey = kp.getPrivate();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	// Phase 4: Alice → Charlie 수신
	// 1. Charlie 사설키로 봉투 개봉
	// 2. Alice 공개키로 전송 동의 검증
	// 3. Bob 공개키로 의료기록 검증
	public void receive(EnvelopeContent env) {
		try {
			byte[] decrypted = DigitalEnvelope.open(env, this.privateKey);
			SignedDocument outerDoc = DigitalEnvelope.deserialize(decrypted);

			DigitalSignature ds = new DigitalSignature();

			// Alice의 전송 동의 서명 검증
			boolean aliceValid = ds.verify(outerDoc.getPlainText(), outerDoc.getSignature(), outerDoc.getSenderPubKey());
			System.out.println("[Charlie] Alice 전송 동의 검증: " + (aliceValid ? "성공" : "실패"));

			// 내부 Bob의 서명 문서 복원 및 검증
			SignedDocument innerDoc = DigitalEnvelope.deserialize(outerDoc.getPlainText());
			boolean bobValid = ds.verify(innerDoc.getPlainText(), innerDoc.getSignature(), innerDoc.getSenderPubKey());
			System.out.println("[Charlie] Bob 의료기록 검증: " + (bobValid ? "성공" : "실패"));
			System.out.println("[Charlie] 수신 의료기록: " + new String(innerDoc.getPlainText(), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
