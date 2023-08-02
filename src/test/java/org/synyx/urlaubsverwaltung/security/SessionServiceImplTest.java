package org.synyx.urlaubsverwaltung.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;
import org.springframework.session.Session;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    private SessionServiceImpl<Session> sut;

    @Mock
    private FindByIndexNameSessionRepository<Session> sessionRepository;

    @BeforeEach
    void setUp() {
        sut = new SessionServiceImpl<>(sessionRepository);
    }

    @Test
    void ensureToDeleteSessionByEvent() {

        when(sessionRepository.findByPrincipalName("username"))
            .thenReturn(Map.of("65266d07-2ab0-400b-86b5-4b609e552399", new MapSession("someId")));

        final Person person = new Person("username", "username", "username", "username@example.org");
        sut.deleteSessionByEvent(new PersonDeletedEvent(person));

        verify(sessionRepository).deleteById("someId");
    }

    @Test
    void markSessionToReloadAuthorities() {

        final String username = "username";
        when(sessionRepository.findByPrincipalName(username))
            .thenReturn(Map.of("65266d07-2ab0-400b-86b5-4b609e552399", new MapSession()));

        sut.markSessionToReloadAuthorities(username);

        final ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        final Session session = captor.getValue();
        assertThat((Boolean) session.getAttribute("reloadAuthorities")).isTrue();
    }

    @Test
    void unmarkSessionToReloadAuthorities() {

        final String someSessionId = "SomeSessionId";

        final MapSession mapSession = new MapSession();
        mapSession.setId(someSessionId);
        when(sessionRepository.findById(someSessionId)).thenReturn(mapSession);

        sut.unmarkSessionToReloadAuthorities(someSessionId);

        final ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        final Session session = captor.getValue();
        assertThat((Boolean) session.getAttribute("reloadAuthorities")).isNull();
    }
}
