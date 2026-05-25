package envelope;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;

public class KeyManager {

    private static final int RSA_KEY_SIZE = 2048;
    private static final int AES_KEY_SIZE = 256;

    // 비대칭 암호화 키 쌍 (공개키 + 사설키) 생성
    // 키의 용도:
    //   1. 송신자의 사설키 — 전자서명 생성 (sign)
    //   2. 송신자의 공개키 — 전자서명 검증 (verify)
    //   3. 수신자의 공개키 — 전자봉투 봉인, 즉 AES 비밀키 암호화 (seal)
    //   4. 수신자의 사설키 — 전자봉투 개봉, 즉 AES 비밀키 복호화 (open)

    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(RSA_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }

    // 키 한 개를 파일로 저장
    public static void saveKey(Key key, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(key);
        }
    }

    // 키 쌍을 공개키 파일과 사설키 파일에 각각 저장
    public static void saveKey(KeyPair keyPair, String publicKeyFilename, String privateKeyFilename) throws IOException {
        saveKey(keyPair.getPublic(), publicKeyFilename);
        saveKey(keyPair.getPrivate(), privateKeyFilename);
    }

    //수정된 메서드 -> 서프레스 워닝 위치 수정함
    public static <T extends Key> T loadKey(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            @SuppressWarnings("unchecked")
            T key = (T) ois.readObject();
            return key;
        }
    }

    // 추가된 메서드
    public static KeyPair getOrGenerateKeyPair(String name) throws Exception {
        String pubKeyFilename = name + "_pub.key";
        String privateKeyFilename = name + "_priv.key";

        try {
            // 1. 일단 스트림을 열어 기존 파일이 있는지 읽어오기 시도
            PublicKey pub = loadKey(pubKeyFilename);
            PrivateKey priv = loadKey(privateKeyFilename);
            System.out.println("[KeyManager] " + name + "의 기존 키 파일을 성공적으로 불러왔습니다.");
            return new KeyPair(pub, priv);

        } catch (FileNotFoundException e) {
            // 2. 파일이 없어서 에러가 나면 여기서 즉시 새로 생성하고 파일로 저장까지 완료
            System.out.println("[KeyManager] " + name + "의 키 파일이 없어 새로 생성하고 저장합니다.");
            KeyPair keyPair = generateRSAKeyPair();
            saveKey(keyPair, pubKeyFilename, privateKeyFilename);
            return keyPair;
        }
    }
}
