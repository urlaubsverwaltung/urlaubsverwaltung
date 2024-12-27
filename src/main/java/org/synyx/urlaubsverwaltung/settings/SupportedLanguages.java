package org.synyx.urlaubsverwaltung.settings;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import static java.util.Locale.forLanguageTag;

public enum SupportedLanguages {

    // order is important as it represents the order in UI for instance

    GERMAN(Locale.GERMAN),
    GERMAN_AUSTRIA(forLanguageTag("de-AT")),
    ENGLISH(Locale.ENGLISH),
    GREEK(forLanguageTag("el"));

    private final Locale locale;

    SupportedLanguages(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the SupportedLanguage with the specified locale.
     *
     * @return the SupportedLanguage with the specified locale
     * @throws IllegalArgumentException if there is no SupportedLanguage with the specified locale
     */
    public static Optional<SupportedLanguages> valueOfLocale(Locale locale) {
        return Arrays.stream(SupportedLanguages.values())
            .filter(supportedLanguages -> supportedLanguages.locale.equals(locale))
            .findFirst();
    }

    public static int compareSupportedLanguageLocale(Locale l1, Locale l2) {
        // locale of not supported language will be sorted last
        final int length = SupportedLanguages.values().length;
        final Integer i = valueOfLocale(l1).map(Enum::ordinal).orElse(length);
        final Integer i1 = valueOfLocale(l2).map(Enum::ordinal).orElse(length);
        return i.compareTo(i1);
    }
}
