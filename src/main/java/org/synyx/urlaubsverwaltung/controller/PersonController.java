
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

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.service.ApplicationService;
import org.synyx.urlaubsverwaltung.service.CryptoService;
import org.synyx.urlaubsverwaltung.service.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.DateUtil;

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

    // attribute names
    private static final String PERSON = "person";
    private static final String PERSONS = "persons";
    private static final String ACCOUNT = "account";
    private static final String ACCOUNTS = "accounts";
    private static final String APPLICATIONS = "applications";
    private static final String ENTITLEMENT = "entitlement";
    private static final String ENTITLEMENTS = "entitlements";
    private static final String APRIL = "april";

    private static final String PERSON_ID = "personId";
    private static final String YEAR = "year";

    // links
    private static final String WEB = "web/";
    private static final String LIST_LINK = "/staff/list";
    private static final String DETAIL_LINK = "/staff/detail";
    private static final String OVERVIEW_LINK = "/staff/{" + PERSON_ID + "}/overview";
    private static final String EDIT_LINK = "/staff/{" + PERSON_ID + "}/edit";

    // logger
    private static final Logger LOG = Logger.getLogger(PersonController.class);

    private PersonService personService;
    private ApplicationService applicationService;
    private HolidaysAccountService accountService;
    private CryptoService cryptoService;

    public PersonController(PersonService personService, ApplicationService applicationService,
        HolidaysAccountService accountService, CryptoService cryptoService) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.accountService = accountService;
        this.cryptoService = cryptoService;
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

        setLoggedUser(model);

        prepareStaffView(model);

        return LIST_JSP;
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

        setLoggedUser(model);

        prepareStaffView(model);

        return DETAIL_JSP;
    }


    /**
     * prepares view of staffs; preparing is for both views (list and detail) identic
     *
     * @param  model
     */
    private void prepareStaffView(Model model) {

        int year = DateMidnight.now(GregorianChronology.getInstance()).getYear();

        List<Person> persons = personService.getAllPersons();

        Map<Person, HolidaysAccount> accounts = new HashMap<Person, HolidaysAccount>();
        HolidaysAccount account;

        Map<Person, HolidayEntitlement> entitlements = new HashMap<Person, HolidayEntitlement>();
        HolidayEntitlement entitlement;

        for (Person person : persons) {
            account = accountService.getHolidaysAccount(year, person);

            if (account != null) {
                accounts.put(person, account);
            }

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
        model.addAttribute(ACCOUNTS, accounts);
        model.addAttribute(ENTITLEMENTS, entitlements);
        model.addAttribute(APRIL, april);
    }


    /**
     * Overview for user: information about one's holiday accounts, etc.
     *
     * @param  personId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = OVERVIEW_LINK, method = RequestMethod.GET)
    public String showOverview(@PathVariable(PERSON_ID) Integer personId, Model model) {

        Person person = personService.getPersonByID(personId);
        int year = DateMidnight.now(GregorianChronology.getInstance()).getYear();

        List<Application> applications = applicationService.getAllApplicationsForPerson(person);
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

        return OVERVIEW_JSP;
    }


    /**
     * Form to edit a user.
     *
     * @param  personId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = EDIT_LINK, method = RequestMethod.GET)
    public String editPersonForm(@PathVariable(PERSON_ID) Integer personId, Model model) {

        Person person = personService.getPersonByID(personId);

        setLoggedUser(model);
        model.addAttribute(PERSON, person);

        return PERSON_FORM_JSP;
    }


    /**
     * Speichert Datenaenderungen eines Staffs ab.
     *
     * @param  person
     * @param  personId
     *
     * @return
     */
    @RequestMapping(value = EDIT_LINK, method = RequestMethod.PUT)
    public String editPerson(@ModelAttribute(PERSON) Person person,
        @PathVariable(PERSON_ID) Integer personId) {

        Person personToUpdate = personService.getPersonByID(personId);

        personToUpdate.setLastName(person.getLastName());
        personToUpdate.setFirstName(person.getFirstName());
        personToUpdate.setEmail(person.getEmail());

        personService.save(personToUpdate);

        // to be implemented....
// int year = person.getYearForCurrentUrlaubsanspruch();
//
// accountService.newUrlaubsanspruch(person, year, person.getCurrentUrlaubsanspruch().doubleValue());
// accountService.newUrlaubskonto(person, person.getCurrentUrlaubsanspruch().doubleValue(), 0.0, year);

        LOG.info("Der Staff " + person.getFirstName() + " " + person.getLastName() + " wurde editiert.");

        return "redirect:" + WEB + LIST_LINK;
    }


    private void setLoggedUser(Model model) {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        Person loggedUser = personService.getPersonByLogin(user);

        model.addAttribute("loggedUser", loggedUser);
    }
}
