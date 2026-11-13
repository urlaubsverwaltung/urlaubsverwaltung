package org.synyx.urlaubsverwaltung.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonPermissionsChangedEvent;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonPermissionsChangedSessionListenerTest {

    @Mock
    private SessionService sessionService;

    @Test
    void ensureToMarkSessionToReloadAuthoritiesWhenPermissionsChanged() {

        final Person person = new Person("username", "Meier", "Nina", "nina@example.org");
        person.setId(1L);

        new PersonPermissionsChangedSessionListener(sessionService)
            .on(PersonPermissionsChangedEvent.of(person, List.of(USER, OFFICE), List.of(USER)));

        verify(sessionService).markSessionToReloadAuthorities("username");
    }
}
