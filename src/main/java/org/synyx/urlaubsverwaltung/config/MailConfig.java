package org.synyx.urlaubsverwaltung.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;


/**
 * Configuration to send mails.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Configuration
public class MailConfig {

    @Bean(name = "javaMailSender")
    public JavaMailSenderImpl javaMailSender() {

        return new JavaMailSenderImpl();
    }

}
