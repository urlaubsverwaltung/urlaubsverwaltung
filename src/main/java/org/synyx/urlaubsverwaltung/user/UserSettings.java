package org.synyx.urlaubsverwaltung.user;

import java.util.Locale;
import java.util.Optional;

class UserSettings {

    private final Theme theme;
    private final Locale locale;

    UserSettings(Theme theme) {
        this(theme, null);
    }

    UserSettings(Theme theme, Locale locale) {
        this.theme = theme;
        this.locale = locale;
    }

    Theme theme() {
        return theme;
    }

    Optional<Locale> locale() {
        return Optional.ofNullable(locale);
    }
}
