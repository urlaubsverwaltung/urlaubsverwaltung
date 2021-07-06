package org.synyx.urlaubsverwaltung.overview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.web.ApplicationForLeave;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.web.ExtendedSickNote;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getFirstDayOfYear;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getLastDayOfYear;

/**
 * Controller to display the personal overview page with basic information about
 * overtime, applications for leave and sick notes.
 */
@Controller
@RequestMapping("/")
public class OverviewViewController {

    private static final String BEFORE_APRIL_ATTRIBUTE = "beforeApril";
    private static final String PERSON_ATTRIBUTE = "person";

    private final PersonService personService;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final ApplicationService applicationService;
    private final WorkDaysCountService workDaysCountService;
    private final SickNoteService sickNoteService;
    private final OvertimeService overtimeService;
    private final SettingsService settingsService;
    private final DepartmentService departmentService;
    private final Clock clock;

    @Autowired
    public OverviewViewController(PersonService personService, AccountService accountService,
                                  VacationDaysService vacationDaysService,
                                  ApplicationService applicationService, WorkDaysCountService workDaysCountService,
                                  SickNoteService sickNoteService, OvertimeService overtimeService,
                                  SettingsService settingsService, DepartmentService departmentService, Clock clock) {
        this.personService = personService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.applicationService = applicationService;
        this.workDaysCountService = workDaysCountService;
        this.sickNoteService = sickNoteService;
        this.overtimeService = overtimeService;
        this.settingsService = settingsService;
        this.departmentService = departmentService;
        this.clock = clock;
    }

    @GetMapping
    public String index() {

        return "redirect:/web/overview";
    }

    @GetMapping("/web/overview")
    public String showOverview(@RequestParam(value = "year", required = false) String year) {

        final Person signedInUser = personService.getSignedInUser();

        if (hasText(year)) {
            return "redirect:/web/person/" + signedInUser.getId() + "/overview?year=" + year;
        }

        return "redirect:/web/person/" + signedInUser.getId() + "/overview";
    }

    @GetMapping("/web/person/{personId}/overview")
    public String showOverview(@PathVariable("personId") Integer personId,
                               @RequestParam(value = "year", required = false) Integer year, Model model)
        throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        final Person signedInUser = personService.getSignedInUser();

        if (!departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(format("User '%s' has not the correct permissions to access the overview page of user '%s'",
                signedInUser.getId(), person.getId()));
        }

        model.addAttribute(PERSON_ATTRIBUTE, person);

        final ZonedDateTime now = ZonedDateTime.now(clock);
        final int yearToShow = year == null ? now.getYear() : year;

        prepareApplications(person, yearToShow, model);
        prepareHolidayAccounts(person, yearToShow, model);
        prepareSickNoteList(person, yearToShow, model);
        prepareSettings(model);

        model.addAttribute("year", now.getYear());
        model.addAttribute("currentYear", now.getYear());
        model.addAttribute("currentMonth", now.getMonthValue());
        model.addAttribute("signedInUser", signedInUser);
        model.addAttribute("userIsAllowedToWriteOvertime", overtimeService.isUserIsAllowedToWriteOvertime(signedInUser, person));

        return "person/overview";
    }

    private void prepareSickNoteList(Person person, int year, Model model) {

        final List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, getFirstDayOfYear(year), getLastDayOfYear(year));

        final List<ExtendedSickNote> extendedSickNotes = sickNotes.stream()
            .map(input -> new ExtendedSickNote(input, workDaysCountService))
            .sorted(Comparator.comparing(ExtendedSickNote::getStartDate).reversed())
            .collect(toList());
        model.addAttribute("sickNotes", extendedSickNotes);

        final SickDaysOverview sickDaysOverview = new SickDaysOverview(sickNotes, workDaysCountService);
        model.addAttribute("sickDaysOverview", sickDaysOverview);
    }

    private void prepareApplications(Person person, int year, Model model) {

        // get the person's applications for the given year
        final List<Application> applications =
            applicationService.getApplicationsForACertainPeriodAndPerson(getFirstDayOfYear(year), getLastDayOfYear(year), person);

        if (!applications.isEmpty()) {
            final List<ApplicationForLeave> applicationsForLeave = applications.stream()
                .map(application -> new ApplicationForLeave(application, workDaysCountService))
                .sorted(Comparator.comparing(ApplicationForLeave::getStartDate).reversed())
                .collect(toList());
            model.addAttribute("applications", applicationsForLeave);

            final UsedDaysOverview usedDaysOverview = new UsedDaysOverview(applications, year, workDaysCountService);
            model.addAttribute("usedDaysOverview", usedDaysOverview);
        }

        model.addAttribute("overtimeTotal", overtimeService.getTotalOvertimeForPersonAndYear(person, year));
        model.addAttribute("overtimeLeft", overtimeService.getLeftOvertimeForPerson(person));
    }

    private void prepareHolidayAccounts(Person person, int year, Model model) {

        // get person's holidays account and entitlement for the given year
        final Optional<Account> account = accountService.getHolidaysAccount(year, person);
        if (account.isPresent()) {
            Account acc = account.get();
            final Optional<Account> accountNextYear = accountService.getHolidaysAccount(year + 1, person);
            model.addAttribute("vacationDaysLeft", vacationDaysService.getVacationDaysLeft(account.get(), accountNextYear));
            model.addAttribute("account", acc);
            model.addAttribute(BEFORE_APRIL_ATTRIBUTE, DateUtil.isBeforeApril(LocalDate.now(clock), acc.getYear()));
        }
    }

    private void prepareSettings(Model model) {
        model.addAttribute("settings", settingsService.getSettings());
    }
}
