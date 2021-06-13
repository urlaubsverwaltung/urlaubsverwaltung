package org.synyx.urlaubsverwaltung.availability.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Deprecated(forRemoval = true, since = "4.4.0")
@Service
class FreeTimeAbsenceProvider extends AbstractTimedAbsenceProvider {

    private final WorkingTimeService workingTimeService;

    @Autowired
    FreeTimeAbsenceProvider(PublicHolidayAbsenceProvider nextPriorityProvider, WorkingTimeService workingTimeService) {

        super(nextPriorityProvider);

        this.workingTimeService = workingTimeService;
    }

    @Override
    TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, LocalDate date) {

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


    private Optional<TimedAbsence> checkForFreeTime(LocalDate currentDay, Person person) {

        DayLength expectedWorkTime = getExpectedWorkTimeFor(person, currentDay);
        BigDecimal expectedWorkTimeDuration = expectedWorkTime.getDuration();

        boolean expectedWorkTimeIsLessThanFullDay = expectedWorkTimeDuration.compareTo(BigDecimal.ONE) < 0;

        if (expectedWorkTimeIsLessThanFullDay) {
            return Optional.of(new TimedAbsence(expectedWorkTime.getInverse()));
        }

        return Optional.empty();
    }


    private DayLength getExpectedWorkTimeFor(Person person, LocalDate currentDay) {

        Optional<WorkingTime> workingTimeOrNot = workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(person,
            currentDay);

        if (workingTimeOrNot.isEmpty()) {
            throw new FreeTimeAbsenceException("Person " + person + " does not have workingTime configured");
        }

        WorkingTime workingTime = workingTimeOrNot.get();

        return workingTime.getDayLengthForWeekDay(currentDay.getDayOfWeek());
    }
}
