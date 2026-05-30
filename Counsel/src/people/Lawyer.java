package people;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import envelope.DigitalEnvelope;
import envelope.DigitalEnvelope.EnvelopeContent;
import envelope.DigitalEnvelope.SignedDocument;

public class Lawyer extends Person {

	public Lawyer(String name, KeyPair keyPair) {
		super(name, keyPair);
	}

	// 제출받은 전자봉투를 개봉하고 서명을 검증하는 행위
	public boolean receiveAndVerifyEvidence(EnvelopeContent envelope, PublicKey senderKey1, String name1,
					PublicKey senderKey2, String name2) 
					throws ClassNotFoundException, IOException, InvalidKeyException, 
					NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		byte[] opened = DigitalEnvelope.open(envelope, getPrivateKey(), 1);
		SignedDocument deserialized = DigitalEnvelope.bytesToSigned(opened);

		boolean verified = verifySignature(deserialized, senderKey1, name1);
		if (verified) {
			System.out.println(getName() + ": " + name1 + " 가 제출한 증거(기록)를 안전하게 확인했습니다.");
		} else {
			System.out.println(getName() + ": 제출된 증거 확인에 실패하였습니다.");
		}
		
		byte[] opened2 = DigitalEnvelope.open(envelope, getPrivateKey(), 2);
		SignedDocument deserialized2 = DigitalEnvelope.bytesToSigned(opened2);

		verified = verifySignature(deserialized2, senderKey2, name2);
		if (verified) {
			System.out.println(getName() + ": " + name2 + " 가 제출한 증거(기록)를 안전하게 확인했습니다.");
		} else {
			System.out.println(getName() + ": 제출된 증거 확인에 실패하였습니다.");
		}
		
		return verified;
	}
}
