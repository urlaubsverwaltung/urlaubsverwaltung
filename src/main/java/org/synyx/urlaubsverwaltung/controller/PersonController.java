
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

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;
import org.synyx.urlaubsverwaltung.service.ApplicationService;
import org.synyx.urlaubsverwaltung.service.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.util.GravatarUtil;
import org.synyx.urlaubsverwaltung.validator.PersonValidator;
import org.synyx.urlaubsverwaltung.view.PersonForm;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private static final String ENTITLEMENT = "entitlement";
    private static final String ENTITLEMENTS = "entitlements";
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

    // logger
    private static final Logger LOG = Logger.getLogger(PersonController.class);

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
     * @param  model
     */
    private void prepareStaffView(List<Person> persons, int year, Model model) {

        Map<Person, String> gravatarUrls = new HashMap<Person, String>();
        String url;

        Map<Person, HolidaysAccount> accounts = new HashMap<Person, HolidaysAccount>();
        HolidaysAccount account;

        Map<Person, HolidayEntitlement> entitlements = new HashMap<Person, HolidayEntitlement>();
        HolidayEntitlement entitlement;

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

            // get person's entitlement
            entitlement = accountService.getHolidayEntitlement(year, person);

            if (entitlement != null) {
                entitlements.put(person, entitlement);
            }
        }

        int april = 0;

        int month = DateMidnight.now(GregorianChronology.getInstance()).getMonthOfYear();

        if (month >= 1 && month <= 3) {
            april = 1;
        }

        model.addAttribute(PERSONS, persons);
        model.addAttribute(GRAVATAR_URLS, gravatarUrls);
        model.addAttribute(ACCOUNTS, accounts);
        model.addAttribute(ENTITLEMENTS, entitlements);
        model.addAttribute(APRIL, april);
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
     * @param  model
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
     * @param  year
     * @param  model
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

        List<Application> applications = applicationService.getApplicationsByPersonAndYear(person, year);

        if (applications.isEmpty()) {
            model.addAttribute(NO_APPS, true);
        } else {
            model.addAttribute(APPLICATIONS, applications);
        }

        HolidaysAccount account = accountService.getHolidaysAccount(year, person);
        HolidayEntitlement entitlement = accountService.getHolidayEntitlement(year, person);
        DateMidnight date = DateMidnight.now(GregorianChronology.getInstance());
        int april = 0;

        if (DateUtil.isBeforeApril(date)) {
            april = 1;
        }

        setLoggedUser(model);
        model.addAttribute(PERSON, person);
        model.addAttribute(ACCOUNT, account);
        model.addAttribute(ENTITLEMENT, entitlement);
        model.addAttribute(YEAR, date.getYear());
        model.addAttribute(APRIL, april);

        // get url of person's gravatar image
        String url = gravatarUtil.createImgURL(person.getEmail());
        model.addAttribute(GRAVATAR, url);
    }


    /**
     * Prepares the view object PersonForm and returns jsp with form to edit a user.
     *
     * @param  personId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = EDIT_LINK, method = RequestMethod.GET)
    public String editPersonForm(@PathVariable(PERSON_ID) Integer personId, Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE) {
            Person person = personService.getPersonByID(personId);

            int year = DateMidnight.now(GregorianChronology.getInstance()).getYear();
            BigDecimal daysEnt = null;
            BigDecimal remainingEnt = null;
            BigDecimal daysAcc = null;
            BigDecimal remainingAcc = null;
            boolean daysExpire = true;

            HolidayEntitlement entitlement = accountService.getHolidayEntitlement(year, person);

            if (entitlement != null) {
                daysEnt = entitlement.getVacationDays();
                remainingEnt = entitlement.getRemainingVacationDays();
            }

            HolidaysAccount account = accountService.getHolidaysAccount(year, person);

            if (account != null) {
                daysAcc = account.getVacationDays();
                remainingAcc = account.getRemainingVacationDays();
            }

            PersonForm personForm = new PersonForm(person, Integer.toString(year), daysEnt, remainingEnt, daysAcc,
                    remainingAcc, daysExpire);

            preparePersonForm(person, personForm, model);

            return PERSON_FORM_JSP;
        } else {
            return ERROR_JSP;
        }
    }


    private void preparePersonForm(Person person, PersonForm personForm, Model model) {

        setLoggedUser(model);
        model.addAttribute(PERSON, person);
        model.addAttribute(PERSONFORM, personForm);
        model.addAttribute("currentYear", DateMidnight.now().getYear());
    }


    /**
     * Gets informations out of view object PersonForm and edits the concerning person and their entitlement to holidays
     * and leave account.
     *
     * @param  personForm
     * @param  personId
     *
     * @return
     */
    @RequestMapping(value = EDIT_LINK, method = RequestMethod.PUT)
    public String editPerson(@PathVariable(PERSON_ID) Integer personId,
        @ModelAttribute(PERSONFORM) PersonForm personForm, Errors errors, Model model) {

        Person personToUpdate = personService.getPersonByID(personId);

        validator.validate(personForm, errors); // validates all fields except the account's days fields
        validator.validateAccountDays(personForm, errors); // validates holidays account's remaining vacation days and
                                                           // vacation days

        if (errors.hasErrors()) {
            preparePersonForm(personToUpdate, personForm, model);

            return PERSON_FORM_JSP;
        } else {
            // set person information from PersonForm object on person that is updated
            personToUpdate = personForm.fillPersonObject(personToUpdate);

            personService.save(personToUpdate);

            int year = Integer.parseInt(personForm.getYear());
            BigDecimal daysEnt = personForm.getVacationDaysEnt();
            BigDecimal remainingEnt = personForm.getRemainingVacationDaysEnt();

            // check if there is an existing entitlement to holidays
            HolidayEntitlement entitlement = accountService.getHolidayEntitlement(year, personToUpdate);

            // if not, create one
            if (entitlement == null) {
                entitlement = accountService.newHolidayEntitlement(personToUpdate, year, daysEnt, remainingEnt);
                accountService.saveHolidayEntitlement(entitlement);
            } else {
                accountService.editHolidayEntitlement(entitlement, daysEnt, remainingEnt);
            }

            HolidaysAccount account = accountService.getHolidaysAccount(year, personToUpdate);

            BigDecimal daysAcc = personForm.getVacationDaysAcc();
            BigDecimal remainingAcc = personForm.getRemainingVacationDaysAcc();
            boolean remainingDaysExpire = personForm.isRemainingVacationDaysExpireAcc();

            if (account == null) {
                account = accountService.newHolidaysAccount(personToUpdate, year, daysAcc, remainingAcc,
                        remainingDaysExpire);
                accountService.saveHolidaysAccount(account);
            } else {
                accountService.editHolidaysAccount(account, daysAcc, remainingAcc, remainingDaysExpire);
            }

            LOG.info(DateMidnight.now(GregorianChronology.getInstance()).toString(DATE_FORMAT) + " ID: " + personId
                + " Der Mitarbeiter " + personToUpdate.getFirstName() + " " + personToUpdate.getLastName()
                + " wurde editiert.");

            return "redirect:/web" + ACTIVE_LINK;
        }
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
}
