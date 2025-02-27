package org.synyx.urlaubsverwaltung.application.application;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SubmittedSickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SubmittedSickNoteService;
import org.synyx.urlaubsverwaltung.util.DurationFormatter;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
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
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_EDIT;

/**
 * Controller for showing applications for leave in a certain state.
 */
@Controller
@RequestMapping("/web")
class ApplicationForLeaveViewController implements HasLaunchpad {

    private final ApplicationService applicationService;
    private final SubmittedSickNoteService sickNoteService;
    private final WorkDaysCountService workDaysCountService;
    private final DepartmentService departmentService;
    private final PersonService personService;
    private final SettingsService settingsService;
    private final Clock clock;
    private final MessageSource messageSource;

    ApplicationForLeaveViewController(
        ApplicationService applicationService, SubmittedSickNoteService sickNoteService, WorkDaysCountService workDaysCountService,
        DepartmentService departmentService, PersonService personService, SettingsService settingsService, Clock clock,
        MessageSource messageSource
    ) {
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
        this.workDaysCountService = workDaysCountService;
        this.departmentService = departmentService;
        this.personService = personService;
        this.settingsService = settingsService;
        this.clock = clock;
        this.messageSource = messageSource;
    }

    @GetMapping("/application")
    public String showApplication(Model model, Locale locale) {
        prepareApplicationModels(model, locale, Tab.APPLICATION);
        return "application/application-overview";
    }

    @GetMapping("/application/replacement")
    public String showApplicationWithReplacementContent(Model model, Locale locale) {
        prepareApplicationModels(model, locale, Tab.REPLACEMENT);
        return "application/application-overview";
    }

    @GetMapping("/sicknote/submitted")
    public String showApplicationWithSickNoteSubmittedContent(Model model, Locale locale) {
        prepareApplicationModels(model, locale, Tab.SICK_NOTE);
        return "application/application-overview";
    }

    private void prepareApplicationModels(Model model, Locale locale, Tab activeTab) {

        final SickNoteSettings sickNoteSettings = settingsService.getSettings().getSickNoteSettings();

        final Person signedInUser = personService.getSignedInUser();
        model.addAttribute("signedInUser", signedInUser);

        model.addAttribute("canAccessApplicationStatistics", isAllowedToAccessApplicationStatistics(signedInUser));
        model.addAttribute("canAccessCancellationRequests", isAllowedToAccessCancellationRequest(signedInUser));
        model.addAttribute("canAccessOtherApplications", isAllowedToAccessOtherApplications(signedInUser));
        model.addAttribute("canAccessSickNoteSubmissions", sickNoteSettings.getUserIsAllowedToSubmitSickNotes() && isAllowedToAccessSickNoteSubmissions(signedInUser));

        final List<Person> membersAsDepartmentHead = signedInUser.hasRole(DEPARTMENT_HEAD) ? departmentService.getMembersForDepartmentHead(signedInUser) : List.of();
        final List<Person> membersAsSecondStageAuthority = signedInUser.hasRole(SECOND_STAGE_AUTHORITY) ? departmentService.getMembersForSecondStageAuthority(signedInUser) : List.of();

        // prepare everything as we don't know whether to render 'userApplications' or 'userHolidayReplacements'
        // when activeTab matches 'submitted sick notes' for instance.
        // however, we could consider the referer header. feel free to improve this :-)
        prepareUserApplications(model, signedInUser, membersAsDepartmentHead, membersAsSecondStageAuthority, locale);
        prepareUserHolidayReplacements(model, signedInUser, locale);
        prepareOtherApplications(model, signedInUser, membersAsDepartmentHead, membersAsSecondStageAuthority, locale);
        prepareOtherSubmittedSickNotes(model, signedInUser, locale);
        prepareApplicationCancellationRequests(model, signedInUser, membersAsDepartmentHead, membersAsSecondStageAuthority, locale);

        model.addAttribute("activeContent", activeTab.name);
    }

    private void prepareUserApplications(Model model, Person signedInUser, List<Person> membersAsDepartmentHead, List<Person> membersAsSecondStageAuthority, Locale locale) {
        final List<ApplicationForLeave> userApplications = getApplicationsForLeaveForUser(signedInUser);
        final List<ApplicationForLeaveDto> userApplicationsDtos = mapToApplicationForLeaveDtoList(userApplications, signedInUser, membersAsDepartmentHead, membersAsSecondStageAuthority, locale);
        model.addAttribute("userApplications", userApplicationsDtos);
    }

    private void prepareUserHolidayReplacements(Model model, Person signedInUser, Locale locale) {
        final LocalDate holidayReplacementForDate = LocalDate.now(clock);
        final List<ApplicationReplacementDto> replacements = getHolidayReplacements(signedInUser, holidayReplacementForDate, locale);
        model.addAttribute("applications_holiday_replacements", replacements);
    }

    private void prepareOtherApplications(Model model, Person signedInUser, List<Person> membersAsDepartmentHead, List<Person> membersAsSecondStageAuthority, Locale locale) {
        final List<ApplicationForLeave> otherApplications = getOtherRelevantApplicationsForLeave(signedInUser, membersAsDepartmentHead, membersAsSecondStageAuthority);
        final List<ApplicationForLeaveDto> otherApplicationsDtos = mapToApplicationForLeaveDtoList(otherApplications, signedInUser, membersAsDepartmentHead, membersAsSecondStageAuthority, locale);
        model.addAttribute("otherApplications", otherApplicationsDtos);
    }

    private void prepareOtherSubmittedSickNotes(Model model, Person signedInUser, Locale locale) {
        final List<SubmittedSickNote> sickNotes = sickNoteService.findSubmittedSickNotes(getPersonsForRelevantSubmittedSickNotes(signedInUser));
        final List<SubmittedSickNoteDto> otherSickNotesDtos = mapToSickNoteDtoList(sickNotes, locale);
        model.addAttribute("otherSickNotes", otherSickNotesDtos);
    }

    private void prepareApplicationCancellationRequests(Model model, Person signedInUser, List<Person> membersAsDepartmentHead, List<Person> membersAsSecondStageAuthority, Locale locale) {
        final List<ApplicationForLeave> applicationsForLeaveCancellationRequests = getAllRelevantApplicationsForLeaveCancellationRequests(signedInUser, membersAsDepartmentHead, membersAsSecondStageAuthority);
        final List<ApplicationForLeaveDto> cancellationDtoList = mapToApplicationForLeaveDtoList(applicationsForLeaveCancellationRequests, signedInUser, membersAsDepartmentHead, membersAsSecondStageAuthority, locale);
        if (!cancellationDtoList.isEmpty()) {
            model.addAttribute("applications_cancellation_request", cancellationDtoList);
        }
    }

    private static boolean isAllowedToAccessApplicationStatistics(Person signedInUser) {
        return signedInUser.hasAnyRole(OFFICE, BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
    }

    private static boolean isAllowedToAccessCancellationRequest(Person signedInUser) {
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(APPLICATION_CANCELLATION_REQUESTED) && (signedInUser.hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)));
    }

    private static boolean isAllowedToAccessOtherApplications(Person signedInUser) {
        return signedInUser.hasAnyRole(OFFICE, BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
    }

    private static boolean isAllowedToAccessSickNoteSubmissions(Person signedInUser) {
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(SICK_NOTE_EDIT) && (signedInUser.hasAnyRole(BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)));
    }

    private List<SubmittedSickNoteDto> mapToSickNoteDtoList(List<SubmittedSickNote> sickNotes, Locale locale) {
        return sickNotes.stream()
            .map(sickNote -> toView(sickNote, messageSource, locale))
            .toList();
    }

    private static SubmittedSickNoteDto toView(SubmittedSickNote submittedSickNote, MessageSource messageSource, Locale locale) {
        final SickNote sickNote = submittedSickNote.sickNote();
        final Person person = sickNote.getPerson();
        return new SubmittedSickNoteDto(
            sickNote.getId().toString(),
            sickNote.getWorkDays(),
            new SickNotePersonDto(person.getNiceName(), person.getInitials(), person.getGravatarURL(), person.isInactive(), sickNote.getId()),
            sickNote.getSickNoteType().getMessageKey(),
            toDurationOfAbsenceDescription(submittedSickNote, messageSource, locale),
            submittedSickNote.extensionSubmitted(),
            submittedSickNote.additionalWorkdays().orElse(null)
        );
    }

    private List<ApplicationForLeaveDto> mapToApplicationForLeaveDtoList(List<ApplicationForLeave> applications, Person signedInUser, List<Person> membersAsDepartmentHead,
                                                                         List<Person> membersAsSecondStageAuthority, Locale locale) {

        return applications.stream()
            .map(applicationForLeave -> {
                final boolean allowedToAccessPersonData = departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, applicationForLeave.getPerson());
                return toView(applicationForLeave, signedInUser, membersAsDepartmentHead, membersAsSecondStageAuthority, messageSource, locale, allowedToAccessPersonData);
            })
            .toList();
    }

    private static ApplicationForLeaveDto toView(ApplicationForLeave application, Person signedInUser, List<Person> membersAsDepartmentHead,
                                                 List<Person> membersAsSecondStageAuthority, MessageSource messageSource, Locale locale, boolean allowedToAccessPersonData) {
        final Person person = application.getPerson();

        final boolean isWaiting = application.hasStatus(WAITING);
        final boolean isAllowed = application.hasStatus(ALLOWED);
        final boolean isTemporaryAllowed = application.hasStatus(TEMPORARY_ALLOWED);
        final boolean isCancellationRequested = application.hasStatus(ALLOWED_CANCELLATION_REQUESTED);
        final boolean twoStageApproval = application.isTwoStageApproval();

        final boolean isBoss = signedInUser.hasRole(BOSS);
        final boolean isOffice = signedInUser.hasRole(OFFICE);
        final boolean isDepartmentHeadOfPerson = membersAsDepartmentHead.contains(person);
        final boolean isSecondStageAuthorityOfPerson = membersAsSecondStageAuthority.contains(person);
        final boolean isOwn = person.equals(signedInUser);

        final boolean isAllowedToEdit = isWaiting && isOwn;
        final boolean isAllowedToTemporaryApprove = twoStageApproval && isWaiting && (isDepartmentHeadOfPerson && !isOwn) && !isBoss && !isSecondStageAuthorityOfPerson;
        final boolean isAllowedToApprove = (isWaiting && (isBoss || ((isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson) && !isOwn))) || (isTemporaryAllowed && (isBoss || (isSecondStageAuthorityOfPerson && !isOwn)));
        final boolean isAllowedToCancel = ((isWaiting || isTemporaryAllowed || isAllowed) && isOwn) || ((isWaiting || isTemporaryAllowed || isAllowed || isCancellationRequested) && isOffice);
        final boolean isAllowedToReject = (isWaiting || isTemporaryAllowed) && !isOwn && (isBoss || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson);

        return ApplicationForLeaveDto.builder()
            .id(application.getId())
            .person(toViewPerson(person, allowedToAccessPersonData))
            .vacationType(toViewVacationType(application.getVacationType(), locale))
            .status(application.getStatus())
            .duration(DurationFormatter.toDurationString(application.getHours(), messageSource, locale))
            .dayLength(application.getDayLength())
            .workDays(application.getWorkDays())
            .statusWaiting(isWaiting)
            .editAllowed(isAllowedToEdit)
            .approveAllowed(isAllowedToApprove)
            .temporaryApproveAllowed(isAllowedToTemporaryApprove)
            .rejectAllowed(isAllowedToReject)
            .cancelAllowed(isAllowedToCancel)
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

    private static String toDurationOfAbsenceDescription(SubmittedSickNote submittedSickNote, MessageSource messageSource, Locale locale) {
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", locale);

        final LocalDate startDate = submittedSickNote.startDate();
        final LocalDate endDate = submittedSickNote.nextEndDate();

        final String dateStartString = startDate.format(dateFormatter);

        if (startDate.isEqual(endDate)) {
            final DayLength dayLength = submittedSickNote.sickNote().getDayLength();
            final String dayLengthText = dayLength == null ? "" : messageSource.getMessage(dayLength.name(), new Object[]{}, locale);
            return messageSource.getMessage("absence.period.singleDay", new Object[]{dateStartString, dayLengthText}, locale);
        }

        final String dateEndString = endDate.format(dateFormatter);
        return messageSource.getMessage("absence.period.multipleDays", new Object[]{dateStartString, dateEndString}, locale);
    }

    private static ApplicationPersonDto toViewPerson(Person person, boolean allowedToAccessPersonData) {
        final Long id = allowedToAccessPersonData ? person.getId() : null;
        return new ApplicationPersonDto(person.getNiceName(), person.getInitials(), person.getGravatarURL(), person.isInactive(), id);
    }

    private static ApplicationForLeaveDto.VacationTypeDto toViewVacationType(VacationType<?> vacationType, Locale locale) {
        return new ApplicationForLeaveDto.VacationTypeDto(vacationType.getCategory().name(), vacationType.getLabel(locale), vacationType.getColor());
    }

    private List<ApplicationReplacementDto> getHolidayReplacements(Person signedInUser, LocalDate holidayReplacementForDate, Locale locale) {
        return applicationService.getForHolidayReplacement(signedInUser, holidayReplacementForDate)
            .stream()
            .sorted(comparing(Application::getStartDate))
            .map(application -> {
                final boolean allowedToAccessPersonData = departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, application.getPerson());
                return toApplicationReplacementDto(application, signedInUser, locale, allowedToAccessPersonData);
            })
            .toList();
    }

    private List<ApplicationForLeave> getAllRelevantApplicationsForLeaveCancellationRequests(Person signedInUser, List<Person> membersAsDepartmentHead, List<Person> membersAsSecondStageAuthority) {

        if (!signedInUser.hasRole(OFFICE) && !signedInUser.hasRole(APPLICATION_CANCELLATION_REQUESTED)) {
            return List.of();
        }

        final List<Application> cancellationRequests = new ArrayList<>();
        if (signedInUser.hasRole(OFFICE) || (signedInUser.hasRole(BOSS) && signedInUser.hasRole(APPLICATION_CANCELLATION_REQUESTED))) {
            cancellationRequests.addAll(applicationService.getForStates(List.of(ALLOWED_CANCELLATION_REQUESTED)));
        } else {
            if (signedInUser.hasRole(SECOND_STAGE_AUTHORITY) && signedInUser.hasRole(APPLICATION_CANCELLATION_REQUESTED)) {
                cancellationRequests.addAll(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), membersAsSecondStageAuthority));
            }

            if (signedInUser.hasRole(DEPARTMENT_HEAD) && signedInUser.hasRole(APPLICATION_CANCELLATION_REQUESTED)) {
                cancellationRequests.addAll(applicationService.getForStatesAndPerson(List.of(ALLOWED_CANCELLATION_REQUESTED), membersAsDepartmentHead));
            }
        }

        return cancellationRequests.stream()
            .distinct()
            .filter(withoutApplicationsOf(signedInUser))
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .toList();
    }

    private List<ApplicationForLeave> getOtherRelevantApplicationsForLeave(Person signedInUser, List<Person> membersAsDepartmentHead, List<Person> membersAsSecondStageAuthority) {

        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            // Boss and Office can see all waiting and temporary allowed applications leave
            return getApplicationsForLeaveForBossOrOffice().stream()
                .filter(withoutApplicationsOf(signedInUser))
                .toList();
        }

        final List<ApplicationForLeave> applicationsForLeave = new ArrayList<>();

        if (signedInUser.hasRole(SECOND_STAGE_AUTHORITY)) {
            // Department head can see waiting and temporary allowed applications for leave of certain department(s)
            applicationsForLeave.addAll(getApplicationsForLeaveForSecondStageAuthority(signedInUser, membersAsSecondStageAuthority));
        }

        if (signedInUser.hasRole(DEPARTMENT_HEAD)) {
            // Department head can see only waiting applications for leave of certain department(s)
            applicationsForLeave.addAll(getApplicationsForLeaveForDepartmentHead(signedInUser, membersAsDepartmentHead));
        }

        return applicationsForLeave.stream()
            .filter(distinctByKey(ApplicationForLeave::getId))
            .toList();
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForBossOrOffice() {
        return applicationService.getForStates(List.of(WAITING, TEMPORARY_ALLOWED)).stream()
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .toList();
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForUser(Person user) {
        final List<ApplicationStatus> states = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED);

        return applicationService.getForStatesAndPerson(states, List.of(user)).stream()
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .toList();
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForDepartmentHead(Person head, List<Person> members) {
        return applicationService.getForStatesAndPerson(List.of(WAITING), members).stream()
            .filter(withoutApplicationsOf(head))
            .filter(withoutSecondStageAuthorityApplications())
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .toList();
    }

    private List<ApplicationForLeave> getApplicationsForLeaveForSecondStageAuthority(Person secondStage, List<Person> members) {
        return applicationService.getForStatesAndPerson(List.of(WAITING, TEMPORARY_ALLOWED), members).stream()
            .filter(withoutApplicationsOf(secondStage))
            .map(application -> new ApplicationForLeave(application, workDaysCountService))
            .sorted(comparing(ApplicationForLeave::getStartDate))
            .toList();
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

    private ApplicationReplacementDto toApplicationReplacementDto(Application application, Person holidayReplacementPerson, Locale locale, boolean allowedToAccessPersonData) {
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
            .person(toViewPerson(applicationPerson, allowedToAccessPersonData))
            .note(note)
            .pending(pending)
            .duration(DurationFormatter.toDurationString(application.getHours(), messageSource, locale))
            .workDays(workDays)
            .durationOfAbsenceDescription(toDurationOfAbsenceDescription(application, messageSource, locale))
            .dayLength(dayLength)
            .build();
    }

    private List<Person> getPersonsForRelevantSubmittedSickNotes(Person signedInUser) {

        if (signedInUser.hasRole(OFFICE) || (signedInUser.hasRole(BOSS) && signedInUser.hasRole(SICK_NOTE_EDIT))) {
            return personService.getActivePersons().stream().filter(person -> !person.equals(signedInUser)).toList();
        }

        final List<Person> membersForDepartmentHead = signedInUser.hasRole(DEPARTMENT_HEAD) && signedInUser.hasRole(SICK_NOTE_EDIT)
            ? departmentService.getMembersForDepartmentHead(signedInUser)
            : List.of();

        final List<Person> memberForSecondStageAuthority = signedInUser.hasRole(SECOND_STAGE_AUTHORITY) && signedInUser.hasRole(SICK_NOTE_EDIT)
            ? departmentService.getMembersForSecondStageAuthority(signedInUser)
            : List.of();

        return Stream.concat(memberForSecondStageAuthority.stream(), membersForDepartmentHead.stream())
            .filter(person -> !person.hasRole(INACTIVE))
            .distinct()
            .sorted(comparing(Person::getFirstName).thenComparing(Person::getLastName))
            .toList();
    }

    private enum Tab {
        APPLICATION("application"),
        REPLACEMENT("replacement"),
        SICK_NOTE("sicknote");

        private final String name;

        Tab(String name) {
            this.name = name;
        }
    }
}
