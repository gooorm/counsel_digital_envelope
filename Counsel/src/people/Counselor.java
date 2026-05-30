package people;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;

import envelope.DigitalEnvelope;
import envelope.DigitalEnvelope.EnvelopeContent;
import envelope.DigitalEnvelope.SignedDocument;
import envelope.DigitalSignature;

public class Counselor extends Person {
	private byte[] counselingRecord;

	public Counselor(String name, KeyPair keyPair) {
		super(name, keyPair);
	}

	public boolean setCounselingRecord(String record) {
	    if (record == null) {
	        return false;
	    }

	    this.counselingRecord = record.getBytes();
	    return true;
	}

	// 내담자에게 동의서를 받고 검증하는 행위
	public boolean receiveAndVerifyConsent(SignedDocument consent, PublicKey publicKey, String name) throws Exception {
		boolean verified = verifySignature(consent, publicKey, name);
		if (verified) {
			System.out.println(getName() + ": 내담자의 제3자 제공 동의서를 확인했습니다.");
		}
		else {
			System.out.println(getName() + ": 내담자의 제3자 제공 동의서 확인에 실패하였습니다.");
		}
		return verified;
	}

	// 상담 기록을 서명하고 전자봉투로 포장하여 반환하는 행위
	public EnvelopeContent sealRecordFor(Person receiver) throws Exception {
		if (counselingRecord == null) {
			throw new IllegalStateException("상담 기록이 없습니다.");
		}

		byte[] signature = DigitalSignature.sign(counselingRecord, getPrivateKey());
		SignedDocument confirmedDoc = new SignedDocument(signature, counselingRecord,
				getPublicKey());

		byte[] serialized = DigitalEnvelope.signedToBytes(confirmedDoc);
		return DigitalEnvelope.seal(serialized, receiver.getPublicKey());
	}

	// 파일에 저장된 상담 기록을 읽어서 전자봉투를 생성하는 행위
	public EnvelopeContent sealRecordFromFile(SignedDocument clientSigned, String filePath, PublicKey receiverKey) throws Exception {
		byte[] record;
		try (FileInputStream fis = new FileInputStream(filePath)) {
			record = fis.readAllBytes();
		}

		byte[] signature = DigitalSignature.sign(record, getPrivateKey());
		SignedDocument confirmedDoc = new SignedDocument(signature, record, getPublicKey());

		
		byte[] serialized = DigitalEnvelope.signedToBytes(confirmedDoc);
		byte[] serialized2 = DigitalEnvelope.signedToBytes(clientSigned);
		return DigitalEnvelope.seal(serialized, serialized2, receiverKey);
	}
	// 상담 기록을 파일로 저장
	public void writeRecordToFile(String fileName) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(fileName)) {
			fos.write(counselingRecord);
		}
	}
}
