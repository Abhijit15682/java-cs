package com.hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptographicHashingDemo {

    public static void main(String[] args) {
        String inputData = "HelloJava";
        
        // List of all standard JCA algorithm names
        String[] algorithms = {
            "MD5", 
            "SHA-1", 
            "SHA-224", 
            "SHA-256", 
            "SHA-384", 
            "SHA-512", 
            "SHA3-256"
        };
        
        System.out.println("Input Text: " + inputData + "\n");
        System.out.println("--- Generated Cryptographic Hashes ---");
        
        for (String algo : algorithms) {
            try {
                String hashHex = generateHash(inputData, algo);
                System.out.printf("%-10s : %s%n", algo, hashHex);
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Algorithm not supported: " + algo);
            }
        }
    }

    /**
     * Generates a hexadecimal hash string for a given text input and algorithm.
     */
    public static String generateHash(String input, String algorithm) throws NoSuchAlgorithmException {
        // 1. Get the appropriate MessageDigest instance
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        
        // 2. Perform the hashing to get raw bytes
        byte[] encodedBlocks = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        
        // 3. Convert the raw byte array into a readable Hexadecimal String
        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedBlocks) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0'); // Pad with a leading zero if single digit
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
}
