package org.synyx.urlaubsverwaltung.web.statistics;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;

/**
 * Controller to generate applications for leave vacation overview.
 *
 */
@RequestMapping("/web/application")
@Controller
public class ApplicationForLeaveVacationOverviewController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private DepartmentService departmentService;

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/vacationoverview", method = RequestMethod.POST)
    public String applicationForLeaveVacationOverview() {

        return "redirect:/web/application/vacationoverview";
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/vacationoverview", method = RequestMethod.GET)
    public String applicationForLeaveVacationOverview(Model model) {
        Person signedInUser = sessionService.getSignedInUser();

        prepareDepartments(signedInUser, model);

        model.addAttribute("currentYear", LocalDate.now().getYear());
        model.addAttribute("currentMonth", LocalDate.now().getMonthOfYear());
        return "application/vacation_overview";
    }

    private void prepareDepartments(Person person, Model model) {
        if (person.hasRole(Role.BOSS) || person.hasRole(Role.OFFICE)) {
            model.addAttribute("departments", departmentService.getAllDepartments());
        } else if (person.hasRole(Role.SECOND_STAGE_AUTHORITY)) {
            model.addAttribute("departments", departmentService.getManagedDepartmentsOfSecondStageAuthority(person));
        } else if (person.hasRole(Role.DEPARTMENT_HEAD)) {
            model.addAttribute("departments", departmentService.getManagedDepartmentsOfDepartmentHead(person));
        } else {
            model.addAttribute("departments", departmentService.getAssignedDepartmentsOfMember(person));
        }
    }
}
