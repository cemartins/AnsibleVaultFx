package net.martins.ansible.vault.crypto.decoders;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface Cypher {

    /**
     * Encrypt data with password
     *
     * @param data     raw data
     * @param password password
     * @return encrypted data
     * @throws IOException if encryption fails
     */
    byte[] encrypt(byte[] data, String password) throws GeneralSecurityException;

    /**
     * Decrypt data with password
     *
     * @param data     encrypted data
     * @param password password
     * @return decrypted data
     * @throws IOException if decryption fails
     */
    byte[] decrypt(byte[] data, String password) throws GeneralSecurityException;

    /**
     * Get Cypher info line
     *
     * @return info line
     * @see <a href="https://docs.ansible.com/ansible/latest/user_guide/vault.html#vault-format">Vault Format</a>
     */
    String infoLine();

}
