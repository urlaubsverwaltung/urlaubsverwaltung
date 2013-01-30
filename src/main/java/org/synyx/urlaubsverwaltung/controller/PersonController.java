package org.synyx.urlaubsverwaltung.controller;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.chrono.GregorianChronology;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;
import org.synyx.urlaubsverwaltung.service.ApplicationService;
import org.synyx.urlaubsverwaltung.service.CalculationService;
import org.synyx.urlaubsverwaltung.service.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.GravatarUtil;
import org.synyx.urlaubsverwaltung.util.NumberUtil;
import org.synyx.urlaubsverwaltung.validator.PersonValidator;
import org.synyx.urlaubsverwaltung.view.PersonForm;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 * @author  Aljona Murygina
 */
@Controller
public class PersonController {

    // links
    private static final String ACTIVE_LINK = "/staff";
    private static final String INACTIVE_LINK = "/staff/inactive";
    private static final String OVERVIEW_LINK = "/overview"; // personal overview
    private static final String OVERVIEW_STAFF_LINK = "/staff/{" + PersonConstants.PERSON_ID + "}/overview"; // overview of other person
    private static final String EDIT_LINK = "/staff/{" + PersonConstants.PERSON_ID + "}/edit";
    private static final String DEACTIVATE_LINK = "/staff/{" + PersonConstants.PERSON_ID + "}/deactivate";
    private static final String ACTIVATE_LINK = "/staff/{" + PersonConstants.PERSON_ID + "}/activate";

    // audit logger: logs nontechnically occurences like 'user x applied for leave' or 'subtracted n days from
    // holidays account y'
    private static final Logger LOG = Logger.getLogger("audit");
    private PersonService personService;
    private ApplicationService applicationService;
    private HolidaysAccountService accountService;
    private CalculationService calculationService;
    private GravatarUtil gravatarUtil;
    private PersonValidator validator;
    private OwnCalendarService calendarService;

    public PersonController(PersonService personService, ApplicationService applicationService,
        HolidaysAccountService accountService, CalculationService calculationService, GravatarUtil gravatarUtil,
        PersonValidator validator, OwnCalendarService calendarService) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.accountService = accountService;
        this.calculationService = calculationService;
        this.gravatarUtil = gravatarUtil;
        this.validator = validator;
        this.calendarService = calendarService;
    }

    /**
     * Shows list with inactive staff, default: for current year.
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = INACTIVE_LINK, method = RequestMethod.GET)
    public String showInactiveStaff(Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE) {
            setLoggedUser(model);

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
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = ACTIVE_LINK, method = RequestMethod.GET)
    public String showActiveStaff(Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE) {
            setLoggedUser(model);

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
     * @param  year
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = INACTIVE_LINK, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showInactiveStaffByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE) {
            setLoggedUser(model);

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
     * @param  year
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = ACTIVE_LINK, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showActiveStaffByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE) {
            setLoggedUser(model);

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
     * @param  persons
     * @param  year
     * @param  model
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
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = OVERVIEW_LINK, method = RequestMethod.GET)
    public String showPersonalOverview(Model model) {

        if (getLoggedUser().getRole() == Role.INACTIVE) {
            return ControllerConstants.LOGIN_LINK;
        } else {
            Person person = getLoggedUser();
            prepareOverview(person, DateMidnight.now(GregorianChronology.getInstance()).getYear(), model);
            model.addAttribute("loggedUser", person);

            return PersonConstants.OVERVIEW_JSP;
        }
    }


    /**
     * Personal overview with year as parameter for user: information about one's leave accounts, entitlement of
     * holidays, list of applications, etc.
     *
     * @param  year
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = OVERVIEW_LINK, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showPersonalOverviewByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        if (getLoggedUser().getRole() == Role.INACTIVE) {
            return ControllerConstants.LOGIN_LINK;
        } else {
            Person person = getLoggedUser();
            prepareOverview(person, year, model);
            model.addAttribute("loggedUser", person);

            return PersonConstants.OVERVIEW_JSP;
        }
    }


    /**
     * The office is able to see overviews of staff with information about this person's leave accounts, entitlement of
     * holidays, list of applications, etc. The default overview of a staff member (no parameter set for year) is for
     * the current year.
     *
     * @param  personId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = OVERVIEW_STAFF_LINK, method = RequestMethod.GET)
    public String showStaffOverview(@PathVariable(PersonConstants.PERSON_ID) Integer personId, Model model) {

        if (getLoggedUser().getRole() != Role.OFFICE) {
            return ControllerConstants.ERROR_JSP;
        } else {
            Person person = personService.getPersonByID(personId);
            List<Person> persons;

            if (person.isActive()) {
                persons = personService.getAllPersons();
            } else {
                persons = personService.getInactivePersons();
            }

            model.addAttribute(ControllerConstants.PERSONS, persons);

            prepareOverview(person, DateMidnight.now(GregorianChronology.getInstance()).getYear(), model);
            model.addAttribute("loggedUser", getLoggedUser());

            return PersonConstants.OVERVIEW_JSP;
        }
    }


    /**
     * The office is able to see overviews of staff members with information about this person's leave accounts,
     * entitlement of holidays, list of applications, etc.; staff overview with year as parameter.
     *
     * @param  personId
     * @param  year
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = OVERVIEW_STAFF_LINK, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showStaffOverviewByYear(@PathVariable(PersonConstants.PERSON_ID) Integer personId,
        @RequestParam(ControllerConstants.YEAR) int year, Model model) {

        if (getLoggedUser().getRole() != Role.OFFICE) {
            return ControllerConstants.ERROR_JSP;
        } else {
            Person person = personService.getPersonByID(personId);

            List<Person> persons;

            if (person.isActive()) {
                persons = personService.getAllPersons();
            } else {
                persons = personService.getInactivePersons();
            }

            model.addAttribute(ControllerConstants.PERSONS, persons);

            prepareOverview(person, year, model);
            model.addAttribute("loggedUser", getLoggedUser());

            return PersonConstants.OVERVIEW_JSP;
        }
    }


    /**
     * Prepares model for overview with given person and year.
     *
     * @param  person
     * @param  year
     * @param  model
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

        setLoggedUser(model);
        model.addAttribute(ControllerConstants.PERSON, person);
        model.addAttribute(ControllerConstants.ACCOUNT, account);
        model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());

        // get url of person's gravatar image
        String url = gravatarUtil.createImgURL(person.getEmail());
        model.addAttribute(PersonConstants.GRAVATAR, url);
    }


    /**
     * Prepares the view object PersonForm and returns jsp with form to edit a user.
     *
     * @param  request
     * @param  personId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = EDIT_LINK, method = RequestMethod.GET)
    public String editPersonForm(HttpServletRequest request,
        @PathVariable(PersonConstants.PERSON_ID) Integer personId, Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE) {
            Person person = personService.getPersonByID(personId);

            int year = DateMidnight.now(GregorianChronology.getInstance()).getYear();

            Locale locale = RequestContextUtils.getLocale(request);

            PersonForm personForm = preparePersonForm(year, person, locale);
            addModelAttributesForPersonForm(person, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Prepares the view object PersonForm and returns jsp with form to edit a user.
     *
     * @param  request
     * @param  year
     * @param  personId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = EDIT_LINK, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String editPersonFormForYear(HttpServletRequest request,
        @RequestParam(ControllerConstants.YEAR) int year,
        @PathVariable(PersonConstants.PERSON_ID) Integer personId, Model model) {

        int currentYear = DateMidnight.now().getYear();

        if (year - currentYear > 2 || currentYear - year > 2) {
            return ControllerConstants.ERROR_JSP;
        }

        if (getLoggedUser().getRole() == Role.OFFICE) {
            Person person = personService.getPersonByID(personId);

            Locale locale = RequestContextUtils.getLocale(request);

            PersonForm personForm = preparePersonForm(year, person, locale);
            addModelAttributesForPersonForm(person, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Prepares PersonForm object with the given parameters.
     *
     * @param  year
     * @param  person
     * @param  locale
     */
    private PersonForm preparePersonForm(int year, Person person, Locale locale) {

        Account account = accountService.getHolidaysAccount(year, person);

        BigDecimal annualVacationDays = null;
        BigDecimal remainingVacationDays = null;
        boolean remainingVacationDaysExpire = true;

        if (account != null) {
            annualVacationDays = account.getAnnualVacationDays();
            remainingVacationDays = account.getRemainingVacationDays();
            remainingVacationDaysExpire = account.isRemainingVacationDaysExpire();
        }

        String ann = "";
        String rem = "";

        if (annualVacationDays != null) {
            ann = NumberUtil.formatNumber(annualVacationDays, locale);
        }

        if (remainingVacationDays != null) {
            rem = NumberUtil.formatNumber(remainingVacationDays, locale);
        }

        return new PersonForm(person, String.valueOf(year), account, ann, rem, remainingVacationDaysExpire);
    }


    /**
     * Adding attributes to model.
     *
     * @param  person
     * @param  personForm
     * @param  model
     */
    private void addModelAttributesForPersonForm(Person person, PersonForm personForm, Model model) {

        setLoggedUser(model);
        model.addAttribute(ControllerConstants.PERSON, person);
        model.addAttribute(PersonConstants.PERSONFORM, personForm);
        model.addAttribute("currentYear", DateMidnight.now().getYear());
    }


    /**
     * Gets informations out of view object PersonForm and edits the concerning person and their entitlement to holidays
     * account.
     *
     * @param  request
     * @param  personId
     * @param  personForm
     * @param  errors
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = EDIT_LINK, method = RequestMethod.PUT)
    public String editPerson(HttpServletRequest request,
        @PathVariable(PersonConstants.PERSON_ID) Integer personId,
        @ModelAttribute(PersonConstants.PERSONFORM) PersonForm personForm, Errors errors, Model model) {

        Locale locale = RequestContextUtils.getLocale(request);

        Person personToUpdate = personService.getPersonByID(personId);

        validator.validateProperties(personForm, errors); // validates if the set value of the property key is valid

        if (errors.hasErrors()) {
            addModelAttributesForPersonForm(personToUpdate, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        }

        validator.validate(personForm, errors); // validates the name fields, the email field and the year field

        validator.validateAnnualVacation(personForm, errors, locale); // validates holiday entitlement's

        // vacation days

        validator.validateRemainingVacationDays(personForm, errors, locale); // validates holiday

        // entitlement's remaining
        // vacation days

        if (errors.hasErrors()) {
            addModelAttributesForPersonForm(personToUpdate, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        }

        // set person information from PersonForm object on person that is updated
        personToUpdate = personForm.fillPersonObject(personToUpdate);

        personService.save(personToUpdate);

        int year = Integer.parseInt(personForm.getYear());
        int dayFrom = Integer.parseInt(personForm.getDayFrom());
        int monthFrom = Integer.parseInt(personForm.getMonthFrom());
        int dayTo = Integer.parseInt(personForm.getDayTo());
        int monthTo = Integer.parseInt(personForm.getMonthTo());

        DateMidnight validFrom = new DateMidnight(year, monthFrom, dayFrom);
        DateMidnight validTo = new DateMidnight(year, monthTo, dayTo);

        BigDecimal annualVacationDays = new BigDecimal(personForm.getAnnualVacationDays());
        BigDecimal remainingVacationDays = new BigDecimal(personForm.getRemainingVacationDays());
        boolean expiring = personForm.isRemainingVacationDaysExpire();

        // check if there is an existing account
        Account account = accountService.getHolidaysAccount(year, personToUpdate);

        if (account == null) {
            accountService.createHolidaysAccount(personToUpdate, validFrom, validTo, annualVacationDays,
                remainingVacationDays, expiring);
        } else {
            accountService.editHolidaysAccount(account, validFrom, validTo, annualVacationDays, remainingVacationDays,
                expiring);
        }

        LOG.info(DateMidnight.now(GregorianChronology.getInstance()).toString(ControllerConstants.DATE_FORMAT) + " ID: " + personId
            + " Der Mitarbeiter " + personToUpdate.getFirstName() + " " + personToUpdate.getLastName()
            + " wurde editiert.");

        return "redirect:/web/staff/" + personToUpdate.getId() + "/overview";
    }


    /**
     * This method deactivates a person, i.e. information about a deactivated person remains, but he/she has no right to
     * login, to apply for leave, etc.
     *
     * @param  person
     *
     * @return
     */
    @RequestMapping(value = DEACTIVATE_LINK, method = RequestMethod.PUT)
    public String deactivatePerson(@PathVariable(PersonConstants.PERSON_ID) Integer personId) {

        Person person = personService.getPersonByID(personId);

        personService.deactivate(person);
        personService.save(person);

        return "redirect:/web" + ACTIVE_LINK;
    }


    /**
     * This method activates a person (e.g. after unintended deactivating of a person), i.e. this person has once again
     * his user rights)
     *
     * @param  person
     *
     * @return
     */
    @RequestMapping(value = ACTIVATE_LINK, method = RequestMethod.PUT)
    public String activatePerson(@PathVariable(PersonConstants.PERSON_ID) Integer personId) {

        Person person = personService.getPersonByID(personId);

        personService.activate(person);
        personService.save(person);

        return "redirect:/web" + ACTIVE_LINK;
    }


    /*
     * This method gets logged-in user and his username; with the username you get the person's ID to be able to show
     * overview of this person. Logged-in user is added to model.
     */
    private void setLoggedUser(Model model) {

        model.addAttribute(PersonConstants.LOGGED_USER, getLoggedUser());
    }


    /**
     * This method allows to get a person by logged-in user.
     *
     * @return  Person that is logged in
     */
    private Person getLoggedUser() {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        return personService.getPersonByLogin(user);
    }
}
