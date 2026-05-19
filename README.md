# counsel_digital_envelope
코드보안 전자봉투 프로젝트


DigitalEnvelope
open(), seal()

DigitalSignature
sign(), verify()

KeyManager
generateRSAKeyPair(), generateAESKey()
saveKey / loadKey

###Alice → Bob###
Alice
1. Alice 개인키로 서명
2. Bob 공개키로 봉투 생성
Bob
1. Bob 개인키로 봉투 개봉
2. Alice 공개키로 서명 검증

###Bob → Alice###
Bob
1. Bob 개인키로 서명
2. Alice 공개키로 봉투 생성
Alice
1. Alice 개인키로 봉투 개봉
2. Bob 공개키로 서명 검증

###Alice → Charlie###
Alice
1. Alice 개인키로 "전송 동의" 서명
2. Charlie 공개키로 봉투 생성
Charlie
1. Charlie 개인키로 봉투 개봉
2. Alice 공개키로 동의 검증
3. Bob 공개키로 의료기록 검증

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