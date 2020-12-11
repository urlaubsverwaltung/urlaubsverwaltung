package org.synyx.urlaubsverwaltung.absence;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.synyx.urlaubsverwaltung.absence.DayAbsenceDto.Type.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.absence.DayAbsenceDto.Type.VACATION;
import static org.synyx.urlaubsverwaltung.api.SwaggerConfig.EXAMPLE_FIRST_DAY_OF_YEAR;
import static org.synyx.urlaubsverwaltung.api.SwaggerConfig.EXAMPLE_LAST_DAY_OF_MONTH;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@RestControllerAdviceMarker
@Api("Absences: Get all absences for a certain period")
@RestController
@RequestMapping("/api/persons/{personId}")
public class AbsenceApiController {

    public static final String ABSENCES = "absences";

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
    @GetMapping(ABSENCES)
    @PreAuthorize(IS_BOSS_OR_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)")
    public DayAbsencesDto personsAbsences(
        @ApiParam(value = "ID of the person")
        @PathVariable("personId")
            Integer personId,
        @ApiParam(value = "start of interval to get absences from (inclusive)", defaultValue = EXAMPLE_FIRST_DAY_OF_YEAR)
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,
        @ApiParam(value = "end of interval to get absences from (inclusive)", defaultValue = EXAMPLE_LAST_DAY_OF_MONTH)
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate,
        @ApiParam(value = "Type of absences, vacation or sick notes", allowableValues = "VACATION, SICK_NOTE")
        @RequestParam(value = "type", required = false)
            String type) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Start date " + startDate + " must not be after end date " + endDate);
        }

        final Optional<Person> optionalPerson = personService.getPersonByID(personId);
        if (optionalPerson.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for ID=" + personId);
        }

        final Person person = optionalPerson.get();

        final List<DayAbsenceDto> absences = new ArrayList<>();
        try {
            if (type == null || DayAbsenceDto.Type.valueOf(type).equals(VACATION)) {
                absences.addAll(getVacations(startDate, endDate, person));
            }
            if (type == null || DayAbsenceDto.Type.valueOf(type).equals(SICK_NOTE)) {
                absences.addAll(getSickNotes(startDate, endDate, person));
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
        }

        return new DayAbsencesDto(absences);
    }

    private List<DayAbsenceDto> getVacations(LocalDate start, LocalDate end, Person person) {

        List<DayAbsenceDto> absences = new ArrayList<>();

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(start, end, person).stream()
            .filter(application ->
                application.hasStatus(WAITING)
                    || application.hasStatus(TEMPORARY_ALLOWED)
                    || application.hasStatus(ALLOWED)
                    || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
            .collect(toList());

        for (Application application : applications) {
            LocalDate startDate = application.getStartDate();
            LocalDate endDate = application.getEndDate();

            LocalDate day = startDate;

            while (!day.isAfter(endDate)) {
                if (!day.isBefore(start) && !day.isAfter(end)) {
                    absences.add(new DayAbsenceDto(day, application.getDayLength().getDuration(), application.getDayLength().toString(), VACATION,
                        application.getStatus().name(), application.getId()));
                }

                day = day.plusDays(1);
            }
        }

        return absences;
    }

    private List<DayAbsenceDto> getSickNotes(LocalDate start, LocalDate end, Person person) {

        final List<DayAbsenceDto> absences = new ArrayList<>();

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
                    absences.add(new DayAbsenceDto(day, sickNote.getDayLength().getDuration(),
                        sickNote.getDayLength().toString(), SICK_NOTE, "ACTIVE",
                        sickNote.getId()));
                }

                day = day.plusDays(1);
            }
        }

        return absences;
    }
}
