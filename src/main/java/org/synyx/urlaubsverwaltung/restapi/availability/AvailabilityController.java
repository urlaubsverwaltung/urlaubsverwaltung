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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        notes = "Get all availabilities for a certain period and person"
    )
    @RequestMapping(value = "/availabilities", method = RequestMethod.GET)
    public AvailabilityListResponse personsAvailabilities(
        @ApiParam(value = "start of interval to get availabilities from (inclusive)", defaultValue = "2016-01-01")
        @RequestParam("from")
        String startDateString,
        @ApiParam(value = "end of interval to get availabilities from (inclusive)", defaultValue = "2016-12-31")
        @RequestParam("to")
        String endDateString,
        @ApiParam(
            value = "login name of the person - if not provided availabilities for all active persons are fetched"
        )
        @RequestParam(value = "person", required = false)
        String personLoginName) {

        DateMidnight startDate = DateMidnight.parse(startDateString);
        DateMidnight endDate = DateMidnight.parse(endDateString);
        Optional<Person> optionalPerson = personLoginName == null ? Optional.empty()
                                                                  : personService.getPersonByLogin(personLoginName);

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startdate " + startDateString + " must not be after endDate "
                + endDateString);
        }

        if (optionalPerson.isPresent()) {
            AvailabilityList personsAvailabilities = availabilityService.getPersonsAvailabilities(startDate, endDate,
                    optionalPerson.get());

            return new AvailabilityListResponse(Collections.singletonList(personsAvailabilities));
        } else {
            List<Person> activePersons = personService.getActivePersons();

            List<AvailabilityList> availabilitiesListedByPerson = new ArrayList<>();

            for (Person activePerson : activePersons) {
                AvailabilityList personsAvailabilities = availabilityService.getPersonsAvailabilities(startDate,
                        endDate, activePerson);

                availabilitiesListedByPerson.add(personsAvailabilities);
            }

            return new AvailabilityListResponse(availabilitiesListedByPerson);
        }
    }

    private class AvailabilityListResponse {

        private List<AvailabilityList> availabilitiesListedByPerson;

        public AvailabilityListResponse(List<AvailabilityList> availabilities) {

            this.availabilitiesListedByPerson = availabilities;
        }

        public List<AvailabilityList> getAvailabilitiesListedByPerson() {

            return availabilitiesListedByPerson;
        }
    }
}
