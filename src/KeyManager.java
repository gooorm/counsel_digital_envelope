import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class KeyManager {

    private static final int RSA_KEY_SIZE = 2048;
    private static final int AES_KEY_SIZE = 256;

    // 비대칭 암호화 키 쌍 (공개키 + 사설키) 생성
    // 키의 용도:
    //   1. 송신자의 사설키 — 전자서명 생성 (sign)
    //   2. 송신자의 공개키 — 전자서명 검증 (verify)
    //   3. 수신자의 공개키 — 전자봉투 봉인, 즉 AES 비밀키 암호화 (seal)
    //   4. 수신자의 사설키 — 전자봉투 개봉, 즉 AES 비밀키 복호화 (open)

    public KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(RSA_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    // 대칭 암호화 비밀키 생성
    // 본문(서명 + 원문 + 송신자 공개키 묶음)을 암호화하는 데 쓰이며,
    // 이 비밀키 자체는 수신자의 공개키로 다시 비대칭 암호화되어 전자봉투가 됨
    public SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }

    // 키 한 개를 파일로 저장
    public void saveKey(Key key, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(key);
        }
    }

    // 키 쌍을 공개키 파일과 사설키 파일에 각각 저장
    public void saveKey(KeyPair keyPair, String publicKeyFilename, String privateKeyFilename) throws IOException {
        saveKey(keyPair.getPublic(), publicKeyFilename);
        saveKey(keyPair.getPrivate(), privateKeyFilename);
    }

    // 파일에서 키를 불러오기 (반환 타입을 제네릭으로 지정 가능)
    @SuppressWarnings("unchecked")
    public <T extends Key> T loadKey(String filename, Class<T> type) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (T) ois.readObject();
        }
    }
}
