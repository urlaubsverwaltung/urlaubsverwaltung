package org.synyx.urlaubsverwaltung.person.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

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
    @PreAuthorize(SecurityRules.IS_OFFICE)
    public ResponseEntity<List<PersonResponse>> persons(HttpServletRequest request) {

        List<PersonResponse> persons = personService.getActivePersons().stream()
            .map(p -> createPersonResponse(p, request))
            .collect(toList());

        return new ResponseEntity<>(persons, OK);
    }

    @ApiOperation(value = "Get one active person by id", notes = "Get one active person by id")
    @GetMapping(PERSON_URL)
    @PreAuthorize(SecurityRules.IS_OFFICE + " or @securityProvider.loggedInUserRequestsOwnData(authentication, #id)")
    public ResponseEntity<PersonResponse> getPerson(@PathVariable Integer id, HttpServletRequest request) {

        return personService.getPersonByID(id)
            .map(value -> new ResponseEntity<>(createPersonResponse(value, request), OK))
            .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    private PersonResponse createPersonResponse(Person person, HttpServletRequest request) {
        final PersonResponse personResponse = PersonResponseMapper.mapToResponse(person);
        final String baseUri = new URIBuilder()
            .setScheme(request.getScheme())
            .setPort(request.getServerPort())
            .setHost(request.getServerName())
            .setPath(ROOT_URL)
            .toString();
        personResponse.add(new Link(baseUri + "/" + person.getId()).withSelfRel());
        return personResponse;
    }
}
