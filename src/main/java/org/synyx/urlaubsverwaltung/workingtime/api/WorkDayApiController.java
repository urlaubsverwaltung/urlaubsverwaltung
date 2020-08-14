package org.synyx.urlaubsverwaltung.workingtime.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.workingtime.NoValidWorkingTimeException;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.synyx.urlaubsverwaltung.api.RestApiDateFormat.DATE_PATTERN;
import static org.synyx.urlaubsverwaltung.api.SwaggerConfig.EXAMPLE_YEAR;

@RestControllerAdviceMarker
@Api("Work Days: Get information about work day in a certain period")
@RestController("restApiWorkDayController")
@RequestMapping("/api")
public class WorkDayApiController {

    private final PersonService personService;
    private final WorkDaysService workDaysService;

    @Autowired
    WorkDayApiController(PersonService personService, WorkDaysService workDaysService) {

        this.personService = personService;
        this.workDaysService = workDaysService;
    }

    /**
     * Calculate number of work days for the given period and person.
     *
     * @param startDate     start date as String (e.g. 2013-3-21)
     * @param endDate       end date as String (e.g. 2013-3-21)
     * @param length   day length as String (FULL, MORNING or NOON)
     * @param personId id of the person to number of work days for
     * @return number of days as String for the given parameters or "N/A" if parameters are not valid in any way
     */
    @ApiOperation(
        value = "Calculate the work days for a certain period and person",
        notes = "The calculation depends on the working time of the person."
    )
    @GetMapping("/workdays")
    @PreAuthorize(SecurityRules.IS_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public WorkDayDto workDays(
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = EXAMPLE_YEAR + "-01-01")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = EXAMPLE_YEAR + "-01-08")
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate,
        @ApiParam(value = "Day Length", defaultValue = "FULL", allowableValues = "FULL, MORNING, NOON")
        @RequestParam("length")
            String length,
        @ApiParam(value = "ID of the person")
        @RequestParam("person")
            Integer personId) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }

        final Optional<Person> person = personService.getPersonByID(personId);
        if (person.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for ID=" + personId);
        }

        final DayLength howLong;
        try {
            howLong = DayLength.valueOf(length);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
        }

        final BigDecimal days;
        try {
            days = workDaysService.getWorkDays(howLong, startDate, endDate, person.get());
        } catch (NoValidWorkingTimeException e) {
            throw new ResponseStatusException(NO_CONTENT, e.getMessage());
        }

        return new WorkDayDto(days.toString());
    }
}
