package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

@RestControllerAdviceMarker
@Tag(name = "sick notes", description = "Sick Notes: Get all sick notes for a certain period")
@RestController
@RequestMapping("/api")
public class SickNoteApiController {

    public static final String SICKNOTES = "sicknotes";

    private final PersonService personService;
    private final SickNoteService sickNoteService;
    private final DepartmentService departmentService;

    @Autowired
    SickNoteApiController(SickNoteService sickNoteService, PersonService personService, DepartmentService departmentService) {
        this.personService = personService;
        this.sickNoteService = sickNoteService;
        this.departmentService = departmentService;
    }

    @Operation(
        summary = "Get all sick notes for a certain period",
        description = "Get all sick notes for a certain period. "
            + "Information only reachable for users with role office or for users with the 'SICK_NOTE_VIEW' role."
    )
    @GetMapping(path = SICKNOTES, produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_VIEW')")
    public SickNotesDto getSickNotes(
        @Parameter(description = "Start date with pattern yyyy-MM-dd")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate startDate,
        @Parameter(description = "End date with pattern yyyy-MM-dd")
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }

        final Person signedInUser = personService.getSignedInUser();
        final List<Person> managedPersons = getMembersOfPersons(signedInUser);

        final List<SickNoteDto> sickNoteResponse = sickNoteService.getForStatesAndPerson(List.of(ACTIVE), managedPersons, startDate, endDate).stream()
            .map(SickNoteDto::new)
            .toList();

        return new SickNotesDto(sickNoteResponse);
    }

    @Operation(
        summary = "Get all sick notes for a certain period and person",
        description = "Get all sick notes for a certain period and person. "
            + "Information only reachable for users with role office and for own sick notes."
    )
    @GetMapping(path = "/persons/{personId}/" + SICKNOTES, produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_VIEW') or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public SickNotesDto personsSickNotes(
        @Parameter(description = "ID of the person")
        @PathVariable("personId")
        Long personId,
        @Parameter(description = "Start date with pattern yyyy-MM-dd")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate startDate,
        @Parameter(description = "End date with pattern yyyy-MM-dd")
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }

        final Optional<Person> optionalPerson = personService.getPersonByID(personId);
        if (optionalPerson.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for ID=" + personId);
        }

        final Person person = optionalPerson.get();
        final Person signedInUser = personService.getSignedInUser();
        if (!isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_VIEW, person)) {
            throw new ResponseStatusException(FORBIDDEN, "Not allowed to access data of the person with the ID=" + personId);
        }

        final List<SickNoteDto> sickNoteResponse = sickNoteService.getForStatesAndPerson(List.of(ACTIVE), List.of(person), startDate, endDate).stream()
            .map(SickNoteDto::new)
            .toList();

        return new SickNotesDto(sickNoteResponse);
    }

    private boolean isPersonAllowedToExecuteRoleOn(Person requestPerson, Role role, Person person) {

        if (requestPerson.equals(person) || requestPerson.hasRole(OFFICE)) {
            return true;
        }

        if (requestPerson.hasRole(role)) {
            return requestPerson.hasRole(BOSS)
                || departmentService.isDepartmentHeadAllowedToManagePerson(requestPerson, person)
                || departmentService.isSecondStageAuthorityAllowedToManagePerson(requestPerson, person);
        }

        return false;
    }

    private List<Person> getMembersOfPersons(Person signedInUser) {

        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            return personService.getActivePersons();
        }

        final List<Person> membersForDepartmentHead = signedInUser.hasRole(DEPARTMENT_HEAD)
            ? departmentService.getMembersForDepartmentHead(signedInUser)
            : List.of();

        final List<Person> memberForSecondStageAuthority = signedInUser.hasRole(SECOND_STAGE_AUTHORITY)
            ? departmentService.getMembersForSecondStageAuthority(signedInUser)
            : List.of();

        return Stream.concat(memberForSecondStageAuthority.stream(), membersForDepartmentHead.stream())
            .filter(person -> !person.hasRole(INACTIVE))
            .distinct()
            .sorted(comparing(Person::getFirstName).thenComparing(Person::getLastName))
            .toList();
    }
}
