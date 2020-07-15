package org.synyx.urlaubsverwaltung.availability.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.availability.api.TimedAbsence.Type.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;


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
    TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, Instant date) {

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

    private Optional<TimedAbsence> checkForHolidays(Instant currentDay, Person person) {

        BigDecimal expectedWorkingDuration = publicHolidaysService.getWorkingDurationOfDate(currentDay,
            getFederalState(currentDay, person));

        boolean fullDayPublicHoliday = expectedWorkingDuration.compareTo(ZERO.getDuration()) == 0;
        boolean halfDayPublicHoliday = expectedWorkingDuration.compareTo(NOON.getDuration()) == 0;

        TimedAbsence absence = null;

        if (fullDayPublicHoliday) {
            absence = new TimedAbsence(FULL, PUBLIC_HOLIDAY);
        } else if (halfDayPublicHoliday) {
            absence = new TimedAbsence(NOON, PUBLIC_HOLIDAY);
        }

        return Optional.ofNullable(absence);
    }

    private FederalState getFederalState(Instant date, Person person) {
        return workingTimeService.getFederalStateForPerson(person, date);
    }
}
