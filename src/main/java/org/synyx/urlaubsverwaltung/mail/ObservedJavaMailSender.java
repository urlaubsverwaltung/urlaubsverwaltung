package org.synyx.urlaubsverwaltung.mail;

import io.micrometer.observation.ObservationRegistry;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * {@link JavaMailSenderImpl} whose transports are observed as {@code mail.connect} and {@code mail.send},
 * see {@link ObservedTransport}.
 */
class ObservedJavaMailSender extends JavaMailSenderImpl {

    private final ObservationRegistry observationRegistry;

    private ObservedJavaMailSender(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    /**
     * Creates an observed copy of the given sender with the same session and connection settings,
     * so the configuration by Spring Boot ({@code spring.mail.*}) is kept as is.
     */
    static ObservedJavaMailSender copyOf(JavaMailSenderImpl original, ObservationRegistry observationRegistry) {
        final ObservedJavaMailSender copy = new ObservedJavaMailSender(observationRegistry);
        // setJavaMailProperties resets the session, so it has to be set before the session
        copy.setJavaMailProperties(original.getJavaMailProperties());
        copy.setSession(original.getSession());
        copy.setProtocol(original.getProtocol());
        copy.setHost(original.getHost());
        copy.setPort(original.getPort());
        copy.setUsername(original.getUsername());
        copy.setPassword(original.getPassword());
        copy.setDefaultEncoding(original.getDefaultEncoding());
        copy.setDefaultFileTypeMap(original.getDefaultFileTypeMap());
        return copy;
    }

    @Override
    protected Transport getTransport(Session session) throws NoSuchProviderException {
        return new ObservedTransport(session, super.getTransport(session), observationRegistry);
    }
}
