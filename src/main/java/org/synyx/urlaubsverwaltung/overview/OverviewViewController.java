package org.synyx.urlaubsverwaltung.overview;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.HolidayAccountVacationDays;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationForLeave;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Comparator.comparing;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.web.util.UriUtils.encodeQueryParam;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToCancelApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToCancelDirectlyApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToEditApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToRevokeApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToStartCancellationRequest;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeType.EXTERNAL;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_CANCEL;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;

/**
 * Controller to display the personal overview page with basic information about
 * overtime, applications for leave and sick notes.
 */
@Controller
@RequestMapping("/")
public class OverviewViewController implements HasLaunchpad {

    private static final String PERSON_ATTRIBUTE = "person";
    private static final int NUMBER_OF_PAST_APPLICATION_ON_OVERVIEW = 1;
    private static final int NUMBER_OF_FUTR_APPLICATION_ON_OVERVIEW = 5;
    private static final int NUMBER_OF_PAST_SICK_NOTES_ON_OVERVIEW = 3;
    private static final int NUMBER_OF_FUTR_SICK_NOTES_ON_OVERVIEW = 1;
    private static final int NUMBER_OF_PAST_OVERTIMES_ON_OVERVIEW = 3;
    private static final int NUMBER_OF_FUTR_OVERTIMES_ON_OVERVIEW = 1;

    private final PersonService personService;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final WorkDaysCountService workDaysCountService;
    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;
    private final OvertimeService overtimeService;
    private final SettingsService settingsService;
    private final DepartmentService departmentService;
    private final VacationTypeViewModelService vacationTypeViewModelService;
    private final Clock clock;

    @Autowired
    public OverviewViewController(
        PersonService personService, AccountService accountService,
        VacationDaysService vacationDaysService,
        WorkDaysCountService workDaysCountService, ApplicationService applicationService,
        SickNoteService sickNoteService, OvertimeService overtimeService,
        SettingsService settingsService, DepartmentService departmentService,
        VacationTypeViewModelService vacationTypeViewModelService, Clock clock
    ) {
        this.personService = personService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.workDaysCountService = workDaysCountService;
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
        this.overtimeService = overtimeService;
        this.settingsService = settingsService;
        this.departmentService = departmentService;
        this.vacationTypeViewModelService = vacationTypeViewModelService;
        this.clock = clock;
    }

    @GetMapping
    public String index() {
        return "redirect:/web/overview";
    }

    @GetMapping("/web/overview")
    public String showOverview(
        @RequestParam(value = "year", required = false) String year
    ) {
        final Person signedInUser = personService.getSignedInUser();
        return format("redirect:/web/person/%d/overview%s",
            signedInUser.getId(),
            hasText(year) ? "?year=" + encodeQueryParam(year, UTF_8) : ""
        );
    }

    @GetMapping("/web/person/{personId}/overview")
    public String showOverview(
        @PathVariable("personId") Long personId,
        @RequestParam(value = "year", required = false) Integer year,
        Model model, Locale locale
    ) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        final Person signedInUser = personService.getSignedInUser();

        model.addAttribute(PERSON_ATTRIBUTE, person);
        model.addAttribute("departmentsOfPerson", departmentService.getAssignedDepartmentsOfMember(person));

        if (!departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            model.addAttribute("canAccessCalendarShare", "false");
            return "person/person-overview-reduced";
        }

        final LocalDate now = LocalDate.now(clock);
        final int yearToShow = year == null ? now.getYear() : year;

        // for calendar.js
        final List<VacationTypeDto> vacationTypeColors = vacationTypeViewModelService.getVacationTypeColors();
        model.addAttribute("vacationTypeColors", vacationTypeColors);

        prepareHolidayAccountInformation(person, yearToShow, now, model);
        prepareApplicationInformation(person, signedInUser, yearToShow, model, locale);
        prepareSickNoteInformation(person, signedInUser, yearToShow, model);
        prepareOvertimeInformation(overtimeService, person, signedInUser, yearToShow, model);

        model.addAttribute("currentYear", now.getYear());
        model.addAttribute("selectedYear", yearToShow);

        model.addAttribute("canAccessCalendarShare", person.equals(signedInUser) || signedInUser.hasRole(OFFICE) || signedInUser.hasRole(BOSS));

        return "person/person-overview";
    }

    private void prepareHolidayAccountInformation(Person person, int year, LocalDate now, Model model) {

        // get person's holidays account and entitlement for the given year
        final Optional<Account> maybeAccount = accountService.getHolidaysAccount(year, person);
        if (maybeAccount.isPresent()) {
            final Account account = maybeAccount.get();
            model.addAttribute("account", account);

            final List<Account> accountNextYear = accountService.getHolidaysAccount(year + 1, person).stream().toList();
            final Map<Account, HolidayAccountVacationDays> accountHolidayAccountVacationDaysMap = vacationDaysService.getVacationDaysLeft(List.of(account), Year.of(year), accountNextYear);
            final VacationDaysLeft vacationDaysLeft = accountHolidayAccountVacationDaysMap.get(account).vacationDaysYear();
            model.addAttribute("vacationDaysLeft", vacationDaysLeft);

            model.addAttribute("vacationDaysLeftDays", vacationDaysLeft.getLeftVacationDays(now, account.doRemainingVacationDaysExpire(), account.getExpiryDate()));
            model.addAttribute("remainingVacationDaysLeftDays", vacationDaysLeft.getRemainingVacationDaysLeft(now, account.doRemainingVacationDaysExpire(), account.getExpiryDate()));

            final BigDecimal expiredRemainingVacationDays = vacationDaysLeft.getExpiredRemainingVacationDays(now, account.getExpiryDate());
            model.addAttribute("showExpiredVacationDays", showExpiredVacationDays(now, account, expiredRemainingVacationDays));
            model.addAttribute("expiredRemainingVacationDays", expiredRemainingVacationDays);
        } else {
            model.addAttribute("showExpiredVacationDays", false);
        }
    }

    private void prepareApplicationInformation(Person person, Person signedInUser, int year, Model model, Locale locale) {
        // get the person's applications for the given year
        final LocalDate startDate = Year.of(year).atDay(1);
        final LocalDate endDate = startDate.with(lastDayOfYear());
        final List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate, person);

        final List<ApplicationDto> applicationsForLeave;
        final ApplicationDaysUsedSummaryDto usedDaysOverview;

        if (applications.isEmpty()) {
            applicationsForLeave = List.of();
            usedDaysOverview = new ApplicationDaysUsedSummaryDto(List.of(), year, workDaysCountService);
        } else {
            final LocalDate today = LocalDate.now(clock);
            final List<ApplicationForLeave> allForLeave = applications.stream()
                .map(application -> new ApplicationForLeave(application, workDaysCountService))
                .toList();
            // always show 3 applications if no application is in the fill with future application
            final List<ApplicationDto> pastApplicationDtos = allForLeave.stream()
                .filter(a -> a.getStartDate().isBefore(today))
                .sorted(comparing(ApplicationForLeave::getStartDate))
                .limit(NUMBER_OF_PAST_APPLICATION_ON_OVERVIEW)
                .map(a -> applicationDto(a, signedInUser, locale))
                .toList();

            final List<ApplicationDto> futureApplicationDtos = allForLeave.stream()
                .filter(a -> !a.getStartDate().isBefore(today))
                .sorted(comparing(ApplicationForLeave::getStartDate).reversed())
                .limit((long) NUMBER_OF_FUTR_APPLICATION_ON_OVERVIEW - pastApplicationDtos.size())
                .map(a -> applicationDto(a, signedInUser, locale))
                .toList();

            applicationsForLeave = Stream.concat(futureApplicationDtos.stream(), pastApplicationDtos.stream()).toList();
            usedDaysOverview = new ApplicationDaysUsedSummaryDto(applications, year, workDaysCountService);
        }

        model.addAttribute("applicationOverviewInformation", new ApplicationOverviewDto(
            applicationsForLeave,
            usedDaysOverview,
            person.equals(signedInUser),
            !person.equals(signedInUser) && (signedInUser.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(signedInUser, APPLICATION_ADD, person)),
            applicationsForLeave.size(),
            applications.size()
        ));
    }

    private void prepareSickNoteInformation(Person person, Person signedInUser, int year, Model model) {

        final LocalDate from = Year.of(year).atDay(1);
        final LocalDate to = from.with(lastDayOfYear());

        final List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, from, to);

        final LocalDate today = LocalDate.now(clock);

        // most of the time there are no future sick notes therefore we fill with past sick notes
        final List<SickNote> futureSickNotes = sickNotes.stream()
            .filter(s -> !s.getStartDate().isBefore(today))
            .sorted(comparing(SickNote::getStartDate))
            .limit(NUMBER_OF_FUTR_SICK_NOTES_ON_OVERVIEW)
            .toList();


        final List<SickNote> pastSickNotes = sickNotes.stream()
            .filter(s -> s.getStartDate().isBefore(today))
            .sorted(comparing(SickNote::getStartDate).reversed())
            .limit((long) NUMBER_OF_PAST_SICK_NOTES_ON_OVERVIEW - futureSickNotes.size())
            .toList();

        final List<SickNote> shownSickNotes = Stream.concat(futureSickNotes.stream(), pastSickNotes.stream()).toList();

        final boolean isSamePerson = person.equals(signedInUser);
        final boolean userIsAllowedToSubmitSickNotes = isSamePerson && settingsService.getSettings().getSickNoteSettings().getUserIsAllowedToSubmitSickNotes();
        final boolean userIsAllowedToAddSickNotes = isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_ADD, person);
        final boolean isAllowedToAddOrSubmitSickNotes =
            signedInUser.hasRole(OFFICE)
                || userIsAllowedToAddSickNotes
                || userIsAllowedToSubmitSickNotes;

        model.addAttribute("sickNotesOverview", new SickNotesOverviewDTO(
            shownSickNotes.stream().map(sickNote -> {
                final boolean isAllowedToEdit =
                    signedInUser.hasRole(OFFICE)
                        || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_EDIT, person)
                        || (isSamePerson && sickNote.isSubmitted());

                final boolean allowedToCancel =
                    signedInUser.hasRole(OFFICE)
                        || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_CANCEL, person);

                return new SickNoteDto(
                    sickNote.getId(),
                    sickNote.getStartDate(),
                    sickNote.getEndDate(),
                    sickNote.getDayLength(),
                    sickNote.isAubPresent(),
                    sickNote.getWorkDays(),
                    sickNote.getWorkDaysWithAub(),
                    sickNote.getStatus(),
                    sickNote.getSickNoteType(),
                    isAllowedToEdit,
                    allowedToCancel
                );
            }).toList(),
            new SickDaysSummaryDto(sickNotes, workDaysCountService, from, to),
            isAllowedToAddOrSubmitSickNotes,
            person.equals(signedInUser) || signedInUser.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_VIEW, person) || departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person) || departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person),
            shownSickNotes.size(),
            sickNotes.size()
        ));
    }

    private void prepareOvertimeInformation(OvertimeService overtimeService, Person person, Person signedInUser, int year, Model model) {

        final List<Overtime> overtimes = overtimeService.getOvertimeRecordsForPersonAndYear(person, year);

        final LocalDate today = LocalDate.now(clock);

        // most of the time there are no future sick notes therefore we fill with past sick notes
        final List<Overtime> futureOvertimes = overtimes.stream()
            .filter(s -> !s.startDate().isBefore(today))
            .sorted(comparing(Overtime::startDate))
            .limit(NUMBER_OF_FUTR_OVERTIMES_ON_OVERVIEW)
            .toList();


        final List<Overtime> pastOvertimes = overtimes.stream()
            .filter(s -> s.startDate().isBefore(today))
            .sorted(comparing(Overtime::startDate).reversed())
            .limit((long) NUMBER_OF_PAST_OVERTIMES_ON_OVERVIEW - futureOvertimes.size())
            .toList();

        final List<Overtime> shownOvertimes = Stream.concat(futureOvertimes.stream(), pastOvertimes.stream()).toList();

        final OvertimeOverviewDto overtimeOverviewDto = new OvertimeOverviewDto(
            settingsService.getSettings().getOvertimeSettings().isOvertimeActive(),
            overtimeService.isUserIsAllowedToCreateOvertime(signedInUser, person),
            overtimeService.getTotalOvertimeForPersonAndYear(person, year),
            overtimeService.getLeftOvertimeForPerson(person),
            shownOvertimes.stream()
                .map(overtime -> {
                    final boolean isAllowedToEdit = overtimeService.isUserIsAllowedToUpdateOvertime(signedInUser, person, overtime);

                    return new OvertimeRecordDto(
                        overtime.id().value(),
                        overtime.startDate(),
                        overtime.endDate(),
                        overtime.duration(),
                        overtime.type().equals(EXTERNAL),
                        isAllowedToEdit
                    );
                }).toList(),
            shownOvertimes.size(),
            overtimes.size()
        );

        model.addAttribute("overtimeOverviewInformation", overtimeOverviewDto);
    }

    private ApplicationVacationTypeDto applicationVacationTypDto(VacationType<?> vacationType, Locale locale) {
        return new ApplicationVacationTypeDto(vacationType.getLabel(locale), vacationType.getCategory(), vacationType.getColor());
    }

    private ApplicationDto applicationDto(ApplicationForLeave applicationForLeave, Person signedInUser, Locale locale) {
        final List<PersonDto> holidayReplacements = applicationForLeave.getHolidayReplacements().stream()
            .map(hr -> new PersonDto(hr.getPerson().getGravatarURL(), hr.getPerson().getNiceName(), hr.getPerson().getInitials()))
            .toList();

        final boolean requiresApprovalToCancel = applicationForLeave.getVacationType().isRequiresApprovalToCancel();
        final boolean isDepartmentHeadOfPerson = departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, applicationForLeave.getPerson());
        final boolean isSecondStageAuthorityOfPerson = departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, applicationForLeave.getPerson());

        final boolean allowedToEdit = isAllowedToEditApplication(applicationForLeave, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson);

        final boolean allowedToRevoke = isAllowedToRevokeApplication(applicationForLeave, signedInUser, requiresApprovalToCancel);
        final boolean allowedToCancel = isAllowedToCancelApplication(applicationForLeave, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson);
        final boolean allowedToCancelDirectly = isAllowedToCancelDirectlyApplication(applicationForLeave, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson, requiresApprovalToCancel);
        final boolean allowedToStartCancellationRequest = isAllowedToStartCancellationRequest(applicationForLeave, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson, requiresApprovalToCancel);

        return new ApplicationDto(
            applicationForLeave.getId(),
            applicationForLeave.getPerson().getId(),
            applicationForLeave.getStatus(),
            applicationVacationTypDto(applicationForLeave.getVacationType(), locale),
            applicationForLeave.getApplicationDate(),
            applicationForLeave.getStartDate(),
            applicationForLeave.getEndDate(),
            applicationForLeave.getStartTime(),
            applicationForLeave.getEndTime(),
            applicationForLeave.getStartDateWithTime(),
            applicationForLeave.getEndDateWithTime(),
            applicationForLeave.getWeekDayOfStartDate(),
            applicationForLeave.getWeekDayOfEndDate(),
            applicationForLeave.getDayLength(),
            applicationForLeave.getWorkDays(),
            applicationForLeave.getHours(),
            applicationForLeave.getEditedDate(),
            applicationForLeave.getCancelDate(),
            holidayReplacements,
            allowedToEdit,
            allowedToRevoke,
            allowedToCancel,
            allowedToCancelDirectly,
            allowedToStartCancellationRequest
        );
    }

    private static boolean showExpiredVacationDays(LocalDate now, Account account, BigDecimal expiredRemainingVacationDays) {
        final boolean isBeforeExpiryDate = now.isBefore(account.getExpiryDate());
        final boolean hasExpiredRemainingVacationDays = expiredRemainingVacationDays.compareTo(BigDecimal.ZERO) > 0;
        return account.doRemainingVacationDaysExpire() && !isBeforeExpiryDate && hasExpiredRemainingVacationDays;
    }

    private boolean isPersonAllowedToExecuteRoleOn(Person person, Role role, Person personToShowDetails) {
        final boolean isBossOrDepartmentHeadOrSecondStageAuthority = person.hasRole(BOSS)
            || departmentService.isDepartmentHeadAllowedToManagePerson(person, personToShowDetails)
            || departmentService.isSecondStageAuthorityAllowedToManagePerson(person, personToShowDetails);
        return person.hasRole(role) && isBossOrDepartmentHeadOrSecondStageAuthority;
    }
}
