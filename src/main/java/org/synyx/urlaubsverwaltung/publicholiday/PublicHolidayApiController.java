package org.synyx.urlaubsverwaltung.publicholiday;

import de.jollyday.Holiday;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.synyx.urlaubsverwaltung.api.SwaggerConfig.EXAMPLE_FIRST_DAY_OF_YEAR;
import static org.synyx.urlaubsverwaltung.api.SwaggerConfig.EXAMPLE_LAST_DAY_OF_YEAR;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@RestControllerAdviceMarker
@Api("Public Holidays: Get information about public holidays")
@RestController
@RequestMapping("/api")
public class PublicHolidayApiController {

    public static final String PUBLIC_HOLIDAYS = "public-holidays";

    private final PublicHolidaysService publicHolidaysService;
    private final PersonService personService;
    private final WorkingTimeService workingTimeService;
    private final SettingsService settingsService;

    @Autowired
    public PublicHolidayApiController(PublicHolidaysService publicHolidaysService, PersonService personService,
                                      WorkingTimeService workingTimeService, SettingsService settingsService) {

        this.publicHolidaysService = publicHolidaysService;
        this.personService = personService;
        this.workingTimeService = workingTimeService;
        this.settingsService = settingsService;
    }

    @ApiOperation(
        value = "Get all public holidays for a certain period", notes = "Get all public holidays for a certain period. "
        + "Information only reachable for users with role office."
    )
    @GetMapping(PUBLIC_HOLIDAYS)
    @PreAuthorize(IS_OFFICE)
    public PublicHolidaysDto getPublicHolidays(
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = EXAMPLE_FIRST_DAY_OF_YEAR)
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = EXAMPLE_LAST_DAY_OF_YEAR)
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate) {

        checkValidPeriod(startDate, endDate);

        final FederalState federalState = settingsService.getSettings().getWorkingTimeSettings().getFederalState();

        final List<PublicHolidayDto> publicHolidays = getPublicHolidays(startDate, endDate, federalState);
        return new PublicHolidaysDto(publicHolidays);
    }

    @ApiOperation(
        value = "Get all public holidays for a certain period", notes = "Get all public holidays for a certain period. "
        + "Information only reachable for users with role office and for own public holidays."
    )
    @GetMapping("/persons/{personId}/" + PUBLIC_HOLIDAYS)
    @PreAuthorize(IS_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public PublicHolidaysDto personsPublicHolidays(
        @ApiParam(value = "ID of the person to get the public holidays for.")
        @PathVariable("personId")
            Integer personId,
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = EXAMPLE_FIRST_DAY_OF_YEAR)
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = EXAMPLE_LAST_DAY_OF_YEAR)
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate) {

        checkValidPeriod(startDate, endDate);

        final Optional<Person> optionalPerson = personService.getPersonByID(personId);
        if (optionalPerson.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No person found for ID=" + personId);
        }

        final Person person = optionalPerson.get();
        final FederalState federalState = workingTimeService.getFederalStateForPerson(person, startDate);

        final List<PublicHolidayDto> publicHolidays = getPublicHolidays(startDate, endDate, federalState);
        return new PublicHolidaysDto(publicHolidays);
    }

    private List<PublicHolidayDto> getPublicHolidays(LocalDate startDate, LocalDate endDate, FederalState federalState) {
        return publicHolidaysService.getHolidays(startDate, endDate, federalState).stream()
            .map(holiday -> this.mapPublicHolidayToDto(holiday, federalState))
            .collect(toList());
    }

    private void checkValidPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }
    }

    private PublicHolidayDto mapPublicHolidayToDto(Holiday holiday, FederalState federalState) {
        final BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(holiday.getDate(), federalState);
        final DayLength absenceType = publicHolidaysService.getAbsenceTypeOfDate(holiday.getDate(), federalState);
        return new PublicHolidayDto(holiday, workingDuration, absenceType.name());
    }
}
