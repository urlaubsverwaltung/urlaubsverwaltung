
package org.synyx.urlaubsverwaltung.web.person;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.calendar.Day;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonInteractionService;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.util.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.validator.PersonValidator;

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

    @Autowired
    private PersonInteractionService personInteractionService;

    @Autowired
    private PersonService personService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PersonValidator validator;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private WorkingTimeService workingTimeService;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor(locale));
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

        if (sessionService.isOffice()) {
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

        if (sessionService.isOffice()) {
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

        WorkingTime workingTime = workingTimeService.getCurrentOne(person);

        return new PersonForm(person, String.valueOf(year), account, workingTime, person.getPermissions(),
                person.getNotifications(), locale);
    }


    /**
     * Adding attributes to model.
     *
     * @param  person
     * @param  personForm
     * @param  model
     */
    private void addModelAttributesForPersonForm(Person person, PersonForm personForm, Model model) {

        sessionService.setLoggedUser(model);
        model.addAttribute(ControllerConstants.PERSON, person);
        model.addAttribute(PersonConstants.PERSONFORM, personForm);
        model.addAttribute("currentYear", DateMidnight.now().getYear());
        model.addAttribute("weekDays", Day.values());

        if (person.getId() != null) {
            model.addAttribute("workingTimes", workingTimeService.getByPerson(person));
        }
    }


    /**
     * Prepares the view object PersonForm and returns jsp with form to touch a new user.
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = NEW_LINK, method = RequestMethod.GET)
    public String newPersonForm(Model model) {

        if (sessionService.isOffice()) {
            Person person = new Person();

            PersonForm personForm = new PersonForm();
            addModelAttributesForPersonForm(person, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Gets information out of view object PersonForm and edits the concerning person and their entitlement to holidays
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

        personForm.setLocale(locale); // needed for number parsing
        validator.validate(personForm, errors);

        if (errors.hasGlobalErrors()) {
            model.addAttribute("errors", errors);
        }

        if (errors.hasErrors()) {
            addModelAttributesForPersonForm(personToUpdate, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        }

        personInteractionService.createOrUpdate(personToUpdate, personForm, locale);

        return "redirect:/web/staff/";
    }


    @RequestMapping(value = NEW_LINK, method = RequestMethod.POST)
    public String newPerson(HttpServletRequest request,
        @ModelAttribute(PersonConstants.PERSONFORM) PersonForm personForm, Errors errors, Model model) {

        Locale locale = RequestContextUtils.getLocale(request);

        Person person = new Person();

        // validate login name
        validator.validateLogin(personForm.getLoginName(), errors);

        personForm.setLocale(locale); // needed for number parsing
        validator.validate(personForm, errors);

        if (errors.hasGlobalErrors()) {
            model.addAttribute("errors", errors);
        }

        if (errors.hasErrors()) {
            addModelAttributesForPersonForm(person, personForm, model);

            return PersonConstants.PERSON_FORM_JSP;
        }

        personInteractionService.createOrUpdate(person, personForm, locale);

        return "redirect:/web/staff/";
    }
}
