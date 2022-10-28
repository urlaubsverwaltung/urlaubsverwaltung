package org.synyx.urlaubsverwaltung.availability.api;

import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.ArrayList;

import static java.math.BigDecimal.ZERO;

/**
 * This class is used to build a chain of responsibility (https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern)
 * which defines in which order absences are to be checked. This ensures, that multiple overlapping absences for a
 * certain date do not sum up to more than a full day. Priorities are: free time > public holidays > sick > vacation
 */
@Deprecated(forRemoval = true, since = "4.4.0")
abstract class AbstractTimedAbsenceProvider {

    private final AbstractTimedAbsenceProvider nextPriorityAbsenceProvider;

    protected AbstractTimedAbsenceProvider(AbstractTimedAbsenceProvider nextPriorityAbsenceProvider) {
        this.nextPriorityAbsenceProvider = nextPriorityAbsenceProvider;
    }

    /**
     * Convenience function for initial call, so that the caller does not have to create an empty list himself.
     */
    TimedAbsenceSpans checkForAbsence(Person person, LocalDate date) {
        return checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()), person, date);
    }

    /**
     * Checks for absences for the given person on the given day. Recursively calls the next priority provider if the
     * absence spans do not yet sum up to a full day.
     */
    TimedAbsenceSpans checkForAbsence(TimedAbsenceSpans knownAbsences, Person person, LocalDate date) {

        final TimedAbsenceSpans updatedAbsences = addAbsence(knownAbsences, person, date);

        if (isPersonAbsentForWholeDay(updatedAbsences) || isLastPriorityProvider()) {
            return updatedAbsences;
        } else {
            return nextPriorityAbsenceProvider.checkForAbsence(updatedAbsences, person, date);
        }
    }

    /**
     * Each provider implements his own logic to retrieve absences via this method.
     */
    abstract TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, LocalDate date);

    /**
     * This method is used to check if the provider has a follow up provider to call.
     *
     * @return {@code true} if no follow up provider exists.
     */
    abstract boolean isLastPriorityProvider();

    private boolean isPersonAbsentForWholeDay(TimedAbsenceSpans timedAbsenceSpans) {
        return ZERO.compareTo(timedAbsenceSpans.calculatePresenceRatio()) == 0;
    }
}
