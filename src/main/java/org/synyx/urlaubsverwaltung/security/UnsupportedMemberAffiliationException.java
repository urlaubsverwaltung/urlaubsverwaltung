package org.synyx.urlaubsverwaltung.security;

/**
 * Is thrown in case of missing member affiliation.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UnsupportedMemberAffiliationException extends Exception {

    public UnsupportedMemberAffiliationException(String message) {

        super(message);
    }
}
