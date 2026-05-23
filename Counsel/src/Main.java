import envelope.DigitalEnvelope;
import people.Client;
import people.Counselor;
import people.Lawyer;

public class Main {
    public static void main(String[] args) throws Exception {

    	Client client = new Client("내담자");
        Counselor counselor = new Counselor("상담사");
        Lawyer lawyer = new Lawyer("담당 변호사");

        System.out.println("--- [1] 상담 기록 작성 ---");
        counselor.setMedicalRecord("점순이가 너네 집엔 봄감자가 없냐고 괴롭혀서 내담자가 힘들어했다");
        System.out.println(counselor.getName() + ": 상담 기록을 작성했습니다.");

        System.out.println("\n--- [2] 내담자의 정보 제공 동의 ---");
        DigitalEnvelope.SignedDocument consent = client.signConsent("제 상담 기록을 제 3자에게 제공하는 것을 동의합니다.");
        counselor.receiveAndVerifyConsent(consent, client);

        System.out.println("\n--- [3] 상담사의 상담 기록 봉인 및 제출 ---");
        DigitalEnvelope.EnvelopeContent envelope = counselor.sealRecordFor(lawyer);
        System.out.println(counselor.getName() + ": 상담 기록을 제출했습니다.");

        System.out.println("\n--- [4] 법원(변호사)의 증거 수신 및 검증 ---");
        lawyer.receiveAndVerifyEvidence(envelope, counselor);
    }
}