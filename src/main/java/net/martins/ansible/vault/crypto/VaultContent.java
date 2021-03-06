package net.martins.ansible.vault.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public class VaultContent {

    private static final Logger logger = LoggerFactory.getLogger(VaultContent.class);

    private final static String CHAR_ENCODING = "UTF-8";

    private final byte[] salt;
    private final byte[] hmac;
    private final byte[] data;

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getHmac() {
        return hmac;
    }

    public byte[] getData() {
        return data;
    }

    public VaultContent(byte[] encryptedVault) throws GeneralSecurityException {
        byte[][] vaultContents = splitData(encryptedVault);
        try {
            salt = Util.unhex(new String(vaultContents[0], CHAR_ENCODING));
            hmac = Util.unhex(new String(vaultContents[1], CHAR_ENCODING));
            data = Util.unhex(new String(vaultContents[2], CHAR_ENCODING));
        } catch (UnsupportedEncodingException e) {
            throw new GeneralSecurityException("Cannot convert vault contents to " + CHAR_ENCODING, e);
        }
    }

    public VaultContent(byte[] salt, byte[] hmac, byte[] data) {
        this.salt = salt;
        this.hmac = hmac;
        this.data = data;
    }

    public byte[] toByteArray() {
        return toString().getBytes();
    }

    public String toString() {
        logger.debug("Salt: {} - HMAC: {} - Data: {} - TargetLen: {}"
                , salt.length, hmac.length, data.length, (salt.length + hmac.length + data.length) * 2);
        String saltString = Util.hexit(salt);
        logger.debug("Salt String Length: {}", saltString.length());
        String hmacString = Util.hexit(hmac);
        logger.debug("HMAC String Length: {}", hmacString.length());
        String dataString = Util.hexit(data, -1);
        logger.debug("DATA String Length: {}", dataString.length());
        String complete = saltString + "\n" + hmacString + "\n" + dataString;
        logger.debug("Complete: {} \n{}", complete.length(), complete);
        String result = Util.hexit(complete.getBytes(), 80);
        logger.debug("Result: [{}] {}\n{}", complete.length() * 2, result.length(), result);
        return result;
    }

    private int[] getDataLengths(byte[] encodedData) throws GeneralSecurityException {
        int[] result = new int[3];

        int idx = 0;
        int saltLen = 0;
        while (encodedData[idx] != '\n' && idx < encodedData.length) {
            saltLen++;
            idx++;
        }
        // Skip the newline
        idx++;
        if (idx == encodedData.length) {
            throw new GeneralSecurityException("Malformed data - salt incomplete");
        }
        result[0] = saltLen;

        int hmacLen = 0;
        while (encodedData[idx] != '\n' && idx < encodedData.length) {
            hmacLen++;
            idx++;
        }
        // Skip the newline
        idx++;
        if (idx == encodedData.length) {
            throw new GeneralSecurityException("Malformed data - hmac incomplete");
        }
        result[1] = hmacLen;
        int dataLen = 0;
        while (idx < encodedData.length) {
            dataLen++;
            idx++;
        }
        result[2] = dataLen;

        return result;
    }

    private byte[][] splitData(byte[] encodedData) throws GeneralSecurityException {
        int[] partsLength = getDataLengths(encodedData);

        byte[][] result = new byte[3][];

        int idx = 0;
        int saltIdx = 0;
        result[0] = new byte[partsLength[0]];
        while (encodedData[idx] != '\n' && idx < encodedData.length) {
            result[0][saltIdx++] = encodedData[idx++];
        }
        // Skip the newline
        idx++;
        if (idx == encodedData.length) {
            throw new GeneralSecurityException("Malformed data - salt incomplete");
        }
        int macIdx = 0;
        result[1] = new byte[partsLength[1]];
        while (encodedData[idx] != '\n' && idx < encodedData.length) {
            result[1][macIdx++] = encodedData[idx++];
        }
        // Skip the newline
        idx++;
        if (idx == encodedData.length) {
            throw new GeneralSecurityException("Malformed data - hmac incomplete");
        }
        int dataIdx = 0;
        result[2] = new byte[partsLength[2]];
        while (idx < encodedData.length) {
            result[2][dataIdx++] = encodedData[idx++];
        }
        return result;
    }

}
