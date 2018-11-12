package org.synyx.urlaubsverwaltung.restapi.holiday;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.jollyday.Holiday;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.restapi.ResponseWrapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author  Aljona Murygina <murygina@synyx.de>
 */
@Api(value = "Public Holidays", description = "Get information about public holidays")
@RestController("restApiCalendarController")
@RequestMapping("/api")
public class PublicHolidayController {

    private final PublicHolidaysService publicHolidaysService;
    private final PersonService personService;
    private final WorkingTimeService workingTimeService;
    private final SettingsService settingsService;

    @Autowired
    public PublicHolidayController(PublicHolidaysService publicHolidaysService, PersonService personService,
        WorkingTimeService workingTimeService, SettingsService settingsService) {

        this.publicHolidaysService = publicHolidaysService;
        this.personService = personService;
        this.workingTimeService = workingTimeService;
        this.settingsService = settingsService;
    }

    @ApiOperation(
        value = "Get all public holidays for a certain period", notes = "Get all public holidays for a certain period"
    )
    @RequestMapping(value = "/holidays", method = RequestMethod.GET)
    public ResponseWrapper<PublicHolidayListResponse> getPublicHolidays(
        @ApiParam(value = "Year to get the public holidays for", defaultValue = "2016")
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

        List<PublicHolidayResponse> publicHolidayResponses = holidays.stream().map(holiday ->
                        new PublicHolidayResponse(holiday,
                            publicHolidaysService.getWorkingDurationOfDate(holiday.getDate().toDateMidnight(),
                                federalState))).collect(Collectors.toList());

        return new ResponseWrapper<>(new PublicHolidayListResponse(publicHolidayResponses));
    }


    private FederalState getFederalState(String year, Optional<String> optionalMonth, Optional<Person> optionalPerson) {

        if (optionalPerson.isPresent()) {
            DateMidnight validFrom = getValidFrom(year, optionalMonth);

            return workingTimeService.getFederalStateForPerson(optionalPerson.get(), validFrom);
        }

        return settingsService.getSettings().getWorkingTimeSettings().getFederalState();
    }


    private static DateMidnight getValidFrom(String year, Optional<String> optionalMonth) {

        int holidaysYear = Integer.parseInt(year);

        if (optionalMonth.isPresent()) {
            int holidaysMonth = Integer.parseInt(optionalMonth.get());

            return new DateMidnight(holidaysYear, holidaysMonth, 1);
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
