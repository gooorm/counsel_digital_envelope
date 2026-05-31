package envelope;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;

public final class KeyManager {

    private KeyManager() {}

    private static final int RSA_KEY_SIZE = 2048;
    private static final int AES_KEY_SIZE = 256;

    private static final String RSA_ALGORITHM = "RSA";
    private static final String AES_ALGORITHM = "AES";

    //----------------------------------------------------------------------

    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGen.initialize(RSA_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }

    //--------------------------------------------------------------------

    private static boolean saveKey(Key key, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(key);
            return true;
        } catch (IOException e) {
            System.err.println("[KeyManager] 키 저장 실패: " + filename + " - " + e.getMessage());
            return false;
        }
    }

    public static boolean saveKey(KeyPair keyPair, String publicKeyFilename, String privateKeyFilename) {
        boolean pubSaved = saveKey(keyPair.getPublic(), publicKeyFilename);
        boolean privSaved = saveKey(keyPair.getPrivate(), privateKeyFilename);
        return pubSaved && privSaved;
    }

    public static <T extends Key> T loadKey(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            @SuppressWarnings("unchecked")
            T key = (T) ois.readObject();
            return key;
        }
    }

    //-----------------------------------------------------------------------------

    public static KeyPair getOrGenerateKeyPair(String name) throws NoSuchAlgorithmException, IOException, ClassNotFoundException {
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
