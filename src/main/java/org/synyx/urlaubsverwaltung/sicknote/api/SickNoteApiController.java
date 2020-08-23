package org.synyx.urlaubsverwaltung.sicknote.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.synyx.urlaubsverwaltung.api.SwaggerConfig.EXAMPLE_FIRST_DAY_OF_YEAR;
import static org.synyx.urlaubsverwaltung.api.SwaggerConfig.EXAMPLE_LAST_DAY_OF_YEAR;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@RestControllerAdviceMarker
@Api("Sick Notes: Get all sick notes for a certain period")
@RestController("restApiSickNoteController")
@RequestMapping("/api")
public class SickNoteApiController {

    private final SickNoteService sickNoteService;
    private final PersonService personService;

    @Autowired
    SickNoteApiController(SickNoteService sickNoteService, PersonService personService) {
        this.sickNoteService = sickNoteService;
        this.personService = personService;
    }

    @ApiOperation(
        value = "Get all sick notes for a certain period", notes = "Get all sick notes for a certain period. "
        + "If a person is specified, only the sick notes of this person are fetched. "
        + "Information only reachable for users with role office."
    )
    @GetMapping("/sicknotes")
    @PreAuthorize(IS_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public SickNotesDto sickNotes(
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = EXAMPLE_FIRST_DAY_OF_YEAR)
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = EXAMPLE_LAST_DAY_OF_YEAR)
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate,
        @ApiParam(value = "ID of the person")
        @RequestParam(value = "person", required = false)
            Integer personId) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }

        final Optional<Person> optionalPerson = personId == null ? Optional.empty() : personService.getPersonByID(personId);

        final List<SickNote> sickNotes;
        if (optionalPerson.isPresent()) {
            sickNotes = sickNoteService.getByPersonAndPeriod(optionalPerson.get(), startDate, endDate);
        } else {
            sickNotes = sickNoteService.getByPeriod(startDate, endDate);
        }

        final List<SickNoteDto> sickNoteResponse = sickNotes.stream()
            .filter(SickNote::isActive)
            .map(SickNoteDto::new)
            .collect(toList());

        return new SickNotesDto(sickNoteResponse);
    }
}
