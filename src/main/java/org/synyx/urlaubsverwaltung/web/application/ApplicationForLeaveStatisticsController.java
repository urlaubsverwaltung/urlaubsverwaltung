package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.DateFormat;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.sicknote.FilterRequest;
import org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatisticsBuilder;

import java.util.ArrayList;
import java.util.List;


/**
 * Controller to generate applications for leave statistics.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@RequestMapping("/application")
@Controller
public class ApplicationForLeaveStatisticsController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PersonService personService;

    @Autowired
    private ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;

    @RequestMapping(value = "/statistics", method = RequestMethod.POST)
    public String applicationForLeaveStatistics(@ModelAttribute("filterRequest") FilterRequest filterRequest) {

        if (sessionService.isOffice()) {
            DateMidnight now = DateMidnight.now();
            DateMidnight from = now;
            DateMidnight to = now;

            if (filterRequest.getPeriod().equals(FilterRequest.Period.YEAR)) {
                from = now.dayOfYear().withMinimumValue();
                to = now.dayOfYear().withMaximumValue();
            } else if (filterRequest.getPeriod().equals(FilterRequest.Period.QUARTAL)) {
                from = now.dayOfMonth().withMinimumValue().minusMonths(2);

                // TODO: This is quickfix...
                if (from.getYear() != now.getYear()) {
                    from = now.dayOfYear().withMinimumValue();
                }

                to = now.dayOfMonth().withMaximumValue();
            } else if (filterRequest.getPeriod().equals(FilterRequest.Period.MONTH)) {
                from = now.dayOfMonth().withMinimumValue();
                to = now.dayOfMonth().withMaximumValue();
            }

            return "redirect:/web/application/statistics?from=" + from.toString(DateFormat.PATTERN) + "&to="
                + to.toString(DateFormat.PATTERN);
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/statistics", method = RequestMethod.GET, params = { "from", "to" })
    public String applicationForLeaveStatistics(@RequestParam("from") String from,
        @RequestParam("to") String to, Model model) {

        if (sessionService.isOffice()) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern(DateFormat.PATTERN);
            DateMidnight fromDate = DateMidnight.parse(from, formatter);
            DateMidnight toDate = DateMidnight.parse(to, formatter);

            // NOTE: Not supported at the moment
            if (fromDate.getYear() != toDate.getYear()) {
                model.addAttribute("filterRequest", new FilterRequest());
                model.addAttribute("errors", "INVALID_PERIOD");

                return ControllerConstants.APPLICATION + "/app_statistics";
            }

            List<ApplicationForLeaveStatistics> statistics = new ArrayList<>();

            for (Person person : personService.getActivePersons()) {
                statistics.add(applicationForLeaveStatisticsBuilder.build(person, fromDate, toDate));
            }

            model.addAttribute("from", fromDate);
            model.addAttribute("to", toDate);
            model.addAttribute("statistics", statistics);
            model.addAttribute("filterRequest", new FilterRequest());

            return ControllerConstants.APPLICATION + "/app_statistics";
        }

        return ControllerConstants.ERROR_JSP;
    }
}
