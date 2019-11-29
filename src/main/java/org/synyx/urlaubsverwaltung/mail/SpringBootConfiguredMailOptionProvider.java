package org.synyx.urlaubsverwaltung.mail;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.synyx.urlaubsverwaltung.mail.config.MailConfigurationProperties;

public class SpringBootConfiguredMailOptionProvider implements MailOptionProvider {

    private final MailConfigurationProperties mailConfigurationProperties;
    private final MailProperties mailProperties;

    public SpringBootConfiguredMailOptionProvider(MailConfigurationProperties mailConfigurationProperties,
                                                  MailProperties mailProperties) {
        this.mailConfigurationProperties = mailConfigurationProperties;
        this.mailProperties = mailProperties;
    }

    @Override
    public String getSender() {
        return mailConfigurationProperties.getSender();
    }

    @Override
    public String getAdministrator() {
        return mailConfigurationProperties.getAdministrator();
    }

    @Override
    public String getApplicationUrl() {
        return formatApplicationUrl(mailConfigurationProperties.getApplicationUrl());
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
