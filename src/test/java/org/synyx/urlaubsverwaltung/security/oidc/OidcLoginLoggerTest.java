package org.synyx.urlaubsverwaltung.security.oidc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OidcLoginLoggerTest {

    private static final Integer UNIQUE_ID = 42;

    private PersonService personService;
    private OidcLoginLogger sut;
    private Authentication authentication;
    private ListAppender<ILoggingEvent> loggingEventAppender;

    @BeforeEach
    void setup() {
        personService = mock(PersonService.class);
        authentication = prepareAuthentication();
        loggingEventAppender = prepareLoggingEventAppender();

        sut = new OidcLoginLogger(personService);
    }

    private ListAppender<ILoggingEvent> prepareLoggingEventAppender() {

        // get Logback Logger
        Logger OidcLoginLoggerLogger = (Logger) LoggerFactory.getLogger(OidcLoginLogger.class);

        // because of global test logging level WARN
        OidcLoginLoggerLogger.setLevel(Level.INFO);

        // create and start a ListAppender
        ListAppender<ILoggingEvent> loggingEventAppender = new ListAppender<>();
        loggingEventAppender.start();

        // add the appender to the logger
        OidcLoginLoggerLogger.addAppender(loggingEventAppender);

        return loggingEventAppender;
    }

    private Authentication prepareAuthentication() {
        Authentication authentication = mock(Authentication.class);
        OidcUser oidcUser = mock(OidcUser.class);
        OidcIdToken oidcIdToken = mock(OidcIdToken.class);
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getIdToken()).thenReturn(oidcIdToken);
        when(oidcIdToken.getSubject()).thenReturn(UNIQUE_ID.toString());

        return authentication;
    }

    @Test
    void ensureLoggingUserIdForExistingUser() {

        Person person = new Person("username", "lastname", "firstname", "firstname.lastname@example.org");
        person.setId(UNIQUE_ID);
        when(personService.getPersonByUsername(UNIQUE_ID.toString())).thenReturn(Optional.of(person));

        sut.handle(new AuthenticationSuccessEvent(authentication));

        assertThat(loggingEventAppender.list)
            .extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
            .containsExactly(Tuple.tuple("User '42' has signed in", Level.INFO));
    }

    @Test
    void ensureLoggingErrorOnNonExistingUser() {
        when(personService.getPersonByUsername(UNIQUE_ID.toString())).thenReturn(Optional.empty());

        sut.handle(new AuthenticationSuccessEvent(authentication));

        assertThat(loggingEventAppender.list)
            .extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
            .containsExactly(Tuple.tuple("Could not find signed-in user with id '42'", Level.ERROR));

    }


}
