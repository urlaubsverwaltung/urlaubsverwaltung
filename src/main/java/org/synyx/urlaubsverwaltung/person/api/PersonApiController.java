package org.synyx.urlaubsverwaltung.person.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.absence.AbsenceApiController;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteApiController;
import org.synyx.urlaubsverwaltung.vacations.VacationApiController;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountApiController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.synyx.urlaubsverwaltung.absence.AbsenceApiController.ABSENCES;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteApiController.SICKNOTES;
import static org.synyx.urlaubsverwaltung.vacations.VacationApiController.VACATIONS;
import static org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountApiController.WORKDAYS;

@RestControllerAdviceMarker
@Tag(name = "persons", description = "Persons: Get information about the persons of the application")
@RestController
@RequestMapping("/api/persons")
public class PersonApiController {

    private final PersonService personService;

    @Autowired
    PersonApiController(PersonService personService) {
        this.personService = personService;
    }

    @Operation(
        summary = "Get all active persons of the application",
        description = "Get all active persons of the application"
    )
    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public ResponseEntity<List<PersonDto>> persons() {

        final List<PersonDto> persons = personService.getActivePersons().stream()
            .map(this::createPersonResponse)
            .toList();

        return new ResponseEntity<>(persons, OK);
    }

    @Operation(summary = "Get one active person by id", description = "Get one active person by id")
    @GetMapping("/{id}")
    @PreAuthorize(IS_OFFICE)
    public ResponseEntity<PersonDto> getPerson(@PathVariable Long id) {
        return personService.getPersonByID(id)
            .map(value -> new ResponseEntity<>(createPersonResponse(value), OK))
            .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @PreAuthorize("hasAuthority('PERSON_ADD')")
    @Operation(summary = "Creates a new person with the given parameters", description = "Creates a new person with the given parameters of firstName, lastName and email. The authority 'USER' and 'PERSON_ADD' is needed to execute this method.")
    @ApiResponses(value = {@ApiResponse(description = "successful operation", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PersonProvisionDto.class))})})
    @PostMapping
    public ResponseEntity<PersonDto> create(@RequestBody @Valid PersonProvisionDto personProvisionDto) {

        final String predictedUsername = personProvisionDto.getEmail();
        if (personService.getPersonByUsername(predictedUsername).isPresent()) {
            throw new ResponseStatusException(CONFLICT);
        }

        final Person person = personService.create(predictedUsername, personProvisionDto.getFirstName(), personProvisionDto.getLastName(), personProvisionDto.getEmail());
        return new ResponseEntity<>(createPersonResponse(person), CREATED);
    }

    private PersonDto createPersonResponse(Person person) {
        final PersonDto personDto = PersonMapper.mapToDto(person);

        personDto.add(linkTo(methodOn(PersonApiController.class).getPerson(person.getId())).withSelfRel());
        personDto.add(linkTo(methodOn(AbsenceApiController.class).personsAbsences(person.getId(), null, null, List.of("vacation, sick_note, public_holiday, no_workday"))).withRel(ABSENCES));
        personDto.add(linkTo(methodOn(SickNoteApiController.class).personsSickNotes(person.getId(), null, null)).withRel(SICKNOTES));
        personDto.add(linkTo(methodOn(VacationApiController.class).getVacations(person.getId(), null, null)).withRel(VACATIONS));
        personDto.add(linkTo(methodOn(WorkDaysCountApiController.class).personsWorkDays(person.getId(), null, null, null)).withRel(WORKDAYS));
        return personDto;
    }
}
