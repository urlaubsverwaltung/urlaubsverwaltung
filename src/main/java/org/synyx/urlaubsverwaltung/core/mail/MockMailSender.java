package org.synyx.urlaubsverwaltung.core.mail;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;

/**
 * Mock mail sender without any functionality for development.
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
public class MockMailSender implements JavaMailSender {

    private String host;
    private int port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public MimeMessage createMimeMessage() {
        return null;
    }

    @Override
    public MimeMessage createMimeMessage(InputStream inputStream) throws MailException {
        return null;
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        /* OK */
    }

    @Override
    public void send(MimeMessage[] mimeMessages) throws MailException {
        /* OK */
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
       /* OK */
    }

    @Override
    public void send(MimeMessagePreparator[] mimeMessagePreparators) throws MailException {
       /* OK */
    }

    @Override
    public void send(SimpleMailMessage simpleMailMessage) throws MailException {
        /* OK */
    }

    @Override
    public void send(SimpleMailMessage[] simpleMailMessages) throws MailException {
       /* OK */
    }
}
