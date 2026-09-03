package org.synyx.urlaubsverwaltung.user;

import java.util.Locale;

import static org.synyx.urlaubsverwaltung.user.Theme.SYSTEM;

public class UserSettingsDto {

    private Theme theme = SYSTEM;

    private Locale locale;

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme == null ? SYSTEM : theme;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
