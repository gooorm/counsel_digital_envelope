import java.io.FileOutputStream;
import java.util.Scanner;

import envelope.DigitalEnvelope.EnvelopeContent;
import envelope.DigitalEnvelope.SignedDocument;
import people.Client;
import people.Counselor;
import people.Lawyer;

public class Main {
    public static void main(String[] args) throws Exception {

    	Scanner sc = new Scanner(System.in);
    	Client client = new Client("내담자", KeyManager.getOrGenerateKeyPair("client"));
        Counselor counselor = new Counselor("상담사", KeyManager.getOrGenerateKeyPair("counselor"));
        Lawyer lawyer = new Lawyer("담당 변호사", KeyManager.getOrGenerateKeyPair("lawyer"));
 
        System.out.println("--- [1] 상담 기록 작성 ---");
        System.out.print("상담 내용을 작성해주세요: ");
        String record = sc.nextLine();
        counselor.setMedicalRecord(record);

        // 상담 기록을 파일로 저장 (FileOutputStream 사용)
        try (FileOutputStream fos = new FileOutputStream("상담기록.txt")) {
            fos.write(record.getBytes());
        }
        System.out.println(counselor.getName() + ": 상담 기록을 작성하고 '상담기록.txt'에 저장했습니다.");
        
        System.out.println("\n--- [2] 내담자의 정보 제공 동의 ---");
        System.out.print("상담 기록을 제 3자에게 제공하는 것에 동의합니까?(Y/n)): ");
        String agree = sc.next();
        if(agree.toLowerCase().equals("y")) {
            SignedDocument consent = client.signConsent(agree);
            counselor.receiveAndVerifyConsent(consent, client);
            System.out.println("\n--- [3] 상담사의 상담 기록 봉인 및 제출 ---");
            // 저장된 파일을 읽어 전자봉투 생성 (FileInputStream 사용)
            EnvelopeContent envelope = counselor.sealRecordFromFile("상담기록.txt", lawyer);
            System.out.println(counselor.getName() + ": '상담기록.txt'를 읽어 전자봉투를 생성하고 제출했습니다.");
            
            
            System.out.println("\n--- [4] 법원(변호사)의 증거 수신 및 검증 ---");
            lawyer.receiveAndVerifyEvidence(envelope, counselor);
            
        }else {
        	System.out.println("상담 기록 제출에 실패하였습니다.");
        	System.out.println("좋은 하루 되세요.");
        }
        

        sc.close();
    }
}
