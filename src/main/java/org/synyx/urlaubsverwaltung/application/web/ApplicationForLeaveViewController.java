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
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

/**
 * Controller for showing applications for leave in a certain state.
 */
@Controller
@RequestMapping("/web")
public class ApplicationForLeaveViewController {

    private final ApplicationService applicationService;
    private final WorkDaysCountService calendarService;
    private final DepartmentService departmentService;
    private final PersonService personService;

    @Autowired
    public ApplicationForLeaveViewController(ApplicationService applicationService, WorkDaysCountService calendarService,
                                             DepartmentService departmentService, PersonService personService) {
        this.applicationService = applicationService;
        this.calendarService = calendarService;
        this.departmentService = departmentService;
        this.personService = personService;
    }

    /*
     * Show waiting applications for leave.
     */
    @PreAuthorize(IS_PRIVILEGED_USER)
    @GetMapping("/application")
    public String showWaiting(Model model) {

        final List<ApplicationForLeave> applicationsForLeave = getAllRelevantApplicationsForLeave();
        model.addAttribute("applications", applicationsForLeave);

        return "application/app_list";
    }

    private List<ApplicationForLeave> getAllRelevantApplicationsForLeave() {

        final Person user = personService.getSignedInUser();

        final boolean isHeadOf = user.hasRole(DEPARTMENT_HEAD);
        final boolean isSecondStage = user.hasRole(SECOND_STAGE_AUTHORITY);
        final boolean isBoss = user.hasRole(BOSS);
        final boolean isOffice = user.hasRole(OFFICE);

        if (isBoss || isOffice) {
            // Boss and Office can see all waiting and temporary allowed applications leave
            return getApplicationsForLeaveForBossOrOffice();
        }

        final List<ApplicationForLeave> applicationsForLeave = new ArrayList<>();
        if (isSecondStage) {
            // Department head can see waiting and temporary allowed applications for leave of certain department(s)
            applicationsForLeave.addAll(getApplicationsForLeaveForSecondStageAuthority(user));
        }

        if (isHeadOf) {
            // Department head can see only waiting applications for leave of certain department(s)
            applicationsForLeave.addAll(getApplicationsForLeaveForDepartmentHead(user));
        }

        return applicationsForLeave.stream().filter(distinctByKey(ApplicationForLeave::getId)).collect(toList());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

        final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForBossOrOffice() {

        return getApplicationsByStates(WAITING, TEMPORARY_ALLOWED).stream()
            .map(application -> new ApplicationForLeave(application, calendarService))
            .sorted(dateComparator())
            .collect(toList());
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForDepartmentHead(Person head) {

        final List<Person> members = departmentService.getManagedMembersOfDepartmentHead(head);
        return getApplicationsByStates(WAITING).stream()
            .filter(includeApplicationsOf(members))
            .filter(withoutOwnApplications(head))
            .filter(withoutSecondStageAuthorityApplications())
            .map(application -> new ApplicationForLeave(application, calendarService))
            .sorted(dateComparator())
            .collect(toList());
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForSecondStageAuthority(Person secondStage) {

        final List<Person> members = departmentService.getManagedMembersForSecondStageAuthority(secondStage);
        return getApplicationsByStates(WAITING, TEMPORARY_ALLOWED).stream()
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
}
