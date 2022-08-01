package org.synyx.urlaubsverwaltung.user;

import java.util.Locale;

public class LocaleDto {

    private final Locale locale;
    private final String displayName;

    private final boolean displayNameOverflow;

    LocaleDto(Locale locale, String displayName) {
        this(locale, displayName, false);
    }

    LocaleDto(Locale locale, String displayName, boolean displayNameOverflow) {
        this.locale = locale;
        this.displayName = displayName;
        this.displayNameOverflow = displayNameOverflow;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isDisplayNameOverflow() {
        return displayNameOverflow;
    }

    @Override
    public String toString() {
        return "LocaleDto{" +
            "locale=" + locale +
            ", displayName='" + displayName + '\'' +
            ", displayNameOverflow=" + displayNameOverflow +
            '}';
    }
}
