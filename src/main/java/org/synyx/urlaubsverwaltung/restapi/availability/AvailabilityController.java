package org.synyx.urlaubsverwaltung.restapi.availability;

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
 * @author  Marc Kannegiesser - kannegiesser@synyx.de
 * @author  Timo Eifler - eifler@synyx.de
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
        value = "Get all availabilities for a certain period and person",
        notes =
            "Get all availabilities for a certain period and person. Maximum allowed period per request is one month."
    )
    @RequestMapping(value = "/availabilities", method = RequestMethod.GET)
    public AvailabilityList personsAvailabilities(
        @ApiParam(value = "start of interval to get availabilities from (inclusive)", defaultValue = "2016-01-01")
        @RequestParam("from")
        String startDateString,
        @ApiParam(value = "end of interval to get availabilities from (inclusive)", defaultValue = "2016-01-31")
        @RequestParam("to")
        String endDateString,
        @ApiParam(value = "login name of the person")
        @RequestParam(value = "person")
        String personLoginName) {

        Optional<Person> optionalPerson = personService.getPersonByLogin(personLoginName);

        if (!optionalPerson.isPresent()) {
            throw new IllegalArgumentException("No person found for loginName = " + personLoginName);
        }

        DateMidnight startDate = DateMidnight.parse(startDateString);
        DateMidnight endDate = DateMidnight.parse(endDateString);

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startdate " + startDateString + " must not be after endDate "
                + endDateString);
        }

        boolean requestedDateRangeIsMoreThanOneMonth = startDate.minusDays(1).isBefore(endDate.minusMonths(1));

        if (requestedDateRangeIsMoreThanOneMonth) {
            throw new IllegalArgumentException("Requested date range to large. Maximum allowed range is one month");
        }

        return availabilityService.getPersonsAvailabilities(startDate, endDate, optionalPerson.get());
    }
}
