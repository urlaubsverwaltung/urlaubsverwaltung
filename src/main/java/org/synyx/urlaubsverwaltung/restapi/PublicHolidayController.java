package org.synyx.urlaubsverwaltung.restapi;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.jollyday.Holiday;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.synyx.urlaubsverwaltung.core.calendar.PublicHolidaysService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author  Aljona Murygina <murygina@synyx.de>
 */
@Api(value = "Public Holidays", description = "Get information about public holidays")
@RestController("restApiCalendarController")
@RequestMapping("/api")
public class PublicHolidayController {

    private static final String ROOT_URL = "/holidays";

    @Autowired
    private PublicHolidaysService publicHolidaysService;

    @ApiOperation(
        value = "Get all public holidays for a certain period", notes = "Get all public holidays for a certain period"
    )
    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    public ResponseWrapper<PublicHolidayListResponse> getPublicHolidays(
        @ApiParam(value = "Year to get the public holidays for", defaultValue = "2015")
        @RequestParam("year")
        String year,
        @ApiParam(value = "Month of year to get the public holidays for")
        @RequestParam(value = "month", required = false)
        String month) {

        PublicHolidayListResponse emptyResponse = new PublicHolidayListResponse();

        boolean hasYear = StringUtils.hasText(year);
        boolean hasMonth = StringUtils.hasText(month);

        Set<Holiday> holidays = new HashSet<>();

        if (hasYear && !hasMonth) {
            try {
                holidays = publicHolidaysService.getHolidays(Integer.parseInt(year));
            } catch (NumberFormatException ex) {
                return new ResponseWrapper<>(emptyResponse);
            }
        }

        if (hasYear && hasMonth) {
            try {
                holidays = publicHolidaysService.getHolidays(Integer.parseInt(year), Integer.parseInt(month));
            } catch (NumberFormatException ex) {
                return new ResponseWrapper<>(emptyResponse);
            }
        }

        List<PublicHolidayResponse> publicHolidayResponses = holidays.stream()
            .map(holiday ->
                        new PublicHolidayResponse(holiday,
                            publicHolidaysService.getWorkingDurationOfDate(holiday.getDate().toDateMidnight())))
            .collect(Collectors.toList());

        return new ResponseWrapper<>(new PublicHolidayListResponse(publicHolidayResponses));
    }
}
