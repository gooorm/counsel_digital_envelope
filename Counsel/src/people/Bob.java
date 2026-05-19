package people;

import envelope.DigitalEnvelope.EnvelopeContent;

public class Bob extends Person{

//		- Alice 서명 검증
//		- 상담기록 작성
//		- 서명
//		- Alice에게 전송

//		1. Bob 개인키로 봉투 개봉
//		2. Alice 공개키로 서명 검증
	
	public void receiveConsent(EnvelopeContent env) {
		
	}
	public EnvelopeContent sendMedicalRecord(/*...*/) {
		return null;
	}
}
