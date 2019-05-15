package org.synyx.urlaubsverwaltung.mail.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;


/**
 * Configuration to send mails.
 */
@Configuration
public class MailConfig {

    @Bean(name = "javaMailSender")
    public JavaMailSenderImpl javaMailSender() {

        return new JavaMailSenderImpl();
    }

}
