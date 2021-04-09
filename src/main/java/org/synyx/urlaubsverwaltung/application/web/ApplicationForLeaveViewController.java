package org.synyx.urlaubsverwaltung.application.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.application.dao.HolidayReplacementEntity;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.WeekDay;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Controller for showing applications for leave in a certain state.
 */
@Controller
@RequestMapping("/web")
public class ApplicationForLeaveViewController {

    private final ApplicationService applicationService;
    private final WorkDaysCountService workDaysCountService;
    private final DepartmentService departmentService;
    private final PersonService personService;
    private final Clock clock;

    @Autowired
    public ApplicationForLeaveViewController(ApplicationService applicationService, WorkDaysCountService workDaysCountService,
                                             DepartmentService departmentService, PersonService personService, Clock clock) {
        this.applicationService = applicationService;
        this.workDaysCountService = workDaysCountService;
        this.departmentService = departmentService;
        this.personService = personService;
        this.clock = clock;
    }

    /*
     * Show waiting applications for leave.
     */
    @GetMapping("/application")
    public String showWaiting(Model model) {

        final Person signedInUser = personService.getSignedInUser();
        model.addAttribute("signedInUser", signedInUser);

        final List<ApplicationForLeave> applicationsForLeave = getAllRelevantApplicationsForLeave(signedInUser);
        model.addAttribute("applications", applicationsForLeave);

        final List<ApplicationForLeave> applicationsForLeaveCancellationRequests = getAllRelevantApplicationsForLeaveCancellationRequests();
        model.addAttribute("applications_cancellation_request", applicationsForLeaveCancellationRequests);

        final LocalDate holidayReplacementForDate = LocalDate.now(clock);
        final List<ApplicationReplacementDto> replacements = getHolidayReplacements(signedInUser, holidayReplacementForDate);
        model.addAttribute("applications_holiday_replacements", replacements);

        return "application/app_list";
    }

    private List<ApplicationReplacementDto> getHolidayReplacements(Person holidayReplacement, LocalDate holidayReplacementForDate) {
        return applicationService.getForHolidayReplacement(holidayReplacement, holidayReplacementForDate)
            .stream()
            .map(application -> toApplicationReplacementDto(application, holidayReplacement))
            .sorted(comparing(ApplicationReplacementDto::getStartDate))
            .collect(toList());
    }

    private List<ApplicationForLeave> getAllRelevantApplicationsForLeaveCancellationRequests() {

        final Person signedInUser = personService.getSignedInUser();

        List<Application> cancellationRequests;
        if (signedInUser.hasRole(OFFICE)) {
            cancellationRequests = applicationService.getForStates(List.of(ALLOWED_CANCELLATION_REQUESTED));
        } else {
            cancellationRequests = applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), List.of(signedInUser));
        }

        return cancellationRequests.stream()
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .collect(toList());
    }

    private List<ApplicationForLeave> getAllRelevantApplicationsForLeave(Person signedInUser) {

        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            // Boss and Office can see all waiting and temporary allowed applications leave
            return getApplicationsForLeaveForBossOrOffice();
        }

        final List<ApplicationForLeave> applicationsForLeave = new ArrayList<>();

        if (signedInUser.hasRole(SECOND_STAGE_AUTHORITY)) {
            // Department head can see waiting and temporary allowed applications for leave of certain department(s)
            applicationsForLeave.addAll(getApplicationsForLeaveForSecondStageAuthority(signedInUser));
        }

        if (signedInUser.hasRole(DEPARTMENT_HEAD)) {
            // Department head can see only waiting applications for leave of certain department(s)
            applicationsForLeave.addAll(getApplicationsForLeaveForDepartmentHead(signedInUser));
        }

        if (signedInUser.hasRole(USER)) {
            // Department head can see only waiting applications for leave of certain department(s)
            applicationsForLeave.addAll(getApplicationsForLeaveForUser(signedInUser));
        }

        return applicationsForLeave.stream()
            .filter(distinctByKey(ApplicationForLeave::getId))
            .collect(toList());
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForBossOrOffice() {
        return applicationService.getForStates(List.of(WAITING, TEMPORARY_ALLOWED)).stream()
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .collect(toList());
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForUser(Person user) {
        return applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), List.of(user)).stream()
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .collect(toList());
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForDepartmentHead(Person head) {
        final List<Person> members = departmentService.getManagedMembersOfDepartmentHead(head);
        return applicationService.getForStatesAndPerson(List.of(WAITING), members).stream()
            .filter(withoutApplicationsOf(head))
            .filter(withoutSecondStageAuthorityApplications())
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .collect(toList());
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForSecondStageAuthority(Person secondStage) {
        final List<Person> members = departmentService.getManagedMembersForSecondStageAuthority(secondStage);
        return applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), members).stream()
            .filter(withoutApplicationsOf(secondStage))
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .collect(toList());
    }

    private Predicate<Application> withoutApplicationsOf(Person head) {
        return application -> !application.getPerson().equals(head);
    }

    private Predicate<Application> withoutSecondStageAuthorityApplications() {
        return application -> !application.getPerson().getPermissions().contains(SECOND_STAGE_AUTHORITY);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private ApplicationReplacementDto toApplicationReplacementDto(Application application, Person holidayReplacementPerson) {
        final DayLength dayLength = application.getDayLength();
        final LocalDate startDate = application.getStartDate();
        final LocalDate endDate = application.getEndDate();
        final Person applicationPerson = application.getPerson();
        final BigDecimal workDays = workDaysCountService.getWorkDaysCount(dayLength, startDate, endDate, applicationPerson);

        final String note = application.getHolidayReplacements().stream()
            .filter(holidayReplacementEntity -> holidayReplacementEntity.getPerson().equals(holidayReplacementPerson))
            .findFirst()
            .map(HolidayReplacementEntity::getNote)
            .orElse("");

        final boolean pending = WAITING.equals(application.getStatus()) || TEMPORARY_ALLOWED.equals(application.getStatus());

        return ApplicationReplacementDto.builder()
            .personGravatarURL(applicationPerson.getGravatarURL())
            .personName(applicationPerson.getNiceName())
            .note(note)
            .pending(pending)
            .hours(application.getHours())
            .workDays(workDays)
            .startDate(startDate)
            .endDate(endDate)
            .startTime(application.getStartTime())
            .endTime(application.getEndTime())
            .startDateWithTime(application.getStartDateWithTime())
            .endDateWithTime(application.getEndDateWithTime())
            .dayLength(dayLength)
            .weekDayOfStartDate(toWeekDay(startDate))
            .weekDayOfEndDate(toWeekDay(endDate))
            .build();
    }

    private static WeekDay toWeekDay(LocalDate localDate) {
        return WeekDay.getByDayOfWeek(localDate.getDayOfWeek().getValue());
    }
}
