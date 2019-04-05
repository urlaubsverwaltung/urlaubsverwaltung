package org.synyx.urlaubsverwaltung.overview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.web.ApplicationForLeave;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.web.ExtendedSickNote;
import org.synyx.urlaubsverwaltung.statistics.web.SickDaysOverview;
import org.synyx.urlaubsverwaltung.statistics.web.UsedDaysOverview;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.web.ControllerConstants.YEAR_ATTRIBUTE;

/**
 * Controller to display the personal overview page with basic information about
 * overtime, applications for leave and sick notes.
 */
@Controller
@RequestMapping("/web")
public class OverviewController {

    private static final String BEFORE_APRIL_ATTRIBUTE = "beforeApril";
    private static final String PERSON_ATTRIBUTE = "person";

    private final PersonService personService;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final SessionService sessionService;
    private final ApplicationService applicationService;
    private final WorkDaysService calendarService;
    private final SickNoteService sickNoteService;
    private final OvertimeService overtimeService;
    private final SettingsService settingsService;

    @Autowired
    public OverviewController(PersonService personService, AccountService accountService, VacationDaysService vacationDaysService, SessionService sessionService, ApplicationService applicationService, WorkDaysService calendarService, SickNoteService sickNoteService, OvertimeService overtimeService, SettingsService settingsService) {
        this.personService = personService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.sessionService = sessionService;
        this.applicationService = applicationService;
        this.calendarService = calendarService;
        this.sickNoteService = sickNoteService;
        this.overtimeService = overtimeService;
        this.settingsService = settingsService;
    }

    @GetMapping("/overview")
    public String showOverview(@RequestParam(value = YEAR_ATTRIBUTE, required = false) String year) {

        Person user = sessionService.getSignedInUser();

        if (StringUtils.hasText(year)) {
            return "redirect:/web/staff/" + user.getId() + "/overview?year=" + year;
        }

        return "redirect:/web/staff/" + user.getId() + "/overview";
    }

    @GetMapping("/staff/{personId}/overview")
    public String showOverview(@PathVariable("personId") Integer personId,
                               @RequestParam(value = YEAR_ATTRIBUTE, required = false) Integer year, Model model)
            throws UnknownPersonException, AccessDeniedException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        Person signedInUser = sessionService.getSignedInUser();

        if (!sessionService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(
                    String.format("User '%s' has not the correct permissions to access the overview page of user '%s'",
                            signedInUser.getLoginName(), person.getLoginName()));
        }

        model.addAttribute(PERSON_ATTRIBUTE, person);

        Integer yearToShow = year == null ? ZonedDateTime.now(UTC).getYear() : year;
        prepareApplications(person, yearToShow, model);
        prepareHolidayAccounts(person, yearToShow, model);
        prepareSickNoteList(person, yearToShow, model);
        prepareSettings(model);

        model.addAttribute(YEAR_ATTRIBUTE, ZonedDateTime.now(UTC).getYear());
        model.addAttribute("currentYear", ZonedDateTime.now(UTC).getYear());
        model.addAttribute("currentMonth", ZonedDateTime.now(UTC).getMonthValue());

        return "person/overview";
    }

    private void prepareSickNoteList(Person person, int year, Model model) {

        List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, DateUtil.getFirstDayOfYear(year),
                DateUtil.getLastDayOfYear(year));

        List<ExtendedSickNote> extendedSickNotes = sickNotes.stream()
            .map(input -> new ExtendedSickNote(input, calendarService))
            .sorted(Comparator.comparing(ExtendedSickNote::getStartDate).reversed())
            .collect(toList());

        model.addAttribute("sickNotes", extendedSickNotes);

        SickDaysOverview sickDaysOverview = new SickDaysOverview(sickNotes, calendarService);
        model.addAttribute("sickDaysOverview", sickDaysOverview);
    }

    private void prepareApplications(Person person, int year, Model model) {

        // get the person's applications for the given year
        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(DateUtil.getFirstDayOfYear(year),
            DateUtil.getLastDayOfYear(year), person).stream()
            .filter(input -> !input.hasStatus(ApplicationStatus.REVOKED))
            .collect(toList());

        if (!applications.isEmpty()) {
            List<ApplicationForLeave> applicationsForLeave = applications.stream()
                .map(application -> new ApplicationForLeave(application, calendarService))
                .sorted(Comparator.comparing(ApplicationForLeave::getStartDate).reversed())
                .collect(toList());

            model.addAttribute("applications", applicationsForLeave);

            UsedDaysOverview usedDaysOverview = new UsedDaysOverview(applications, year, calendarService);
            model.addAttribute("usedDaysOverview", usedDaysOverview);
        }

        model.addAttribute("overtimeTotal", overtimeService.getTotalOvertimeForPersonAndYear(person, year));
        model.addAttribute("overtimeLeft", overtimeService.getLeftOvertimeForPerson(person));
    }

    private void prepareHolidayAccounts(Person person, int year, Model model) {

        // get person's holidays account and entitlement for the given year
        Optional<Account> account = accountService.getHolidaysAccount(year, person);

        if (account.isPresent()) {
            Account acc = account.get();
            final Optional<Account> accountNextYear = accountService.getHolidaysAccount(year + 1, person);
            model.addAttribute("vacationDaysLeft", vacationDaysService.getVacationDaysLeft(account.get(), accountNextYear));
            model.addAttribute("account", acc);
            model.addAttribute(BEFORE_APRIL_ATTRIBUTE, DateUtil.isBeforeApril(LocalDate.now(UTC), acc.getYear()));
        }
    }

    private void prepareSettings(Model model) {

        model.addAttribute("settings", settingsService.getSettings());
    }

}
