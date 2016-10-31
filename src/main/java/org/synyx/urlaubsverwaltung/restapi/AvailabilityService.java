package org.synyx.urlaubsverwaltung.restapi;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author  Marc Kannegiesser - kannegiesser@synyx.de
 */
@Service
public class AvailabilityService {

    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;
    private final PublicHolidaysService publicHolidaysService;
    private final WorkingTimeService workingTimeService;

    @Autowired
    public AvailabilityService(ApplicationService applicationService, SickNoteService sickNoteService,
        PublicHolidaysService publicHolidaysService, WorkingTimeService workingTimeService) {

        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
        this.publicHolidaysService = publicHolidaysService;
        this.workingTimeService = workingTimeService;
    }

    /**
     * Fetch an {@link AvailabilityList} for the given person on all days in the given period of time.
     */
    public AvailabilityList getPersonsAvailabilities(DateMidnight startDate, DateMidnight endDate, Person person) {

        Map<DateMidnight, Application> vacations = getVacations(startDate, endDate, person);
        Map<DateMidnight, SickNote> sickNotes = getSickNotes(startDate, endDate, person);

        List<DayAvailability> availabilities = new ArrayList<>();

        DateMidnight currentDay = startDate;

        while (!currentDay.isAfter(endDate)) {
            availabilities.add(getAvailabilityfor(currentDay, person, vacations, sickNotes));

            currentDay = currentDay.plusDays(1);
        }

        return new AvailabilityList(availabilities, person);
    }


    private Map<DateMidnight, Application> getVacations(DateMidnight start, DateMidnight end, Person person) {

        Map<DateMidnight, Application> vacationsMap = new HashMap<>();

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(start, end,
                    person)
                .stream()
                .filter(application ->
                            application.hasStatus(ApplicationStatus.WAITING)
                            || application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)
                            || application.hasStatus(ApplicationStatus.ALLOWED))
                .collect(Collectors.toList());

        for (Application application : applications) {
            DateMidnight startDate = application.getStartDate();
            DateMidnight endDate = application.getEndDate();

            DateMidnight day = startDate;

            while (!day.isAfter(endDate)) {
                if (!day.isBefore(start) && !day.isAfter(end)) {
                    vacationsMap.put(day, application);
                }

                day = day.plusDays(1);
            }
        }

        return vacationsMap;
    }


    private Map<DateMidnight, SickNote> getSickNotes(DateMidnight start, DateMidnight end, Person person) {

        Map<DateMidnight, SickNote> sickNotesMap = new HashMap<>();

        List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, start, end)
                .stream()
                .filter(SickNote::isActive)
                .collect(Collectors.toList());

        for (SickNote sickNote : sickNotes) {
            DateMidnight startDate = sickNote.getStartDate();
            DateMidnight endDate = sickNote.getEndDate();

            DateMidnight day = startDate;

            while (!day.isAfter(endDate)) {
                if (!day.isBefore(start) && !day.isAfter(end)) {
                    sickNotesMap.put(day, sickNote);
                }

                day = day.plusDays(1);
            }
        }

        return sickNotesMap;
    }


    private DayAvailability getAvailabilityfor(DateMidnight currentDay, Person person,
        Map<DateMidnight, Application> vacations, Map<DateMidnight, SickNote> sickNotes) {

        List<DayAvailability.TimedAbsence> absenceSpans = new ArrayList<>();
        Optional<DayAvailability.TimedAbsence> freeTimeAbsence = checkForFreeTime(currentDay, person);

        if (freeTimeAbsence.isPresent()) {
            absenceSpans.add(freeTimeAbsence.get());
        }

        Optional<DayAvailability.TimedAbsence> holidayAbsence = checkForHolidays(currentDay, person);

        if (holidayAbsence.isPresent()) {
            absenceSpans.add(holidayAbsence.get());
        }

        Optional<DayAvailability.TimedAbsence> sickNoteAbsence = checkForSickNote(currentDay, person, sickNotes);

        if (sickNoteAbsence.isPresent()) {
            absenceSpans.add(sickNoteAbsence.get());
        }

        Optional<DayAvailability.TimedAbsence> vacationAbsence = checkForVacations(currentDay, person, vacations);

        if (vacationAbsence.isPresent()) {
            absenceSpans.add(vacationAbsence.get());
        }

        BigDecimal presenceRatio = calculatePresenceRatio(absenceSpans);

        return new DayAvailability(presenceRatio, currentDay.toString("yyyy-MM-dd"), absenceSpans);
    }


    private Optional<DayAvailability.TimedAbsence> checkForFreeTime(DateMidnight currentDay, Person person) {

        DayLength expectedWorktime = getExpectedWorktimeFor(person, currentDay);
        BigDecimal expectedWorktimeDuration = expectedWorktime.getDuration();

        boolean expectedWorktimeIsLessThanFullDay = expectedWorktimeDuration.compareTo(BigDecimal.ONE) == -1;

        if (expectedWorktimeIsLessThanFullDay) {
            return Optional.of(new DayAvailability.TimedAbsence(expectedWorktime.getInverse(),
                        DayAvailability.TimedAbsence.Type.FREETIME));
        }

        return Optional.empty();
    }


    private Optional<DayAvailability.TimedAbsence> checkForHolidays(DateMidnight currentDay, Person person) {

        BigDecimal expectedWorkingDuration = publicHolidaysService.getWorkingDurationOfDate(currentDay,
                getFederalState(currentDay, person));

        boolean fullDayHoliday = expectedWorkingDuration.compareTo(DayLength.ZERO.getDuration()) == 0;
        boolean halfDayHoliday = expectedWorkingDuration.compareTo(DayLength.NOON.getDuration()) == 0;

        DayAvailability.TimedAbsence absence = null;

        if (fullDayHoliday) {
            absence = new DayAvailability.TimedAbsence(DayLength.FULL, DayAvailability.TimedAbsence.Type.HOLIDAY);
        } else if (halfDayHoliday) {
            absence = new DayAvailability.TimedAbsence(DayLength.NOON, DayAvailability.TimedAbsence.Type.HOLIDAY);
        }

        return Optional.ofNullable(absence);
    }


    private Optional<DayAvailability.TimedAbsence> checkForVacations(DateMidnight currentDay, Person person,
        Map<DateMidnight, Application> vacations) {

        Application vacation = vacations.get(currentDay);

        if (vacation != null) {
            return Optional.of(new DayAvailability.TimedAbsence(vacation.getDayLength(),
                        DayAvailability.TimedAbsence.Type.VACATION));
        }

        return Optional.empty();
    }


    private Optional<DayAvailability.TimedAbsence> checkForSickNote(DateMidnight currentDay, Person person,
        Map<DateMidnight, SickNote> sickNotes) {

        SickNote sickNote = sickNotes.get(currentDay);

        if (sickNote != null) {
            return Optional.of(new DayAvailability.TimedAbsence(sickNote.getDayLength(),
                        DayAvailability.TimedAbsence.Type.SICK_NOTE));
        }

        return Optional.empty();
    }


    private BigDecimal calculatePresenceRatio(List<DayAvailability.TimedAbsence> absenceSpans) {

        BigDecimal absenceRatio = BigDecimal.ZERO;

        for (DayAvailability.TimedAbsence absenceSpan : absenceSpans) {
            absenceRatio = absenceRatio.add(absenceSpan.getRatio());
        }

        BigDecimal presenceRatio = BigDecimal.ONE.subtract(absenceRatio);

        boolean negativePresenceRatio = presenceRatio.compareTo(BigDecimal.ZERO) < 0;

        if (negativePresenceRatio) {
            presenceRatio = BigDecimal.ZERO;
        }

        return presenceRatio;
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


    private FederalState getFederalState(DateMidnight date, Person person) {

        return workingTimeService.getFederalStateForPerson(person, date);
    }
}
