package org.synyx.urlaubsverwaltung.overview;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.jspecify.annotations.NonNull;
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
import org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator;
import org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissions;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNotePermissionEvaluator;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNotePermissions;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Comparator.comparing;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeType.EXTERNAL;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

/**
 * Controller to display the personal overview page with basic information about
 * overtime, applications for leave and sick notes.
 */
@Controller
@RequestMapping("/")
public class OverviewViewController implements HasLaunchpad, HasPersonSearch {

    private static final String PERSON_ATTRIBUTE = "person";
    private static final int NUMBER_OF_PAST_APPLICATION_ON_OVERVIEW = 1;
    private static final int NUMBER_OF_CURRENT_APPLICATION_ON_OVERVIEW = 1;
    private static final int NUMBER_OF_FUTR_APPLICATION_ON_OVERVIEW = 4;
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
    private final SickNotePermissionEvaluator sickNotePermissionEvaluator;
    private final ApplicationForLeavePermissionEvaluator applicationForLeavePermissionEvaluator;
    private final VacationTypeViewModelService vacationTypeViewModelService;
    private final PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;
    private final Clock clock;

    @Autowired
    public OverviewViewController(
        PersonService personService, AccountService accountService,
        VacationDaysService vacationDaysService,
        WorkDaysCountService workDaysCountService, ApplicationService applicationService,
        SickNoteService sickNoteService, OvertimeService overtimeService,
        SettingsService settingsService, DepartmentService departmentService,
        SickNotePermissionEvaluator sickNotePermissionEvaluator,
        ApplicationForLeavePermissionEvaluator applicationForLeavePermissionEvaluator,
        VacationTypeViewModelService vacationTypeViewModelService, PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier,
        Clock clock
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
        this.sickNotePermissionEvaluator = sickNotePermissionEvaluator;
        this.applicationForLeavePermissionEvaluator = applicationForLeavePermissionEvaluator;
        this.vacationTypeViewModelService = vacationTypeViewModelService;
        this.personSearchUiFragmentSupplier = personSearchUiFragmentSupplier;
        this.clock = clock;
    }

    @Override
    public PersonSuggestionUrlStrategy personSuggestionUrlStrategy() {
        return (suggestion, context) -> {
            String url = "/web/person/%s/overview".formatted(suggestion.getId());
            final String year = context.getRequest().getParameter("year");
            if (hasText(year)) {
                url += "?year=" + year;
            }
            return url;
        };
    }

    @Override
    public PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier() {
        return personSearchUiFragmentSupplier;
    }

    @GetMapping
    public String index() {
        return "redirect:/web/overview";
    }

    @GetMapping("/web/overview")
    public String showOverview(
        @RequestParam(value = "year", required = false) Integer year
    ) {
        final Person signedInUser = personService.getSignedInUser();
        final String yearParam = year == null ? "" : "?year=" + year;
        return format("redirect:/web/person/%d/overview%s", signedInUser.getId(), yearParam);
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
            final List<ApplicationForLeave> allForLeave = toApplicationsForLeave(applications);

            // show the applications closest to today: the last one, the currently running one and the next ones
            final List<ApplicationForLeave> currentApplications = allForLeave.stream()
                .filter(a -> isCurrent(a, today))
                .sorted(comparing(ApplicationForLeave::getStartDate))
                .limit(NUMBER_OF_CURRENT_APPLICATION_ON_OVERVIEW)
                .toList();

            final List<ApplicationForLeave> pastApplications = allForLeave.stream()
                .filter(a -> a.getEndDate().isBefore(today))
                .sorted(comparing(ApplicationForLeave::getStartDate).reversed())
                .limit(NUMBER_OF_PAST_APPLICATION_ON_OVERVIEW)
                .toList();

            // a currently running application takes one of the slots of the future applications
            final List<ApplicationForLeave> futureApplications = allForLeave.stream()
                .filter(a -> a.getStartDate().isAfter(today))
                .sorted(comparing(ApplicationForLeave::getStartDate))
                .limit((long) NUMBER_OF_FUTR_APPLICATION_ON_OVERVIEW - currentApplications.size())
                .toList();

            // future applications on top, the past one at the bottom
            applicationsForLeave = Stream.of(pastApplications, currentApplications, futureApplications)
                .flatMap(List::stream)
                .sorted(comparing(ApplicationForLeave::getStartDate).reversed())
                .map(a -> applicationDto(a, signedInUser, locale))
                .toList();
            usedDaysOverview = new ApplicationDaysUsedSummaryDto(applications, year, workDaysCountService);
        }

        model.addAttribute("applicationOverviewInformation", new ApplicationOverviewDto(
            applicationsForLeave,
            usedDaysOverview,
            person.equals(signedInUser),
            !person.equals(signedInUser) && applicationForLeavePermissionEvaluator.isAllowedToApplyForPerson(signedInUser, person),
            applicationsForLeave.size(),
            applications.size()
        ));
    }

    private static boolean isCurrent(ApplicationForLeave applicationForLeave, LocalDate today) {
        return !applicationForLeave.getStartDate().isAfter(today) && !applicationForLeave.getEndDate().isBefore(today);
    }

    private List<ApplicationForLeave> toApplicationsForLeave(List<Application> applications) {
        final Map<Application, SortedMap<Integer, BigDecimal>> workDaysByYearByApplication = workDaysCountService.getWorkDaysCountByYearForApplications(applications);
        return applications.stream()
            .map(application -> new ApplicationForLeave(application, workDaysByYearByApplication.get(application)))
            .toList();
    }

    /**
     * Selects the entries closest to today: the next upcoming ones and, since most of the time there are no upcoming
     * ones, the most recent past ones to fill up the remaining slots.
     *
     * @return the selected entries, sorted descending by start date, the upcoming ones first
     */
    static <T> List<T> entriesClosestToToday(List<T> entries, Function<T, LocalDate> startDate, LocalDate today, int pastLimit, int futureLimit) {

        final Comparator<T> byStartDate = comparing(startDate);

        final List<T> futureEntries = entries.stream()
            .filter(entry -> !startDate.apply(entry).isBefore(today))
            .sorted(byStartDate)
            .limit(futureLimit)
            .toList();

        final List<T> pastEntries = entries.stream()
            .filter(entry -> startDate.apply(entry).isBefore(today))
            .sorted(byStartDate.reversed())
            .limit((long) pastLimit - futureEntries.size())
            .toList();

        return Stream.concat(futureEntries.stream(), pastEntries.stream())
            .sorted(byStartDate.reversed())
            .toList();
    }

    private void prepareSickNoteInformation(Person person, Person signedInUser, int year, Model model) {

        final LocalDate from = Year.of(year).atDay(1);
        final LocalDate to = from.with(lastDayOfYear());

        final List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, from, to);

        final LocalDate today = LocalDate.now(clock);

        final List<SickNote> shownSickNotes = entriesClosestToToday(sickNotes, SickNote::getStartDate, today,
            NUMBER_OF_PAST_SICK_NOTES_ON_OVERVIEW, NUMBER_OF_FUTR_SICK_NOTES_ON_OVERVIEW);

        final SickNotePermissions permissions = sickNotePermissionEvaluator.of(signedInUser, person);

        model.addAttribute("sickNotesOverview", new SickNotesOverviewDTO(
            mapToSickNoteDtos(shownSickNotes, permissions),
            new SickDaysSummaryDto(sickNotes, workDaysCountService, from, to),
            permissions.isAllowedToAdd() || permissions.isAllowedToSubmit(),
            permissions.isAllowedToView(),
            shownSickNotes.size(),
            sickNotes.size()
        ));
    }

    private @NonNull List<SickNoteDto> mapToSickNoteDtos(List<SickNote> shownSickNotes, SickNotePermissions permissions) {
        return shownSickNotes.stream()
            .map(sickNote -> new SickNoteDto(
                sickNote.getId(),
                sickNote.getStartDate(),
                sickNote.getEndDate(),
                sickNote.getDayLength(),
                sickNote.isAubPresent(),
                sickNote.getWorkDays(),
                sickNote.getWorkDaysWithAub(),
                sickNote.getStatus(),
                sickNote.getSickNoteType(),
                permissions.isAllowedToEdit(sickNote),
                permissions.isAllowedToCancel(sickNote)
            )).toList();
    }

    private void prepareOvertimeInformation(OvertimeService overtimeService, Person person, Person signedInUser, int year, Model model) {

        final List<Overtime> overtimes = overtimeService.getOvertimeRecordsForPersonAndYear(person, year);

        final LocalDate today = LocalDate.now(clock);

        final List<Overtime> shownOvertimes = entriesClosestToToday(overtimes, Overtime::startDate, today,
            NUMBER_OF_PAST_OVERTIMES_ON_OVERVIEW, NUMBER_OF_FUTR_OVERTIMES_ON_OVERVIEW);

        final OvertimeOverviewDto overtimeOverviewDto = new OvertimeOverviewDto(
            settingsService.getSettings().getOvertimeSettings().isOvertimeActive(),
            overtimeService.isUserIsAllowedToCreateOvertime(signedInUser, person),
            overtimeService.getTotalOvertimeForPersonAndYear(person, year),
            overtimeService.getLeftOvertimeForPerson(person),
            mapToShownOvertimesDto(overtimeService, person, signedInUser, shownOvertimes),
            shownOvertimes.size(),
            overtimes.size()
        );

        model.addAttribute("overtimeOverviewInformation", overtimeOverviewDto);
    }

    private static @NonNull List<OvertimeRecordDto> mapToShownOvertimesDto(OvertimeService overtimeService, Person person, Person signedInUser, List<Overtime> shownOvertimes) {
        return shownOvertimes.stream()
            .map(overtime -> new OvertimeRecordDto(
                overtime.id().value(),
                overtime.startDate(),
                overtime.endDate(),
                overtime.duration(),
                overtime.type().equals(EXTERNAL),
                overtimeService.isUserIsAllowedToUpdateOvertime(signedInUser, person, overtime)
            )).toList();
    }

    private ApplicationVacationTypeDto applicationVacationTypDto(VacationType<?> vacationType, Locale locale) {
        return new ApplicationVacationTypeDto(vacationType.getLabel(locale), vacationType.getCategory(), vacationType.getColor());
    }

    private ApplicationDto applicationDto(ApplicationForLeave applicationForLeave, Person signedInUser, Locale locale) {
        final List<PersonDto> holidayReplacements = applicationForLeave.getHolidayReplacements().stream()
            .map(hr -> new PersonDto(hr.getPerson().getGravatarURL(), hr.getPerson().getNiceName(), hr.getPerson().getInitials()))
            .toList();

        final ApplicationForLeavePermissions permissions = applicationForLeavePermissionEvaluator.of(signedInUser, applicationForLeave);

        final boolean allowedToEdit = permissions.isAllowedToEdit();

        final boolean allowedToRevoke = permissions.isAllowedToRevoke();
        final boolean allowedToCancel = permissions.isAllowedToCancel();
        final boolean allowedToCancelDirectly = permissions.isAllowedToCancelDirectly();
        final boolean allowedToStartCancellationRequest = permissions.isAllowedToStartCancellationRequest();

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
            applicationForLeave.getWorkDaysByYear(),
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

}
