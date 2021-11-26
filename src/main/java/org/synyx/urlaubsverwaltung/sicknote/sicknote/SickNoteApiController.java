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
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@RestControllerAdviceMarker
@Tag(name = "sick notes", description = "Sick Notes: Get all sick notes for a certain period")
@RestController
@RequestMapping("/api")
public class SickNoteApiController {

    public static final String SICKNOTES = "sicknotes";

    private final PersonService personService;
    private final SickNoteService sickNoteService;

    @Autowired
    SickNoteApiController(SickNoteService sickNoteService, PersonService personService) {
        this.personService = personService;
        this.sickNoteService = sickNoteService;
    }

    @Operation(
        summary = "Get all sick notes for a certain period",
        description = "Get all sick notes for a certain period. "
            + "Information only reachable for users with role office."
    )
    @GetMapping(SICKNOTES)
    @PreAuthorize(IS_OFFICE)
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

        final List<SickNoteDto> sickNoteResponse = sickNoteService.getByPeriod(startDate, endDate).stream()
            .filter(SickNote::isActive)
            .map(SickNoteDto::new)
            .collect(toList());

        return new SickNotesDto(sickNoteResponse);
    }

    @Operation(
        summary = "Get all sick notes for a certain period and person",
        description = "Get all sick notes for a certain period and person. "
            + "Information only reachable for users with role office and for own sicknotes."
    )
    @GetMapping("/persons/{personId}/" + SICKNOTES)
    @PreAuthorize(IS_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public SickNotesDto personsSickNotes(
        @Parameter(description = "ID of the person")
        @PathVariable("personId")
            Integer personId,
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
        final List<SickNoteDto> sickNoteResponse = sickNoteService.getByPersonAndPeriod(person, startDate, endDate).stream()
            .filter(SickNote::isActive)
            .map(SickNoteDto::new)
            .collect(toList());

        return new SickNotesDto(sickNoteResponse);
    }
}
