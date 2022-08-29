package org.synyx.urlaubsverwaltung.user;

import java.util.Locale;

/**
 * Event describing the user's preferred locale has changed. This could be the case when the user changed it in the settings,
 * or when the user has logged in and the locale has to be setup in the application (e.g. user session).
 */
public class UserLocaleChangedEvent {

    private final Locale locale;

    UserLocaleChangedEvent(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }
}
