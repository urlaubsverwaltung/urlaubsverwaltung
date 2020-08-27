package org.synyx.urlaubsverwaltung.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Validated
@ConfigurationProperties(prefix = "uv.mail")
public class MailProperties {

    @Email
    @NotEmpty
    private String sender;

    @Email
    @NotEmpty
    private String administrator;

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
}
