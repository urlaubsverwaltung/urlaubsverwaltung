package org.synyx.urlaubsverwaltung.person.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.user.UserSettingsService;

import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
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

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final UserSettingsService userSettingsService;

    @Autowired
    PersonApiController(PersonService personService, UserSettingsService userSettingsService) {
        this.personService = personService;
        this.userSettingsService = userSettingsService;
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
        summary = "Updates settings for the current person",
        description = """
            Updates settings for the current authenticated person.

            Needed basic authorities:
            * user
            """
    )
    @PatchMapping(path = "/me/settings", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Void> updateSettings(
        @AuthenticationPrincipal OidcUser oidcUser,
        @RequestBody PersonSettingsDto personSettingsDto
    ) {

        if (oidcUser == null) {
            LOG.info("cannot update logged in person settings without OidcUser.");
            return ResponseEntity.status(NOT_FOUND).build();
        }

        final String username = oidcUser.getSubject();

        final HttpStatus status = personService.getPersonByUsername(username)
            .map(person -> {
                if (personSettingsDto.navigationCollapsed() != null) {
                    userSettingsService.updateNavigationState(person, personSettingsDto.navigationCollapsed());
                }
                return NO_CONTENT;
            }).orElseGet(() -> {
                LOG.info("could not update person settings. username {} not found.", username);
                return NOT_FOUND;
            });

        return ResponseEntity.status(status).build();
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
}
