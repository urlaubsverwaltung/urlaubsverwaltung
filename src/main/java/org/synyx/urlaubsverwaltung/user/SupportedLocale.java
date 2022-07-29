package org.synyx.urlaubsverwaltung.user;

import java.util.Locale;

enum SupportedLocale {

    GERMAN(Locale.GERMAN),
    GERMAN_AUSTRIA(Locale.forLanguageTag("de-AT")),
    ENGLISH(Locale.ENGLISH),
    GREEK(Locale.forLanguageTag("el"));

    private final Locale locale;

    SupportedLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }
}
