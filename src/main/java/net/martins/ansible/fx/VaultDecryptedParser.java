package net.martins.ansible.fx;

import net.martins.ansible.vault.VaultHandler;
import net.martins.ansible.vault.crypto.Util;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses a clear text ansible vault variable
 * and encrypts it
 */
public class VaultDecryptedParser {

    private static final String COLLON = ":";
    private static final String VAULT_INDENT = "    ";
    private String variableName;
    private String variableValue;


    /**
     * Parses the context of textArea (encrypted or decrypted vault variable) and
     * stores the fields that is finds
     * @param textAreaContent
     */
    public void parseEncryptedText(String textAreaContent) throws CharacterCodingException {

        final List<String> lines = Arrays.stream(textAreaContent.split(Util.LINE_BREAK))
                .filter(l -> StringUtils.hasText(l))
                .map(l -> l.trim())
                .collect(Collectors.toList());

        String firstLine = lines.get(0);
        if(firstLine.contains(COLLON)) {
            variableName = firstLine.substring(0, firstLine.indexOf(COLLON));
            lines.set(0, firstLine.substring(firstLine.indexOf(COLLON) + 1));
        }
        variableValue = String.join("", lines).trim();
    }

    public String getEncryptedVault(String password) throws IOException {
        StringBuilder variableBuilder = new StringBuilder();

        if(variableName != null) {
            variableBuilder.append(variableName).append(COLLON);
            variableBuilder.append(" ").append(VaultEncryptedParser.VARIABLE_MARKER);
            variableBuilder.append(Util.LINE_BREAK);
        }

        final byte[] clearText = variableValue.getBytes(StandardCharsets.UTF_8);
        final byte [] encryptedText = VaultHandler.encrypt(clearText, password);
        final String vaultText = Arrays.stream(new String(encryptedText, StandardCharsets.UTF_8).split(Util.LINE_BREAK))
                .map(l -> VAULT_INDENT.concat(l).concat(Util.LINE_BREAK))
                .collect(Collectors.joining());
        variableBuilder.append(vaultText);
        variableBuilder.append(Util.LINE_BREAK);
        return variableBuilder.toString();
    }
}
