package org.synyx.urlaubsverwaltung.person.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.synyx.urlaubsverwaltung.person.api.PersonApiController.ROOT_URL;


@Api("Persons: Get information about the persons of the application")
@RestController("restApiPersonController")
@RequestMapping(ROOT_URL)
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
    public ResponseEntity<List<PersonResponse>> persons(HttpServletRequest request) {

        List<PersonResponse> persons = personService.getActivePersons().stream()
            .map(p -> createPersonResponse(p, request))
            .collect(Collectors.toList());

        return new ResponseEntity<>(persons, HttpStatus.OK);
    }

    @ApiOperation(
        value = "Get one active person by id", notes = "Get one active person by id"
    )
    @GetMapping(PERSON_URL)
    public ResponseEntity<PersonResponse> getPerson(@PathVariable Integer id, HttpServletRequest request) {

        Optional<Person> person = personService.getPersonByID(id);
        if (person.isPresent()) {
            return new ResponseEntity<>(createPersonResponse(person.get(), request), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private PersonResponse createPersonResponse(Person person, HttpServletRequest request) {
        PersonResponse personResponse = new PersonResponse(person);
        String baseUri = new URIBuilder()
            .setScheme(request.getScheme())
            .setPort(request.getServerPort())
            .setHost(request.getServerName())
            .setPath(ROOT_URL)
            .toString();
        personResponse.add(new Link(baseUri + "/" + person.getId()).withSelfRel());
        return personResponse;
    }
}
