package org.synyx.urlaubsverwaltung.user;

import java.util.Locale;
import java.util.Optional;

public class UserSettings {

    public static final UserSettings DEFAULT = new UserSettings(Theme.SYSTEM, false);

    private final Theme theme;
    private final boolean navigationCollapsed;
    private final Locale locale;
    private final Locale localeBrowserSpecific;

    UserSettings(Theme theme, boolean navigationCollapsed) {
        this(theme, navigationCollapsed, null, null);
    }

    public UserSettings(Theme theme, boolean navigationCollapsed, Locale locale, Locale localeBrowserSpecific) {
        this.theme = theme;
        this.navigationCollapsed = navigationCollapsed;
        this.locale = locale;
        this.localeBrowserSpecific = localeBrowserSpecific;
    }

    public Theme theme() {
        return theme;
    }

    public boolean navigationCollapsed() {
        return navigationCollapsed;
    }

    public Optional<Locale> locale() {
        return Optional.ofNullable(locale);
    }

    public Optional<Locale> localeBrowserSpecific() {
        return Optional.ofNullable(localeBrowserSpecific);
    }
}
