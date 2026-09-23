package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.joining;

/**
 * Human readable explanation of a blackout period for a blocked day, e.g. as tooltip in calendars.
 */
public final class BlackoutPeriodDescriptions {

    private static final String SEPARATOR = " · ";

    private BlackoutPeriodDescriptions() {
        // ok
    }

    public static String describe(BlackoutPeriod blackoutPeriod, MessageSource messageSource, Locale locale) {

        if (blackoutPeriod.appliesToAllVacationTypes() || blackoutPeriod.getVacationTypes().isEmpty()) {
            return messageSource.getMessage("blackoutperiod.day.description", new Object[]{blackoutPeriod.getTitle()}, locale);
        }

        final String vacationTypes = blackoutPeriod.getVacationTypes().stream()
            .map(vacationType -> vacationType.getLabel(locale))
            .collect(joining(", "));

        return messageSource.getMessage("blackoutperiod.day.description.restricted",
            new Object[]{blackoutPeriod.getTitle(), vacationTypes}, locale);
    }

    public static String describeAll(List<BlackoutPeriod> blackoutPeriods, MessageSource messageSource, Locale locale) {
        return blackoutPeriods.stream()
            .map(blackoutPeriod -> describe(blackoutPeriod, messageSource, locale))
            .collect(joining(SEPARATOR));
    }
}
