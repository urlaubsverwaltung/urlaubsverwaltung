
package org.synyx.urlaubsverwaltung.person.web;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;

import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.web.SecurityUtil;
import org.synyx.urlaubsverwaltung.util.NumberUtil;
import org.synyx.urlaubsverwaltung.validator.PersonValidator;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;

import java.math.BigDecimal;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class PersonManagementController {

    private static final String ACTIVE_LINK = "/staff";
    private static final String NEW_LINK = ACTIVE_LINK + "/new";
    private static final String EDIT_LINK = ACTIVE_LINK + "/{" + PersonConstants.PERSON_ID + "}/edit";
    private static final String DEACTIVATE_LINK = ACTIVE_LINK + "/{" + PersonConstants.PERSON_ID + "}/deactivate";
    private static final String ACTIVATE_LINK = ACTIVE_LINK + "/{" + PersonConstants.PERSON_ID + "}/activate";

    private PersonService personService;
    private AccountService accountService;
    private PersonValidator validator;
    private SecurityUtil securityUtil;

    public PersonManagementController(PersonService personService, AccountService accountService,
        PersonValidator validator, SecurityUtil securityUtil) {

        this.personService = personService;
        this.accountService = accountService;
        this.validator = validator;
        this.securityUtil = securityUtil;
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

        if (securityUtil.isOffice()) {
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

        if (securityUtil.isOffice()) {
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

        securityUtil.setLoggedUser(model);
        model.addAttribute(ControllerConstants.PERSON, person);
        model.addAttribute(PersonConstants.PERSONFORM, personForm);
        model.addAttribute("currentYear", DateMidnight.now().getYear());
    }


    /**
     * Prepares the view object PersonForm and returns jsp with form to create a new user.
     *
     * @param  request
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = NEW_LINK, method = RequestMethod.GET)
    public String newPersonForm(HttpServletRequest request, Model model) {

        if (securityUtil.isOffice()) {
            Person person = new Person();

            PersonForm personForm = new PersonForm();
            personForm.setDefaultValuesForValidity();
            addModelAttributesForPersonForm(person, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
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

        if (errors.hasGlobalErrors()) {
            model.addAttribute("errors", errors);
        }

        validator.validateAnnualVacation(personForm, errors, locale); // validates holiday entitlement's

        // vacation days

        validator.validateRemainingVacationDays(personForm, errors, locale); // validates holiday

        // entitlement's remaining
        // vacation days

        if (errors.hasErrors()) {
            addModelAttributesForPersonForm(personToUpdate, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        }

        personService.createOrUpdate(personToUpdate, personForm);

        return "redirect:/web/staff/" + personToUpdate.getId() + "/overview";
    }


    @RequestMapping(value = NEW_LINK, method = RequestMethod.POST)
    public String newPerson(HttpServletRequest request,
        @ModelAttribute(PersonConstants.PERSONFORM) PersonForm personForm, Errors errors, Model model) {

        Locale locale = RequestContextUtils.getLocale(request);

        Person person = new Person();

        validator.validateProperties(personForm, errors); // validates if the set value of the property key is valid

        if (errors.hasErrors()) {
            addModelAttributesForPersonForm(person, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        }

        validator.validate(personForm, errors); // validates the name fields, the email field and the year field

        if (errors.hasGlobalErrors()) {
            model.addAttribute("errors", errors);
        }

        // validate login name
        validator.validateLogin(personForm.getLoginName(), errors);

        validator.validateAnnualVacation(personForm, errors, locale); // validates holiday entitlement's

        // vacation days

        validator.validateRemainingVacationDays(personForm, errors, locale); // validates holiday

        // entitlement's remaining
        // vacation days

        if (errors.hasErrors()) {
            addModelAttributesForPersonForm(person, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        }

        personService.createOrUpdate(person, personForm);

        return "redirect:/web/staff/" + person.getId() + "/overview";
    }


    /**
     * This method deactivates a person, i.e. information about a deactivated person remains, but he/she has no right to
     * login, to apply for leave, etc.
     *
     * @param  personId
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
     * @param  personId
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
}
