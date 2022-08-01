package org.synyx.urlaubsverwaltung.user;

import java.util.Locale;

public class UserSettingsDto {

    private String selectedTheme;

    private Locale locale;

    public String getSelectedTheme() {
        return selectedTheme;
    }

    public UserSettingsDto setSelectedTheme(String selectedTheme) {
        this.selectedTheme = selectedTheme;
        return this;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
