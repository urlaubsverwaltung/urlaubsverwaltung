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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.synyx.urlaubsverwaltung.person.api.PersonMapper.mapToDto;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@RestControllerAdviceMarker
@Tag(name = "persons", description = "Persons: Returns information about the persons and provides links to further information like absences, sick notes, ...")
@RestController
@RequestMapping("/api/persons")
public class PersonApiController {

    private final PersonService personService;

    @Autowired
    PersonApiController(PersonService personService) {
        this.personService = personService;
    }

    @Operation(summary = "Returns the person by current authentication", description = "Returns the current logged in person.")
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<PersonDto> me(@AuthenticationPrincipal OidcUser oidcUser) {
        return personService.getPersonByUsername(oidcUser.getSubject())
                .map(person -> new ResponseEntity<>(mapToDto(person), OK))
                .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @Operation(summary = "Return person by id", description = "Returns the person with the given id.")
    @GetMapping("/{personId}")
    @PreAuthorize(IS_BOSS_OR_OFFICE +
            " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
            " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)" +
            " or @userApiMethodSecurity.isInDepartmentOfSecondStageAuthority(authentication, #personId)")
    public ResponseEntity<PersonDto> getPerson(@PathVariable Long personId) {
        return personService.getPersonByID(personId)
                .map(person -> new ResponseEntity<>(mapToDto(person), OK))
                .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @Operation(summary = "Returns all active persons", description = "Returns all active persons.")
    @GetMapping
    @PreAuthorize(IS_BOSS_OR_OFFICE)
    public ResponseEntity<PersonsDto> persons() {

        final List<PersonDto> persons = personService.getActivePersons().stream()
                .map(PersonMapper::mapToDto)
                .toList();

        return new ResponseEntity<>(new PersonsDto(persons), OK);
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
        return new ResponseEntity<>(mapToDto(person), CREATED);
    }

}
