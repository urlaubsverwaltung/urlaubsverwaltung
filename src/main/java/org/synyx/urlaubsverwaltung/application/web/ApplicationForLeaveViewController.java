package org.synyx.urlaubsverwaltung.application.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCEL_RE;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;


/**
 * Controller for showing applications for leave in a certain state.
 */
@Controller
@RequestMapping("/web")
public class ApplicationForLeaveViewController {

    private final ApplicationService applicationService;
    private final WorkDaysService calendarService;
    private final DepartmentService departmentService;
    private final PersonService personService;

    @Autowired
    public ApplicationForLeaveViewController(ApplicationService applicationService, WorkDaysService calendarService,
                                             DepartmentService departmentService, PersonService personService) {
        this.applicationService = applicationService;
        this.calendarService = calendarService;
        this.departmentService = departmentService;
        this.personService = personService;
    }

    /*
     * Show waiting applications for leave.
     */
    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @GetMapping("/application")
    public String showWaiting(Model model) {

        final List<ApplicationForLeave> applicationsForLeave = getAllRelevantApplicationsForLeave();
        model.addAttribute("applications", applicationsForLeave);

        final List<ApplicationForLeave> applicationsForLeaveCancellationRequests = getAllRelevantApplicationsForLeaveCancellationRequests();
        model.addAttribute("applications_cancellation_request", applicationsForLeaveCancellationRequests);

        return "application/app_list";
    }

    private List<ApplicationForLeave> getAllRelevantApplicationsForLeaveCancellationRequests() {

        final Person user = personService.getSignedInUser();

        final boolean isBoss = user.hasRole(BOSS);
        final boolean isOffice = user.hasRole(OFFICE);
        if (isBoss || isOffice) {
            final List<Application> applications = getApplicationsByStates(ALLOWED_CANCEL_RE);

            return applications.stream()
                .map(application -> new ApplicationForLeave(application, calendarService))
                .sorted(dateComparator())
                .collect(toList());
        }

        return List.of();
    }

    private List<ApplicationForLeave> getAllRelevantApplicationsForLeave() {

        final Person user = personService.getSignedInUser();

        final boolean isBoss = user.hasRole(BOSS);
        final boolean isOffice = user.hasRole(OFFICE);
        if (isBoss || isOffice) {
            // Boss and Office can see all waiting and temporary allowed applications leave
            return getApplicationsForLeaveForBossOrOffice();
        }

        final List<ApplicationForLeave> applicationsForLeave = new ArrayList<>();

        final boolean isSecondStage = user.hasRole(SECOND_STAGE_AUTHORITY);
        if (isSecondStage) {
            // Department head can see waiting and temporary allowed applications for leave of certain department(s)
            applicationsForLeave.addAll(getApplicationsForLeaveForSecondStageAuthority(user));
        }

        final boolean isHeadOf = user.hasRole(DEPARTMENT_HEAD);
        if (isHeadOf) {
            // Department head can see only waiting applications for leave of certain department(s)
            applicationsForLeave.addAll(getApplicationsForLeaveForDepartmentHead(user));
        }

        return applicationsForLeave.stream().filter(distinctByKey(ApplicationForLeave::getId)).collect(toList());
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForBossOrOffice() {
        final List<Application> applications = getApplicationsByStates(WAITING, TEMPORARY_ALLOWED);

        return applications.stream()
            .map(application -> new ApplicationForLeave(application, calendarService))
            .sorted(dateComparator())
            .collect(toList());
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForDepartmentHead(Person head) {

        final List<Application> waitingApplications = getApplicationsByStates(WAITING);
        final List<Person> members = departmentService.getManagedMembersOfDepartmentHead(head);

        return waitingApplications.stream()
            .filter(includeApplicationsOf(members))
            .filter(withoutOwnApplications(head))
            .filter(withoutSecondStageAuthorityApplications())
            .map(application -> new ApplicationForLeave(application, calendarService))
            .sorted(dateComparator())
            .collect(toList());
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForSecondStageAuthority(Person secondStage) {

        final List<Application> applications = getApplicationsByStates(WAITING, TEMPORARY_ALLOWED);
        final List<Person> members = departmentService.getManagedMembersForSecondStageAuthority(secondStage);

        return applications.stream()
            .filter(includeApplicationsOf(members))
            .filter(withoutOwnApplications(secondStage))
            .map(application -> new ApplicationForLeave(application, calendarService))
            .sorted(dateComparator())
            .collect(toList());
    }

    private Predicate<Application> withoutOwnApplications(Person head) {
        return application -> !application.getPerson().equals(head);
    }

    private Predicate<Application> withoutSecondStageAuthorityApplications() {
        return application -> !application.getPerson().getPermissions().contains(SECOND_STAGE_AUTHORITY);
    }

    private Predicate<Application> includeApplicationsOf(List<Person> members) {
        return application -> members.contains(application.getPerson());
    }

    private Comparator<ApplicationForLeave> dateComparator() {
        return Comparator.comparing(Application::getStartDate);
    }

    private List<Application> getApplicationsByStates(ApplicationStatus... state) {
        return stream(state)
            .flatMap(applicationStatus -> applicationService.getApplicationsForACertainState(applicationStatus).stream())
            .collect(toList());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
