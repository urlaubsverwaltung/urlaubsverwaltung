package org.synyx.urlaubsverwaltung.restapi.person;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.restapi.ResponseWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Persons", description = "Get information about the persons of the application")
@RestController("restApiPersonController")
@RequestMapping("/api")
public class PersonController {

    private static final String ROOT_URL = "/persons";

    private final PersonService personService;

    @Autowired
    PersonController(PersonService personService) {

        this.personService = personService;
    }

    @ApiOperation(
        value = "Get all active persons of the application", notes = "Get all active persons of the application"
    )
    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    public ResponseWrapper<PersonListResponse> persons(
        @ApiParam(value = "LDAP Login")
        @RequestParam(value = "ldap", required = false)
        String ldapName) {

        List<Person> persons = new ArrayList<>();

        if (ldapName == null) {
            persons = personService.getActivePersons();
        } else {
            Optional<Person> person = personService.getPersonByLogin(ldapName);

            if (person.isPresent()) {
                persons.add(person.get());
            }
        }

        List<PersonResponse> personResponses = persons.stream().map(PersonResponse::new).collect(Collectors.toList());

        return new ResponseWrapper<>(new PersonListResponse(personResponses));
    }
}
