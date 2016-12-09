package org.synyx.urlaubsverwaltung.restapi.availability;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;


/**
 * @author  Timo Eifler - eifler@synyx.de
 */
@Service
class FreeTimeAbsenceProvider extends AbstractTimedAbsenceProvider {

    private final WorkingTimeService workingTimeService;

    @Autowired
    FreeTimeAbsenceProvider(HolidayAbsenceProvider nextPriorityProvider, WorkingTimeService workingTimeService) {

        super(nextPriorityProvider);

        this.workingTimeService = workingTimeService;
    }

    @Override
    TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, DateMidnight date) {

        Optional<TimedAbsence> freeTimeAbsence = checkForFreeTime(date, person);

        if (freeTimeAbsence.isPresent()) {
            List<TimedAbsence> knownAbsencesList = knownAbsences.getAbsencesList();
            knownAbsencesList.add(freeTimeAbsence.get());

            return new TimedAbsenceSpans(knownAbsencesList);
        }

        return knownAbsences;
    }


    @Override
    boolean isLastPriorityProvider() {

        return false;
    }


    private Optional<TimedAbsence> checkForFreeTime(DateMidnight currentDay, Person person) {

        DayLength expectedWorktime = getExpectedWorktimeFor(person, currentDay);
        BigDecimal expectedWorktimeDuration = expectedWorktime.getDuration();

        boolean expectedWorktimeIsLessThanFullDay = expectedWorktimeDuration.compareTo(BigDecimal.ONE) < 0;

        if (expectedWorktimeIsLessThanFullDay) {
            return Optional.of(new TimedAbsence(expectedWorktime.getInverse(), TimedAbsence.Type.FREETIME));
        }

        return Optional.empty();
    }


    private DayLength getExpectedWorktimeFor(Person person, DateMidnight currentDay) {

        Optional<WorkingTime> workingTimeOrNot = workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(person,
                currentDay);

        if (!workingTimeOrNot.isPresent()) {
            throw new IllegalStateException("Person " + person + " does not have workingTime configured");
        }

        WorkingTime workingTime = workingTimeOrNot.get();

        DayLength dayLengthForWeekDay = workingTime.getDayLengthForWeekDay(currentDay.getDayOfWeek());

        return dayLengthForWeekDay;
    }
}
