package org.synyx.urlaubsverwaltung.user;

import java.util.Locale;

class UserSettings {

    private final Theme theme;
    private final Locale locale;

    UserSettings(Theme theme, Locale locale) {
        this.theme = theme;
        this.locale = locale;
    }

    Theme theme() {
        return theme;
    }

    Locale locale() {
        return locale;
    }
}
