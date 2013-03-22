package org.synyx.urlaubsverwaltung.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.synyx.urlaubsverwaltung.calendar.GoogleCalendarService;

import java.io.IOException;
import java.math.BigDecimal;
import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;

/**
 * Controller for calendar relevant stuff.
 *
 * @author Aljona Murygina
 */
@Controller
public class CalendarController {

    private static final String JSP_FOLDER = "calendar/";
    private GoogleCalendarService googleCalendarService;
    private OwnCalendarService ownCalendarService;

    public CalendarController(GoogleCalendarService googleCalendarService, OwnCalendarService ownCalendarService) {
        this.googleCalendarService = googleCalendarService;
        this.ownCalendarService = ownCalendarService;
    }

    /**
     * Is used in application form: ajax call to calculate vacation days for the
     * by datepicker given dates
     *
     * @param start start date as String (e.g. 2013-3-21)
     * @param end end date as String (e.g. 2013-3-21)
     * @param length day length as String (FULL, MORNING or NOON)
     * @return number of days as String for the given parameters or "N/A" if parameters are not valid in any way
     */
    @RequestMapping(value = "/calendar/vacation", method = RequestMethod.GET)
    @ResponseBody
    public String getNumberOfDays(@RequestParam("start") String start, @RequestParam("end") String end,
            @RequestParam("length") String length) {

        if (StringUtils.hasText(start) && StringUtils.hasText(end) && StringUtils.hasText(length)) {

            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd"); // please do not change, because is used in custom.js
            DateMidnight startDate = DateMidnight.parse(start, fmt);
            DateMidnight endDate = DateMidnight.parse(end, fmt);

            if (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {

                DayLength howLong = DayLength.valueOf(length);

                BigDecimal days = ownCalendarService.getVacationDays(howLong, startDate, endDate);

                return days.toString();
            }
        }

        return "N/A";
    }

    @RequestMapping(value = "/calendar", method = RequestMethod.GET)
    public String getCalendarSite(Model model) {

        return JSP_FOLDER + "calendar";
    }

    @RequestMapping(value = "/calendar/setup", method = RequestMethod.GET)
    public String setupGoogleCalendar(Model model) throws IOException {

        googleCalendarService.setUp();

        return JSP_FOLDER + "setup";
    }

    @RequestMapping(value = "/calendar/event", method = RequestMethod.GET)
    public String addEvent(Model model) throws IOException {

        googleCalendarService.addEvent();

        return JSP_FOLDER + "event";
    }
}
