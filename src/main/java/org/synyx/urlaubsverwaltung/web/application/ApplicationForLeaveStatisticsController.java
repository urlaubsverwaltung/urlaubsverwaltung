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
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.FilterRequest;
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

        if (sessionService.isBoss() || sessionService.isOffice()) {
            DateMidnight from = filterRequest.getStartDate();
            DateMidnight to = filterRequest.getEndDate();

            return "redirect:/web/application/statistics?from=" + from.toString(DateFormat.PATTERN) + "&to="
                + to.toString(DateFormat.PATTERN);
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public String applicationForLeaveStatistics(@RequestParam(value = "from", required = false) String from,
        @RequestParam(value = "to", required = false) String to, Model model) {

        if (sessionService.isBoss() || sessionService.isOffice()) {
            DateMidnight fromDate;
            DateMidnight toDate;

            DateTimeFormatter formatter = DateTimeFormat.forPattern(DateFormat.PATTERN);
            int currentYear = DateMidnight.now().getYear();

            if (from == null) {
                fromDate = DateUtil.getFirstDayOfYear(currentYear);
            } else {
                fromDate = DateMidnight.parse(from, formatter);
            }

            if (to == null) {
                toDate = DateUtil.getLastDayOfYear(currentYear);
            } else {
                toDate = DateMidnight.parse(to, formatter);
            }

            // NOTE: Not supported at the moment
            if (fromDate.getYear() != toDate.getYear()) {
                model.addAttribute("filterRequest", new FilterRequest());
                model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, "INVALID_PERIOD");

                return "application" + "/app_statistics";
            }

            List<ApplicationForLeaveStatistics> statistics = new ArrayList<>();

            for (Person person : personService.getActivePersons()) {
                statistics.add(applicationForLeaveStatisticsBuilder.build(person, fromDate, toDate));
            }

            model.addAttribute("from", fromDate);
            model.addAttribute("to", toDate);
            model.addAttribute("statistics", statistics);
            model.addAttribute("filterRequest", new FilterRequest());

            return "application" + "/app_statistics";
        }

        return ControllerConstants.ERROR_JSP;
    }
}
