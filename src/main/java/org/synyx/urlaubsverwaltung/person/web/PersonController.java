package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.security.web.SecurityUtil;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.chrono.GregorianChronology;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.account.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.util.GravatarUtil;
import org.synyx.urlaubsverwaltung.validator.PersonValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Aljona Murygina
 */
@Controller
public class PersonController {

    // links
    private static final String ACTIVE_LINK = "/staff";
    private static final String INACTIVE_LINK = "/staff/inactive";
    private static final String OVERVIEW_LINK = "/overview"; // personal overview
    private static final String OVERVIEW_STAFF_LINK = "/staff/{" + PersonConstants.PERSON_ID + "}/overview"; // overview of other person

    private PersonService personService;
    private ApplicationService applicationService;
    private HolidaysAccountService accountService;
    private CalculationService calculationService;
    private GravatarUtil gravatarUtil;
    private PersonValidator validator;
    private OwnCalendarService calendarService;
    private SecurityUtil securityUtil;

    public PersonController(PersonService personService, ApplicationService applicationService,
                            HolidaysAccountService accountService, CalculationService calculationService, GravatarUtil gravatarUtil,
                            PersonValidator validator, OwnCalendarService calendarService, SecurityUtil securityUtil) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.accountService = accountService;
        this.calculationService = calculationService;
        this.gravatarUtil = gravatarUtil;
        this.validator = validator;
        this.calendarService = calendarService;
        this.securityUtil = securityUtil;
    }

    /**
     * Shows list with inactive staff, default: for current year.
     *
     * @param model
     * @return
     */
    @RequestMapping(value = INACTIVE_LINK, method = RequestMethod.GET)
    public String showInactiveStaff(Model model) {

        if (securityUtil.isOffice()) {
            securityUtil.setLoggedUser(model);

            List<Person> persons = personService.getInactivePersons();

            if (persons.isEmpty()) {
                model.addAttribute(PersonConstants.NOTEXISTENT, true);
                model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());
            } else {
                prepareStaffView(persons, DateMidnight.now().getYear(), model);
            }

            return PersonConstants.STAFF_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Shows list with active staff, default: for current year.
     *
     * @param model
     * @return
     */
    @RequestMapping(value = ACTIVE_LINK, method = RequestMethod.GET)
    public String showActiveStaff(Model model) {

        if (securityUtil.isOffice()) {
            securityUtil.setLoggedUser(model);

            List<Person> persons = personService.getAllPersons();
            prepareStaffView(persons, DateMidnight.now().getYear(), model);

            return PersonConstants.STAFF_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Shows list with inactive staff for the given year.
     *
     * @param year
     * @param model
     * @return
     */
    @RequestMapping(value = INACTIVE_LINK, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showInactiveStaffByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        if (securityUtil.isOffice()) {
            securityUtil.setLoggedUser(model);

            List<Person> persons = personService.getInactivePersons();

            if (persons.isEmpty()) {
                model.addAttribute(PersonConstants.NOTEXISTENT, true);
                model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());
            } else {
                prepareStaffView(persons, year, model);
            }

            return PersonConstants.STAFF_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Shows list with active staff for the given year.
     *
     * @param year
     * @param model
     * @return
     */
    @RequestMapping(value = ACTIVE_LINK, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showActiveStaffByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        if (securityUtil.isOffice()) {
            securityUtil.setLoggedUser(model);

            List<Person> persons = personService.getAllPersons();
            prepareStaffView(persons, year, model);

            return PersonConstants.STAFF_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * prepares view of staffs; preparing is for both views (list and detail) identic
     *
     * @param persons
     * @param year
     * @param model
     */
    private void prepareStaffView(List<Person> persons, int year, Model model) {

        Map<Person, String> gravatarUrls = new HashMap<Person, String>();
        String url;

        Map<Person, Account> accounts = new HashMap<Person, Account>();
        Account account;

        Map<Person, BigDecimal> leftDays = new HashMap<Person, BigDecimal>();

        for (Person person : persons) {
            // get url of person's gravatar image
            url = gravatarUtil.createImgURL(person.getEmail());

            if (url != null) {
                gravatarUrls.put(person, url);
            }

            // get person's account
            account = accountService.getHolidaysAccount(year, person);

            if (account != null) {
                accounts.put(person, account);

                BigDecimal vacationDaysLeft = calculationService.calculateLeftVacationDays(account);
                leftDays.put(person, vacationDaysLeft);
            }
        }

        model.addAttribute(ControllerConstants.PERSONS, persons);
        model.addAttribute(PersonConstants.GRAVATAR_URLS, gravatarUrls);
        model.addAttribute(ControllerConstants.ACCOUNTS, accounts);
        model.addAttribute("leftDays", leftDays);
        model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());
    }


    /**
     * Default personal overview for user: information about one's leave accounts, entitlement of holidays, list of
     * applications, etc. If there is no parameter set for year, take current year for view.
     *
     * @param model
     * @return
     */
    @RequestMapping(value = OVERVIEW_LINK, method = RequestMethod.GET)
    public String showPersonalOverview(Model model) {

        if (securityUtil.isInactive()) {
            return ControllerConstants.LOGIN_LINK;
        } else {
            Person person = securityUtil.getLoggedUser();
            prepareOverview(person, DateMidnight.now(GregorianChronology.getInstance()).getYear(), model);
            securityUtil.setLoggedUser(model);

            return PersonConstants.OVERVIEW_JSP;
        }
    }


    /**
     * Personal overview with year as parameter for user: information about one's leave accounts, entitlement of
     * holidays, list of applications, etc.
     *
     * @param year
     * @param model
     * @return
     */
    @RequestMapping(value = OVERVIEW_LINK, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showPersonalOverviewByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        if (securityUtil.isInactive()) {
            return ControllerConstants.LOGIN_LINK;
        } else {
            Person person = securityUtil.getLoggedUser();
            prepareOverview(person, year, model);
            securityUtil.setLoggedUser(model);

            return PersonConstants.OVERVIEW_JSP;
        }
    }


    /**
     * The office is able to see overviews of staff with information about this person's leave accounts, entitlement of
     * holidays, list of applications, etc. The default overview of a staff member (no parameter set for year) is for
     * the current year.
     *
     * @param personId
     * @param model
     * @return
     */
    @RequestMapping(value = OVERVIEW_STAFF_LINK, method = RequestMethod.GET)
    public String showStaffOverview(@PathVariable(PersonConstants.PERSON_ID) Integer personId, Model model) {

        if (securityUtil.isOffice()) {
            Person person = personService.getPersonByID(personId);
            List<Person> persons;

            if (person.isActive()) {
                persons = personService.getAllPersons();
            } else {
                persons = personService.getInactivePersons();
            }

            model.addAttribute(ControllerConstants.PERSONS, persons);

            prepareOverview(person, DateMidnight.now(GregorianChronology.getInstance()).getYear(), model);
            
            securityUtil.setLoggedUser(model);

            return PersonConstants.OVERVIEW_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * The office is able to see overviews of staff members with information about this person's leave accounts,
     * entitlement of holidays, list of applications, etc.; staff overview with year as parameter.
     *
     * @param personId
     * @param year
     * @param model
     * @return
     */
    @RequestMapping(value = OVERVIEW_STAFF_LINK, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showStaffOverviewByYear(@PathVariable(PersonConstants.PERSON_ID) Integer personId,
                                          @RequestParam(ControllerConstants.YEAR) int year, Model model) {

        if (securityUtil.isOffice()) {
            Person person = personService.getPersonByID(personId);

            List<Person> persons;

            if (person.isActive()) {
                persons = personService.getAllPersons();
            } else {
                persons = personService.getInactivePersons();
            }

            model.addAttribute(ControllerConstants.PERSONS, persons);

            prepareOverview(person, year, model);
            securityUtil.setLoggedUser(model);

            return PersonConstants.OVERVIEW_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Prepares model for overview with given person and year.
     *
     * @param person
     * @param year
     * @param model
     */
    private void prepareOverview(Person person, int year, Model model) {

        // get the person's applications for the given year
        List<Application> apps = applicationService.getAllApplicationsByPersonAndYear(person, year);

        if (!apps.isEmpty()) {
            List<Application> applications = new ArrayList<Application>();

            BigDecimal numberOfHolidayDays = BigDecimal.valueOf(0);
            BigDecimal numberOfSpecialLeaveDays = BigDecimal.ZERO;
            BigDecimal numberOfUnpaidLeaveDays = BigDecimal.ZERO;
            BigDecimal numberOfOvertimeDays = BigDecimal.ZERO;

            for (Application a : apps) {
                if (a.getStatus() != ApplicationStatus.CANCELLED) {
                    applications.add(a);

                    if (a.getStatus() == ApplicationStatus.ALLOWED || a.getStatus() == ApplicationStatus.WAITING) {
                        BigDecimal days = BigDecimal.ZERO;

                        if (a.getStartDate().getYear() != a.getEndDate().getYear()) {
                            // 2 possibilities:
                            // e.g.
                            // Dez. 2011 - Jan. 2012 ------ year ------ Dez. 2012 - Jan. 2013

                            if (a.getStartDate().getYear() == year - 1 && a.getEndDate().getYear() == year) {
                                days = calendarService.getVacationDays(a,
                                        new DateMidnight(a.getEndDate().getYear(), DateTimeConstants.JANUARY, 1),
                                        a.getEndDate());
                            } else if (a.getStartDate().getYear() == year && a.getEndDate().getYear() == year + 1) {
                                days = calendarService.getVacationDays(a, a.getStartDate(),
                                        new DateMidnight(a.getStartDate().getYear(), DateTimeConstants.DECEMBER, 31));
                            }
                        } else {
                            days = a.getDays();
                        }

                        switch (a.getVacationType()) {
                            case HOLIDAY:
                                numberOfHolidayDays = numberOfHolidayDays.add(days);
                                break;

                            case SPECIALLEAVE:
                                numberOfSpecialLeaveDays = numberOfSpecialLeaveDays.add(days);
                                break;

                            case UNPAIDLEAVE:
                                numberOfUnpaidLeaveDays = numberOfUnpaidLeaveDays.add(days);
                                break;

                            case OVERTIME:
                                numberOfOvertimeDays = numberOfOvertimeDays.add(days);
                                break;
                        }
                    }
                } else {
                    if (a.isFormerlyAllowed() == true) {
                        applications.add(a);
                    }
                }
            }

            if (applications.isEmpty()) {
                model.addAttribute(PersonConstants.NO_APPS, true);
            } else {
                model.addAttribute(ControllerConstants.APPLICATIONS, applications);
            }

            model.addAttribute("numberOfHolidayDays", numberOfHolidayDays);
            model.addAttribute("numberOfSpecialLeaveDays", numberOfSpecialLeaveDays);
            model.addAttribute("numberOfUnpaidLeaveDays", numberOfUnpaidLeaveDays);
            model.addAttribute("numberOfOvertimeDays", numberOfOvertimeDays);
        }

        // get person's holidays account and entitlement for the given year
        Account account = accountService.getHolidaysAccount(year, person);

        if (account != null) {
            BigDecimal vacationDaysLeft = calculationService.calculateLeftVacationDays(account);
            model.addAttribute(PersonConstants.LEFT_DAYS, vacationDaysLeft);
        }

        securityUtil.setLoggedUser(model);
        model.addAttribute(ControllerConstants.PERSON, person);
        model.addAttribute(ControllerConstants.ACCOUNT, account);
        model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());

        // get url of person's gravatar image
        String url = gravatarUtil.createImgURL(person.getEmail());
        model.addAttribute(PersonConstants.GRAVATAR, url);
    }

}
