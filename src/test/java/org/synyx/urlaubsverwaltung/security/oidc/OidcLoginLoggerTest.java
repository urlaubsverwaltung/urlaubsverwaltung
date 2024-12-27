package org.synyx.urlaubsverwaltung.security.oidc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static ch.qos.logback.classic.Level.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OidcLoginLoggerTest {

    private OidcLoginLogger sut;

    @Mock
    private PersonService personService;

    private ListAppender<ILoggingEvent> loggingEventAppender;

    @BeforeEach
    void setup() {
        loggingEventAppender = loggingEventAppender();

        sut = new OidcLoginLogger(personService);
    }

    @Test
    void ensureLoggingUserIdForExistingUser() {

        final Person person = new Person("uniqueIdentifier", "lastname", "firstname", "firstname.lastname@example.org");
        person.setId(42L);
        when(personService.getPersonByUsername("uniqueIdentifier")).thenReturn(Optional.of(person));

        final Authentication authentication = prepareAuthentication();
        sut.handle(new AuthenticationSuccessEvent(authentication));

        assertThat(loggingEventAppender.list)
            .extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
            .containsExactly(Tuple.tuple("User '42' has signed in", INFO));
    }

    @Test
    void ensureLoggingErrorOnNonExistingUser() {
        when(personService.getPersonByUsername("uniqueIdentifier")).thenReturn(Optional.empty());

        final Authentication authentication = prepareAuthentication();
        sut.handle(new AuthenticationSuccessEvent(authentication));

        assertThat(loggingEventAppender.list)
            .extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
            .containsExactly(Tuple.tuple("Could not find signed-in user with id 'uniqueIdentifier'", Level.ERROR));
    }

    @Test
    void ensureNotLoggingIfJWT() {

        final Authentication authentication = mock(Authentication.class);
        final Jwt jwt = mock(Jwt.class);
        when(authentication.getPrincipal()).thenReturn(jwt);

        sut.handle(new AuthenticationSuccessEvent(authentication));

        assertThat(loggingEventAppender.list)
            .extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
            .isEmpty();
    }

    private Authentication prepareAuthentication() {
        final Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("uniqueIdentifier");
        return authentication;
    }

    private ListAppender<ILoggingEvent> loggingEventAppender() {

        // get Logback Logger
        final Logger OidcLoginLoggerLogger = (Logger) LoggerFactory.getLogger(OidcLoginLogger.class);

        // because of global test logging level WARN
        OidcLoginLoggerLogger.setLevel(INFO);

        // create and start a ListAppender
        final ListAppender<ILoggingEvent> loggingEventAppender = new ListAppender<>();
        loggingEventAppender.start();

        // add the appender to the logger
        OidcLoginLoggerLogger.addAppender(loggingEventAppender);

        return loggingEventAppender;
    }
}
