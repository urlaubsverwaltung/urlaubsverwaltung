package org.synyx.urlaubsverwaltung.mail;

import io.micrometer.observation.tck.TestObservationRegistry;
import jakarta.mail.Address;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.URLName;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static io.micrometer.observation.tck.TestObservationRegistryAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObservedTransportTest {

    private ObservedTransport sut;

    private TestObservationRegistry observationRegistry;

    @Mock
    private Transport delegate;

    @BeforeEach
    void setUp() {
        observationRegistry = TestObservationRegistry.create();
        when(delegate.getURLName()).thenReturn(new URLName("smtp", null, -1, null, null, null));
        sut = new ObservedTransport(Session.getInstance(new Properties()), delegate, observationRegistry);
    }

    @Test
    void observesConnect() throws Exception {
        sut.connect("smtp.example.org", 587, "user", "secret");

        verify(delegate).connect("smtp.example.org", 587, "user", "secret");
        assertThat(observationRegistry)
            .hasNumberOfObservationsEqualTo(1)
            .hasSingleObservationThat()
            .hasNameEqualTo("mail.connect")
            .hasContextualNameEqualTo("mail connect")
            .hasLowCardinalityKeyValue("server.address", "smtp.example.org")
            .hasLowCardinalityKeyValue("server.port", "587")
            .hasLowCardinalityKeyValue("network.protocol.name", "smtp")
            .doesNotHaveError()
            .hasBeenStarted()
            .hasBeenStopped();
    }

    @Test
    void observesConnectWithUnknownServer() throws Exception {
        sut.connect(null, -1, null, null);

        assertThat(observationRegistry)
            .hasSingleObservationThat()
            .hasLowCardinalityKeyValue("server.address", "unknown")
            .hasLowCardinalityKeyValue("server.port", "unknown");
    }

    @Test
    void observesFailedConnectAndRethrows() throws Exception {
        final AuthenticationFailedException authenticationFailed = new AuthenticationFailedException("535 bad credentials");
        doThrow(authenticationFailed).when(delegate).connect("smtp.example.org", 587, "user", "wrong");

        assertThatThrownBy(() -> sut.connect("smtp.example.org", 587, "user", "wrong"))
            .isSameAs(authenticationFailed);

        assertThat(observationRegistry)
            .hasSingleObservationThat()
            .hasBeenStopped()
            .assertThatError()
            .isSameAs(authenticationFailed);
    }

    @Test
    void observesSend() throws Exception {
        final MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        final Address[] recipients = {new InternetAddress("person@example.org")};

        sut.sendMessage(message, recipients);

        verify(delegate).sendMessage(message, recipients);
        assertThat(observationRegistry)
            .hasSingleObservationThat()
            .hasNameEqualTo("mail.send")
            .doesNotHaveError();
    }

    @Test
    void delegatesConnectionState() {
        when(delegate.isConnected()).thenReturn(true, false);

        assertThat(sut.isConnected()).isTrue();
        assertThat(sut.isConnected()).isFalse();
    }
}
