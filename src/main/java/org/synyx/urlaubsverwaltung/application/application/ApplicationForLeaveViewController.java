package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

/**
 * Controller for showing applications for leave in a certain state.
 */
@Controller
@RequestMapping("/web")
class ApplicationForLeaveViewController {

    private final ApplicationService applicationService;
    private final WorkDaysCountService workDaysCountService;
    private final DepartmentService departmentService;
    private final PersonService personService;
    private final Clock clock;
    private final MessageSource messageSource;

    @Autowired
    ApplicationForLeaveViewController(ApplicationService applicationService, WorkDaysCountService workDaysCountService,
                                      DepartmentService departmentService, PersonService personService, Clock clock,
                                      MessageSource messageSource) {
        this.applicationService = applicationService;
        this.workDaysCountService = workDaysCountService;
        this.departmentService = departmentService;
        this.personService = personService;
        this.clock = clock;
        this.messageSource = messageSource;
    }

    @GetMapping("/application")
    public String showApplication(Model model, Locale locale) {

        prepareApplicationModels(model, locale);
        model.addAttribute("activeContent", "application");

        return "thymeleaf/application/application-overview";
    }

    @GetMapping("/application/replacement")
    public String showApplicationWithReplacementContent(Model model, Locale locale) {

        prepareApplicationModels(model, locale);
        model.addAttribute("activeContent", "replacement");

        return "thymeleaf/application/application-overview";
    }

    private void prepareApplicationModels(Model model, Locale locale) {
        final Person signedInUser = personService.getSignedInUser();
        model.addAttribute("signedInUser", signedInUser);

        model.addAttribute("canAddApplicationForAnotherUser", signedInUser.hasRole(OFFICE));
        model.addAttribute("canAccessApplicationStatistics", signedInUser.hasRole(OFFICE) || signedInUser.hasRole(BOSS) || signedInUser.hasRole(DEPARTMENT_HEAD) || signedInUser.hasRole(SECOND_STAGE_AUTHORITY));
        model.addAttribute("canAccessCancellationRequests", signedInUser.hasRole(OFFICE));

        final List<ApplicationForLeave> userApplications = getApplicationsForLeaveForUser(signedInUser);
        final List<ApplicationForLeaveDto> userApplicationsDtos = mapToApplicationForLeaveDtoList(userApplications, signedInUser, locale);
        model.addAttribute("userApplications", userApplicationsDtos);

        final List<ApplicationForLeave> otherApplications = getOtherRelevantApplicationsForLeave(signedInUser);
        final List<ApplicationForLeaveDto> otherApplicationsDtos = mapToApplicationForLeaveDtoList(otherApplications, signedInUser, locale);
        model.addAttribute("otherApplications", otherApplicationsDtos);

        if (signedInUser.hasRole(OFFICE)) {
            final List<ApplicationForLeave> applicationsForLeaveCancellationRequests = getAllRelevantApplicationsForLeaveCancellationRequests(signedInUser);
            final List<ApplicationForLeaveDto> cancellationDtoList = mapToApplicationForLeaveDtoList(applicationsForLeaveCancellationRequests, signedInUser, locale);
            model.addAttribute("applications_cancellation_request", cancellationDtoList);
        }

        final LocalDate holidayReplacementForDate = LocalDate.now(clock);
        final List<ApplicationReplacementDto> replacements = getHolidayReplacements(signedInUser, holidayReplacementForDate, locale);
        model.addAttribute("applications_holiday_replacements", replacements);
    }

    private List<ApplicationForLeaveDto> mapToApplicationForLeaveDtoList(List<ApplicationForLeave> applications, Person signedInUser, Locale locale) {
        return applications.stream()
            .map(applicationForLeave -> toView(applicationForLeave, signedInUser, messageSource, locale))
            .collect(toList());
    }

    private static ApplicationForLeaveDto toView(ApplicationForLeave application, Person signedInUser, MessageSource messageSource, Locale locale) {
        final Person person = application.getPerson();
        final boolean isWaiting = application.hasStatus(WAITING);
        final boolean isAllowed = application.hasStatus(WAITING);
        final boolean isTemporaryAllowed = application.hasStatus(WAITING);
        final boolean isCancellationRequested = application.hasStatus(ALLOWED_CANCELLATION_REQUESTED);
        final boolean twoStageApproval = application.isTwoStageApproval();

        final boolean isBoss = signedInUser.hasRole(BOSS);
        final boolean isOffice = signedInUser.hasRole(OFFICE);
        final boolean isDepartmentHead = signedInUser.hasRole(DEPARTMENT_HEAD);
        final boolean isSecondStageAuthority = signedInUser.hasRole(SECOND_STAGE_AUTHORITY);
        final boolean isOwn = person.equals(signedInUser);
        final boolean isAllowedToAllow = isBoss || ((isDepartmentHead || isSecondStageAuthority) && !isOwn);

        return ApplicationForLeaveDto.builder()
            .id(application.getId())
            .person(toViewPerson(person))
            .vacationType(toViewVacationType(application.getVacationType()))
            .duration(toDurationString(application.getHours(), messageSource, locale))
            .dayLength(application.getDayLength())
            .workDays(decimalToString(application.getWorkDays(), locale))
            .statusWaiting(isWaiting)
            .editAllowed(isWaiting && isOwn)
            .approveAllowed(isAllowedToAllow)
            .temporaryApproveAllowed(twoStageApproval && isWaiting && (isDepartmentHead && !isOwn) && !isBoss && !isSecondStageAuthority)
            .rejectAllowed(!isOwn && (isBoss || isDepartmentHead || isSecondStageAuthority))
            .canCancel(isOwn || (isOffice && (isWaiting || isAllowed || isTemporaryAllowed || isCancellationRequested)))
            .cancellationRequested(isCancellationRequested)
            .durationOfAbsenceDescription(toDurationOfAbsenceDescription(application, messageSource, locale))
            .build();
    }

    private static String toDurationOfAbsenceDescription(Application application, MessageSource messageSource, Locale locale) {
        final String timePattern = messageSource.getMessage("pattern.time", new Object[]{}, locale);
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", locale);
        final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timePattern, locale);

        final LocalDate startDate = application.getStartDate();
        final LocalDate endDate = application.getEndDate();

        final String dateStartString = startDate.format(dateFormatter);

        if (startDate.isEqual(endDate)) {
            final ZonedDateTime startDateWithTime = application.getStartDateWithTime();
            final ZonedDateTime endDateWithTime = application.getEndDateWithTime();

            if (startDateWithTime != null && endDateWithTime != null) {
                final String startTime = startDateWithTime.format(timeFormatter);
                final String endTime = endDateWithTime.format(timeFormatter);
                return messageSource.getMessage("absence.period.singleDay.withStartAndEndTime", new Object[]{dateStartString, startTime, endTime}, locale);
            }

            final DayLength dayLength = application.getDayLength();
            final String dayLengthText = dayLength == null ? "" : messageSource.getMessage(dayLength.name(), new Object[]{}, locale);
            return messageSource.getMessage("absence.period.singleDay", new Object[]{dateStartString, dayLengthText}, locale);
        }

        final String dateEndString = endDate.format(dateFormatter);
        return messageSource.getMessage("absence.period.multipleDays", new Object[]{dateStartString, dateEndString}, locale);
    }

    private static ApplicationPersonDto toViewPerson(Person person) {
        return new ApplicationPersonDto(person.getNiceName(), person.getGravatarURL());
    }

    private static ApplicationForLeaveDto.VacationType toViewVacationType(VacationTypeEntity vacationType) {
        return new ApplicationForLeaveDto.VacationType(vacationType.getCategory().name(), vacationType.getMessageKey());
    }

    private static String toDurationString(Duration javaTimeDuration, MessageSource messageSource, Locale locale) {
        if (javaTimeDuration == null) {
            return "";
        }

        final boolean negative = javaTimeDuration.isNegative();
        final int hours = javaTimeDuration.abs().toHoursPart();
        final int minutes = javaTimeDuration.abs().toMinutesPart();

        String value = "";

        if (hours > 0) {
            value += hours + " " + messageSource.getMessage("hours.abbr", new Object[]{}, locale);
        }

        if (minutes > 0) {
            if (hours > 0) {
                value += " ";
            }
            value += minutes + " " + messageSource.getMessage("minutes.abbr", new Object[]{}, locale);
        }

        if (hours == 0 && minutes == 0) {
            value = messageSource.getMessage("overtime.person.zero", new Object[]{}, locale);
        }

        return negative ? "-" + value : value;
    }

    private static String decimalToString(BigDecimal decimal, Locale locale) {
        if (decimal == null) {
            return "";
        }
        final DecimalFormatSymbols symbol = new DecimalFormatSymbols(locale);
        return new DecimalFormat("#0.##", symbol).format(decimal);
    }

    private List<ApplicationReplacementDto> getHolidayReplacements(Person holidayReplacement, LocalDate holidayReplacementForDate, Locale locale) {
        return applicationService.getForHolidayReplacement(holidayReplacement, holidayReplacementForDate)
            .stream()
            .sorted(comparing(Application::getStartDate))
            .map(application -> toApplicationReplacementDto(application, holidayReplacement, locale))
            .collect(toList());
    }

    private List<ApplicationForLeave> getAllRelevantApplicationsForLeaveCancellationRequests(Person signedInUser) {

        List<Application> cancellationRequests;
        if (signedInUser.hasRole(OFFICE)) {
            cancellationRequests = applicationService.getForStates(List.of(ALLOWED_CANCELLATION_REQUESTED));
        } else {
            cancellationRequests = List.of();
        }

        return cancellationRequests.stream()
            .filter(withoutApplicationsOf(signedInUser))
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .collect(toList());
    }

    private List<ApplicationForLeave> getOtherRelevantApplicationsForLeave(Person signedInUser) {

        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            // Boss and Office can see all waiting and temporary allowed applications leave
            return getApplicationsForLeaveForBossOrOffice().stream()
                .filter(withoutApplicationsOf(signedInUser))
                .collect(toList());
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
        final List<ApplicationStatus> states = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED);

        return applicationService.getForStatesAndPerson(states, List.of(user)).stream()
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .collect(toList());
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForDepartmentHead(Person head) {
        final List<Person> members = departmentService.getMembersForDepartmentHead(head);
        return applicationService.getForStatesAndPerson(List.of(WAITING), members).stream()
            .filter(withoutApplicationsOf(head))
            .filter(withoutSecondStageAuthorityApplications())
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .collect(toList());
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForSecondStageAuthority(Person secondStage) {
        final List<Person> members = departmentService.getMembersForSecondStageAuthority(secondStage);
        return applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), members).stream()
            .filter(withoutApplicationsOf(secondStage))
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .collect(toList());
    }

    private Predicate<Application> withoutApplicationsOf(Person person) {
        return application -> !application.getPerson().equals(person);
    }

    private Predicate<Application> withoutSecondStageAuthorityApplications() {
        return application -> !application.getPerson().getPermissions().contains(SECOND_STAGE_AUTHORITY);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private ApplicationReplacementDto toApplicationReplacementDto(Application application, Person holidayReplacementPerson, Locale locale) {
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
            .person(toViewPerson(applicationPerson))
            .note(note)
            .pending(pending)
            .duration(toDurationString(application.getHours(), messageSource, locale))
            .workDays(decimalToString(workDays, locale))
            .durationOfAbsenceDescription(toDurationOfAbsenceDescription(application, messageSource, locale))
            .dayLength(dayLength)
            .build();
    }
}
