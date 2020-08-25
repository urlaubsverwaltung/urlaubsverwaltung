package org.synyx.urlaubsverwaltung.statistics.vacationoverview.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

/**
 * Controller to generate applications for leave vacation overview.
 */
@RequestMapping("/web/application")
@Controller
public class ApplicationForLeaveVacationOverviewViewController {

    private static final String DEPARTMENTS = "departments";

    private final PersonService personService;
    private final DepartmentService departmentService;

    @Autowired
    public ApplicationForLeaveVacationOverviewViewController(PersonService personService, DepartmentService departmentService) {
        this.personService = personService;
        this.departmentService = departmentService;
    }

    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping("/vacationoverview")
    public String applicationForLeaveVacationOverview(Model model) {
        Person signedInUser = personService.getSignedInUser();

        prepareDepartments(signedInUser, model);

        model.addAttribute("currentYear", ZonedDateTime.now(UTC).getYear());
        model.addAttribute("currentMonth", ZonedDateTime.now(UTC).getMonthValue());
        return "application/vacation_overview";
    }

    private void prepareDepartments(Person person, Model model) {
        if (person.hasRole(BOSS) || person.hasRole(OFFICE)) {
            model.addAttribute(DEPARTMENTS, departmentService.getAllDepartments());
        } else if (person.hasRole(SECOND_STAGE_AUTHORITY)) {
            model.addAttribute(DEPARTMENTS, departmentService.getManagedDepartmentsOfSecondStageAuthority(person));
        } else if (person.hasRole(DEPARTMENT_HEAD)) {
            model.addAttribute(DEPARTMENTS, departmentService.getManagedDepartmentsOfDepartmentHead(person));
        } else {
            model.addAttribute(DEPARTMENTS, departmentService.getAssignedDepartmentsOfMember(person));
        }
    }
}
