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
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@Tag(
    name = "public holidays",
    description = """
        Public Holidays: Returns information about public holidays
        """
)
@RestControllerAdviceMarker
@RestController
@RequestMapping("/api")
public class PublicHolidayApiController {

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
        description = """
            Returns all global public holidays for a certain period based on the global public holiday regulations

            Needed basic authorities:
            * user
            """
    )
    @GetMapping(path = "public-holidays", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
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

        final WorkingTimeSettings workingTimeSettings = settingsService.getSettings().getWorkingTimeSettings();
        final FederalState federalState = workingTimeSettings.getFederalState();

        final List<PublicHolidayDto> publicHolidays = getPublicHolidays(startDate, endDate, federalState);
        return new PublicHolidaysDto(publicHolidays);
    }

    @Operation(
        summary = "Returns all public holidays for a certain period and given person",
        description = """
            Returns all public holidays for a certain period based on the specific public holiday regulations of the given person

            Needed basic authorities:
            * user

            Needed additional authorities:
            * user                   - if the requested public holidays of the person id is the one of the authenticated user
            * department_head        - if the requested public holidays of the person id is a managed person of the department head and not of the authenticated user
            * second_stage_authority - if the requested public holidays of the person id is a managed person of the second stage authority and not of the authenticated user
            * boss or office         - if the requested public holidays of the person id is any id but not of the authenticated user
            """
    )
    @GetMapping(path = "/persons/{personId}/public-holidays", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize(IS_BOSS_OR_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfSecondStageAuthority(authentication, #personId)")
    public PublicHolidaysDto personsPublicHolidays(
        @Parameter(description = "ID of the person to get the public holidays for.")
        @PathVariable("personId")
        Long personId,
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
        final DateRange dateRange = new DateRange(startDate, endDate);

        final List<PublicHolidayDto> publicHolidays = workingTimeService.getFederalStatesByPersonAndDateRange(person, dateRange)
            .entrySet().stream()
            .map(entry -> getPublicHolidays(entry.getKey().startDate(), entry.getKey().endDate(), entry.getValue()))
            .flatMap(List::stream)
            .sorted(Comparator.comparing(PublicHolidayDto::getDate))
            .toList();

        return new PublicHolidaysDto(publicHolidays);
    }

    private List<PublicHolidayDto> getPublicHolidays(LocalDate startDate, LocalDate endDate, FederalState federalState) {
        return publicHolidaysService.getPublicHolidays(startDate, endDate, federalState).stream()
            .map(this::mapPublicHolidayToDto)
            .toList();
    }

    private void checkValidPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }
    }

    private PublicHolidayDto mapPublicHolidayToDto(PublicHoliday publicHoliday) {
        return new PublicHolidayDto(publicHoliday, publicHoliday.dayLength().getDuration(), publicHoliday.dayLength().name());
    }
}
