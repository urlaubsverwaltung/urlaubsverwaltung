/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.controller;

import java.math.BigDecimal;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;
import org.synyx.urlaubsverwaltung.service.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.NumberUtil;
import org.synyx.urlaubsverwaltung.validator.PersonValidator;
import org.synyx.urlaubsverwaltung.view.PersonForm;

/**
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
public class UserManagementController {

    private static final String ACTIVE_LINK = "/staff";
    private static final String NEW_LINK = ACTIVE_LINK + "/new";
    private static final String EDIT_LINK = ACTIVE_LINK + "/{" + PersonConstants.PERSON_ID + "}/edit";
    private static final String DEACTIVATE_LINK = ACTIVE_LINK + "/{" + PersonConstants.PERSON_ID + "}/deactivate";
    private static final String ACTIVATE_LINK = ACTIVE_LINK + "/{" + PersonConstants.PERSON_ID + "}/activate";
    // audit logger: logs nontechnically occurences like 'user x applied for leave' or 'subtracted n days from
    // holidays account y'
    private static final Logger LOG = Logger.getLogger("audit");
    private PersonService personService;
    private HolidaysAccountService accountService;
    private PersonValidator validator;

    public UserManagementController(PersonService personService, HolidaysAccountService accountService,
            PersonValidator validator) {

        this.personService = personService;
        this.accountService = accountService;
        this.validator = validator;
    }

    /**
     * Prepares the view object PersonForm and returns jsp with form to edit a
     * user.
     *
     * @param request
     * @param personId
     * @param model
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
     * Prepares the view object PersonForm and returns jsp with form to edit a
     * user.
     *
     * @param request
     * @param year
     * @param personId
     * @param model
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
     * @param year
     * @param person
     * @param locale
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
     * @param person
     * @param personForm
     * @param model
     */
    private void addModelAttributesForPersonForm(Person person, PersonForm personForm, Model model) {

        setLoggedUser(model);
        model.addAttribute(ControllerConstants.PERSON, person);
        model.addAttribute(PersonConstants.PERSONFORM, personForm);
        model.addAttribute("currentYear", DateMidnight.now().getYear());
        
    }
    
   /**
     * Prepares the view object PersonForm and returns jsp with form to create a new
     * user.
     *
     * @param request
     * @param model
     *
     * @return
     */
    @RequestMapping(value = NEW_LINK, method = RequestMethod.GET)
    public String newPersonForm(HttpServletRequest request, Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE) {
            Person person = new Person();

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
     * Prepares the view object PersonForm and returns jsp with form to create a new
     * user.
     *
     * @param request
     * @param year
     * @param model
     *
     * @return
     */
    @RequestMapping(value = NEW_LINK, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String newPersonFormForYear(HttpServletRequest request,
            @RequestParam(ControllerConstants.YEAR) int year, Model model) {

        int currentYear = DateMidnight.now().getYear();

        if (year - currentYear > 2 || currentYear - year > 2) {
            return ControllerConstants.ERROR_JSP;
        }

        if (getLoggedUser().getRole() == Role.OFFICE) {
            Person person = new Person();

            Locale locale = RequestContextUtils.getLocale(request);

            PersonForm personForm = preparePersonForm(year, person, locale);
            addModelAttributesForPersonForm(person, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }

    /**
     * Gets informations out of view object PersonForm and edits the concerning
     * person and their entitlement to holidays account.
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

        saveOrUpdatePerson(personToUpdate, personForm);

        return "redirect:/web/staff/" + personToUpdate.getId() + "/overview";
    }

    @RequestMapping(value = NEW_LINK, method = RequestMethod.POST)
    public String newPerson(HttpServletRequest request, @ModelAttribute(PersonConstants.PERSONFORM) PersonForm personForm,
            Errors errors, Model model) {

        Locale locale = RequestContextUtils.getLocale(request);

        Person person = new Person();

        validator.validateProperties(personForm, errors); // validates if the set value of the property key is valid

        if (errors.hasErrors()) {
            addModelAttributesForPersonForm(person, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        }

        validator.validate(personForm, errors); // validates the name fields, the email field and the year field

        validator.validateAnnualVacation(personForm, errors, locale); // validates holiday entitlement's

        // vacation days

        validator.validateRemainingVacationDays(personForm, errors, locale); // validates holiday

        // entitlement's remaining
        // vacation days

        if (errors.hasErrors()) {
            addModelAttributesForPersonForm(person, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        }

        saveOrUpdatePerson(person, personForm);

        return "redirect:/web/staff/" + person.getId() + "/overview";
    }

    private void saveOrUpdatePerson(Person person, PersonForm personForm) {

        boolean newPerson = false;

        if (person.getId() == null) {
            newPerson = true;
        }

        // set person information from PersonForm object on person that is updated
        person = personForm.fillPersonObject(person);

        personService.save(person);

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
        Account account = accountService.getHolidaysAccount(year, person);

        if (account == null) {
            accountService.createHolidaysAccount(person, validFrom, validTo, annualVacationDays,
                    remainingVacationDays, expiring);
        } else {
            accountService.editHolidaysAccount(account, validFrom, validTo, annualVacationDays, remainingVacationDays,
                    expiring);
        }

        if (newPerson) {
            LOG.info(DateMidnight.now(GregorianChronology.getInstance()).toString(ControllerConstants.DATE_FORMAT) + " Ein neuer Mitarbeiter wurde angelegt. ID: " + person.getId()
                    + ", Vorname: " + person.getFirstName() + ", Nachname: " + person.getLastName());
        } else {
            LOG.info(DateMidnight.now(GregorianChronology.getInstance()).toString(ControllerConstants.DATE_FORMAT) + " ID: " + person.getId()
                    + " Der Mitarbeiter " + person.getFirstName() + " " + person.getLastName()
                    + " wurde editiert.");
        }
    }

    /**
     * This method deactivates a person, i.e. information about a deactivated
     * person remains, but he/she has no right to login, to apply for leave,
     * etc.
     *
     * @param person
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
     * This method activates a person (e.g. after unintended deactivating of a
     * person), i.e. this person has once again his user rights)
     *
     * @param person
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
     * @return Person that is logged in
     */
    private Person getLoggedUser() {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        return personService.getPersonByLogin(user);
    }
}
