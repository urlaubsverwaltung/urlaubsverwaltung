
package org.synyx.urlaubsverwaltung.controller;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

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
    private static final String OVERVIEW_JSP = "person/overview";
    private static final String LIST_JSP = "person/staff_list";
    private static final String DETAIL_JSP = "person/staff_detail";
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

    private static final String PERSON_ID = "personId";
    private static final String YEAR = "year";

    // links
    private static final String LIST_LINK = "/staff/list";
    private static final String DETAIL_LINK = "/staff/detail";
    private static final String INACTIVE_LINK = "/staff/inactive";
    private static final String OVERVIEW_LINK = "/overview";
    private static final String EDIT_LINK = "/staff/{" + PERSON_ID + "}/edit";
    private static final String DEACTIVATE_LINK = "/staff/{" + PERSON_ID + "}/deactivate";
    private static final String LOGIN_LINK = "redirect:/login.jsp?login_error=1";

    // logger
    private static final Logger LOG = Logger.getLogger(PersonController.class);

    private PersonService personService;
    private ApplicationService applicationService;
    private HolidaysAccountService accountService;
    private GravatarUtil gravatarUtil;

    public PersonController(PersonService personService, ApplicationService applicationService,
        HolidaysAccountService accountService, GravatarUtil gravatarUtil) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.accountService = accountService;
        this.gravatarUtil = gravatarUtil;
    }

    /**
     * view of staffs and their number of vacation days (as list)
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = LIST_LINK, method = RequestMethod.GET)
    public String showStaffList(Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE) {
            setLoggedUser(model);

            List<Person> persons = personService.getAllPersons();
            prepareStaffView(persons, model);

            return LIST_JSP;
        } else {
            return ERROR_JSP;
        }
    }


    /**
     * view of staffs and their number of vacation days (detailed)
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = DETAIL_LINK, method = RequestMethod.GET)
    public String showStaffDetail(Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE) {
            setLoggedUser(model);

            List<Person> persons = personService.getAllPersons();
            prepareStaffView(persons, model);

            return DETAIL_JSP;
        } else {
            return ERROR_JSP;
        }
    }


    /**
     * view of inactive staff
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
            prepareStaffView(persons, model);

            return DETAIL_JSP;
        } else {
            return ERROR_JSP;
        }
    }


    /**
     * prepares view of staffs; preparing is for both views (list and detail) identic
     *
     * @param  model
     */
    private void prepareStaffView(List<Person> persons, Model model) {

        int year = DateMidnight.now(GregorianChronology.getInstance()).getYear();

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
     * Default overview for user: information about one's leave accounts, entitlement of holidays, list of applications,
     * etc. If there is no parameter set for year, take current year for view.
     *
     * @param  personId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = OVERVIEW_LINK, method = RequestMethod.GET)
    public String showDefaultOverview(Model model) {

        if (getLoggedUser().getRole() == Role.INACTIVE) {
            return LOGIN_LINK;
        } else {
            prepareOverview(DateMidnight.now(GregorianChronology.getInstance()).getYear(), model);

            return OVERVIEW_JSP;
        }
    }


    /**
     * Overview with year as parameter for user: information about one's leave accounts, entitlement of holidays, list
     * of applications, etc.
     *
     * @param  personId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = OVERVIEW_LINK, params = "year", method = RequestMethod.GET)
    public String showOverview(@RequestParam("year") int year, Model model) {

        if (getLoggedUser().getRole() == Role.INACTIVE) {
            return LOGIN_LINK;
        } else {
            prepareOverview(year, model);

            return OVERVIEW_JSP;
        }
    }


    /**
     * Prepares model for overview with given person's ID and year.
     *
     * @param  personId
     * @param  year
     * @param  model
     */
    private void prepareOverview(int year, Model model) {

        Person person = getLoggedUser();

        List<Application> applications = applicationService.getApplicationsByPersonAndYear(person, year);
        HolidaysAccount account = accountService.getHolidaysAccount(year, person);
        HolidayEntitlement entitlement = accountService.getHolidayEntitlement(year, person);
        DateMidnight date = DateMidnight.now(GregorianChronology.getInstance());
        int april = 0;

        if (DateUtil.isBeforeApril(date)) {
            april = 1;
        }

        setLoggedUser(model);
        model.addAttribute(PERSON, person);
        model.addAttribute(APPLICATIONS, applications);
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
            BigDecimal days = null;

            HolidayEntitlement entitlement = accountService.getHolidayEntitlement(year, person);

            if (entitlement != null) {
                days = entitlement.getVacationDays();
            }

            PersonForm personForm = new PersonForm(person, year, days);

            setLoggedUser(model);
            model.addAttribute(PERSON, person);
            model.addAttribute(PERSONFORM, personForm);

            return PERSON_FORM_JSP;
        } else {
            return ERROR_JSP;
        }
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
    public String editPerson(@ModelAttribute(PERSONFORM) PersonForm personForm,
        @PathVariable(PERSON_ID) Integer personId) {

        Person personToUpdate = personService.getPersonByID(personId);

        // set person information from PersonForm object on person that is updated
        personToUpdate = personForm.fillPersonObject(personToUpdate);

        personService.save(personToUpdate);

        // check if there is an existing entitlement to holidays
        HolidayEntitlement entitlement = accountService.getHolidayEntitlement(personForm.getYear(), personToUpdate);

        // if not, create one
        if (entitlement == null) {
            entitlement = accountService.newHolidayEntitlement(personToUpdate, personForm.getYear(),
                    personForm.getVacationDays());
            accountService.saveHolidayEntitlement(entitlement);
        } else {
            // if there is an entitlement: set current entitlement to inactive and creates a new active one with changed
            // information do this with current leave account too
            accountService.editHolidayEntitlement(personToUpdate, personForm.getYear(), personForm.getVacationDays());
        }

        LOG.info(DateMidnight.now(GregorianChronology.getInstance()).toString(DATE_FORMAT) + " ID: " + personId
            + " Der Mitarbeiter " + personToUpdate.getFirstName() + " " + personToUpdate.getLastName()
            + " wurde editiert.");

        return "redirect:/web" + LIST_LINK;
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

        return "redirect:/web" + LIST_LINK;
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
