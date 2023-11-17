package org.synyx.urlaubsverwaltung.workingtime;

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
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@Tag(
    name = "work days",
    description = "Work Days: Returns information about work day in a certain period"
)
@RestControllerAdviceMarker
@RestController
@RequestMapping("/api/persons/{personId}")
public class WorkDaysCountApiController {

    public static final String WORKDAYS = "workdays";

    private final PersonService personService;
    private final WorkDaysCountService workDaysCountService;

    @Autowired
    WorkDaysCountApiController(PersonService personService, WorkDaysCountService workDaysCountService) {
        this.personService = personService;
        this.workDaysCountService = workDaysCountService;
    }

    /**
     * Calculate number of work days for the given period and person.
     *
     * @param startDate start date as String (e.g. 2013-3-21)
     * @param endDate   end date as String (e.g. 2013-3-21)
     * @param length    day length as String (FULL, MORNING or NOON)
     * @param personId  id of the person to number of work days for
     * @return number of days as String for the given parameters or "N/A" if parameters are not valid in any way
     */
    @Operation(
        summary = "Calculate the work days for a certain period and person",
        description = """
            Calculate the work days for a certain period and person

            Needed basic authorities:
            * user

            Needed additional authorities:
            * user                   - if the requested work days of the person id is from the authenticated user
            * department_head        - if the requested work days of the person id is a managed person of the department head and not of the authenticated user
            * second_stage_authority - if the requested work days of the person id is a managed person of the second stage authority and not of the authenticated user
            * boss or office         - if the requested work days of the person id is any id but not of the authenticated user
            """
    )
    @GetMapping(path = WORKDAYS, produces = APPLICATION_JSON_VALUE)
    @PreAuthorize(IS_BOSS_OR_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfSecondStageAuthority(authentication, #personId)")
    public WorkDaysCountDto personsWorkDays(
        @Parameter(description = "ID of the person")
        @PathVariable("personId")
        Long personId,
        @Parameter(description = "Start date with pattern yyyy-MM-dd")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate startDate,
        @Parameter(description = "End date with pattern yyyy-MM-dd")
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate endDate,
        @Parameter(description = "Day Length")
        @RequestParam(value = "length", required = false)
        DayLength length) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }

        final Optional<Person> person = personService.getPersonByID(personId);
        if (person.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for ID=" + personId);
        }

        final DayLength howLong = requireNonNullElse(length, DayLength.FULL);

        final BigDecimal days;
        try {
            days = workDaysCountService.getWorkDaysCount(howLong, startDate, endDate, person.get());
        } catch (WorkDaysCountException e) {
            throw new ResponseStatusException(NO_CONTENT, e.getMessage());
        }

        return new WorkDaysCountDto(days.toString());
    }
}
