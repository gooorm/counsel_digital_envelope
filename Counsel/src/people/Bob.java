package people;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import javax.crypto.SecretKey;

import envelope.DigitalEnvelope;
import envelope.DigitalEnvelope.EnvelopeContent;
import envelope.DigitalEnvelope.SignedDocument;
import envelope.DigitalSignature;
import envelope.KeyManager;

public class Bob extends Person {

	public Bob() {
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

	// Phase 2: Alice → Bob 수신
	// 1. Bob 사설키로 봉투 개봉
	// 2. Alice 공개키로 서명 검증
	public void receiveConsent(EnvelopeContent env) {
		try {
			byte[] decrypted = DigitalEnvelope.open(env, this.privateKey);
			SignedDocument doc = DigitalEnvelope.deserialize(decrypted);

			DigitalSignature ds = new DigitalSignature();
			boolean valid = ds.verify(doc.getPlainText(), doc.getSignature(), doc.getSenderPubKey());

			System.out.println("[Bob] Alice 서명 검증: " + (valid ? "성공" : "실패"));
			System.out.println("[Bob] 수신 동의서: " + new String(doc.getPlainText(), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Phase 3: Bob → Alice
	// 1. Bob 사설키로 상담 소견서 서명
	// 2. Alice 공개키로 봉투 생성
	public EnvelopeContent sendMedicalRecord(PublicKey alicePubKey) {
		try {
			byte[] plainText = "상담일: 26.06.01, 내담자: 홍길동, 소견: 사건 관련 외상 후 스트레스 증상 및 심리적 피해 호소. 법적 증거 자료로 활용 가능한 공식 소견서임을 확인함.".getBytes("UTF-8");

			DigitalSignature ds = new DigitalSignature();
			byte[] signature = ds.sign(plainText, this.privateKey);

			SignedDocument doc = new SignedDocument(signature, plainText, this.publicKey);
			byte[] serialized = DigitalEnvelope.serialize(doc);

			KeyManager km = new KeyManager();
			SecretKey sessionKey = km.generateAESKey();
			return DigitalEnvelope.seal(serialized, alicePubKey, sessionKey);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
