package org.synyx.urlaubsverwaltung.mail.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.synyx.urlaubsverwaltung.mail.MailOptionProvider;
import org.synyx.urlaubsverwaltung.mail.MailSender;
import org.synyx.urlaubsverwaltung.mail.SpringBootConfiguredMailOptionProvider;
import org.synyx.urlaubsverwaltung.mail.SpringBootConfiguredMailSender;

@ConditionalOnProperty(prefix = "spring.mail", name = "host")
@Configuration
@EnableConfigurationProperties(UrlaubsverwaltungMailConfigurationProperties.class)
public class SpringBootConfiguredMailConfig {

    @Bean
    public MailOptionProvider springBootConfiguredMailOptionProvider(UrlaubsverwaltungMailConfigurationProperties urlaubsverwaltungMailConfigurationProperties, MailProperties mailProperties) {
        return new SpringBootConfiguredMailOptionProvider(urlaubsverwaltungMailConfigurationProperties, mailProperties);
    }

    @Bean
    public MailSender springBootConfiguredMailSender(JavaMailSender javaMailSender) {
        return new SpringBootConfiguredMailSender(javaMailSender);
    }
}
