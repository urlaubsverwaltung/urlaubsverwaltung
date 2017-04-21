package org.synyx.urlaubsverwaltung.restapi.sicknote;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.restapi.ResponseWrapper;
import org.synyx.urlaubsverwaltung.restapi.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.restapi.absence.AbsenceResponse;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Sick Notes", description = "Get all sick notes for a certain period")
@RestController("restApiSickNoteController")
@RequestMapping("/api")
public class SickNoteController {

    private final SickNoteService sickNoteService;
    private final PersonService personService;

    @Autowired
    SickNoteController(SickNoteService sickNoteService, PersonService personService) {

        this.sickNoteService = sickNoteService;
        this.personService = personService;
    }

    @ApiOperation(
        value = "Get all sick notes for a certain period", notes = "Get all sick notes for a certain period. "
            + "If a person is specified, only the sick notes of this person are fetched. "
            + "Information only reachable for users with role office."
    )
    @RequestMapping(value = "/sicknotes", method = RequestMethod.GET)
    public ResponseWrapper<SickNoteListResponse> sickNotes(
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = "2016-01-01")
        @RequestParam(value = "from")
        String from,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = "2016-12-31")
        @RequestParam(value = "to")
        String to,
        @ApiParam(value = "ID of the person")
        @RequestParam(value = "person", required = false)
        Integer personId) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(RestApiDateFormat.DATE_PATTERN);
        DateMidnight startDate = formatter.parseDateTime(from).toDateMidnight();
        DateMidnight endDate = formatter.parseDateTime(to).toDateMidnight();

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Parameter 'from' must be before or equals to 'to' parameter");
        }

        Optional<Person> optionalPerson = personId == null ? Optional.empty() : personService.getPersonByID(personId);

        List<SickNote> sickNotes;

        if (optionalPerson.isPresent()) {
            sickNotes = sickNoteService.getByPersonAndPeriod(optionalPerson.get(), startDate, endDate);
        } else {
            sickNotes = sickNoteService.getByPeriod(startDate, endDate);
        }

        List<AbsenceResponse> sickNoteResponses = sickNotes.stream()
                .filter(SickNote::isActive)
                .map(AbsenceResponse::new)
                .collect(Collectors.toList());

        return new ResponseWrapper<>(new SickNoteListResponse(sickNoteResponses));
    }
}
