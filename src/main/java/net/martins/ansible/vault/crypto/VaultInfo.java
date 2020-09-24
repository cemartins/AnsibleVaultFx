package net.martins.ansible.vault.crypto;

import net.martins.ansible.vault.crypto.decoders.Cypher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.util.Optional;

public class VaultInfo {

    private static final Logger logger = LoggerFactory.getLogger(VaultInfo.class);

    public static final String INFO_SEPARATOR = ";";
    public static final String MAGIC_PART_DATA = "$ANSIBLE_VAULT";
    public static final String VERSION_PART_DATA = "1.1";
    public static final int INFO_ELEMENTS = 3;
    public static final int MAGIC_PART_INDEX = 0;
    public static final int VERSION_PART_INDEX = 1;
    public static final int CYPHER_PART_INDEX = 2;

    private final String vaultVersion;
    private final Cypher cypher;

    public static String vaultInfoForCypher(final String vaultCypher) {
        return MAGIC_PART_DATA + ";" + VERSION_PART_DATA + ";" + vaultCypher;
    }

    public VaultInfo(final String infoLine) throws GeneralSecurityException {
        logger.debug("Ansible Vault info: {}", infoLine);

        final String[] infoParts = infoLine.split(INFO_SEPARATOR);

        if (infoParts.length != INFO_ELEMENTS || !MAGIC_PART_DATA.equals(infoParts[MAGIC_PART_INDEX])) {
            throw new GeneralSecurityException("File is not an Ansible Encrypted Vault");
        }

        vaultVersion = infoParts[VERSION_PART_INDEX];

        final String vaultCypherName = infoParts[CYPHER_PART_INDEX];
        final Optional<Cypher> optionalCypher = CypherFactory.getCypher(vaultCypherName);
        if (!optionalCypher.isPresent()) {
            throw new GeneralSecurityException("Unsupported vault cypher");
        }
        this.cypher = optionalCypher.get();
    }

    public String getVaultVersion() {
        return vaultVersion;
    }

    public Cypher getCypher() {
        return cypher;
    }

}
