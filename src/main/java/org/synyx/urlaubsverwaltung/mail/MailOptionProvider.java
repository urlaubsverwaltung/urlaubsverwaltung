package org.synyx.urlaubsverwaltung.mail;


public interface MailOptionProvider {

    String getSender();
    String getAdministrator();
    String getApplicationUrl();
    Integer getMailServerPort();
    String getMailServerHost();

    default String formatApplicationUrl(String applicationurl) {
        return applicationurl.endsWith("/")? applicationurl : applicationurl + "/";
    }
}
