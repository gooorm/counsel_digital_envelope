package people;

import envelope.DigitalEnvelope.EnvelopeContent;

public class Alice extends Person{
	
//		- 키 보유
//		- 동의서 작성
//		- 서명
//		- Bob에게 전송
//		- Charlie에게 재전송

//		1. Alice 개인키로 서명
//		2. Bob 공개키로 봉투 생성
	public void createConsentForm() {
		
	}
	public void signDocument() {
		
	}
	public void receiveRecord(EnvelopeContent env) {
		
	}
	public void sendToBob() {
	}
	public EnvelopeContent sendConsentToBob() {
		return null;
	}
	public EnvelopeContent sendToCharlie() {
		return null;
	}
}
