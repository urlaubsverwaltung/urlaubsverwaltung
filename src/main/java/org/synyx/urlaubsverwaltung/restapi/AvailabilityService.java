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
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public AvailabilityList getPersonsAvailabilities(Integer personId, DateMidnight startDate, DateMidnight endDate,
        Person person) {

        Map<DateMidnight, Application> vacations = getVacations(startDate, endDate, person);
        Map<DateMidnight, SickNote> sickNotes = getSickNotes(startDate, endDate, person);

        List<DayAvailability> availabilities = new ArrayList<>();

        DateMidnight currentDay = startDate;

        while (!currentDay.isAfter(endDate)) {
            availabilities.add(getAvailabilityfor(currentDay, person, vacations, sickNotes));

            currentDay = currentDay.plusDays(1);
        }

        return new AvailabilityList(availabilities, personId);
    }


    private DayAvailability getAvailabilityfor(DateMidnight currentDay, Person person,
        Map<DateMidnight, Application> vacations, Map<DateMidnight, SickNote> sickNotes) {

        DayAvailability.TimedAbsence.Type freeType = DayAvailability.TimedAbsence.Type.FREETIME;

        BigDecimal expectedRatioToWork = publicHolidaysService.getWorkingDurationOfDate(currentDay,
                getFederalState(currentDay, person));

        if (BigDecimal.ZERO.equals(expectedRatioToWork)) {
            freeType = DayAvailability.TimedAbsence.Type.HOLIDAY;
        }

        Application vacation = vacations.get(currentDay);
        SickNote sick = sickNotes.get(currentDay);

        List<DayAvailability.TimedAbsence> spans = new ArrayList<>();

        BigDecimal ratioOfAbsence = BigDecimal.ZERO;

        if (vacation != null) {
            DayAvailability.TimedAbsence span = new DayAvailability.TimedAbsence(vacation.getDayLength(),
                    DayAvailability.TimedAbsence.Type.VACATION);
            spans.add(span);

            BigDecimal availabilityRatio = vacation.getDayLength().getDuration();
            ratioOfAbsence = ratioOfAbsence.add(availabilityRatio);
        }

        if (sick != null) {
            DayAvailability.TimedAbsence span = new DayAvailability.TimedAbsence(sick.getDayLength(),
                    DayAvailability.TimedAbsence.Type.SICK_NOTE);
            spans.add(span);

            BigDecimal availabilityRatio = sick.getDayLength().getDuration();
            ratioOfAbsence = ratioOfAbsence.add(availabilityRatio);
        }

        BigDecimal presenceRatio;

        if (expectedRatioToWork.compareTo(ratioOfAbsence) > 0) {
            presenceRatio = expectedRatioToWork.subtract(ratioOfAbsence);
        } else {
            presenceRatio = BigDecimal.ZERO;
        }

        // if there was no work expected it is either freetime or an holiday
        // FIXME this has to be done only when presenceRatio == 0
        if (spans.isEmpty()) {
            DayAvailability.TimedAbsence span = new DayAvailability.TimedAbsence(DayLength.FULL, freeType);
            spans.add(span);
        }

        return new DayAvailability(presenceRatio, currentDay.toString("yyyy-MM-dd"), spans);
    }


    private FederalState getFederalState(DateMidnight date, Person person) {

        return workingTimeService.getFederalStateForPerson(person, date);
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
}
