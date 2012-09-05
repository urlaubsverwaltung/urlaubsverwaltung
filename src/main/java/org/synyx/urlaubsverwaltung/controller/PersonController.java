package org.synyx.urlaubsverwaltung.controller;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
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

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;
import org.synyx.urlaubsverwaltung.service.ApplicationService;
import org.synyx.urlaubsverwaltung.service.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.DateUtil;
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
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;

/**
 * @author  Aljona Murygina
 */
@Controller
public class PersonController {

    // jsps
    private static final String OVERVIEW_JSP = "person/overview"; // jsp for personal overview
    private static final String OVERVIEW_OFFICE_JSP = "person/overview_office"; // jsp for office's overview
    private static final String STAFF_JSP = "person/staff_view";
    private static final String PERSON_FORM_JSP = "person/person_form";
    private static final String ERROR_JSP = "error";
    // attribute names
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final String LOGGED_USER = "loggedUser";
    private static final String PERSON = "person";
    private static final String PERSONS = "persons";
    private static final String PERSONFORM = "personForm";
    private static final String ACCOUNT = "account";
    private static final String ACCOUNTS = "accounts";
    private static final String APPLICATIONS = "applications";
    private static final String USED_DAYS = "usedDays";
    private static final String APRIL = "april";
    private static final String GRAVATAR = "gravatar";
    private static final String GRAVATAR_URLS = "gravatarUrls";
    private static final String NOTEXISTENT = "notexistent"; // are there any persons to show?
    private static final String NO_APPS = "noapps"; // are there any applications to show?
    private static final String IS_OFFICE = "isOffice";
    private static final String PERSON_ID = "personId";
    private static final String YEAR = "year";
    // links
    private static final String ACTIVE_LINK = "/staff";
    private static final String INACTIVE_LINK = "/staff/inactive";
    private static final String OVERVIEW_LINK = "/overview"; // personal overview
    private static final String OVERVIEW_STAFF_LINK = "/staff/{" + PERSON_ID + "}/overview"; // overview of other person
    private static final String EDIT_LINK = "/staff/{" + PERSON_ID + "}/edit";
    private static final String DEACTIVATE_LINK = "/staff/{" + PERSON_ID + "}/deactivate";
    private static final String ACTIVATE_LINK = "/staff/{" + PERSON_ID + "}/activate";
    private static final String LOGIN_LINK = "redirect:/login.jsp?login_error=1";
    // audit logger: logs nontechnically occurences like 'user x applied for leave' or 'subtracted n days from
    // holidays account y'
    private static final Logger LOG = Logger.getLogger("audit");
    private PersonService personService;
    private ApplicationService applicationService;
    private HolidaysAccountService accountService;
    private GravatarUtil gravatarUtil;
    private PersonValidator validator;

    public PersonController(PersonService personService, ApplicationService applicationService,
            HolidaysAccountService accountService, GravatarUtil gravatarUtil, PersonValidator validator) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.accountService = accountService;
        this.gravatarUtil = gravatarUtil;
        this.validator = validator;
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
                model.addAttribute(NOTEXISTENT, true);
            } else {
                prepareStaffView(persons, DateMidnight.now().getYear(), model);
            }

            return STAFF_JSP;
        } else {
            return ERROR_JSP;
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

            return STAFF_JSP;
        } else {
            return ERROR_JSP;
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
    @RequestMapping(value = INACTIVE_LINK, params = YEAR, method = RequestMethod.GET)
    public String showInactiveStaffByYear(@RequestParam(YEAR) int year, Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE) {
            setLoggedUser(model);

            List<Person> persons = personService.getInactivePersons();

            if (persons.isEmpty()) {
                model.addAttribute(NOTEXISTENT, true);
            } else {
                prepareStaffView(persons, year, model);
            }

            return STAFF_JSP;
        } else {
            return ERROR_JSP;
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
    @RequestMapping(value = ACTIVE_LINK, params = YEAR, method = RequestMethod.GET)
    public String showActiveStaffByYear(@RequestParam(YEAR) int year, Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE) {
            setLoggedUser(model);

            List<Person> persons = personService.getAllPersons();
            prepareStaffView(persons, year, model);

            return STAFF_JSP;
        } else {
            return ERROR_JSP;
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
            }

        }

        addAprilAttributeToModel(model);
        model.addAttribute(PERSONS, persons);
        model.addAttribute(GRAVATAR_URLS, gravatarUrls);
        model.addAttribute(ACCOUNTS, accounts);
        model.addAttribute(YEAR, DateMidnight.now().getYear());
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
            return LOGIN_LINK;
        } else {
            Person person = getLoggedUser();
            prepareOverview(person, DateMidnight.now(GregorianChronology.getInstance()).getYear(), model);
            model.addAttribute(IS_OFFICE, false);

            return OVERVIEW_JSP;
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
    @RequestMapping(value = OVERVIEW_LINK, params = YEAR, method = RequestMethod.GET)
    public String showPersonalOverviewByYear(@RequestParam(YEAR) int year, Model model) {

        if (getLoggedUser().getRole() == Role.INACTIVE) {
            return LOGIN_LINK;
        } else {
            Person person = getLoggedUser();
            prepareOverview(person, year, model);
            model.addAttribute(IS_OFFICE, false);

            return OVERVIEW_JSP;
        }
    }

    /**
     * The office is able to see overviews of staff with information about this person's leave accounts, entitlement of
     * holidays, list of applications, etc. The default overview of a staff member (no parameter set for year) is for
     * the current year.
     * 
     * @param personId
     * @param model
     * 
     * @return 
     */
    @RequestMapping(value = OVERVIEW_STAFF_LINK, method = RequestMethod.GET)
    public String showStaffOverview(@PathVariable(PERSON_ID) Integer personId, Model model) {

        if (getLoggedUser().getRole() != Role.OFFICE) {
            return ERROR_JSP;
        } else {
            Person person = personService.getPersonByID(personId);
            List<Person> persons;

            if (person.isActive()) {
                persons = personService.getAllPersons();
            } else {
                persons = personService.getInactivePersons();
            }

            model.addAttribute(PERSONS, persons);

            prepareOverview(person, DateMidnight.now(GregorianChronology.getInstance()).getYear(), model);
            model.addAttribute(IS_OFFICE, true);

            return OVERVIEW_OFFICE_JSP;
        }
    }

    /**
     * The office is able to see overviews of staff members with information about this person's leave accounts,
     * entitlement of holidays, list of applications, etc.; staff overview with year as parameter.
     * 
     * @param personId
     * @param year
     * @param model
     * 
     * @return 
     */
    @RequestMapping(value = OVERVIEW_STAFF_LINK, params = YEAR, method = RequestMethod.GET)
    public String showStaffOverviewByYear(@PathVariable(PERSON_ID) Integer personId,
            @RequestParam(YEAR) int year, Model model) {

        if (getLoggedUser().getRole() != Role.OFFICE) {
            return ERROR_JSP;
        } else {
            Person person = personService.getPersonByID(personId);

            List<Person> persons;

            if (person.isActive()) {
                persons = personService.getAllPersons();
            } else {
                persons = personService.getInactivePersons();
            }

            model.addAttribute(PERSONS, persons);

            prepareOverview(person, year, model);
            model.addAttribute(IS_OFFICE, true);

            return OVERVIEW_OFFICE_JSP;
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

        // TODO: modify!
        
//        // get the person's applications for the given year
//        List<Application> apps = applicationService.getAllApplicationsByPersonAndYear(person, year);
//
//        if (apps.isEmpty()) {
//            model.addAttribute(NO_APPS, true);
//        } else {
//            List<Application> applications = new ArrayList<Application>();
//
//            for (Application a : apps) {
//                if (a.getStatus() != ApplicationStatus.CANCELLED) {
//                    applications.add(a);
//                } else {
//                    if (a.isFormerlyAllowed() == true) {
//                        applications.add(a);
//                    }
//                }
//            }
//
//            model.addAttribute(APPLICATIONS, applications);
//
//        }
//
//
//
//        // get the number of vacation days that person has used in the given year
//        BigDecimal numberOfUsedDays = applicationService.getUsedVacationDaysOfPersonForYear(person, year);
//        model.addAttribute(USED_DAYS, numberOfUsedDays);
//
//        // get person's holidays account and entitlement for the given year
//        Account account = accountService.getHolidaysAccount(year, person);
//
//        setLoggedUser(model);
//        addAprilAttributeToModel(model);
//        model.addAttribute(PERSON, person);
//        model.addAttribute(ACCOUNT, account);
//        model.addAttribute(YEAR, DateMidnight.now().getYear());
//
//        // get url of person's gravatar image
//        String url = gravatarUtil.createImgURL(person.getEmail());
//        model.addAttribute(GRAVATAR, url);
    }

    /**
     * Prepares the view object PersonForm and returns jsp with form to edit a user.
     * 
     * @param request
     * @param personId
     * @param model
     * 
     * @return 
     */
    @RequestMapping(value = EDIT_LINK, method = RequestMethod.GET)
    public String editPersonForm(HttpServletRequest request,
            @PathVariable(PERSON_ID) Integer personId, Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE) {
            Person person = personService.getPersonByID(personId);

            int year = DateMidnight.now(GregorianChronology.getInstance()).getYear();

            Locale locale = RequestContextUtils.getLocale(request);

            PersonForm personForm = preparePersonForm(year, person, locale);
            addModelAttributesForPersonForm(person, personForm, model);

            return PERSON_FORM_JSP;
        } else {
            return ERROR_JSP;
        }
    }

    /**
     * Prepares the view object PersonForm and returns jsp with form to edit a user.
     * 
     * @param request
     * @param year
     * @param personId
     * @param model
     * 
     * @return 
     */
    @RequestMapping(value = EDIT_LINK, params = YEAR, method = RequestMethod.GET)
    public String editPersonFormForYear(HttpServletRequest request,
            @RequestParam(YEAR) int year,
            @PathVariable(PERSON_ID) Integer personId, Model model) {

        int currentYear = DateMidnight.now().getYear();

        if (year - currentYear > 2 || currentYear - year > 2) {
            return ERROR_JSP;
        }

        if (getLoggedUser().getRole() == Role.OFFICE) {
            Person person = personService.getPersonByID(personId);

            Locale locale = RequestContextUtils.getLocale(request);

            PersonForm personForm = preparePersonForm(year, person, locale);
            addModelAttributesForPersonForm(person, personForm, model);

            return PERSON_FORM_JSP;
        } else {
            return ERROR_JSP;
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
        model.addAttribute(PERSON, person);
        model.addAttribute(PERSONFORM, personForm);
        model.addAttribute("currentYear", DateMidnight.now().getYear());
    }

    /**
     * Gets informations out of view object PersonForm and edits the concerning person and their entitlement to holidays account.
     * 
     * @param request
     * @param personId
     * @param personForm
     * @param errors
     * @param model
     * 
     * @return 
     */
    @RequestMapping(value = EDIT_LINK, method = RequestMethod.PUT)
    public String editPerson(HttpServletRequest request,
            @PathVariable(PERSON_ID) Integer personId,
            @ModelAttribute(PERSONFORM) PersonForm personForm, Errors errors, Model model) {

        Locale locale = RequestContextUtils.getLocale(request);

        Person personToUpdate = personService.getPersonByID(personId);

        validator.validateProperties(personForm, errors); // validates if the set value of the property key is valid

        if (errors.hasErrors()) {
            addModelAttributesForPersonForm(personToUpdate, personForm, model);

            return PERSON_FORM_JSP;
        }

        validator.validate(personForm, errors); // validates the name fields, the email field and the year field

        validator.validateAnnualVacation(personForm, errors, locale); // validates holiday entitlement's
        // vacation days

        validator.validateRemainingVacationDays(personForm, errors, locale); // validates holiday
        // entitlement's remaining
        // vacation days

        if (errors.hasErrors()) {
            addModelAttributesForPersonForm(personToUpdate, personForm, model);

            return PERSON_FORM_JSP;
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
            accountService.createHolidaysAccount(personToUpdate, validFrom, validTo, annualVacationDays, remainingVacationDays, expiring);
        } else {
            accountService.editHolidaysAccount(account, validFrom, validTo, annualVacationDays, remainingVacationDays, expiring);
        }

        LOG.info(DateMidnight.now(GregorianChronology.getInstance()).toString(DATE_FORMAT) + " ID: " + personId
                + " Der Mitarbeiter " + personToUpdate.getFirstName() + " " + personToUpdate.getLastName()
                + " wurde editiert.");


        return "redirect:/web" + ACTIVE_LINK;
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
    public String deactivatePerson(@PathVariable(PERSON_ID) Integer personId) {

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
    public String activatePerson(@PathVariable(PERSON_ID) Integer personId) {

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

        model.addAttribute(LOGGED_USER, getLoggedUser());
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

    /**
     * If current date is before April, the value of the attribute 'april' is 1, otherwise the value is 0.
     * @param model
     * @return model containing attribute 'april'
     */
    private Model addAprilAttributeToModel(Model model) {

        DateMidnight date = DateMidnight.now(GregorianChronology.getInstance());
        int april = 0;

        if (DateUtil.isBeforeApril(date)) {
            april = 1;
        }

        model.addAttribute(APRIL, april);

        return model;

    }
}
