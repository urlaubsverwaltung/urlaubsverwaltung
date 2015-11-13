package org.synyx.urlaubsverwaltung.core.keys;

import org.apache.log4j.Logger;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.CryptoUtil;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;


/**
 * Service to generate key pair (private key and public key) for {@link Person}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class KeyPairService {

    private static final Logger LOG = Logger.getLogger(KeyPairService.class);

    /**
     * Generates a key pair for the person with the given username.
     *
     * @param  username  of the person to create the key pair for
     *
     * @return  the created key pair
     */
    public KeyPair generate(String username) {

        try {
            return CryptoUtil.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("An error occurred while trying to generate a key pair for the person with username = "
                + username, ex);

            throw new IllegalStateException(
                "Could not generate key pair. Check the algorithm used for key pair generation!", ex);
        }
    }
}
