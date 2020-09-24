package net.martins.ansible.fx;

import net.martins.ansible.vault.crypto.Util;
import net.martins.ansible.vault.crypto.VaultInfo;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses an encrypted ansible vault variable
 * and decrypts it
 */
public class VaultEncryptedParser {

    public static final String VARIABLE_MARKER = "!vault |";
    private String variableName;
    private VaultInfo vaultInfo;
    private byte[] encryptedData;


    /**
     * Parses the context of textArea (encrypted or decrypted vault variable) and
     * stores the fields that is finds
     * @param textAreaContent
     */
    public void parseEncryptedText(String textAreaContent) throws GeneralSecurityException {

        final List<String> lines = Arrays.stream(textAreaContent.split(Util.LINE_BREAK))
                .filter(l -> StringUtils.hasText(l))
                .map(l -> l.trim())
                .collect(Collectors.toList());

        String firstLine = lines.get(0);
        if(firstLine.contains(VARIABLE_MARKER)) {
            variableName = firstLine.substring(0, firstLine.indexOf(VARIABLE_MARKER));
            lines.remove(0);
        }

        vaultInfo = new VaultInfo(lines.get(0));

        lines.remove(0);
        final String vaultText = String.join("", lines);
        encryptedData = Util.unhex(vaultText);
    }

    public String getDecryptedVault(String password) throws GeneralSecurityException {
        StringBuilder variableBuilder = new StringBuilder();

        if(variableName != null) {
            variableBuilder.append(variableName);
        }

        final byte[] clearText = vaultInfo.getCypher().decrypt(encryptedData, password);
        variableBuilder.append(new String(clearText, StandardCharsets.UTF_8));
        return variableBuilder.toString();
    }
}
