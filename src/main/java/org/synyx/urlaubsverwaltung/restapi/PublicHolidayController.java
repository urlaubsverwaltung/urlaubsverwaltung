package org.synyx.urlaubsverwaltung.restapi;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.jollyday.Holiday;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.core.calendar.JollydayCalendar;

import java.math.BigDecimal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author  Aljona Murygina <murygina@synyx.de>
 */
@Api(value = "Public Holidays", description = "Get information about public holidays")
@Controller("restApiCalendarController")
public class PublicHolidayController {

    private static final String ROOT_URL = "/public-holiday";

    @Autowired
    private JollydayCalendar jollydayCalendar;

    @ApiOperation(
        value = "Get all public holidays for a certain period", notes = "Get all public holidays for a certain period"
    )
    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    @ModelAttribute("response")
    public PublicHolidayListResponse getPublicHolidays(
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
                holidays = jollydayCalendar.getHolidays(Integer.parseInt(year));
            } catch (NumberFormatException ex) {
                return emptyResponse;
            }
        }

        if (hasYear && hasMonth) {
            try {
                holidays = jollydayCalendar.getHolidays(Integer.parseInt(year), Integer.parseInt(month));
            } catch (NumberFormatException ex) {
                return emptyResponse;
            }
        }

        List<PublicHolidayResponse> publicHolidayResponses = FluentIterable.from(holidays).transform(
                new Function<Holiday, PublicHolidayResponse>() {

                    @Override
                    public PublicHolidayResponse apply(Holiday holiday) {

                        BigDecimal duration = jollydayCalendar.getWorkingDurationOfDate(
                                holiday.getDate().toDateMidnight());

                        return new PublicHolidayResponse(holiday, duration);
                    }
                }).toList();

        return new PublicHolidayListResponse(publicHolidayResponses);
    }
}
