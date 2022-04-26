package org.synyx.urlaubsverwaltung.security;


public interface SessionService<S> {

    /**
     * Mark the session of the given username to reload the authorities on the next page request
     *
     * @param username to mark to reload authorities
     */
    void markSessionToReloadAuthorities(String username);

    /**
     * Saves the given session
     *
     * @param session to save
     */
    void save(S session);
}
