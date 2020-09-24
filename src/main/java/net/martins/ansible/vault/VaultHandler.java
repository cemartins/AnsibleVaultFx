package net.martins.ansible.vault;

import net.martins.ansible.vault.crypto.CypherFactory;
import net.martins.ansible.vault.crypto.Util;
import net.martins.ansible.vault.crypto.VaultInfo;
import net.martins.ansible.vault.crypto.decoders.Cypher;
import net.martins.ansible.vault.crypto.decoders.impl.CypherAES256;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.Security;
import java.util.Arrays;
import java.util.Optional;

public class VaultHandler {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String DEFAULT_CYPHER = CypherAES256.CYPHER_ID;
    private static final String LINE_BREAK = "\n";

    public static byte[] encrypt(byte[] cleartext, String password) throws IOException {
        return encrypt(cleartext, password, DEFAULT_CYPHER);
    }

    public static byte[] encrypt(byte[] cleartext, String password, String cypher) throws IOException {
        Optional<Cypher> cypherInstance = CypherFactory.getCypher(cypher);
        if (!cypherInstance.isPresent()) {
            throw new IOException("Unsupported vault cypher");
        }

        byte[] vaultData = cypherInstance.get().encrypt(cleartext, password);
        String vaultDataString = new String(vaultData);
        String vaultPackage = cypherInstance.get().infoLine() + "\n" + vaultDataString;
        return vaultPackage.getBytes();
    }

    public static byte[] decrypt(String encrypted, String password) throws IOException {
        final int firstLineBreakIndex = encrypted.indexOf(LINE_BREAK);

        final String infoLinePart = encrypted.substring(0, firstLineBreakIndex);
        final VaultInfo vaultInfo = new VaultInfo(infoLinePart);

        final String vaultDataPart = encrypted.substring(firstLineBreakIndex + 1);
        final byte[] encryptedData = getVaultData(vaultDataPart);

        return vaultInfo.getCypher().decrypt(encryptedData, password);
    }

    private static byte[] getVaultData(String vaultData) {
        final String rawData = removeLineBreaks(vaultData);
        return Util.unhex(rawData);
    }

    private static String removeLineBreaks(final String string) {
        final String[] lines = string.split(LINE_BREAK);
        return String.join("", Arrays.asList(lines));
    }

}
