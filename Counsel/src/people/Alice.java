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

public class Alice extends Person {

	private byte[] bobSignedDocBytes; // Phase 3에서 받은 Bob 서명 문서 (Phase 4 재사용)

	public Alice() {
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

	// Phase 2: Alice → Bob
	// 1. Alice 사설키로 동의서 서명
	// 2. Bob 공개키로 봉투 생성
	public EnvelopeContent sendConsentToBob(PublicKey bobPubKey) {
		try {
			byte[] plainText = "본인은 심리 상담 진행 및 기록 보관에 동의합니다. 환자: 홍길동".getBytes("UTF-8");

			DigitalSignature ds = new DigitalSignature();
			byte[] signature = ds.sign(plainText, this.privateKey);

			SignedDocument doc = new SignedDocument(signature, plainText, this.publicKey);
			byte[] serialized = DigitalEnvelope.serialize(doc);

			KeyManager km = new KeyManager();
			SecretKey sessionKey = km.generateAESKey();
			return DigitalEnvelope.seal(serialized, bobPubKey, sessionKey);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Phase 3: Bob → Alice 수신
	// 1. Alice 사설키로 봉투 개봉
	// 2. Bob 공개키로 서명 검증
	public void receiveRecord(EnvelopeContent env) {
		try {
			bobSignedDocBytes = DigitalEnvelope.open(env, this.privateKey);
			SignedDocument doc = DigitalEnvelope.deserialize(bobSignedDocBytes);

			DigitalSignature ds = new DigitalSignature();
			boolean valid = ds.verify(doc.getPlainText(), doc.getSignature(), doc.getSenderPubKey());

			System.out.println("[Alice] Bob 서명 검증: " + (valid ? "성공" : "실패"));
			System.out.println("[Alice] 수신 의료기록: " + new String(doc.getPlainText(), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Phase 4: Alice → Charlie
	// 1. Alice 사설키로 Bob 문서 전체에 전송 동의 서명 (이중 서명)
	// 2. Charlie 공개키로 봉투 생성
	public EnvelopeContent sendToCharlie(PublicKey charliePubKey) {
		try {
			DigitalSignature ds = new DigitalSignature();
			byte[] aliceConsentSig = ds.sign(bobSignedDocBytes, this.privateKey);

			SignedDocument outerDoc = new SignedDocument(aliceConsentSig, bobSignedDocBytes, this.publicKey);
			byte[] serialized = DigitalEnvelope.serialize(outerDoc);

			KeyManager km = new KeyManager();
			SecretKey sessionKey = km.generateAESKey();
			return DigitalEnvelope.seal(serialized, charliePubKey, sessionKey);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
