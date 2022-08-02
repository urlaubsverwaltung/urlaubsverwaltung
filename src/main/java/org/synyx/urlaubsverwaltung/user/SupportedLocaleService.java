package org.synyx.urlaubsverwaltung.user;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

@Service
class SupportedLocaleService {
    Set<Locale> getSupportedLocales() {
        return Arrays.stream(SupportedLocale.values()).map(SupportedLocale::getLocale).collect(toUnmodifiableSet());
    }
}
