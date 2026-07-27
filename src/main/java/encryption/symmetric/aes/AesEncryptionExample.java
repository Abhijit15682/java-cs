package encryption.symmetric.aes;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;


public class AesEncryptionExample {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256; // 128,192 or 256 bits
    private static final int IV_SIZE_BYTES = 12; // Recommended IV size for GCM
    private static final int TAG_BIT_LENGTH = 128; // Authentication tag length

    public static void main(String[] args) throws Exception {
        String originalText = "Hello, this is a secret message!";
        // 1. Generate secure random AES secret key
        SecretKey secretKey = generateKey();
        // 2. Generate a unique Initial Vector(IV) for this encryption
        byte[] iv = generateIv();
        // 3. Encrypt the data
        String encryptedText = encrypt(originalText, secretKey, iv);
        System.out.println("Encrypted (Base64) : " + encryptedText);
        // 4. Decrypt the data back
        String decryptedText = decrypt(encryptedText, secretKey, iv);
        System.out.println("Decrypted : "+decryptedText);

    }
    // Generate a secure AES key
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        return keyGenerator.generateKey();
    }

    //Generator a unique Initialization Vector(IV)
    public static byte[] generateIv() {
        byte[] iv = new byte[IV_SIZE_BYTES];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
    // Encrypt plain text
    public static String encrypt(String input, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] cipherText = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(cipherText);
    }

    // Decrypt cipher text
    public static String decrypt(String chiperText, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] decodedBytes = Base64.getDecoder().decode(chiperText);
        byte[] plainTextBytes = cipher.doFinal(decodedBytes);
        return new String(plainTextBytes, StandardCharsets.UTF_8);
    }
    
}
