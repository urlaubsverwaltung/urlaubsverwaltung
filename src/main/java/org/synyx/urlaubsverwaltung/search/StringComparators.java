package org.synyx.urlaubsverwaltung.search;

import org.springframework.context.i18n.LocaleContextHolder;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Locale-aware {@link Comparator}s for strings.
 *
 * <p>
 * In contrast to the natural ordering of {@link String}, these comparators are based on a {@link Collator} so that
 * strings with umlauts, accents or apostrophes are sorted next to their base letters
 * (e.g. "Joél" is sorted between "Joe" and "Jof", "Özil" next to "Ozil").
 */
public final class StringComparators {

    private StringComparators() {
        // ok
    }

    /**
     * Locale-aware and case-insensitive string comparator based on the current user locale.
     *
     * <p>
     * Note that the returned comparator is not thread-safe. Create a new one per usage.
     *
     * @return locale-aware string comparator
     */
    public static Comparator<String> localeAwareComparator() {
        return localeAwareComparator(LocaleContextHolder.getLocale());
    }

    /**
     * Locale-aware and case-insensitive string comparator for the given locale.
     *
     * <p>
     * Note that the returned comparator is not thread-safe. Create a new one per usage.
     *
     * @param locale locale used for collation
     * @return locale-aware string comparator
     */
    public static Comparator<String> localeAwareComparator(Locale locale) {
        return collator(locale)::compare;
    }

    static Collator collator(Locale locale) {
        final Collator collator = Collator.getInstance(locale);
        // SECONDARY: umlauts and accents are significant, case is not (matches previous toLowerCase based sorting)
        collator.setStrength(Collator.SECONDARY);
        return collator;
    }
}
