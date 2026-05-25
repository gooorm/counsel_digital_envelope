import java.io.FileOutputStream;
import java.util.Scanner;

import envelope.DigitalEnvelope.EnvelopeContent;
import envelope.DigitalEnvelope.SignedDocument;
import people.Client;
import people.Counselor;
import people.Lawyer;

public class Main {
    public static void main(String[] args) throws Exception {

    	try (Scanner sc = new Scanner(System.in)) {
    		Client client = new Client("내담자");
            Counselor counselor = new Counselor("상담사");
            Lawyer lawyer = new Lawyer("담당 변호사");

            System.out.println("--- [1] 상담 기록 작성 ---");
            System.out.print("상담 내용을 작성해주세요: ");
            String record = sc.nextLine();
            counselor.setMedicalRecord(record);

            // 상담 기록을 파일로 저장
            String recordF = "record.txt";
            try (FileOutputStream fos = new FileOutputStream(recordF)) {
                fos.write(record.getBytes());
            }
            System.out.println(counselor.getName() + ": 상담 기록을 작성하고 " + recordF + "에 저장했습니다.");
            
            System.out.println("\n--- [2] 내담자의 정보 제공 동의 ---");
            
            String agree = "";
            while (true) {
                System.out.print("상담 기록을 제 3자에게 제공하는 것에 동의합니까?(y/n): ");
                agree = sc.nextLine().trim().toLowerCase();

                if (agree.equals("y") || agree.equals("n")) {
                    break;
                } else {
                    System.out.println("동의에 실패하였습니다. y 또는 n만 입력해주세요.");
                }
            }
            
            if(agree.equals("y")) {
                SignedDocument consent = client.signConsent(agree);
                counselor.receiveAndVerifyConsent(consent, client);
                System.out.println("\n--- [3] 상담사의 상담 기록 봉인 및 제출 ---");
                // 저장된 파일을 읽어 전자봉투 생성 (FileInputStream 사용)
                EnvelopeContent envelope = counselor.sealRecordFromFile(recordF, lawyer);
                System.out.println(counselor.getName() + ": '" + recordF + "'를 읽어 전자봉투를 생성하고 제출했습니다.");
                
                System.out.println("\n--- [4] 법원(변호사)의 증거 수신 및 검증 ---");
                lawyer.receiveAndVerifyEvidence(envelope, counselor);
                
            }else {
            	System.out.println("상담 기록 제출 동의에 거부하여 문서는 제출되지 않습니다.");
            	System.out.println("좋은 하루 되세요.");
            }
    	}
    	
    }
}
