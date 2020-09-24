package net.martins.ansible.vault.crypto.decoders.impl;

import net.martins.ansible.vault.crypto.Util;
import net.martins.ansible.vault.crypto.VaultContent;
import net.martins.ansible.vault.crypto.VaultInfo;
import net.martins.ansible.vault.crypto.decoders.Cypher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class CypherAES256 implements Cypher {

    public static final String CYPHER_ID = "AES256";

    private static final Logger logger = LoggerFactory.getLogger(CypherAES256.class);
    private static final String JDK8_UPF_URL = "http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html";

    private static final int AES_KEYLEN = 256;
    private static final String CHAR_ENCODING = "UTF-8";
    private static final String KEYGEN_ALGO = "HmacSHA256";
    private static final String CYPHER_KEY_ALGO = "AES";
    private static final String CYPHER_ALGO = "AES/CTR/PKCS7Padding";
    private static final int KEYLEN = 32;
    private static final int IVLEN = 16;
    private static final int ITERATIONS = 10000;
    private static final int SALT_LENGTH = 32;

    private boolean hasValidAESProvider() {
        boolean canCrypt = false;
        try {
            int maxKeyLen = Cipher.getMaxAllowedKeyLength(CYPHER_ALGO);
            logger.debug("Available keylen: {}", maxKeyLen);
            if (maxKeyLen >= AES_KEYLEN) {
                canCrypt = true;
            } else {
                logger.warn(String.format("JRE doesn't support %s keylength for %sInstall unrestricted policy files from:%s",
                        AES_KEYLEN, CYPHER_KEY_ALGO, JDK8_UPF_URL));
            }
        } catch (Exception ex) {
            logger.warn(String.format("Failed to check for proper cypher algorithms: %s", ex.getMessage()));
        }
        return canCrypt;
    }

    public byte[] calculateHMAC(byte[] key, byte[] data) throws GeneralSecurityException {
        byte[] computedMac;

        SecretKeySpec hmacKey = new SecretKeySpec(key, KEYGEN_ALGO);
        Mac mac = Mac.getInstance(KEYGEN_ALGO);
        mac.init(hmacKey);
        computedMac = mac.doFinal(data);

        return computedMac;
    }

    public boolean verifyHMAC(byte[] hmac, byte[] key, byte[] data) throws GeneralSecurityException {
        byte[] calculated = calculateHMAC(key, data);
        return Arrays.equals(hmac, calculated);
    }

    public byte[] decryptAES(byte[] cypher, byte[] key, byte[] iv) throws GeneralSecurityException {

        SecretKeySpec keySpec = new SecretKeySpec(key, CYPHER_KEY_ALGO);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(CYPHER_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(cypher);
    }

    public byte[] encryptAES(byte[] cleartext, byte[] key, byte[] iv) throws GeneralSecurityException {
        SecretKeySpec keySpec = new SecretKeySpec(key, CYPHER_KEY_ALGO);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(CYPHER_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(cleartext);
    }

    public byte[] decrypt(byte[] data, String password) throws GeneralSecurityException {
        byte[] decrypted;

        if (!hasValidAESProvider()) {
            throw new GeneralSecurityException("Missing valid AES256 provider - install unrestricted policy profiles.");
        }

        VaultContent vaultContent = new VaultContent(data);

        byte[] salt = vaultContent.getSalt();
        byte[] hmac = vaultContent.getHmac();
        byte[] cypher = vaultContent.getData();
        logger.debug("Salt: {} - {}", salt.length, Util.hexit(salt, 100));
        logger.debug("HMAC: {} - {}", hmac.length, Util.hexit(hmac, 100));
        logger.debug("Data: {} - {}", cypher.length, Util.hexit(cypher, 100));

        EncryptionKeychain keys = new EncryptionKeychain(salt, password, KEYLEN, IVLEN, ITERATIONS, KEYGEN_ALGO);
        keys.createKeys();

        byte[] cypherKey = keys.getEncryptionKey();
        logger.debug("Key 1: {} - {}", cypherKey.length, Util.hexit(cypherKey, 100));
        byte[] hmacKey = keys.getHmacKey();
        logger.debug("Key 2: {} - {}", hmacKey.length, Util.hexit(hmacKey, 100));
        byte[] iv = keys.getIv();
        logger.debug("IV: {} - {}", iv.length, Util.hexit(iv, 100));

        if (verifyHMAC(hmac, hmacKey, cypher)) {
            logger.debug("Signature matches - decrypting");
            decrypted = decryptAES(cypher, cypherKey, iv);
            try {
                logger.debug("Decoded:\n{}", new String(decrypted, CHAR_ENCODING));
            } catch (UnsupportedEncodingException e) {
                throw new GeneralSecurityException("Cannot convert decrypted contents to " + CHAR_ENCODING, e);
            }
        } else {
            throw new GeneralSecurityException("HMAC Digest doesn't match - possibly it's the wrong password.");
        }

        return decrypted;
    }

    public String infoLine() {
        return VaultInfo.vaultInfoForCypher(CYPHER_ID);
    }

    public byte[] encrypt(byte[] data, String password) throws GeneralSecurityException {
        EncryptionKeychain keys = new EncryptionKeychain(SALT_LENGTH, password, KEYLEN, IVLEN, ITERATIONS, KEYGEN_ALGO);
        keys.createKeys();
        byte[] cypherKey = keys.getEncryptionKey();
        logger.debug("Key 1: {} - {}", cypherKey.length, Util.hexit(cypherKey, 100));
        byte[] hmacKey = keys.getHmacKey();
        logger.debug("Key 2: {} - {}", hmacKey.length, Util.hexit(hmacKey, 100));
        byte[] iv = keys.getIv();
        logger.debug("IV: {} - {}", iv.length, Util.hexit(iv, 100));
        logger.debug("Data length: {}", data.length);
        byte[] encrypted = encryptAES(data, keys.getEncryptionKey(), keys.getIv());
        byte[] hmacHash = calculateHMAC(keys.getHmacKey(), encrypted);
        VaultContent vaultContent = new VaultContent(keys.getSalt(), hmacHash, encrypted);
        return vaultContent.toByteArray();
    }

}
