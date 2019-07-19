package org.synyx.urlaubsverwaltung.person.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    private final PersonService personService;

    @Autowired
    PersonApiController(PersonService personService) {

        this.personService = personService;
    }

    @ApiOperation(
        value = "Get all active persons of the application", notes = "Get all active persons of the application"
    )
    @GetMapping(ROOT_URL)
    public List<PersonResponse> persons() {

        return personService.getActivePersons().stream().map(PersonResponse::new).collect(Collectors.toList());
    }
}
