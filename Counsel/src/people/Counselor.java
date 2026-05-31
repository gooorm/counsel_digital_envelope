package people;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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
	public boolean receiveAndVerifyConsent(SignedDocument consent, PublicKey publicKey, String name) {
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
	public EnvelopeContent sealRecordFromFile(SignedDocument clientSigned, String filePath, PublicKey receiverKey)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException,
					NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException {
		// 파일에서 암호화된 EnvelopeContent 읽고 개인키로 복호화
		byte[] record = decrypt(filePath);

		byte[] signature = DigitalSignature.sign(record, getPrivateKey());
		SignedDocument confirmedDoc = new SignedDocument(signature, record, getPublicKey());

		
		byte[] serialized = DigitalEnvelope.signedToBytes(confirmedDoc);
		byte[] serialized2 = DigitalEnvelope.signedToBytes(clientSigned);
		return DigitalEnvelope.seal(serialized, serialized2, receiverKey);
	}
	private byte[] decrypt(String dataFile) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
		
		byte[] encryptedData = new byte[0];
		try(FileInputStream fs =  new FileInputStream(dataFile)) {
			encryptedData = fs.readAllBytes();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try(ByteArrayOutputStream bs  = new ByteArrayOutputStream()){
			int offset = 0;
			while(offset < encryptedData.length) {
				int length = Math.min(encryptedData.length - offset, 128);
				byte[] plainBlock = cipher.doFinal(encryptedData, offset, length);
				bs.write(plainBlock);
				offset += length;
			}
			return bs.toByteArray();
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
	
	
	// 상담 기록을 파일로 저장 (상담사 공개키로 암호화해서 저장)
	public boolean writeRecordToFile(String fileName) throws NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");;
		try {
			cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		
		try(ByteArrayOutputStream bs = new ByteArrayOutputStream()){
			int offset = 0;
			while(offset < counselingRecord.length) {
				int length = Math.min(counselingRecord.length  - offset, getPublicKey().getEncoded().length);
				byte[] cipherBlock = cipher.doFinal(counselingRecord, offset, length);
				bs.write(cipherBlock);
				offset += length;
			}
			try (FileOutputStream fs = new FileOutputStream(fileName)){
				fs.write(bs.toByteArray());
				return true;
			}
			
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
