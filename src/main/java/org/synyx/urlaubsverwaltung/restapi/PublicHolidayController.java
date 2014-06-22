package org.synyx.urlaubsverwaltung.restapi;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.wordnik.swagger.annotations.Api;
import de.jollyday.Holiday;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.synyx.urlaubsverwaltung.core.calendar.JollydayCalendar;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Aljona Murygina <murygina@synyx.de>
 */
@Api(value = "Public Holidays", description = "Get information about public holidays")
@Controller("restApiCalendarController")
public class PublicHolidayController {

    private static final String ROOT_URL = "/public-holiday";

    @Autowired
    private JollydayCalendar jollydayCalendar;

    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    @ModelAttribute("response")
    public PublicHolidayListResponse getPublicHolidays(@RequestParam("year") String year,
                                                       @RequestParam(value = "month", required = false) String month) {

        PublicHolidayListResponse emptyResponse = new PublicHolidayListResponse(Collections.EMPTY_LIST);

        boolean hasYear = StringUtils.hasText(year);
        boolean hasMonth = StringUtils.hasText(month);

        Set<Holiday> holidays = new HashSet<>();

        if(hasYear && !hasMonth) {

            try {
                holidays = jollydayCalendar.getHolidays(Integer.parseInt(year));
            } catch (NumberFormatException ex) {
                return emptyResponse;
            }

        }

        if(hasYear && hasMonth) {

            try {
                holidays = jollydayCalendar.getHolidays(Integer.parseInt(year), Integer.parseInt(month));
            } catch (NumberFormatException ex) {
                return emptyResponse;
            }

        }

        List<PublicHolidayResponse> publicHolidayResponses = Lists.transform(Lists.newArrayList(holidays), new Function<Holiday, PublicHolidayResponse>() {

            @Override
            public PublicHolidayResponse apply(Holiday holiday) {

                return new PublicHolidayResponse(holiday);

            }
        });

        return new PublicHolidayListResponse(publicHolidayResponses);

    }

}
