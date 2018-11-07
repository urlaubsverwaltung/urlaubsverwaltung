package org.synyx.urlaubsverwaltung.web.overview;

import java.util.List;
import java.util.Optional;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.application.ApplicationForLeave;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.web.sicknote.ExtendedSickNote;
import org.synyx.urlaubsverwaltung.web.statistics.SickDaysOverview;
import org.synyx.urlaubsverwaltung.web.statistics.UsedDaysOverview;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

/**
 * Controller to display the personal overview page with basic information about
 * overtime, applications for leave and sick notes.
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping("/web")
public class OverviewController {

	@Autowired
	private PersonService personService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private VacationDaysService vacationDaysService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private WorkDaysService calendarService;

	@Autowired
	private SickNoteService sickNoteService;

	@Autowired
	private OvertimeService overtimeService;

	@Autowired
	private SettingsService settingsService;

	@RequestMapping(value = "/overview", method = RequestMethod.GET)
	public String showOverview(
			@RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) String year) {

		Person user = sessionService.getSignedInUser();

		if (StringUtils.hasText(year)) {
			return "redirect:/web/staff/" + user.getId() + "/overview?year=" + year;
		}

		return "redirect:/web/staff/" + user.getId() + "/overview";
	}

	@RequestMapping(value = "/staff/{personId}/overview", method = RequestMethod.GET)
	public String showOverview(@PathVariable("personId") Integer personId,
			@RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Integer year, Model model)
			throws UnknownPersonException, AccessDeniedException {

		Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
		Person signedInUser = sessionService.getSignedInUser();

		if (!sessionService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
			throw new AccessDeniedException(
					String.format("User '%s' has not the correct permissions to access the overview page of user '%s'",
							signedInUser.getLoginName(), person.getLoginName()));
		}

		model.addAttribute(PersonConstants.PERSON_ATTRIBUTE, person);

		Integer yearToShow = year == null ? DateMidnight.now().getYear() : year;
		prepareApplications(person, yearToShow, model);
		prepareHolidayAccounts(person, yearToShow, model);
		prepareSickNoteList(person, yearToShow, model);
		prepareSettings(model);

		model.addAttribute(ControllerConstants.YEAR_ATTRIBUTE, DateMidnight.now().getYear());
		model.addAttribute("currentYear", DateMidnight.now().getYear());
		model.addAttribute("currentMonth", DateMidnight.now().getMonthOfYear());

		return "person/overview";
	}

	private void prepareSickNoteList(Person person, int year, Model model) {

		List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, DateUtil.getFirstDayOfYear(year),
				DateUtil.getLastDayOfYear(year));

		List<ExtendedSickNote> extendedSickNotes = FluentIterable.from(sickNotes)
				.transform(input -> new ExtendedSickNote(input, calendarService)).toSortedList((o1, o2) -> {
					// show latest sick notes at first
					return o2.getStartDate().compareTo(o1.getStartDate());
				});

		model.addAttribute("sickNotes", extendedSickNotes);

		SickDaysOverview sickDaysOverview = new SickDaysOverview(sickNotes, calendarService);
		model.addAttribute("sickDaysOverview", sickDaysOverview);
	}

	private void prepareApplications(Person person, int year, Model model) {

		// get the person's applications for the given year
		List<Application> applications = FluentIterable
				.from(applicationService.getApplicationsForACertainPeriodAndPerson(DateUtil.getFirstDayOfYear(year),
						DateUtil.getLastDayOfYear(year), person))
				.filter(input -> !input.hasStatus(ApplicationStatus.REVOKED)).toList();

		if (!applications.isEmpty()) {
			ImmutableList<ApplicationForLeave> applicationsForLeave = FluentIterable.from(applications)
					.transform(input -> new ApplicationForLeave(input, calendarService)).toSortedList((o1, o2) -> {
						// show latest applications at first
						return o2.getStartDate().compareTo(o1.getStartDate());
					});

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
			model.addAttribute("vacationDaysLeft", vacationDaysService.getVacationDaysLeft(acc,accountService.getHolidaysAccount(year+1, person)));
			model.addAttribute("account", acc);
			model.addAttribute(PersonConstants.BEFORE_APRIL_ATTRIBUTE, DateUtil.isBeforeApril(DateMidnight.now(), acc.getYear()));
		}
	}

	private void prepareSettings(Model model) {

		model.addAttribute("settings", settingsService.getSettings());
	}

}
