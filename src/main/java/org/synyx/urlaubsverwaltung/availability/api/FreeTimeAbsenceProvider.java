package org.synyx.urlaubsverwaltung.availability.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.availability.api.TimedAbsence.Type.FREETIME;


@Service
class FreeTimeAbsenceProvider extends AbstractTimedAbsenceProvider {

    private final WorkingTimeService workingTimeService;

    @Autowired
    FreeTimeAbsenceProvider(PublicHolidayAbsenceProvider nextPriorityProvider, WorkingTimeService workingTimeService) {

        super(nextPriorityProvider);

        this.workingTimeService = workingTimeService;
    }

    @Override
    TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, Instant date) {

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


    private Optional<TimedAbsence> checkForFreeTime(Instant currentDay, Person person) {

        DayLength expectedWorkTime = getExpectedWorkTimeFor(person, currentDay);
        BigDecimal expectedWorkTimeDuration = expectedWorkTime.getDuration();

        boolean expectedWorkTimeIsLessThanFullDay = expectedWorkTimeDuration.compareTo(BigDecimal.ONE) < 0;

        if (expectedWorkTimeIsLessThanFullDay) {
            return Optional.of(new TimedAbsence(expectedWorkTime.getInverse(), FREETIME));
        }

        return Optional.empty();
    }


    private DayLength getExpectedWorkTimeFor(Person person, Instant currentDay) {

        Optional<WorkingTime> workingTimeOrNot = workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(person,
            currentDay);

        if (workingTimeOrNot.isEmpty()) {
            throw new FreeTimeAbsenceException("Person " + person + " does not have workingTime configured");
        }

        WorkingTime workingTime = workingTimeOrNot.get();

        return workingTime.getDayLengthForWeekDay(currentDay.get(ChronoField.DAY_OF_WEEK));
    }
}
