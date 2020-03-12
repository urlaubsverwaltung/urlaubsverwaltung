package org.synyx.urlaubsverwaltung.sicknote.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.api.ResponseWrapper;
import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
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
    public ResponseWrapper<SickNoteListResponse> sickNotes(
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = RestApiDateFormat.EXAMPLE_FIRST_DAY_OF_YEAR)
        @RequestParam(value = "from")
            String from,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = RestApiDateFormat.EXAMPLE_LAST_DAY_OF_YEAR)
        @RequestParam(value = "to")
            String to,
        @ApiParam(value = "ID of the person")
        @RequestParam(value = "person", required = false)
            Integer personId) {

        final LocalDate startDate;
        final LocalDate endDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN);
            startDate = LocalDate.parse(from, formatter);
            endDate = LocalDate.parse(to, formatter);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Parameter 'from' must be before or equals to 'to' parameter");
        }

        final Optional<Person> optionalPerson = personId == null ? Optional.empty() : personService.getPersonByID(personId);

        final List<SickNote> sickNotes;

        if (optionalPerson.isPresent()) {
            sickNotes = sickNoteService.getByPersonAndPeriod(optionalPerson.get(), startDate, endDate);
        } else {
            sickNotes = sickNoteService.getByPeriod(startDate, endDate);
        }

        final List<SickNoteResponse> sickNoteResponses = sickNotes.stream()
            .filter(SickNote::isActive)
            .map(SickNoteResponse::new)
            .collect(toList());

        return new ResponseWrapper<>(new SickNoteListResponse(sickNoteResponses));
    }
}
