package org.synyx.urlaubsverwaltung.settings;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * Mail relevant settings.
 */
@Embeddable
public class MailSettings {

    @Column(name = "mail_active")
    private boolean active = false;

    @Column(name = "mail_host")
    private String host = "localhost";

    @Column(name = "mail_port")
    private Integer port = 25; // NOSONAR

    @Column(name = "mail_username")
    private String username;

    @Column(name = "mail_password")
    private String password;

    @Column(name = "mail_from")
    private String from = "absender@urlaubsverwaltung.test";

    @Column(name = "mail_administrator")
    private String administrator = "admin@urlaubsverwaltung.test";

    /**
     * Is used as base URL for links within sent mails.
     *
     * @since 2.16.0
     */
    @Column(name = "mail_base_link_url")
    private String baseLinkURL = "http://localhost:8080/";

    public boolean isActive() {

        return active;
    }


    public void setActive(boolean active) {

        this.active = active;
    }


    public String getHost() {

        return host;
    }


    public void setHost(String host) {

        this.host = host;
    }


    public Integer getPort() {

        return port;
    }


    public void setPort(Integer port) {

        this.port = port;
    }


    public String getUsername() {

        return username;
    }


    public void setUsername(String username) {

        this.username = username;
    }


    public String getPassword() {

        return password;
    }


    public void setPassword(String password) {

        this.password = password;
    }


    public String getFrom() {

        return from;
    }


    public void setFrom(String from) {

        this.from = from;
    }


    public String getAdministrator() {

        return administrator;
    }


    public void setAdministrator(String administrator) {

        this.administrator = administrator;
    }


    public String getBaseLinkURL() {

        return baseLinkURL;
    }


    public void setBaseLinkURL(String baseLinkURL) {

        this.baseLinkURL = baseLinkURL;
    }
}
