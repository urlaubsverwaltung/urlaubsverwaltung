package org.synyx.urlaubsverwaltung.publicholiday;

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
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@RestControllerAdviceMarker
@Tag(name = "public holidays", description = "Public Holidays: Get information about public holidays")
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

    @Operation(
        summary = "Get all public holidays for a certain period",
        description = "Get all public holidays for a certain period. "
            + "Information only reachable for users with role office."
    )
    @GetMapping(PUBLIC_HOLIDAYS)
    @PreAuthorize(IS_OFFICE)
    public PublicHolidaysDto getPublicHolidays(
        @Parameter(description = "Start date with pattern yyyy-MM-dd")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,
        @Parameter(description = "End date with pattern yyyy-MM-dd")
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate) {

        checkValidPeriod(startDate, endDate);

        final FederalState federalState = settingsService.getSettings().getWorkingTimeSettings().getFederalState();

        final List<PublicHolidayDto> publicHolidays = getPublicHolidays(startDate, endDate, federalState);
        return new PublicHolidaysDto(publicHolidays);
    }

    @Operation(
        summary = "Get all public holidays for a certain period", description = "Get all public holidays for a certain period. "
        + "Information only reachable for users with role office and for own public holidays."
    )
    @GetMapping("/persons/{personId}/" + PUBLIC_HOLIDAYS)
    @PreAuthorize(IS_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)")
    public PublicHolidaysDto personsPublicHolidays(
        @Parameter(description = "ID of the person to get the public holidays for.")
        @PathVariable("personId")
            Integer personId,
        @Parameter(description = "Start date with pattern yyyy-MM-dd")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,
        @Parameter(description = "End date with pattern yyyy-MM-dd")
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
        return publicHolidaysService.getPublicHolidays(startDate, endDate, federalState).stream()
            .map(publicHoliday -> this.mapPublicHolidayToDto(publicHoliday, federalState))
            .collect(toList());
    }

    private void checkValidPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }
    }

    private PublicHolidayDto mapPublicHolidayToDto(PublicHoliday publicHoliday, FederalState federalState) {
        final BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(publicHoliday.getDate(), federalState);
        final DayLength absenceType = publicHolidaysService.getAbsenceTypeOfDate(publicHoliday.getDate(), federalState);
        return new PublicHolidayDto(publicHoliday, workingDuration, absenceType.name());
    }
}
