package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(prefix = "spring.mail", name = "host")
@Component
public class SpringBootConfiguredMailOptionProvider implements MailOptionProvider {

    private final String sender;
    private final String administrator;
    private final String applicationUrl;
    private final MailProperties mailProperties;

    @Autowired
    public SpringBootConfiguredMailOptionProvider(@Value("${uv.mail.from}") String sender,
                                                  @Value("${uv.mail.administrator}") String administrator,
                                                  @Value("${uv.mail.applicationurl}") String applicationUrl,
                                                  MailProperties mailProperties) {
        this.sender = sender;
        this.administrator = administrator;
        this.applicationUrl = applicationUrl;
        this.mailProperties = mailProperties;
    }

    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public String getAdministrator() {
        return administrator;
    }

    @Override
    public String getApplicationUrl() {
        return formatApplicationUrl(applicationUrl);
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
