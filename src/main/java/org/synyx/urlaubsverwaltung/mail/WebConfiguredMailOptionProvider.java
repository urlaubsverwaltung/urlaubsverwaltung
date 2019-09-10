package org.synyx.urlaubsverwaltung.mail;

import org.synyx.urlaubsverwaltung.settings.SettingsService;

public class WebConfiguredMailOptionProvider implements MailOptionProvider {

    private final SettingsService settingsService;

    public WebConfiguredMailOptionProvider(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public String getSender() {
        return settingsService.getSettings().getMailSettings().getFrom();
    }

    @Override
    public String getAdministrator() {
        return settingsService.getSettings().getMailSettings().getAdministrator();
    }

    @Override
    public String getApplicationUrl() {
        return formatApplicationUrl(settingsService.getSettings().getMailSettings().getBaseLinkURL());
    }

    @Override
    public Integer getMailServerPort() {
        return settingsService.getSettings().getMailSettings().getPort();
    }

    @Override
    public String getMailServerHost() {
        return settingsService.getSettings().getMailSettings().getHost();
    }
}
