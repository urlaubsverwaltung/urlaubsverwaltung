package org.synyx.urlaubsverwaltung.mail.config;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.synyx.urlaubsverwaltung.mail.MailOptionProvider;
import org.synyx.urlaubsverwaltung.mail.MailSender;
import org.synyx.urlaubsverwaltung.mail.SpringBootConfiguredMailSender;
import org.synyx.urlaubsverwaltung.mail.WebConfiguredMailOptionProvider;
import org.synyx.urlaubsverwaltung.mail.WebConfiguredMailSender;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

@ConditionalOnMissingBean(SpringBootConfiguredMailSender.class)
@Configuration
public class WebConfiguredMailConfig {
    @Bean
    public JavaMailSenderImpl javaMailSender() {
        return new JavaMailSenderImpl();
    }

    @Bean
    public MailSender webConfiguredMailSender(SettingsService settingsService) {
        return new WebConfiguredMailSender(javaMailSender(), settingsService);
    }

    @Bean
    public MailOptionProvider webconfiguredMailOptionProvider(SettingsService settingsService) {
        return new WebConfiguredMailOptionProvider(settingsService);
    }
}
