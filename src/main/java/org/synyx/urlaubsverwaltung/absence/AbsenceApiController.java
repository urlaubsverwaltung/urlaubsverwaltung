package org.synyx.urlaubsverwaltung.absence;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.synyx.urlaubsverwaltung.absence.DayAbsence.Type.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.absence.DayAbsence.Type.VACATION;
import static org.synyx.urlaubsverwaltung.api.SwaggerConfig.EXAMPLE_YEAR;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getFirstDayOfMonth;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getFirstDayOfYear;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getLastDayOfMonth;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getLastDayOfYear;

@RestControllerAdviceMarker
@Api("Absences: Get all absences for a certain period")
@RestController("restApiAbsenceController")
@RequestMapping("/api")
public class AbsenceApiController {

    private final PersonService personService;
    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;

    @Autowired
    public AbsenceApiController(PersonService personService, ApplicationService applicationService, SickNoteService sickNoteService) {
        this.personService = personService;
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
    }

    @ApiOperation(
        value = "Get all absences for a certain period and person",
        notes = "Get all absences for a certain period and person"
    )
    @GetMapping("/absences")
    @PreAuthorize(IS_BOSS_OR_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)")
    public List<DayAbsence> personsVacations(
        @ApiParam(value = "Year to get the absences for", defaultValue = EXAMPLE_YEAR)
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

        final Optional<Person> optionalPerson = personService.getPersonByID(personId);
        if (optionalPerson.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for ID=" + personId);
        }

        final List<DayAbsence> absences = new ArrayList<>();
        final Person person = optionalPerson.get();

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = getStartDate(year, Optional.ofNullable(month));
            endDate = getEndDate(year, Optional.ofNullable(month));
        } catch (NumberFormatException | DateTimeException exception) {
            throw new ResponseStatusException(BAD_REQUEST, exception.getMessage());
        }

        try {
            if (type == null || DayAbsence.Type.valueOf(type).equals(VACATION)) {
                absences.addAll(getVacations(startDate, endDate, person));
            }
            if (type == null || DayAbsence.Type.valueOf(type).equals(SICK_NOTE)) {
                absences.addAll(getSickNotes(startDate, endDate, person));
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
        }

        return absences;
    }

    private static LocalDate getStartDate(String year, Optional<String> optionalMonth) {
        return optionalMonth.map(s -> getFirstDayOfMonth(parseInt(year), parseInt(s)))
            .orElseGet(() -> getFirstDayOfYear(parseInt(year)));
    }

    private static LocalDate getEndDate(String year, Optional<String> optionalMonth) {
        return optionalMonth.map(s -> getLastDayOfMonth(parseInt(year), parseInt(s)))
            .orElseGet(() -> getLastDayOfYear(parseInt(year)));
    }

    private List<DayAbsence> getVacations(LocalDate start, LocalDate end, Person person) {

        List<DayAbsence> absences = new ArrayList<>();

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(start, end,
            person)
            .stream()
            .filter(application ->
                application.hasStatus(WAITING)
                    || application.hasStatus(TEMPORARY_ALLOWED)
                    || application.hasStatus(ALLOWED))
            .collect(toList());

        for (Application application : applications) {
            LocalDate startDate = application.getStartDate();
            LocalDate endDate = application.getEndDate();

            LocalDate day = startDate;

            while (!day.isAfter(endDate)) {
                if (!day.isBefore(start) && !day.isAfter(end)) {
                    absences.add(new DayAbsence(day, application.getDayLength().getDuration(), application.getDayLength().toString(), VACATION,
                        application.getStatus().name(), application.getId()));
                }

                day = day.plusDays(1);
            }
        }

        return absences;
    }

    private List<DayAbsence> getSickNotes(LocalDate start, LocalDate end, Person person) {

        final List<DayAbsence> absences = new ArrayList<>();

        final List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, start, end)
            .stream()
            .filter(SickNote::isActive)
            .collect(toList());

        for (SickNote sickNote : sickNotes) {
            final LocalDate startDate = sickNote.getStartDate();
            final LocalDate endDate = sickNote.getEndDate();

            LocalDate day = startDate;

            while (!day.isAfter(endDate)) {
                if (!day.isBefore(start) && !day.isAfter(end)) {
                    absences.add(new DayAbsence(day, sickNote.getDayLength().getDuration(),
                        sickNote.getDayLength().toString(), SICK_NOTE, "ACTIVE",
                        sickNote.getId()));
                }

                day = day.plusDays(1);
            }
        }

        return absences;
    }
}
