package org.synyx.urlaubsverwaltung.person.web;

import org.joda.time.DateMidnight;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.web.SecurityUtil;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.util.GravatarUtil;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Controller for management of {@link Person} entities.
 *
 * @author  Aljona Murygina
 */
@Controller
public class PersonController {

    // links
    private static final String ACTIVE_LINK = "/staff";
    private static final String INACTIVE_LINK = "/staff/inactive";

    private PersonService personService;
    private AccountService accountService;
    private CalculationService calculationService;
    private GravatarUtil gravatarUtil;
    private SecurityUtil securityUtil;

    public PersonController(PersonService personService, AccountService accountService,
        CalculationService calculationService, GravatarUtil gravatarUtil, SecurityUtil securityUtil) {

        this.personService = personService;
        this.accountService = accountService;
        this.calculationService = calculationService;
        this.gravatarUtil = gravatarUtil;
        this.securityUtil = securityUtil;
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
     * @param  model
     *
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
     * @param  year
     * @param  model
     *
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
     * @param  year
     * @param  model
     *
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


    @RequestMapping(value = "/staff/print", params = { ControllerConstants.YEAR, "active" }, method = RequestMethod.GET)
    public String showPrintStaffList(@RequestParam(ControllerConstants.YEAR) int year,
        @RequestParam("active") boolean active, Model model) {

        if (securityUtil.isOffice()) {
            securityUtil.setLoggedUser(model);

            List<Person> persons;

            if (active) {
                persons = personService.getAllPersons();
            } else {
                persons = personService.getInactivePersons();
            }

            if (persons.isEmpty()) {
                model.addAttribute(PersonConstants.NOTEXISTENT, true);
                model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());
            } else {
                prepareStaffView(persons, year, model);
                model.addAttribute("today", DateMidnight.now());
            }

            return "person/staff_list_print";
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * prepares view of staffs; preparing is for both views (list and detail) identic.
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
        Map<Person, BigDecimal> remLeftDays = new HashMap<Person, BigDecimal>();

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

                BigDecimal remVacationDaysLeft = calculationService.calculateLeftRemainingVacationDays(account);
                remLeftDays.put(person, remVacationDaysLeft);
            }
        }

        model.addAttribute(ControllerConstants.PERSONS, persons);
        model.addAttribute(PersonConstants.GRAVATAR_URLS, gravatarUrls);
        model.addAttribute(ControllerConstants.ACCOUNTS, accounts);
        model.addAttribute(PersonConstants.LEFT_DAYS, leftDays);
        model.addAttribute(PersonConstants.REM_LEFT_DAYS, remLeftDays);
        model.addAttribute(PersonConstants.BEFORE_APRIL, DateUtil.isBeforeApril(DateMidnight.now()));
        model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());
    }
}
