package org.synyx.urlaubsverwaltung.availability.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;

import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestControllerAdviceMarker
@Api("Availabilities: Get all availabilities for a certain person and period")
@RestController("restApiAvailabilityController")
@RequestMapping("/api/persons/{personId}")
public class AvailabilityApiController {

    public static final String AVAILABILITIES = "availabilities";
    private final PersonService personService;
    private final AvailabilityService availabilityService;

    @Autowired
    AvailabilityApiController(AvailabilityService availabilityService, PersonService personService) {

        this.availabilityService = availabilityService;
        this.personService = personService;
    }

    @ApiOperation(
        value = "Get all availabilities for a certain period and person",
        notes =
            "Get all availabilities for a certain period and person. Maximum allowed period per request is one month."
    )
    @GetMapping(AVAILABILITIES)
    @PreAuthorize(SecurityRules.IS_OFFICE)
    public AvailabilityListDto personsAvailabilities(
        @ApiParam("id of the person")
        @PathVariable("personId")
            Integer personId,
        @ApiParam(value = "start of interval to get availabilities from (inclusive)", defaultValue = RestApiDateFormat.EXAMPLE_FIRST_DAY_OF_YEAR)
        @RequestParam("from")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
        @ApiParam(value = "end of interval to get availabilities from (inclusive)", defaultValue = RestApiDateFormat.EXAMPLE_LAST_DAY_OF_MONTH)
        @RequestParam("to")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Start date " + startDate + " must not be after end date " + endDate);
        }

        final Optional<Person> optionalPerson = personService.getPersonByID(personId);
        if (optionalPerson.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for id = " + personId);
        }

        boolean requestedDateRangeIsMoreThanOneMonth = startDate.minusDays(1).isBefore(endDate.minusMonths(1));
        if (requestedDateRangeIsMoreThanOneMonth) {
            throw new ResponseStatusException(BAD_REQUEST, "Requested date range to large. Maximum allowed range is one month");
        }

        try {
            return availabilityService.getPersonsAvailabilities(startDate, endDate, optionalPerson.get());
        } catch (FreeTimeAbsenceException e) {
            throw new ResponseStatusException(NO_CONTENT, "There is no content available for this person and the date range");
        }
    }
}
