package org.synyx.urlaubsverwaltung.restapi;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Absences", description = "Get all absences for a certain period")
@Controller("restApiAbsenceController")
public class AbsenceController {

    private enum AbsenceType {

        VACATION,
        SICK_NOTE
    }

    @Autowired
    private PersonService personService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private SickNoteService sickNoteService;

    @ApiOperation(
        value = "Get all absences for a certain period and person",
        notes = "Get all absences for a certain period and person"
    )
    @RequestMapping(value = "/absences", method = RequestMethod.GET)
    @ResponseBody
    @ModelAttribute("response")
    public AbsenceList personsVacations(
        @ApiParam(value = "Year to get the absences for", defaultValue = "2015")
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
                    return new AbsenceList(Collections.emptyList());
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
                    List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(
                                periodStart, periodEnd, person)
                        .stream()
                        .filter(application ->
                                    application.hasStatus(ApplicationStatus.WAITING)
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

                if (type == null || type.equals(AbsenceType.SICK_NOTE.name())) {
                    List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, periodStart, periodEnd)
                        .stream()
                        .filter(sickNote ->
                                sickNote.isActive())
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

                return new AbsenceList(absences);
            } catch (NumberFormatException ex) {
                return new AbsenceList(Collections.emptyList());
            }
        }

        return new AbsenceList(Collections.emptyList());
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
}
