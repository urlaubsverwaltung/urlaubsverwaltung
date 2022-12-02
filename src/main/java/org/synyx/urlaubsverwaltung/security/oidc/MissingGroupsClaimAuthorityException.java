package org.synyx.urlaubsverwaltung.security.oidc;

import org.springframework.security.authentication.AccountStatusException;

/**
 * Thrown when a user doesn't have the required authority from the groups claim.
 */
public class MissingGroupsClaimAuthorityException extends AccountStatusException {
    /**
     * Constructs a <code>MissingAuthorityException</code> with the specified message.
     * @param msg the detail message
     */
    public MissingGroupsClaimAuthorityException(String msg) {
        super(msg);
    }

    /**
     * Constructs a <code>MissingAuthorityException</code> with the specified message and root
     * cause.
     * @param msg the detail message
     * @param cause root cause
     */
    public MissingGroupsClaimAuthorityException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
