package org.synyx.urlaubsverwaltung.availability.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

@Deprecated(forRemoval = true, since = "4.4.0")
@Service
class PublicHolidayAbsenceProvider extends AbstractTimedAbsenceProvider {

    private final PublicHolidaysService publicHolidaysService;
    private final WorkingTimeService workingTimeService;

    @Autowired
    PublicHolidayAbsenceProvider(SickDayAbsenceProvider nextPriorityProvider, PublicHolidaysService publicHolidaysService,
                                 WorkingTimeService workingTimeService) {

        super(nextPriorityProvider);

        this.publicHolidaysService = publicHolidaysService;
        this.workingTimeService = workingTimeService;
    }

    @Override
    TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, LocalDate date) {

        Optional<TimedAbsence> holidayAbsence = checkForHolidays(date, person);

        if (holidayAbsence.isPresent()) {
            List<TimedAbsence> knownAbsencesList = knownAbsences.getAbsencesList();
            knownAbsencesList.add(holidayAbsence.get());

            return new TimedAbsenceSpans(knownAbsencesList);
        }

        return knownAbsences;
    }

    @Override
    boolean isLastPriorityProvider() {

        return false;
    }

    private Optional<TimedAbsence> checkForHolidays(LocalDate currentDay, Person person) {

        final FederalState federalState = getFederalState(currentDay, person);
        final Optional<PublicHoliday> maybePublicHoliday = publicHolidaysService.getPublicHoliday(currentDay, federalState);

        Optional<TimedAbsence> maybeAbsence = Optional.empty();

        if (maybePublicHoliday.isPresent()) {

            final BigDecimal workingDuration = maybePublicHoliday.get().getWorkingDuration();
            boolean fullDayPublicHoliday = workingDuration.compareTo(ZERO.getDuration()) == 0;
            boolean halfDayPublicHoliday = workingDuration.compareTo(NOON.getDuration()) == 0;

            if (fullDayPublicHoliday) {
                maybeAbsence = Optional.of(new TimedAbsence(FULL));
            } else if (halfDayPublicHoliday) {
                maybeAbsence = Optional.of(new TimedAbsence(NOON));
            }
        }

        return maybeAbsence;
    }

    private FederalState getFederalState(LocalDate date, Person person) {
        return workingTimeService.getFederalStateForPerson(person, date);
    }
}
