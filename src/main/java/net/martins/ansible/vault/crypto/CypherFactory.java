package net.martins.ansible.vault.crypto;

import net.martins.ansible.vault.crypto.decoders.Cypher;
import net.martins.ansible.vault.crypto.decoders.impl.CypherAES;
import net.martins.ansible.vault.crypto.decoders.impl.CypherAES256;

import java.util.Optional;

public class CypherFactory {

    public static Optional<Cypher> getCypher(String cypherName) {
        if (cypherName.equals(CypherAES.CYPHER_ID)) {
            return Optional.of(new CypherAES());
        }

        if (cypherName.equals(CypherAES256.CYPHER_ID)) {
            return Optional.of(new CypherAES256());
        }

        return Optional.empty();
    }

}
