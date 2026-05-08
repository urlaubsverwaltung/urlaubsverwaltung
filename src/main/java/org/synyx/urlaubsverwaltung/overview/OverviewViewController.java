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
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
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

        prepareApplicationSummary(person, yearToShow, model, locale);
        prepareHolidayAccounts(person, yearToShow, now, model);
        prepareSickNoteList(person, yearToShow, model);
        prepareSettings(model);

        model.addAttribute("currentYear", now.getYear());
        model.addAttribute("selectedYear", yearToShow);
        model.addAttribute("signedInUser", signedInUser);

        model.addAttribute("overtimeTotal", overtimeService.getTotalOvertimeForPersonAndYear(person, yearToShow));
        model.addAttribute("overtimeLeft", overtimeService.getLeftOvertimeForPerson(person));

        model.addAttribute("canAccessCalendarShare", person.equals(signedInUser) || signedInUser.hasRole(OFFICE) || signedInUser.hasRole(BOSS));
        model.addAttribute("canViewSickNoteAnotherUser", signedInUser.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_VIEW, person) || departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person) || departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person));
        model.addAttribute("canAddSickNoteAnotherUser", signedInUser.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(signedInUser, SICK_NOTE_ADD, person));

        return "person/person-overview";
    }

    private void prepareApplicationSummary(Person person, int year, Model model, Locale locale) {
        // get the person's applications for the given year
        final LocalDate startDate = Year.of(year).atDay(1);
        final LocalDate endDate = startDate.with(lastDayOfYear());
        final List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate, person);

        final List<ApplicationDto> applicationsForLeave;
        final YearlyUsedDaysSummary usedDaysOverview;

        if (applications.isEmpty()) {
            applicationsForLeave = List.of();
            usedDaysOverview = new YearlyUsedDaysSummary(List.of(), year, workDaysCountService);
        } else {
            final LocalDate today = LocalDate.now(clock);
            final List<ApplicationForLeave> allForLeave = applications.stream()
                .map(application -> new ApplicationForLeave(application, workDaysCountService))
                .toList();
            // always show 3 applications if no application is in the fill with future application
            final List<ApplicationDto> pastApplicationDtos = allForLeave.stream()
                .filter(a -> a.getStartDate().isBefore(today))
                .sorted(comparing(ApplicationForLeave::getStartDate).reversed())
                .limit(NUMBER_OF_PAST_APPLICATION_ON_OVERVIEW)
                .map(a -> applicationDto(a, locale))
                .toList();

            final List<ApplicationDto> futureApplicationDtos = allForLeave.stream()
                .filter(a -> !a.getStartDate().isBefore(today))
                .sorted(comparing(ApplicationForLeave::getStartDate))
                .limit((long) NUMBER_OF_FUTR_APPLICATION_ON_OVERVIEW - pastApplicationDtos.size())
                .map(a -> applicationDto(a, locale))
                .toList();

            applicationsForLeave = Stream.concat(pastApplicationDtos.stream(), futureApplicationDtos.stream()).toList();
            usedDaysOverview = new YearlyUsedDaysSummary(applications, year, workDaysCountService);
        }

        model.addAttribute("applications", applicationsForLeave);
        model.addAttribute("usedDaysOverview", usedDaysOverview);
    }

    private ApplicationVacationTypeDto applicationVacationTypDto(VacationType<?> vacationType, Locale locale) {
        return new ApplicationVacationTypeDto(vacationType.getLabel(locale), vacationType.getCategory(), vacationType.getColor());
    }

    private ApplicationDto applicationDto(ApplicationForLeave applicationForLeave, Locale locale) {
        final ApplicationDto dto = new ApplicationDto();
        dto.setId(applicationForLeave.getId());
        dto.setStatus(applicationForLeave.getStatus());
        dto.setVacationType(applicationVacationTypDto(applicationForLeave.getVacationType(), locale));
        dto.setApplicationDate(applicationForLeave.getApplicationDate());
        dto.setStartDate(applicationForLeave.getStartDate());
        dto.setEndDate(applicationForLeave.getEndDate());
        dto.setStartTime(applicationForLeave.getStartTime());
        dto.setEndTime(applicationForLeave.getEndTime());
        dto.setStartDateWithTime(applicationForLeave.getStartDateWithTime());
        dto.setEndDateWithTime(applicationForLeave.getEndDateWithTime());
        dto.setDayLength(applicationForLeave.getDayLength());
        dto.setWorkDays(applicationForLeave.getWorkDays());
        dto.setPersonId(applicationForLeave.getPerson().getId());
        dto.setHours(applicationForLeave.getHours());
        dto.setWeekDayOfStartDate(applicationForLeave.getWeekDayOfStartDate());
        dto.setWeekDayOfEndDate(applicationForLeave.getWeekDayOfEndDate());
        dto.setEditedDate(applicationForLeave.getEditedDate());
        dto.setCancelDate(applicationForLeave.getCancelDate());
        return dto;
    }

    private void prepareSickNoteList(Person person, int year, Model model) {

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


        model.addAttribute("sickNotes", Stream.concat(pastSickNotes.stream(), futureSickNotes.stream()).toList());

        final SickDaysOverview sickDaysOverview = new SickDaysOverview(sickNotes, workDaysCountService, from, to);
        model.addAttribute("sickDaysOverview", sickDaysOverview);
    }

    private void prepareHolidayAccounts(Person person, int year, LocalDate now, Model model) {

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

    private static boolean showExpiredVacationDays(LocalDate now, Account account, BigDecimal expiredRemainingVacationDays) {
        final boolean isBeforeExpiryDate = now.isBefore(account.getExpiryDate());
        final boolean hasExpiredRemainingVacationDays = expiredRemainingVacationDays.compareTo(BigDecimal.ZERO) > 0;
        return account.doRemainingVacationDaysExpire() && !isBeforeExpiryDate && hasExpiredRemainingVacationDays;
    }

    private void prepareSettings(Model model) {
        model.addAttribute("settings", settingsService.getSettings());
    }

    private boolean isPersonAllowedToExecuteRoleOn(Person person, Role role, Person personToShowDetails) {
        final boolean isBossOrDepartmentHeadOrSecondStageAuthority = person.hasRole(BOSS)
            || departmentService.isDepartmentHeadAllowedToManagePerson(person, personToShowDetails)
            || departmentService.isSecondStageAuthorityAllowedToManagePerson(person, personToShowDetails);
        return person.hasRole(role) && isBossOrDepartmentHeadOrSecondStageAuthority;
    }
}
