package org.synyx.urlaubsverwaltung.mail.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.mail.MailOptionProvider;
import org.synyx.urlaubsverwaltung.mail.MailSender;
import org.synyx.urlaubsverwaltung.mail.SpringBootConfiguredMailOptionProvider;
import org.synyx.urlaubsverwaltung.mail.SpringBootConfiguredMailSender;

@ConditionalOnProperty(prefix = "spring.mail", name = "host")
@Configuration
public class SpringBootConfiguredMailConfig {

    private UrlaubsverwaltungMailConfigurationProperties urlaubsverwaltungMailConfigurationProperties;
    private MailProperties mailProperties;

    @Autowired
    public SpringBootConfiguredMailConfig(UrlaubsverwaltungMailConfigurationProperties urlaubsverwaltungMailConfigurationProperties, MailProperties mailProperties) {
        this.urlaubsverwaltungMailConfigurationProperties = urlaubsverwaltungMailConfigurationProperties;
        this.mailProperties = mailProperties;
    }

    @Bean
    public MailOptionProvider springBootConfiguredMailOptionProvider() {
        return new SpringBootConfiguredMailOptionProvider(urlaubsverwaltungMailConfigurationProperties, mailProperties);
    }

    @Bean
    public MailSender springBootConfiguredMailSender(JavaMailSender javaMailSender) {
        return new SpringBootConfiguredMailSender(javaMailSender);
    }

    @ConfigurationProperties(prefix = "uv.mail")
    @Component
    public static class UrlaubsverwaltungMailConfigurationProperties {

        private String sender;
        private String administrator;
        private String applicationurl;

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getAdministrator() {
            return administrator;
        }

        public void setAdministrator(String administrator) {
            this.administrator = administrator;
        }

        public String getApplicationurl() {
            return applicationurl;
        }

        public void setApplicationurl(String applicationurl) {
            this.applicationurl = applicationurl;
        }
    }
}
