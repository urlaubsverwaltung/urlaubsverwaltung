package org.synyx.urlaubsverwaltung.user;

import java.util.Locale;

public class UserSettingsDto {

    private String theme;

    private Locale locale;

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
