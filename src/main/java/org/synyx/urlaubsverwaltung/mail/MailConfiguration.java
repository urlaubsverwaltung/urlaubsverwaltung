package org.synyx.urlaubsverwaltung.mail;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfiguration {

    @Bean
    public MailSender springBootConfiguredMailSender(JavaMailSender javaMailSender) {
        return new CustomMailSender(javaMailSender);
    }
}
