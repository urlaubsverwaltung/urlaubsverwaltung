package de.focusshift.launchpad.core;

import java.util.Locale;
import java.util.Map;

public class AppName {

    private final String defaultName;
    private final Map<Locale, String> names;

    AppName(String defaultName, Map<Locale, String> names) {
        this.defaultName = defaultName;
        this.names = names;
    }

    public String get(Locale locale) {
        return names.getOrDefault(locale, defaultName);
    }
}
