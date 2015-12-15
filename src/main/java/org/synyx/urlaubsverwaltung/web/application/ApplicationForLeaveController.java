package org.synyx.urlaubsverwaltung.web.application;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Controller for showing applications for leave in a certain state.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping("/web")
public class ApplicationForLeaveController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private WorkDaysService calendarService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SessionService sessionService;

    /**
     * Show waiting applications for leave.
     *
     * @return  waiting applications for leave page
     */
    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/application", method = RequestMethod.GET)
    public String showWaiting(Model model) {

        List<ApplicationForLeave> applicationsForLeave = getAllRelevantApplicationsForLeave();

        model.addAttribute("applications", applicationsForLeave);

        return "application/app_list";
    }


    private List<ApplicationForLeave> getAllRelevantApplicationsForLeave() {

        List<Application> applications = applicationService.getApplicationsForACertainState(ApplicationStatus.WAITING);

        if (sessionService.getSignedInUser().hasRole(Role.DEPARTMENT_HEAD)) {
            List<Person> members = departmentService.getManagedMembersOfDepartmentHead(
                    sessionService.getSignedInUser());

            return applications.stream()
                .filter(application -> members.contains(application.getPerson()))
                .map(application -> new ApplicationForLeave(application, calendarService))
                .collect(Collectors.toList());
        }

        return applications.stream()
            .map(application -> new ApplicationForLeave(application, calendarService))
            .collect(Collectors.toList());
    }
}
