package org.synyx.urlaubsverwaltung.security;

/**
 * Is thrown in case of missing member affiliation.
 */
public class UnsupportedMemberAffiliationException extends Exception {

    UnsupportedMemberAffiliationException(String message) {

        super(message);
    }
}
