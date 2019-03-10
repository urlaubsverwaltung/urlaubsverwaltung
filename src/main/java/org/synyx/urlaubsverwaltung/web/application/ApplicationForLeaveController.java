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
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
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

        Person user = sessionService.getSignedInUser();

        boolean isHeadOf = user.hasRole(Role.DEPARTMENT_HEAD);
        boolean isSecondStage = user.hasRole(Role.SECOND_STAGE_AUTHORITY);
        boolean isBoss = user.hasRole(Role.BOSS);
        boolean isOffice = user.hasRole(Role.OFFICE);

        if (isBoss || isOffice) {
            // Boss and Office can see all waiting and temporary allowed applications leave
            return getApplicationsForLeaveForBossOrOffice();
        }

        if (isHeadOf) {
            // Department head can see only waiting applications for leave of certain department(s)
            return getApplicationsForLeaveForDepartmentHead(user);
        }

        if (isSecondStage) {
            // Department head can see waiting and temporary allowed applications for leave of certain department(s)
            return getApplicationsForLeaveForSecondStageAuthority(user);
        }

        return Collections.<ApplicationForLeave>emptyList();
    }


    private List<ApplicationForLeave> getApplicationsForLeaveForBossOrOffice() {

        List<Application> applications = new ArrayList<>();

        List<Application> waitingApplications = applicationService.getApplicationsForACertainState(
                ApplicationStatus.WAITING);

        List<Application> temporaryAllowedApplications = applicationService.getApplicationsForACertainState(
                ApplicationStatus.TEMPORARY_ALLOWED);

        applications.addAll(waitingApplications);
        applications.addAll(temporaryAllowedApplications);

        return applications.stream()
            .map(application -> new ApplicationForLeave(application, calendarService))
            .sorted(dateComparator())
            .collect(Collectors.toList());
    }


    private Comparator<ApplicationForLeave> dateComparator() {

        return (o1, o2) -> o1.getStartDate().compareTo(o2.getStartDate());
    }


    private List<ApplicationForLeave> getApplicationsForLeaveForDepartmentHead(Person head) {

        List<Application> waitingApplications = applicationService.getApplicationsForACertainState(
                ApplicationStatus.WAITING);

        List<Person> members = departmentService.getManagedMembersOfDepartmentHead(head);

        return waitingApplications.stream()
            .filter(includeDepartmentApplications(members))
            .filter(withoutOwnApplications(head))
            .filter(withoutSecondStageAuthorityApplications())
            .map(application -> new ApplicationForLeave(application, calendarService))
            .sorted(dateComparator())
            .collect(Collectors.toList());
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForSecondStageAuthority(Person secondStage) {

        List<Application> applications = new ArrayList<>();

        List<Application> waitingApplications = applicationService.getApplicationsForACertainState(
                ApplicationStatus.WAITING);

        List<Application> temporaryAllowedApplications = applicationService.getApplicationsForACertainState(
                ApplicationStatus.TEMPORARY_ALLOWED);

        applications.addAll(waitingApplications);
        applications.addAll(temporaryAllowedApplications);

        List<Person> members = departmentService.getMembersForSecondStageAuthority(secondStage);

        return applications.stream()
            .filter(includeDepartmentApplications(members))
            .filter(withoutOwnApplications(secondStage))
            .map(application -> new ApplicationForLeave(application, calendarService))
            .sorted(dateComparator())
            .collect(Collectors.toList());
    }

    private Predicate<Application> withoutOwnApplications(Person head) {
        return application -> !application.getPerson().equals(head);
    }

    private Predicate<Application> withoutSecondStageAuthorityApplications() {
        return application -> !application.getPerson().getPermissions().contains(Role.SECOND_STAGE_AUTHORITY);
    }

    private Predicate<Application> includeDepartmentApplications(List<Person> members) {
        return application -> members.contains(application.getPerson());
    }
}
