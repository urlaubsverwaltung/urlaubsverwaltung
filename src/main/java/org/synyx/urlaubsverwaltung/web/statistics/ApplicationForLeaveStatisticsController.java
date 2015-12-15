package org.synyx.urlaubsverwaltung.web.statistics;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;

import org.springframework.web.bind.annotation.*;

import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.util.List;
import java.util.Optional;
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

    @InitBinder
    public void initBinder(DataBinder binder) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
    }


    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/statistics", method = RequestMethod.POST)
    public String applicationForLeaveStatistics(@ModelAttribute("period") FilterPeriod period) {

        return "redirect:/web/application/statistics?from=" + period.getStartDateAsString() + "&to="
            + period.getEndDateAsString();
    }


    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public String applicationForLeaveStatistics(@RequestParam(value = "from", required = false) String from,
        @RequestParam(value = "to", required = false) String to, Model model) {

        FilterPeriod period = new FilterPeriod(Optional.ofNullable(from), Optional.ofNullable(to));

        DateMidnight fromDate = period.getStartDate();
        DateMidnight toDate = period.getEndDate();

        // NOTE: Not supported at the moment
        if (fromDate.getYear() != toDate.getYear()) {
            model.addAttribute("period", period);
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, "INVALID_PERIOD");

            return "application/app_statistics";
        }

        List<Person> persons = getRelevantPersons();

        List<ApplicationForLeaveStatistics> statistics = persons.stream().map(person ->
                    applicationForLeaveStatisticsBuilder.build(person, fromDate, toDate)).collect(Collectors.toList());

        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("statistics", statistics);
        model.addAttribute("period", period);
        model.addAttribute("vacationTypes", VacationType.values());

        return "application/app_statistics";
    }


    private List<Person> getRelevantPersons() {

        Person signedInUser = sessionService.getSignedInUser();

        if (signedInUser.hasRole(Role.DEPARTMENT_HEAD)) {
            return departmentService.getManagedMembersOfDepartmentHead(signedInUser);
        }

        return personService.getActivePersons();
    }
}
