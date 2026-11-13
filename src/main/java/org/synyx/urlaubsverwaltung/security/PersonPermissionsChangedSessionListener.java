package org.synyx.urlaubsverwaltung.security;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.synyx.urlaubsverwaltung.person.PersonPermissionsChangedEvent;

/**
 * Reloads the authorities of a signed-in person on every permission change, wherever it comes from
 * (permissions page, extension, first office user).
 */
@Component
class PersonPermissionsChangedSessionListener {

    private final SessionService sessionService;

    PersonPermissionsChangedSessionListener(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // after commit: a request reloading the authorities before would read the old permissions
    @TransactionalEventListener(fallbackExecution = true)
    void on(PersonPermissionsChangedEvent event) {
        sessionService.markSessionToReloadAuthorities(event.username());
    }
}
