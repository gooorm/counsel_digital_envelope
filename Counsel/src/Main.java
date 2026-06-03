import envelope.DigitalEnvelope.EnvelopeContent;
import envelope.DigitalEnvelope.SignedDocument;
import envelope.KeyManager;
import people.Client;
import people.Counselor;
import people.Lawyer;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * 상담 기록 제3자 제공 — 전자봉투 보안 전송 프로토타입.
 *
 * 시나리오 흐름: [1] 내담자 → 상담사 : 제3자 제공 동의서를 전자서명하여 전송 [2] 상담사 : 동의서 서명을 검증해 내담자 본인임을
 * 확인 [3] 상담사 → 변호사 : 상담 기록을 전자서명 후 전자봉투로 봉인하여 전송 [4] 변호사 : 전자봉투를 개봉하고 상담사의 서명을
 * 검증
 */

public class Main {

	private static final String RECORD_FILE = "상담기록.txt";

	public static void main(String[] args)
			throws ClassNotFoundException, NoSuchAlgorithmException, IOException, InvalidKeyException,
			SignatureException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		Scanner sc = new Scanner(System.in);

		// 등장인물과 키 준비 (기존 키가 있으면 복구, 없으면 생성)
		Client client = new Client("내담자", KeyManager.getOrGenerateKeyPair("client"));
		Counselor counselor = new Counselor("상담사", KeyManager.getOrGenerateKeyPair("counselor"));
		Lawyer lawyer = new Lawyer("변호사", KeyManager.getOrGenerateKeyPair("lawyer"));

		banner("상담 기록 제3자 제공 · 전자봉투 보안 전송 시스템");
		System.out.println(" 흐름 :  내담자 ──(동의서)──▶ 상담사 ──(전자봉투)──▶ 변호사");

		// [준비] 상담사가 상담 기록을 작성하여 파일로 저장
		banner("[ 준비 ] 상담사가 상담 기록을 작성합니다");
		System.out.print("상담 내용 입력 > ");
		String record = sc.nextLine();
		counselor.setCounselingRecord(record);
		if (counselor.writeRecordToFile(RECORD_FILE) != true) {
			System.out.println("상담 파일 저장에 실패하였습니다.");
		}

		System.out.println("→ '" + RECORD_FILE + "'에 저장되었습니다.");

		// [1] 내담자가 동의서에 서명하여 상담사에게 전송
		banner("[ 1 ] 내담자 → 상담사 : 제3자 제공 동의");
		
		String agree = "";
		System.out.print("상담 기록을 변호사에게 제공하는 데 동의하십니까? (Y/n) > ");
		while(true) {
			agree = sc.nextLine().trim().toLowerCase();
			if(agree.equals("y") || agree.equals("n")){
				break;
			}
			else {
				System.out.println("동의에 실패하였습니다. y또는 n만 입력해주세요.");
			}
		}
		if (agree.equals("n")) {
			System.out.println("동의가 확인되지 않아 절차를 종료합니다. 좋은 하루 되세요.");
			return;
		}
		String agreement = client.getName() + "은(는) 상담 기록을 제3자(" + lawyer.getName() + ")에게 제공하는 것에 동의합니다.";
		SignedDocument clientSigned = client.signConsent(agreement);

		waitBeforeEnter("본인 확인을 진행하려면 엔터를 누르십시오.", sc);

		// [2] 상담사가 동의서의 서명을 검증
		banner("[ 2 ] 상담사 : 동의서 서명 검증 (본인 확인)");
		buffering("동의서 서명 검증 중", 2);
		if (counselor.receiveAndVerifyConsent(clientSigned, client.getPublicKey(), client.getName())) {
			waitBeforeEnter("상담 기록을 봉인 및 전송하려면 Enter를 누르십시오.", sc);
			// [3] 상담사가 상담 기록을 봉인하여 변호사에게 전송
			banner("[ 3 ] 상담사 → 변호사 : 상담 기록 봉인·전송");
			EnvelopeContent envelope = counselor.sealRecordFromFile(clientSigned, RECORD_FILE, lawyer.getPublicKey());
			buffering("상담 기록 전송 중", 2);
			System.out.println("→ 상담사가 '" + RECORD_FILE + "'를 읽어 전자서명 후 전자봉투로 봉인하여 전송했습니다.");

			// [4] 변호사가 전자봉투를 개봉하고 발신자 서명을 검증
			waitBeforeEnter("전자봉투 개봉 및 발신자 검증을 하려면 Enter를 누르십시오.", sc);
			banner("[ 4 ] 변호사 : 전자봉투 개봉 및 발신자 검증");
			buffering("전자봉투 개봉 중", 1);
			buffering("발신자 검증 중", 1);
			if (lawyer.receiveAndVerifyEvidence(envelope, counselor.getPublicKey(), counselor.getName(),
					client.getPublicKey(), client.getName())) {
				banner("모든 절차가 안전하게 완료되었습니다");
			} else {
				banner("전자봉투 개봉에 실패하였습니다.");
			}
		}

		sc.close();

	}

	// 구분선이 있는 단계 제목 출력
	private static void banner(String title) {
		System.out.println("\n============================================================");
		System.out.println(" " + title);
		System.out.println("============================================================");
	}

	public static void buffering(String text, int seconds) {
		int totalSteps = 20; // 바 길이
		int delay = (seconds * 1000) / totalSteps;

		System.out.print(text + "... [");

		for (int i = 0; i < totalSteps; i++) {
			System.out.print("=");
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("] 완료");
	}

	public static void waitBeforeEnter(String sentence, Scanner sc) {
		System.out.print(sentence);
		sc.nextLine();
	}
}
