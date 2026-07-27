package blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// 1. Represent an individual Block in the chain
class Block {
    public String hash;
    public String previousHash;
    private String data; 
    private long timeStamp;
    private int nonce; // A counter used for the mining process

    // Block Constructor
    public Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash(); 
    }

    // 2. Calculate the block's cryptographic hash based on its contents
    public String calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Combine all block variables into one input string
            String input = previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + data;
            
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 3. Simple Proof-of-Work (Mining) system
    // Forces the computer to solve a puzzle: find a hash that starts with 'difficulty' number of zeros
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); 
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined successfully! Hash: " + hash);
    }
}

// 4. Manage the Blockchain and verify its integrity
public class SimpleBlockchain {

    public static List<Block> blockchain = new ArrayList<>();
    public static int difficulty = 4; // Number of leading zeros required to mine

    public static void main(String[] args) {
        
        // Add the very first block (Genesis block)
        System.out.println("Mining Block 1 (Genesis)... ");
        addBlock(new Block("Genesis Block - First Transaction Data", "0"));
        
        System.out.println("\nMining Block 2... ");
        addBlock(new Block("Alice pays Bob 10 BTC", blockchain.get(blockchain.size() - 1).hash));
        
        System.out.println("\nMining Block 3... ");
        addBlock(new Block("Bob pays Charlie 5 BTC", blockchain.get(blockchain.size() - 1).hash));
        
        // Check if our blockchain is secure and valid
        System.out.println("\nIs the blockchain valid? " + isChainValid());
        
        // TAMPER DEMONSTRATION: Let's try to cheat by changing Block 2's data
        System.out.println("\n--- Attempting to tamper with Block 2 data ---");
        blockchain.get(1).mineBlock(0); // Forcing a change without mining properly or recalculating later blocks
        // Directly changing the text of block 2 data variable wouldn't match its hash, 
        // and changing the hash breaks the link to block 3.
        
        System.out.println("Is the blockchain still valid? " + isChainValid());
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

    // 5. Verification logic to ensure nothing has been altered
    public static Boolean isChainValid() {
        Block currentBlock; 
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        
        // Loop through the blockchain to check the hashes
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            
            // Check 1: Has the data inside the current block been modified?
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Error: Current hash does not match calculated hash!");
                return false;
            }
            // Check 2: Does the current block link accurately to the previous block's hash?
            if (!currentBlock.previousHash.equals(previousBlock.hash)) {
                System.out.println("Error: Previous hash link broken!");
                return false;
            }
            // Check 3: Has the block actually been mined according to difficulty rules?
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("Error: This block hasn't been properly mined!");
                return false;
            }
        }
        return true;
    }
}
