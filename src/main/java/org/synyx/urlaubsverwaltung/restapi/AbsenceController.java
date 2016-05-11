package org.synyx.urlaubsverwaltung.restapi;

import com.google.common.collect.ImmutableMap;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

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
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.math.BigDecimal;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Absences", description = "Get all absences for a certain period")
@RestController("restApiAbsenceController")
@RequestMapping("/api")
public class AbsenceController {

    private enum AbsenceType {

        VACATION,
        SICK_NOTE
    }

    @Autowired
    private PersonService personService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private SickNoteService sickNoteService;

    @Autowired
    private PublicHolidayController publicHolidayController;

    @ApiOperation(
        value = "Get all absences for a certain period and person",
        notes = "Get all absences for a certain period and person"
    )
    @RequestMapping(value = "/absences", method = RequestMethod.GET, params="person")
    public ResponseWrapper<AbsenceList> personsVacations(
        @ApiParam(value = "Year to get the absences for")
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

        boolean hasYear = StringUtils.hasText(year);
        boolean hasMonth = StringUtils.hasText(month);

        if (hasYear && personId != null) {
            try {
                Optional<Person> personOptional = personService.getPersonByID(personId);

                if (!personOptional.isPresent()) {
                    return new ResponseWrapper<>(new AbsenceList(Collections.emptyList()));
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

                Person person = personOptional.get();
                List<Absence> absences = new ArrayList<>();

                if (type == null || type.equals(AbsenceType.VACATION.name())) {
                    addVacation(absences, periodStart, periodEnd, person);
                }

                if (type == null || type.equals(AbsenceType.SICK_NOTE.name())) {
                    addSickNotes(absences, periodStart, periodEnd, person);
                }

                return new ResponseWrapper<>(new AbsenceList(absences));
            } catch (NumberFormatException ex) {
                return new ResponseWrapper<>(new AbsenceList(Collections.emptyList()));
            }
        }

        return new ResponseWrapper<>(new AbsenceList(Collections.emptyList()));
    }

    private void addVacation(List<Absence> absences, DateMidnight periodStart, DateMidnight periodEnd, Person person) {

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(
                periodStart, periodEnd, person)
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
                absences.add(new Absence(day, application.getDayLength(), AbsenceType.VACATION,
                        application.getStatus().name(), application.getId()));

                day = day.plusDays(1);
            }
        }
    }
    private void addSickNotes(List<Absence> absences, DateMidnight periodStart, DateMidnight periodEnd, Person person) {

        List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, periodStart, periodEnd)
                .stream()
                .filter(SickNote::isActive)
                .collect(Collectors.toList());

        for (SickNote sickNote : sickNotes) {
            DateMidnight startDate = sickNote.getStartDate();
            DateMidnight endDate = sickNote.getEndDate();

            DateMidnight day = startDate;

            while (!day.isAfter(endDate)) {
                absences.add(new Absence(day, sickNote.getDayLength(), AbsenceType.SICK_NOTE, "ACTIVE",
                        sickNote.getId()));

                day = day.plusDays(1);
            }
        }
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

                Map<Integer, List<Absence>> absenceMap = new HashMap<>();
                Map<Integer, String> calendars = new HashMap<>();
                for (Person person: persons) {

                    List<Absence> absences = new ArrayList<>();

                    addVacation(absences, periodStart, periodEnd, person);
                    addSickNotes(absences, periodStart, periodEnd, person);
                    absenceMap.put(person.getId(), absences);
                    // everyone shares the same "system" holiday calendar for now
                    calendars.put(person.getId(), "system");
                }



                return new ResponseWrapper<>(new DepartmentAbsences(
                        ImmutableMap.of("system", publicHolidayController.getPublicHolidays(year, month).getResponse().getPublicHolidays()),
                        absenceMap, calendars));
            } catch (NumberFormatException ex) {
                return new ResponseWrapper<>(new DepartmentAbsences(null, null, null));
            }
        }

        return  new ResponseWrapper<>(new DepartmentAbsences(null, null, null));
    }

    private class Absence {

        private final String date;
        private final BigDecimal dayLength;
        private final String type;
        private final String status;
        private final String href;

        public Absence(DateMidnight date, DayLength dayLength, AbsenceType type, String status, Integer id) {

            this.date = date.toString(RestApiDateFormat.PATTERN);
            this.dayLength = dayLength.getDuration();
            this.type = type.name();
            this.status = status;
            this.href = id.toString();
        }

        public String getDate() {

            return date;
        }


        public BigDecimal getDayLength() {

            return dayLength;
        }


        public String getType() {

            return type;
        }


        public String getStatus() {

            return status;
        }


        public String getHref() {

            return href;
        }
    }

    private class AbsenceList {

        private final List<Absence> absences;

        public AbsenceList(List<Absence> absences) {

            this.absences = absences;
        }

        public List<Absence> getAbsences() {

            return absences;
        }
    }

    private class DepartmentAbsences {

        private final Map<String, List<PublicHolidayResponse>> publicHolidays;

        /** map personId => absences */
        private final Map<Integer, List<Absence>> personAbsences;

        /** map personId => key into publicHolidays map */
        private final Map<Integer, String> personPublicHolidays;

        public DepartmentAbsences(Map<String, List<PublicHolidayResponse>> publicHolidays, Map<Integer, List<Absence>> personAbsences, Map<Integer, String> personPublicHolidays) {

            this.publicHolidays = publicHolidays;
            this.personAbsences = personAbsences;
            this.personPublicHolidays = personPublicHolidays;
        }

        public Map<String, List<PublicHolidayResponse>> getPublicHolidays() {

            return publicHolidays;
        }

        public Map<Integer, List<Absence>> getPersonAbsences() {

            return personAbsences;
        }

        public Map<Integer, String> getPersonPublicHolidays() {

            return personPublicHolidays;
        }
    }
}
