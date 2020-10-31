package net.martins.ansible.fx;

import net.martins.ansible.vault.VaultHandler;
import net.martins.ansible.vault.crypto.Util;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses a clear text ansible vault variable
 * and encrypts it
 */
public class VaultDecryptedParser {

    private static final String VARIABLE_PATERN = "\\s*\\S+:.+$";
    private static final String COLLON = ":";
    private static final String VAULT_INDENT = "    ";
    private static final String VAULT_NO_INDENT = "";
    private String variableName;
    private String variableValue;


    /**
     * Parses the context of textArea (encrypted or decrypted vault variable) and
     * stores the fields that is finds
     * @param textAreaContent
     */
    public void parseEncryptedText(final String textAreaContent) throws GeneralSecurityException {

        String[] linearray = textAreaContent.split(Util.LINE_BREAK);

        if(hasJustOneVariable(linearray)) {
            final List<String> lines = Arrays.stream(linearray)
                    .filter(l -> StringUtils.hasText(l))
                    .map(l -> l.trim())
                    .collect(Collectors.toList());

            String firstLine = lines.get(0);
            variableName = firstLine.substring(0, firstLine.indexOf(COLLON));
            lines.set(0, firstLine.substring(firstLine.indexOf(COLLON) + 1));
            variableValue = String.join("", lines).trim();
        }
        else {
            variableValue = textAreaContent;
        }
    }

    /**
     * If the text contains more then one variable
     * @param linearray
     * @return
     */
    private boolean hasJustOneVariable(final String[] linearray) {
        for(int count=0, i=0; i < linearray.length; i++) {
            if(linearray[i].matches(VARIABLE_PATERN)) {
                count++;
            }
            if(count > 1) {
                return false;
            }
        }
        return true;
    }

    public String getEncryptedVault(final String password) throws GeneralSecurityException {
        final StringBuilder variableBuilder = new StringBuilder();
        final String indent;
        if(variableName != null) {
            variableBuilder.append(variableName).append(COLLON);
            variableBuilder.append(" ").append(VaultEncryptedParser.VARIABLE_MARKER);
            variableBuilder.append(Util.LINE_BREAK);
            indent = VAULT_INDENT;
        }
        else {
            indent = VAULT_NO_INDENT;
        }

        final byte[] clearText = variableValue.getBytes(StandardCharsets.UTF_8);
        final byte [] encryptedText = VaultHandler.encrypt(clearText, password);
        final String vaultText = Arrays.stream(new String(encryptedText, StandardCharsets.UTF_8).split(Util.LINE_BREAK))
                .map(l -> indent.concat(l).concat(Util.LINE_BREAK))
                .collect(Collectors.joining());
        variableBuilder.append(vaultText);
        variableBuilder.append(Util.LINE_BREAK);
        return variableBuilder.toString();
    }
}
