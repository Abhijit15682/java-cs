package encryption.asymmetric.rsa;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;

public class RsaEncryptionExample {

    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int KEY_SIZE = 2048; //Recommended minimum size.

    public static void main(String[] args) throws Exception {
        String originalText = "Secret message encrypted with RSA!";
        System.out.println("Original : " + originalText);

        // 1. Generate a public/private key pair
        KeyPair keyPair = generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // 2. Encrypt using PUBLIC key
        String encryptedText = encrypt(originalText, publicKey);
        System.out.println("Encrypted (Base64): " + encryptedText);

        // 3. Decrypt using PRIVATE key
        String decryptedText = decrypt(encryptedText, privateKey);
        System.out.println("Decrypted : " + decryptedText);
    }

    // Generate a secure 2048 bit RSA key pair 
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(KEY_SIZE);
        return keyPairGen.genKeyPair();        
    }

    // Encrypt plain text using public key
    public static String encrypt(String input, PublicKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] cipherTextBytes = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(cipherTextBytes);
    }

    // Decrypt cipher text using private key
    public static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
        byte[] plainTextBytes = cipher.doFinal(decodedBytes);
        return new String(plainTextBytes, StandardCharsets.UTF_8);
    }

}