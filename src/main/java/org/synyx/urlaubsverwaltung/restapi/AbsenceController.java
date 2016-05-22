package org.synyx.urlaubsverwaltung.restapi;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.jollyday.Holiday;
import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.calendar.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.util.*;

import java.util.stream.Collectors;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Absences", description = "Get all absences for a certain period")
@RestController("restApiAbsenceController")
@RequestMapping("/api")
public class AbsenceController {

    private final PersonService personService;
    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;
    private final DepartmentService departmentService;

    @Autowired
    AbsenceController(PersonService personService, ApplicationService applicationService,
        SickNoteService sickNoteService, DepartmentService departmentService) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
        this.departmentService = departmentService;
    }

    @Autowired
    private PublicHolidaysService publicHolidayService;

    @Autowired
    private WorkingTimeService workingTimeService;

    @ApiOperation(
        value = "Get all absences for a certain period and person",
        notes = "Get all absences for a certain period and person"
    )
    @RequestMapping(value = "/absences", method = RequestMethod.GET, params="person")
    public ResponseWrapper<DayAbsenceList> personsVacations(
        @ApiParam(value = "Year to get the absences for", defaultValue = "2016")
        @RequestParam("year")
        String year,
        @ApiParam(value = "Month of year to get the absences for")
        @RequestParam(value = "month", required = false)
        String month,
        @ApiParam(value = "ID of the person")
        @RequestParam("person")
        Integer personId,
        @ApiParam(value = "Type of absences, vacation or sick notes", allowableValues = "VACATION, SICK_NOTE")
        @RequestParam(value = "type", required = false)
        String type) {

        DateMidnight startDate = getStartDate(year, Optional.ofNullable(month));
        DateMidnight endDate = getEndDate(year, Optional.ofNullable(month));


        Optional<Person> optionalPerson = personService.getPersonByID(personId);

        if (!optionalPerson.isPresent()) {
            throw new IllegalArgumentException("No person found for ID=" + personId);
        }

        List<DayAbsence> absences = new ArrayList<>();
        Person person = optionalPerson.get();

        if (type == null || DayAbsence.Type.valueOf(type).equals(DayAbsence.Type.VACATION)) {
            absences.addAll(getVacations(startDate, endDate, person));
        }

        if (type == null || DayAbsence.Type.valueOf(type).equals(DayAbsence.Type.SICK_NOTE)) {
            absences.addAll(getSickNotes(startDate, endDate, person));
        }

        return new ResponseWrapper<>(new DayAbsenceList(absences));
    }



    /** This is a bit of facade method that combines the results of both /absences and /holidays
     * for all department members (to reduce the number of HTTP requests when showing the overview
     * calendar)
     */

    @ApiOperation(
            value = "Get all absences and public holidays for a certain period and department",
            notes = "Get all absences and public holidays for a certain period and department"
    )
    @RequestMapping(value = "/absences", method = RequestMethod.GET, params = "department")
    public ResponseWrapper<DepartmentAbsences> departmentVacations(
            @ApiParam(value = "Year to get the absences for")
            @RequestParam("year")
            String year,
            @ApiParam(value = "Month of year to get the absences for")
            @RequestParam(value = "month", required = false)
            String month,
            @ApiParam(value = "ID of the department")
            @RequestParam("department")
            Integer departmentId) {

        boolean hasYear = StringUtils.hasText(year);
        boolean hasMonth = StringUtils.hasText(month);

        if (hasYear && departmentId != null) {
            try {
                Optional<Department> deptOptional = departmentService.getDepartmentById(departmentId);

                if (!deptOptional.isPresent()) {
                    return new ResponseWrapper<>(new DepartmentAbsences(null, null, null));
                }

                DateMidnight periodStart;
                DateMidnight periodEnd;

                if (hasMonth) {
                    periodStart = DateUtil.getFirstDayOfMonth(Integer.parseInt(year), Integer.parseInt(month));
                    periodEnd = DateUtil.getLastDayOfMonth(Integer.parseInt(year), Integer.parseInt(month));
                } else {
                    periodStart = DateUtil.getFirstDayOfYear(Integer.parseInt(year));
                    periodEnd = DateUtil.getLastDayOfYear(Integer.parseInt(year));
                }

                List<Person> persons = deptOptional.map(Department::getMembers).orElse(Collections.emptyList());

                Map<Integer, List<DayAbsence>> absenceMap = new HashMap<>();
                Map<Integer, String> calendars = new HashMap<>();
                Map<String, List<PublicHolidayResponse>> catalog = new HashMap<>();

                for (Person person : persons) {

                    List<DayAbsence> absences = new ArrayList<>();
                    absences.addAll(getVacations(periodStart, periodEnd, person));
                    absences.addAll(getSickNotes(periodStart, periodEnd, person));
                    absenceMap.put(person.getId(), absences);
                    // A person's federal state might have changed during the period. We use the one
                    // as of periodEnd. Since this is used for display only and not vacation days calculation
                    // (and unlikely anyway) that should not be a problem.
                    FederalState state = workingTimeService.getFederalStateForPerson(person, periodEnd);
                    calendars.put(person.getId(), state.name());
                    if (!catalog.containsKey(state.name())) {
                        Collection<Holiday> holidays =
                                hasMonth ? publicHolidayService.getHolidays(periodStart.getYear(), periodStart.getMonthOfYear(), state)
                                        : publicHolidayService.getHolidays(periodStart.getYear(), state);

                        catalog.put(state.name(), holidays.stream().map(holiday ->
                                new PublicHolidayResponse(holiday,
                                        publicHolidayService.getWorkingDurationOfDate(holiday.getDate().toDateMidnight(),
                                                state))).collect(Collectors.toList()));

                    }
                }


                return new ResponseWrapper<>(new DepartmentAbsences(catalog, absenceMap, calendars));
            } catch (NumberFormatException ex) {
                return new ResponseWrapper<>(new DepartmentAbsences(null, null, null));
            }
        }

        return new ResponseWrapper<>(new DepartmentAbsences(null, null, null));
    }


    private DateMidnight getStartDate(String year, Optional<String> optionalMonth) throws NumberFormatException {

        if (optionalMonth.isPresent()) {
            return DateUtil.getFirstDayOfMonth(Integer.parseInt(year), Integer.parseInt(optionalMonth.get()));
        }

        return DateUtil.getFirstDayOfYear(Integer.parseInt(year));
    }


    private DateMidnight getEndDate(String year, Optional<String> optionalMonth) throws NumberFormatException {

        if (optionalMonth.isPresent()) {
            return DateUtil.getLastDayOfMonth(Integer.parseInt(year), Integer.parseInt(optionalMonth.get()));
        }

        return DateUtil.getLastDayOfYear(Integer.parseInt(year));
    }


    private List<DayAbsence> getVacations(DateMidnight start, DateMidnight end, Person person) {

        List<DayAbsence> absences = new ArrayList<>();

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
                    absences.add(new DayAbsence(day, application.getDayLength(), DayAbsence.Type.VACATION,
                            application.getStatus().name(), application.getId()));
                }

                day = day.plusDays(1);
            }
        }

        return absences;
    }


    private List<DayAbsence> getSickNotes(DateMidnight start, DateMidnight end, Person person) {

        List<DayAbsence> absences = new ArrayList<>();

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
                    absences.add(new DayAbsence(day, sickNote.getDayLength(), DayAbsence.Type.SICK_NOTE, "ACTIVE",
                            sickNote.getId()));
                }

                day = day.plusDays(1);
            }
        }

        return absences;
    }

    private class DepartmentAbsences {

        private final Map<String, List<PublicHolidayResponse>> publicHolidays;

        /** map personId => absences */
        private final Map<Integer, List<DayAbsence>> personAbsences;

        /** map personId => key into publicHolidays map */
        private final Map<Integer, String> personPublicHolidays;

        public DepartmentAbsences(Map<String, List<PublicHolidayResponse>> publicHolidays, Map<Integer, List<DayAbsence>> personAbsences, Map<Integer, String> personPublicHolidays) {

            this.publicHolidays = publicHolidays;
            this.personAbsences = personAbsences;
            this.personPublicHolidays = personPublicHolidays;
        }

        public Map<String, List<PublicHolidayResponse>> getPublicHolidays() {

            return publicHolidays;
        }

        public Map<Integer, List<DayAbsence>> getPersonAbsences() {

            return personAbsences;
        }

        public Map<Integer, String> getPersonPublicHolidays() {

            return personPublicHolidays;
        }
    }
}
