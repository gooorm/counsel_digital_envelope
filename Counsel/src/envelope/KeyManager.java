package envelope;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;

public final class KeyManager {

    private KeyManager() {}

    private static final int RSA_KEY_SIZE = 2048;
    private static final int AES_KEY_SIZE = 256;

    //----------------------------------------------------------------------

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

    //--------------------------------------------------------------------

    private static void saveKey(Key key, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(key);
        }
    }

    //한개로 합칠지 말지...
    public static void saveKey(KeyPair keyPair, String publicKeyFilename, String privateKeyFilename) throws IOException {
        saveKey(keyPair.getPublic(), publicKeyFilename);
        saveKey(keyPair.getPrivate(), privateKeyFilename);
    }


    public static <T extends Key> T loadKey(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            @SuppressWarnings("unchecked")
            T key = (T) ois.readObject();
            return key;
        }
    }

    //-----------------------------------------------------------------------------

    public static KeyPair getOrGenerateKeyPair(String name) throws Exception {
        String pubKeyFilename = name + "_pub.key";
        String privateKeyFilename = name + "_priv.key";

        try {
            PublicKey pub = loadKey(pubKeyFilename);
            PrivateKey priv = loadKey(privateKeyFilename);
            System.out.println("[KeyManager] " + name + "의 기존 키 파일을 성공적으로 불러왔습니다.");
            return new KeyPair(pub, priv);

        } catch (FileNotFoundException e) {
            System.out.println("[KeyManager] " + name + "의 키 파일이 없어 새로 생성하고 저장합니다.");
            KeyPair keyPair = generateRSAKeyPair();
            saveKey(keyPair, pubKeyFilename, privateKeyFilename);
            return keyPair;
        }
    }
}