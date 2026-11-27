package org.synyx.urlaubsverwaltung.mail;

import io.micrometer.jakarta9.instrument.mail.InstrumentedTransport;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;

import static io.micrometer.jakarta9.instrument.mail.MailObservationDocumentation.LowCardinalityKeyNames.NETWORK_PROTOCOL_NAME;
import static io.micrometer.jakarta9.instrument.mail.MailObservationDocumentation.LowCardinalityKeyNames.SERVER_ADDRESS;
import static io.micrometer.jakarta9.instrument.mail.MailObservationDocumentation.LowCardinalityKeyNames.SERVER_PORT;

/**
 * {@link InstrumentedTransport} that additionally observes {@code connect} as {@code mail.connect}.
 *
 * <p>{@link InstrumentedTransport} only observes {@code sendMessage} ({@code mail.send}). Failures while
 * connecting (authentication, connection refused, timeouts, TLS) happen before {@code sendMessage} is reached
 * and would otherwise be invisible.
 *
 * <p>Uses the same low cardinality key names as {@code mail.send}. Credentials and recipients are never recorded.
 */
class ObservedTransport extends InstrumentedTransport {

    static final String MAIL_CONNECT_OBSERVATION_NAME = "mail.connect";

    private static final String UNKNOWN = "unknown";

    private final Transport delegate;
    private final ObservationRegistry observationRegistry;

    ObservedTransport(Session session, Transport delegate, ObservationRegistry observationRegistry) {
        super(session, delegate, observationRegistry);
        this.delegate = delegate;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public synchronized void connect(String host, int port, String user, String password) throws MessagingException {
        Observation.createNotStarted(MAIL_CONNECT_OBSERVATION_NAME, observationRegistry)
            .contextualName("mail connect")
            .lowCardinalityKeyValue(SERVER_ADDRESS.asString(), host == null || host.isEmpty() ? UNKNOWN : host)
            .lowCardinalityKeyValue(SERVER_PORT.asString(), port > 0 ? String.valueOf(port) : UNKNOWN)
            .lowCardinalityKeyValue(NETWORK_PROTOCOL_NAME.asString(), protocol())
            .observeChecked(() -> super.connect(host, port, user, password));
    }

    /**
     * Delegates to the wrapped transport.
     *
     * <p>{@link InstrumentedTransport} only delegates {@code connect}, so its own connection state is never set and
     * {@code isConnected()} would always return {@code false}. {@code JavaMailSenderImpl} checks
     * {@code isConnected()} before every message and would close and reconnect for each message of a batch.
     */
    @Override
    public synchronized boolean isConnected() {
        return delegate.isConnected();
    }

    private String protocol() {
        final String protocol = url == null ? null : url.getProtocol();
        return protocol == null || protocol.isEmpty() ? UNKNOWN : protocol;
    }
}
