package org.synyx.urlaubsverwaltung.web.statistics;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.DateFormat;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.FilterRequest;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Controller to generate applications for leave statistics.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@RequestMapping("/web/application")
@Controller
public class ApplicationForLeaveStatisticsController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PersonService personService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/statistics", method = RequestMethod.POST)
    public String applicationForLeaveStatistics(@ModelAttribute("filterRequest") FilterRequest filterRequest) {

        DateMidnight from = filterRequest.getStartDate();
        DateMidnight to = filterRequest.getEndDate();

        return "redirect:/web/application/statistics?from=" + from.toString(DateFormat.PATTERN) + "&to="
            + to.toString(DateFormat.PATTERN);
    }


    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public String applicationForLeaveStatistics(@RequestParam(value = "from", required = false) String from,
        @RequestParam(value = "to", required = false) String to, Model model) {

        DateMidnight fromDate = getStartDate(from);
        DateMidnight toDate = getEndDate(to);

        // NOTE: Not supported at the moment
        if (fromDate.getYear() != toDate.getYear()) {
            model.addAttribute("filterRequest", new FilterRequest());
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, "INVALID_PERIOD");

            return "application/app_statistics";
        }

        List<Person> persons = getRelevantPersons();

        List<ApplicationForLeaveStatistics> statistics = persons.stream().map(person ->
                    applicationForLeaveStatisticsBuilder.build(person, fromDate, toDate)).collect(Collectors.toList());

        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("statistics", statistics);
        model.addAttribute("filterRequest", new FilterRequest());
        model.addAttribute("vacationTypes", VacationType.values());

        return "application/app_statistics";
    }


    private DateMidnight getStartDate(String dateToParse) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(DateFormat.PATTERN);

        if (dateToParse == null) {
            return DateUtil.getFirstDayOfYear(DateMidnight.now().getYear());
        }

        return DateMidnight.parse(dateToParse, formatter);
    }


    private DateMidnight getEndDate(String dateToParse) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(DateFormat.PATTERN);

        if (dateToParse == null) {
            return DateUtil.getLastDayOfYear(DateMidnight.now().getYear());
        }

        return DateMidnight.parse(dateToParse, formatter);
    }


    private List<Person> getRelevantPersons() {

        Person signedInUser = sessionService.getSignedInUser();

        if (signedInUser.hasRole(Role.DEPARTMENT_HEAD)) {
            return departmentService.getManagedMembersOfDepartmentHead(signedInUser);
        }

        return personService.getActivePersons();
    }
}
