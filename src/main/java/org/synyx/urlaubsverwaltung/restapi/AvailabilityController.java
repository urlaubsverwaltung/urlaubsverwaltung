package org.synyx.urlaubsverwaltung.restapi;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Availabilities", description = "Get all availabilities for a certain period")
@RestController("restApiAvailabilityController")
@RequestMapping("/api")
public class AvailabilityController {

    private final PersonService personService;
   

    private final AvailabilityService availabilityService;

    @Autowired
    AvailabilityController(AvailabilityService availabilityService, PersonService personService) {
        this.availabilityService = availabilityService;
        this.personService = personService;
    }

    @ApiOperation(
        value = "Get all absences for a certain period and person",
        notes = "Get all absences for a certain period and person"
    )
    @RequestMapping(value = "/availabiliy", method = RequestMethod.GET)
    public AvailabilityList personsAvailabilities(
            @ApiParam(value = "start of interval to get availabilities from (inclusive)", defaultValue = "2016-01-01")
            @RequestParam("startDate")
            String startDateString,
            @ApiParam(value = "end of interval to get availabilities from (inclusive)", defaultValue = "2016-12-31")
            @RequestParam("endDate")
            String endDateString,
            @ApiParam(value = "ID of the person")
            @RequestParam("person")
            Integer personId) {

        DateMidnight startDate = DateMidnight.parse(startDateString);
        DateMidnight endDate = DateMidnight.parse(endDateString);
        Optional<Person> optionalPerson = personService.getPersonByID(personId);

        if (!optionalPerson.isPresent()) {
            throw new IllegalArgumentException("No person found for ID=" + personId);
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startdate " + startDateString + " must not be after endDate " + endDateString);
        }

        if (!optionalPerson.isPresent()) {
            throw new IllegalArgumentException("person " + personId + " not found");
        }

        return availabilityService.getPersonsAvailabilities(personId, startDate, endDate, optionalPerson.get());
    }


}
