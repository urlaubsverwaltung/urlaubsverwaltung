package org.synyx.urlaubsverwaltung.blackoutperiod.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriod;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@Tag(
    name = "blackout periods",
    description = "Blackout periods: Returns blocked days for a given person and period"
)
@RestControllerAdviceMarker
@RestController
@RequestMapping("/api/persons/{personId}")
public class BlackoutPeriodApiController {

    private final PersonService personService;
    private final BlackoutPeriodService blackoutPeriodService;

    public BlackoutPeriodApiController(PersonService personService, BlackoutPeriodService blackoutPeriodService) {
        this.personService = personService;
        this.blackoutPeriodService = blackoutPeriodService;
    }

    @Operation(
        summary = "Returns all blocked days for a certain period and a given person",
        description = """
            Returns all days that are blocked by a blackout period ("Urlaubssperre") for the given person and period.

            Needed basic authorities:
            * user

            Needed additional authorities:
            * user                   - if the requested blackout periods of the person id is from the authenticated user
            * department_head        - if the requested blackout periods of the person id is a managed person of the department head and not of the authenticated user
            * second_stage_authority - if the requested blackout periods of the person id is a managed person of the second stage authority and not of the authenticated user
            * boss or office         - if the requested blackout periods of the person id is any id but not of the authenticated user
            """
    )
    @GetMapping(value = "blackout-periods", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize(IS_BOSS_OR_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfSecondStageAuthority(authentication, #personId)")
    public BlackoutPeriodDaysDto personsBlackoutPeriods(
        @Parameter(description = "ID of the person")
        @PathVariable("personId")
        Long personId,
        @Parameter(description = "start of interval to get blackout periods from (inclusive)")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate startDate,
        @Parameter(description = "end of interval to get blackout periods from (inclusive)")
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Start date " + startDate + " must not be after end date " + endDate);
        }

        final Optional<Person> optionalPerson = personService.getPersonByID(personId);
        if (optionalPerson.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for ID=" + personId);
        }

        final List<BlackoutPeriod> blackoutPeriods = blackoutPeriodService.findBlackoutPeriodsForPerson(optionalPerson.get(), startDate, endDate);

        final List<BlackoutPeriodDayDto> days = blackoutPeriods.stream()
            .flatMap(period -> daysOf(period, startDate, endDate))
            .toList();

        return new BlackoutPeriodDaysDto(days);
    }

    private static Stream<BlackoutPeriodDayDto> daysOf(BlackoutPeriod period, LocalDate rangeStart, LocalDate rangeEnd) {

        final LocalDate from = period.getStartDate().isAfter(rangeStart) ? period.getStartDate() : rangeStart;
        final LocalDate to = period.getEndDate().isBefore(rangeEnd) ? period.getEndDate() : rangeEnd;

        return from.datesUntil(to.plusDays(1)).map(date -> new BlackoutPeriodDayDto(date, period.getTitle()));
    }
}
