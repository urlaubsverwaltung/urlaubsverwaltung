package org.synyx.urlaubsverwaltung.core.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;

import javax.mail.internet.MimeMessage;


/**
 * Mock mail sender without any functionality for development.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class MockMailSender implements JavaMailSender {

    private String host;
    private int port;
    private String username;
    private String password;

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


    public String getUsername() {

        return username;
    }


    public void setUsername(String username) {

        this.username = username;
    }


    public String getPassword() {

        return password;
    }


    public void setPassword(String password) {

        this.password = password;
    }


    @Override
    public MimeMessage createMimeMessage() {

        return null;
    }


    @Override
    public MimeMessage createMimeMessage(InputStream inputStream) {

        return null;
    }


    @Override
    public void send(MimeMessage mimeMessage) {

        /* OK */
    }


    @Override
    public void send(MimeMessage[] mimeMessages) {

        /* OK */
    }


    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) {

        /* OK */
    }


    @Override
    public void send(MimeMessagePreparator[] mimeMessagePreparators) {

        /* OK */
    }


    @Override
    public void send(SimpleMailMessage simpleMailMessage) {

        /* OK */
    }


    @Override
    public void send(SimpleMailMessage[] simpleMailMessages) {

        /* OK */
    }
}
