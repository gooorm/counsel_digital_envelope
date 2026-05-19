# counsel_digital_envelope
코드보안 전자봉투 프로젝트


DigitalEnvelope
open(), seal()

DigitalSignature
sign(), verify()

KeyManager
generateRSAKeyPair(), generateAESKey()
saveKey / loadKey


	// ========================= 수신자 로직 =========================
	//
	// 단계:
	// 1. 전자봉투(sealedKey)를 수신자의 사설키로 복호화하여 AES 비밀키를 얻는다 ┐
	// 2. AES 비밀키로 본문(encryptedData)을 복호화하여 byte[]를 얻는다 ┴ open()
	// 3. byte[]를 역직렬화하여 SignedDocument 객체로 복원한다 → deserialize()
	// 4. SignedDocument의 senderPubKey로 전자서명을 검증한다 (DigitalSignature.verify)
	//
	// 호출 측 사용 예시:
	// byte[] received = DigitalEnvelope.open(envelope, bobPriv);
	// SignedDocument doc = DigitalEnvelope.deserialize(received);
	// boolean ok = signature.verify(doc.getPlainText(), doc.getSignature(),
	// doc.getSenderPubKey());
	// ==============================================================