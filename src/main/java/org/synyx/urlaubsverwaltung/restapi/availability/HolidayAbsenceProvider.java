package org.synyx.urlaubsverwaltung.restapi.availability;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;


/**
 * @author  Timo Eifler - eifler@synyx.de
 */
@Service
class HolidayAbsenceProvider extends AbstractTimedAbsenceProvider {

    private final PublicHolidaysService publicHolidaysService;
    private WorkingTimeService workingTimeService;

    @Autowired
    HolidayAbsenceProvider(SickDayAbsenceProvider nextPriorityProvider, PublicHolidaysService publicHolidaysService,
        WorkingTimeService workingTimeService) {

        super(nextPriorityProvider);

        this.publicHolidaysService = publicHolidaysService;
        this.workingTimeService = workingTimeService;
    }

    @Override
    TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, DateMidnight date) {

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


    private Optional<TimedAbsence> checkForHolidays(DateMidnight currentDay, Person person) {

        BigDecimal expectedWorkingDuration = publicHolidaysService.getWorkingDurationOfDate(currentDay,
                getFederalState(currentDay, person));

        boolean fullDayHoliday = expectedWorkingDuration.compareTo(DayLength.ZERO.getDuration()) == 0;
        boolean halfDayHoliday = expectedWorkingDuration.compareTo(DayLength.NOON.getDuration()) == 0;

        TimedAbsence absence = null;

        if (fullDayHoliday) {
            absence = new TimedAbsence(DayLength.FULL, TimedAbsence.Type.HOLIDAY);
        } else if (halfDayHoliday) {
            absence = new TimedAbsence(DayLength.NOON, TimedAbsence.Type.HOLIDAY);
        }

        return Optional.ofNullable(absence);
    }


    private FederalState getFederalState(DateMidnight date, Person person) {

        return workingTimeService.getFederalStateForPerson(person, date);
    }
}
