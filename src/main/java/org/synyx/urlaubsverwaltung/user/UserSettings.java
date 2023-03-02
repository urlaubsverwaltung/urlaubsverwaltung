package org.synyx.urlaubsverwaltung.user;

import java.util.Locale;
import java.util.Optional;

class UserSettings {

    private final Theme theme;
    private final Locale locale;
    private final Locale localeBrowserSpecific;

    UserSettings(Theme theme) {
        this(theme, null, null);
    }

    UserSettings(Theme theme, Locale locale, Locale localeBrowserSpecific) {
        this.theme = theme;
        this.locale = locale;
        this.localeBrowserSpecific = localeBrowserSpecific;
    }

    Theme theme() {
        return theme;
    }

    Optional<Locale> locale() {
        return Optional.ofNullable(locale);
    }

    Optional<Locale> localeBrowserSpecific() {
        return Optional.ofNullable(localeBrowserSpecific);
    }
}
