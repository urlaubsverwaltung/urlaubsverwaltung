package org.synyx.urlaubsverwaltung.availability.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@RestControllerAdviceMarker
@Tag(name = "availabilities", description = "Availabilities: Get all availabilities for a certain person and period")
@RestController
@RequestMapping("/api/persons/{personId}")
@Deprecated(forRemoval = true, since = "4.4.0")
public class AvailabilityApiController {

    public static final String AVAILABILITIES = "availabilities";

    private final PersonService personService;
    private final AvailabilityService availabilityService;

    @Autowired
    AvailabilityApiController(AvailabilityService availabilityService, PersonService personService) {
        this.availabilityService = availabilityService;
        this.personService = personService;
    }

    @Operation(
        deprecated = true,
        summary = "Get all availabilities for a certain period and person",
        description = "Get all availabilities for a certain period and person. Maximum allowed period per request is one month."
    )
    @GetMapping(AVAILABILITIES)
    @PreAuthorize(IS_OFFICE)
    public AvailabilityListDto personsAvailabilities(
        @Parameter(description = "id of the person")
        @PathVariable("personId")
            Integer personId,
        @Parameter(description = "start of interval to get availabilities from (inclusive)")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,
        @Parameter(description = "end of interval to get availabilities from (inclusive)")
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Start date " + startDate + " must not be after end date " + endDate);
        }

        final Optional<Person> optionalPerson = personService.getPersonByID(personId);
        if (optionalPerson.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for id = " + personId);
        }

        try {
            return availabilityService.getPersonsAvailabilities(startDate, endDate, optionalPerson.get());
        } catch (FreeTimeAbsenceException e) {
            throw new ResponseStatusException(NO_CONTENT, "There is no content available for this person and the date range");
        }
    }
}
