package org.synyx.urlaubsverwaltung.statistics.vacationoverview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;

import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;

/**
 * Controller to generate applications for leave vacation overview.
 *
 */
@RequestMapping("/web/application")
@Controller
public class ApplicationForLeaveVacationOverviewController {

    private final SessionService sessionService;
    private final DepartmentService departmentService;

    @Autowired
    public ApplicationForLeaveVacationOverviewController(SessionService sessionService, DepartmentService departmentService) {
        this.sessionService = sessionService;
        this.departmentService = departmentService;
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @PostMapping("/vacationoverview")
    public String applicationForLeaveVacationOverview() {

        return "redirect:/web/application/vacationoverview";
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @GetMapping("/vacationoverview")
    public String applicationForLeaveVacationOverview(Model model) {
        Person signedInUser = sessionService.getSignedInUser();

        prepareDepartments(signedInUser, model);

        model.addAttribute("currentYear", ZonedDateTime.now(UTC).getYear());
        model.addAttribute("currentMonth", ZonedDateTime.now(UTC).getMonthValue());
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
