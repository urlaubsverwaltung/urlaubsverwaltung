package org.synyx.urlaubsverwaltung.person.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.absence.AbsenceApiController;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.availability.api.AvailabilityApiController;
import org.synyx.urlaubsverwaltung.overview.calendar.VacationApiController;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.api.SickNoteApiController;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountApiController;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.synyx.urlaubsverwaltung.absence.AbsenceApiController.ABSENCES;
import static org.synyx.urlaubsverwaltung.availability.api.AvailabilityApiController.AVAILABILITIES;
import static org.synyx.urlaubsverwaltung.overview.calendar.VacationApiController.VACATIONS;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;
import static org.synyx.urlaubsverwaltung.sicknote.api.SickNoteApiController.SICKNOTES;
import static org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountApiController.WORKDAYS;

@RestControllerAdviceMarker
@Api("Persons: Get information about the persons of the application")
@RestController("restApiPersonController")
@RequestMapping(PersonApiController.ROOT_URL)
public class PersonApiController {

    static final String ROOT_URL = "/api/persons";
    private static final String PERSON_URL = "/{id}";

    private final PersonService personService;

    @Autowired
    PersonApiController(PersonService personService) {
        this.personService = personService;
    }

    @ApiOperation(
        value = "Get all active persons of the application", notes = "Get all active persons of the application"
    )
    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public ResponseEntity<List<PersonDto>> persons() {

        List<PersonDto> persons = personService.getActivePersons().stream()
            .map(this::createPersonResponse)
            .collect(toList());

        return new ResponseEntity<>(persons, OK);
    }

    @ApiOperation(value = "Get one active person by id", notes = "Get one active person by id")
    @GetMapping(PERSON_URL)
    @PreAuthorize(IS_OFFICE)
    public ResponseEntity<PersonDto> getPerson(@PathVariable Integer id) {

        return personService.getPersonByID(id)
            .map(value -> new ResponseEntity<>(createPersonResponse(value), OK))
            .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    private PersonDto createPersonResponse(Person person) {
        final PersonDto personDto = PersonMapper.mapToDto(person);
        personDto.add(linkTo(methodOn(PersonApiController.class).getPerson(person.getId())).withSelfRel());
        personDto.add(linkTo(methodOn(AbsenceApiController.class).personsAbsences(person.getId(), null, null, null)).withRel(ABSENCES));
        personDto.add(linkTo(methodOn(AvailabilityApiController.class).personsAvailabilities(person.getId(), null, null)).withRel(AVAILABILITIES));
        personDto.add(linkTo(methodOn(SickNoteApiController.class).personsSickNotes(person.getId(), null, null)).withRel(SICKNOTES));
        personDto.add(linkTo(methodOn(VacationApiController.class).getVacations(person.getId(), null, null)).withRel(VACATIONS));
        personDto.add(linkTo(methodOn(WorkDaysCountApiController.class).personsWorkDays(person.getId(), null, null, null)).withRel(WORKDAYS));
        return personDto;
    }
}
