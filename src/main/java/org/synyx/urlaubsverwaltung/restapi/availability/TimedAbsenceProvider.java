package org.synyx.urlaubsverwaltung.restapi.availability;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;


/**
 * This class is used to build a chain of responsibility (https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern)
 * which defines in which order absences are to be checked.
 *
 * @author  Timo Eifler - eifler@synyx.de
 */
abstract class TimedAbsenceProvider {

    protected final TimedAbsenceProvider nextPriorityAbsenceProvider;

    public TimedAbsenceProvider(TimedAbsenceProvider nextPriorityAbsenceProvider) {

        this.nextPriorityAbsenceProvider = nextPriorityAbsenceProvider;
    }

    /**
     * Checks for absences for the given person on the given day. Recursively calls the next priority provider if the
     * absence spans do not yet sum up to a full day.
     */
    public TimedAbsenceSpans checkForAbsence(TimedAbsenceSpans knownAbsences, Person person, DateMidnight date) {

        TimedAbsenceSpans updatedAbsences = addAbsence(knownAbsences, person, date);

        if (personIsAbsentForWholeDay(updatedAbsences) || isLastPriorityProvider()) {
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


    private boolean personIsAbsentForWholeDay(TimedAbsenceSpans timedAbsenceSpans) {

        return BigDecimal.ZERO.compareTo(timedAbsenceSpans.calculatePresenceRatio()) == 0;
    }
}
