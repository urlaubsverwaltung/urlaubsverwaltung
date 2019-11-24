package org.synyx.urlaubsverwaltung.mail;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.synyx.urlaubsverwaltung.mail.config.UrlaubsverwaltungMailConfigurationProperties;

public class SpringBootConfiguredMailOptionProvider implements MailOptionProvider {

    private final UrlaubsverwaltungMailConfigurationProperties urlaubsverwaltungMailConfigurationProperties;
    private final MailProperties mailProperties;

    public SpringBootConfiguredMailOptionProvider(UrlaubsverwaltungMailConfigurationProperties urlaubsverwaltungMailConfigurationProperties,
                                                  MailProperties mailProperties) {
        this.urlaubsverwaltungMailConfigurationProperties = urlaubsverwaltungMailConfigurationProperties;
        this.mailProperties = mailProperties;
    }

    @Override
    public String getSender() {
        return urlaubsverwaltungMailConfigurationProperties.getSender();
    }

    @Override
    public String getAdministrator() {
        return urlaubsverwaltungMailConfigurationProperties.getAdministrator();
    }

    @Override
    public String getApplicationUrl() {
        return formatApplicationUrl(urlaubsverwaltungMailConfigurationProperties.getApplicationUrl());
    }

    @Override
    public Integer getMailServerPort() {
        return mailProperties.getPort();
    }

    @Override
    public String getMailServerHost() {
        return mailProperties.getHost();
    }

}
