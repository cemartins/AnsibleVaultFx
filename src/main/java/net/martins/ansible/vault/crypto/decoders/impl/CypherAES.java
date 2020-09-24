package net.martins.ansible.vault.crypto.decoders.impl;

import net.martins.ansible.vault.crypto.VaultInfo;
import net.martins.ansible.vault.crypto.decoders.Cypher;

import java.io.IOException;

public class CypherAES implements Cypher {

    public static final String CYPHER_ID = "AES";

    public byte[] decrypt(byte[] data, String password) throws IOException {
        throw new IOException(CYPHER_ID + " is not implemented.");
    }

    public byte[] encrypt(byte[] data, String password) throws IOException {
        throw new IOException(CYPHER_ID + " is not implemented.");
    }

    public String infoLine() {
        return VaultInfo.vaultInfoForCypher(CYPHER_ID);
    }

}
