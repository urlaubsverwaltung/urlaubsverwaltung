package org.synyx.urlaubsverwaltung.holiday.api;

import de.jollyday.Holiday;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.api.ResponseWrapper;
import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@RestControllerAdviceMarker
@Api("Public Holidays: Get information about public holidays")
@RestController("restApiCalendarController")
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

    @ApiOperation(
        value = "Get all public holidays for a certain period", notes = "Get all public holidays for a certain period"
    )
    @GetMapping("/holidays")
    @PreAuthorize(SecurityRules.IS_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public ResponseWrapper<PublicHolidayListResponse> getPublicHolidays(
        @ApiParam(value = "Year to get the public holidays for", defaultValue = RestApiDateFormat.EXAMPLE_YEAR)
        @RequestParam("year")
            String year,
        @ApiParam(value = "Month of year to get the public holidays for")
        @RequestParam(value = "month", required = false)
            String month,
        @ApiParam(value = "ID of the person to get the public holidays for. Can be missing to get system defaults.")
        @RequestParam(value = "person", required = false)
            Integer personId) {

        Optional<Person> optionalPerson = personId == null ? Optional.empty() : personService.getPersonByID(personId);

        if (personId != null && !optionalPerson.isPresent()) {
            throw new IllegalArgumentException("No person found for ID=" + personId);
        }

        Optional<String> optionalMonth = Optional.ofNullable(month);

        FederalState federalState = getFederalState(year, optionalMonth, optionalPerson);
        Set<Holiday> holidays = getHolidays(year, optionalMonth, federalState);

        List<PublicHolidayResponse> publicHolidayResponses = holidays.stream()
            .map(holiday -> this.mapPublicHolidayToDto(holiday, federalState))
            .collect(toList());

        return new ResponseWrapper<>(new PublicHolidayListResponse(publicHolidayResponses));
    }

    private PublicHolidayResponse mapPublicHolidayToDto(Holiday holiday, FederalState federalState) {
        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(Instant.from(holiday.getDate()), federalState);
        DayLength absenceType = publicHolidaysService.getAbsenceTypeOfDate(Instant.from(holiday.getDate()), federalState);
        return new PublicHolidayResponse(holiday, workingDuration, absenceType.name());
    }


    private FederalState getFederalState(String year, Optional<String> optionalMonth, Optional<Person> optionalPerson) {

        if (optionalPerson.isPresent()) {
            Instant validFrom = getValidFrom(year, optionalMonth);

            return workingTimeService.getFederalStateForPerson(optionalPerson.get(), validFrom);
        }

        return settingsService.getSettings().getWorkingTimeSettings().getFederalState();
    }


    private static Instant getValidFrom(String year, Optional<String> optionalMonth) {

        int holidaysYear = Integer.parseInt(year);

        if (optionalMonth.isPresent()) {
            int holidaysMonth = Integer.parseInt(optionalMonth.get());

            return Instant.now()
                .with(ChronoField.YEAR, holidaysYear)
                .with(ChronoField.MONTH_OF_YEAR, holidaysMonth)
                .with(ChronoField.DAY_OF_MONTH, 1);
        }

        return DateUtil.getFirstDayOfYear(holidaysYear);
    }


    private Set<Holiday> getHolidays(String year, Optional<String> optionalMonth, FederalState federalState) {

        int holidaysYear = Integer.parseInt(year);

        if (optionalMonth.isPresent()) {
            int holidaysMonth = Integer.parseInt(optionalMonth.get());

            return publicHolidaysService.getHolidays(holidaysYear, holidaysMonth, federalState);
        }

        return publicHolidaysService.getHolidays(holidaysYear, federalState);
    }
}
