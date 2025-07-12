package org.synyx.urlaubsverwaltung.person.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.synyx.urlaubsverwaltung.person.api.PersonMapper.mapToDto;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@Tag(
    name = "persons",
    description = """
        Returns information about persons and provides links to further information like absences, sick notes, ...
        """
)
@RestControllerAdviceMarker
@RestController
@RequestMapping("/api/persons")
public class PersonApiController {

    private final PersonService personService;

    @Autowired
    PersonApiController(PersonService personService) {
        this.personService = personService;
    }

    @Operation(
        summary = "Returns the person by current authentication",
        description = """
            Returns the current authenticated person.

            Needed basic authorities:
            * user
            """
    )
    @GetMapping(path = "/me", produces = HAL_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<PersonDto> me(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return new ResponseEntity<>(NOT_FOUND);
        }

        return personService.getPersonByUsername(oidcUser.getSubject())
            .map(person -> new ResponseEntity<>(mapToDto(person), OK))
            .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @Operation(
        summary = "Return the person with the given id",
        description = """
            Returns the person with the given id.

            Needed basic authorities:
            * user

            Needed additional authorities:
            * user                   - if the requested person id is the one of the authenticated user
            * department_head        - if the requested person id is a managed person of the department head and not of the authenticated user
            * second_stage_authority - if the requested person id is a managed person of the second stage authority and not of the authenticated user
            * boss or office         - if the requested person id is any id but not of the authenticated user
            """
    )
    @GetMapping(path = "/{personId}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    @PreAuthorize(IS_BOSS_OR_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfSecondStageAuthority(authentication, #personId)")
    public ResponseEntity<PersonDto> getPerson(@PathVariable Long personId) {
        return personService.getPersonByID(personId)
            .map(person -> new ResponseEntity<>(mapToDto(person), OK))
            .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @Operation(
        summary = "Returns all active or all inactive persons",
        description = """
            Returns all active persons (default) or all inactive persons.

            Needed basic authorities:
            * user

            Needed additional authorities:
            * boss
            * office
            """
    )
    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    @PreAuthorize(IS_BOSS_OR_OFFICE)
    public ResponseEntity<PersonsDto> persons(
        @Parameter(description = "Whether to return all active persons (default) or all inactive persons")
        @RequestParam(name = "active", defaultValue = "true")
        boolean active
    ) {

        final List<Person> persons = active ? personService.getActivePersons() : personService.getInactivePersons();

        final List<PersonDto> personDtoList = persons.stream()
            .map(PersonMapper::mapToDto)
            .toList();

        return new ResponseEntity<>(new PersonsDto(personDtoList), OK);
    }


    @Operation(
        summary = "Creates a new person",
        description = """
            Creates a new person with the given parameters of firstName, lastName and email.

            Needed basic authorities:
            * user

            Needed additional authorities:
            * person_add
            """
    )
    @PreAuthorize("hasAuthority('PERSON_ADD')")
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<PersonDto> create(@RequestBody @Valid PersonProvisionDto personProvisionDto) {

        final String predictedUsername = personProvisionDto.getEmail();
        if (personService.getPersonByUsername(predictedUsername).isPresent()) {
            throw new ResponseStatusException(CONFLICT);
        }

        final Person person = personService.create(predictedUsername, personProvisionDto.getFirstName(), personProvisionDto.getLastName(), personProvisionDto.getEmail());
        return new ResponseEntity<>(mapToDto(person), CREATED);
    }

    @Operation(
        summary = "Deactivates a person",
        description = """
            Deactivates a person by setting the INACTIVE role.

            Needed basic authorities:
            * user

            Needed additional authorities:
            * boss
            * office
            """
    )
    @PutMapping(path = "/{personId}/deactivate", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    @PreAuthorize(IS_BOSS_OR_OFFICE)
    public ResponseEntity<PersonDto> deactivatePerson(@PathVariable Long personId) {
        return personService.getPersonByID(personId)
            .map(person -> {
                if (person.isInactive()) {
                    return new ResponseEntity<>(mapToDto(person), OK);
                }

                // Business rule: Prevent deactivating the last OFFICE user
                if (person.hasRole(Role.OFFICE)) {
                    int otherOfficeUsers = personService.numberOfPersonsWithOfficeRoleExcludingPerson(personId);
                    if (otherOfficeUsers == 0) {
                        throw new ResponseStatusException(BAD_REQUEST, "Cannot deactivate the last user with OFFICE role");
                    }
                }

                final List<Role> updatedRoles = new ArrayList<>(person.getPermissions());
                updatedRoles.add(Role.INACTIVE);

                // Business rule: INACTIVE users cannot have other roles (except USER which is always present)
                updatedRoles.removeIf(role -> role != Role.INACTIVE && role != Role.USER);

                person.setPermissions(updatedRoles);

                final Person updatedPerson = personService.update(person);
                return new ResponseEntity<>(mapToDto(updatedPerson), OK);
            })
            .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @Operation(
        summary = "Activates a person",
        description = """
            Activates a person by removing the INACTIVE role.

            Needed basic authorities:
            * user

            Needed additional authorities:
            * boss
            * office
            """
    )
    @PutMapping(path = "/{personId}/activate", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    @PreAuthorize(IS_BOSS_OR_OFFICE)
    public ResponseEntity<PersonDto> activatePerson(@PathVariable Long personId) {
        return personService.getPersonByID(personId)
            .map(person -> {
                if (person.isActive()) {
                    return new ResponseEntity<>(mapToDto(person), OK);
                }

                final List<Role> updatedRoles = new ArrayList<>(person.getPermissions());
                updatedRoles.remove(Role.INACTIVE);

                // Ensure USER role is present when activating
                if (!updatedRoles.contains(Role.USER)) {
                    updatedRoles.add(Role.USER);
                }

                person.setPermissions(updatedRoles);

                final Person updatedPerson = personService.update(person);
                return new ResponseEntity<>(mapToDto(updatedPerson), OK);
            })
            .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }
}
