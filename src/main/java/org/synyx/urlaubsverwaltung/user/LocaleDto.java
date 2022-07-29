package org.synyx.urlaubsverwaltung.user;

import java.util.Locale;

public class LocaleDto {

    private final Locale locale;
    private final String displayName;

    LocaleDto(Locale locale, String displayName) {
        this.locale = locale;
        this.displayName = displayName;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getDisplayName() {
        return displayName;
    }
}
