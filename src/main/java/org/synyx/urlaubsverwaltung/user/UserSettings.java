package org.synyx.urlaubsverwaltung.user;

import java.util.Locale;
import java.util.Optional;

public class UserSettings {

    private final Theme theme;
    private final Locale locale;
    private final Locale localeBrowserSpecific;

    UserSettings(Theme theme) {
        this(theme, null, null);
    }

    public UserSettings(Theme theme, Locale locale, Locale localeBrowserSpecific) {
        this.theme = theme;
        this.locale = locale;
        this.localeBrowserSpecific = localeBrowserSpecific;
    }

    public Theme theme() {
        return theme;
    }

    public Optional<Locale> locale() {
        return Optional.ofNullable(locale);
    }

    public Optional<Locale> localeBrowserSpecific() {
        return Optional.ofNullable(localeBrowserSpecific);
    }
}
