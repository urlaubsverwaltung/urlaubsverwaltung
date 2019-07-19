package org.synyx.urlaubsverwaltung.person.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.synyx.urlaubsverwaltung.api.ResponseWrapper;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Api("Persons: Get information about the persons of the application")
@RestController("restApiPersonController")
@RequestMapping("/api")
public class PersonApiController {

    private static final String ROOT_URL = "/persons";
    private static final String PERSON_URL = ROOT_URL + "/{id}";

    private final PersonService personService;

    @Autowired
    PersonApiController(PersonService personService) {

        this.personService = personService;
    }

    @ApiOperation(
        value = "Get all active persons of the application", notes = "Get all active persons of the application"
    )
    @GetMapping(ROOT_URL)
    public ResponseEntity<List<PersonResponse>> persons() {

        List<PersonResponse> persons = personService.getActivePersons().stream()
            .map(PersonResponse::new)
            .collect(Collectors.toList());

        return new ResponseEntity<>(persons, HttpStatus.OK);
    }

    @ApiOperation(
        value = "Get one active person by id", notes = "Get one active person by id"
    )
    @GetMapping(PERSON_URL)
    public ResponseEntity<PersonResponse> getPerson(@PathVariable Integer id) {

        Optional<Person> person = personService.getPersonByID(id);
        if(person.isPresent()) {
            return new ResponseEntity<>(new PersonResponse(person.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }


    }
}
