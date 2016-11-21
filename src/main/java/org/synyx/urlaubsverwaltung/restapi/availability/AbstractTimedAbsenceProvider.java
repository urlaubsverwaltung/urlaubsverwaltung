package org.synyx.urlaubsverwaltung.restapi.availability;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;

import java.util.ArrayList;


/**
 * This class is used to build a chain of responsibility (https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern)
 * which defines in which order absences are to be checked. This ensures, that multiple overlapping absences for a
 * certain date do not sum up to more than a full day. Priorities are: free time > holidays > sick > vacation
 *
 * @author  Timo Eifler - eifler@synyx.de
 */
abstract class AbstractTimedAbsenceProvider {

    protected final AbstractTimedAbsenceProvider nextPriorityAbsenceProvider;

    public AbstractTimedAbsenceProvider(AbstractTimedAbsenceProvider nextPriorityAbsenceProvider) {

        this.nextPriorityAbsenceProvider = nextPriorityAbsenceProvider;
    }

    /**
     * Convenience function for initial call, so that the caller does not have to create an empty list himself.
     */
    public TimedAbsenceSpans checkForAbsence(Person person, DateMidnight date) {

        return checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()), person, date);
    }


    /**
     * Checks for absences for the given person on the given day. Recursively calls the next priority provider if the
     * absence spans do not yet sum up to a full day.
     */
    public TimedAbsenceSpans checkForAbsence(TimedAbsenceSpans knownAbsences, Person person, DateMidnight date) {

        TimedAbsenceSpans updatedAbsences = addAbsence(knownAbsences, person, date);

        if (isPersonAbsentForWholeDay(updatedAbsences) || isLastPriorityProvider()) {
            return updatedAbsences;
        } else {
            return nextPriorityAbsenceProvider.checkForAbsence(updatedAbsences, person, date);
        }
    }


    /**
     * Each provider implements his own logic to retrieve absences via this method.
     */
    abstract TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, DateMidnight date);


    /**
     * This method is used to check if the provider has a follow up provider to call.
     *
     * @return  {@code true} if no follow up provider exists.
     */
    abstract boolean isLastPriorityProvider();


    private boolean isPersonAbsentForWholeDay(TimedAbsenceSpans timedAbsenceSpans) {

        return BigDecimal.ZERO.compareTo(timedAbsenceSpans.calculatePresenceRatio()) == 0;
    }
}
