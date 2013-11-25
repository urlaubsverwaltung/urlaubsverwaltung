package org.synyx.urlaubsverwaltung.person.web;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.application.web.UsedDaysOverview;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.web.SecurityUtil;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.util.GravatarUtil;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;


/**
 * Controller for the different ways of displaying the personal overview page.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class PersonalOverviewController {

    private static final Logger LOG = Logger.getLogger("error");

    private static final String OVERVIEW_LINK = "/overview"; // personal overview
    private static final String OVERVIEW_STAFF_LINK = "/staff/{" + PersonConstants.PERSON_ID + "}/overview"; // overview of other person

    private PersonService personService;
    private AccountService accountService;
    private CalculationService calculationService;
    private GravatarUtil gravatarUtil;
    private SecurityUtil securityUtil;
    private ApplicationService applicationService;
    private OwnCalendarService calendarService;

    public PersonalOverviewController(PersonService personService, AccountService accountService,
        CalculationService calculationService, GravatarUtil gravatarUtil, SecurityUtil securityUtil,
        ApplicationService applicationService, OwnCalendarService calendarService) {

        this.personService = personService;
        this.accountService = accountService;
        this.calculationService = calculationService;
        this.gravatarUtil = gravatarUtil;
        this.securityUtil = securityUtil;
        this.applicationService = applicationService;
        this.calendarService = calendarService;
    }

    /**
     * Default personal overview for user: information about one's leave accounts, entitlement of holidays, list of
     * applications, etc. If there is no parameter set for year, take current year for view.
     *
     * @param  year
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = OVERVIEW_LINK, method = RequestMethod.GET)
    public String showPersonalOverview(@RequestParam(value = ControllerConstants.YEAR, required = false) String year,
        Model model) {

        if (securityUtil.isInactive()) {
            return ControllerConstants.LOGIN_LINK;
        } else {
            Person person = securityUtil.getLoggedUser();
            prepareOverview(person, parseYearParameter(year), model);
            securityUtil.setLoggedUser(model);

            return PersonConstants.OVERVIEW_JSP;
        }
    }


    /**
     * The office is able to see overviews of staff with information about this person's leave accounts, entitlement of
     * holidays, list of applications, etc. The default overview of a staff member (no parameter set for year) is for
     * the current year.
     *
     * @param  personId
     * @param  year
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = OVERVIEW_STAFF_LINK, method = RequestMethod.GET)
    public String showStaffOverview(@PathVariable(PersonConstants.PERSON_ID) Integer personId,
        @RequestParam(value = ControllerConstants.YEAR, required = false) String year, Model model) {

        Person person = personService.getPersonByID(personId);
        List<Person> persons;

        if (person.isActive()) {
            persons = personService.getAllPersons();
        } else {
            persons = personService.getInactivePersons();
        }

        model.addAttribute(ControllerConstants.PERSONS, persons);

        prepareOverview(person, parseYearParameter(year), model);

        securityUtil.setLoggedUser(model);

        return PersonConstants.OVERVIEW_JSP;
    }


    /**
     * Parses the year of the given String, if parsing fails, the current year is returned.
     *
     * @param  input
     *
     * @return  parsed Integer of the given String, if parsing failed the current year is returned
     */
    private Integer parseYearParameter(String input) {

        // default value for year is the current year
        Integer year = DateMidnight.now(GregorianChronology.getInstance()).getYear();

        if (StringUtils.hasText(input)) {
            try {
                year = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                LOG.warn("Tried to show overview for an invalid year entry: " + input, ex);
            }
        }

        return year;
    }


    /**
     * Prepares model for overview with given person and year.
     *
     * @param  person
     * @param  year
     * @param  model
     */
    private void prepareOverview(Person person, int year, Model model) {

        prepareApplications(person, year, model);

        prepareHolidayAccounts(person, year, model);

        securityUtil.setLoggedUser(model);
        model.addAttribute(ControllerConstants.PERSON, person);
        model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());

        String url = gravatarUtil.createImgURL(person.getEmail());
        model.addAttribute(PersonConstants.GRAVATAR, url);
    }


    private void prepareApplications(Person person, int year, Model model) {

        // get the person's applications for the given year
        List<Application> apps = applicationService.getAllApplicationsByPersonAndYear(person, year);

        if (!apps.isEmpty()) {
            List<Application> applications = new ArrayList<Application>();

            UsedDaysOverview usedDaysOverview = new UsedDaysOverview(apps, year, calendarService);
            model.addAttribute("usedDaysOverview", usedDaysOverview);

            for (Application a : apps) {
                if ((a.getStatus() != ApplicationStatus.CANCELLED)
                        || (a.getStatus() == ApplicationStatus.CANCELLED && a.isFormerlyAllowed())) {
                    applications.add(a);
                }
            }

            if (applications.isEmpty()) {
                model.addAttribute(PersonConstants.NO_APPS, true);
            } else {
                model.addAttribute(ControllerConstants.APPLICATIONS, applications);
            }
        }

        model.addAttribute("ALLOWED", ApplicationStatus.ALLOWED);
        model.addAttribute("WAITING", ApplicationStatus.WAITING);
    }


    private void prepareHolidayAccounts(Person person, int year, Model model) {

        // get person's holidays account and entitlement for the given year
        Account account = accountService.getHolidaysAccount(year, person);

        if (account != null) {
            BigDecimal vacationDaysLeft = calculationService.calculateLeftVacationDays(account);
            BigDecimal remainingVacationDaysLeft = calculationService.calculateLeftRemainingVacationDays(account);
            model.addAttribute(PersonConstants.LEFT_DAYS, vacationDaysLeft);
            model.addAttribute(PersonConstants.REM_LEFT_DAYS, remainingVacationDaysLeft);
            model.addAttribute(PersonConstants.BEFORE_APRIL, DateUtil.isBeforeApril(DateMidnight.now()));
        }

        model.addAttribute(ControllerConstants.ACCOUNT, account);
    }
}
