
package org.synyx.urlaubsverwaltung.web.person;

import com.google.common.base.Optional;

import org.joda.time.DateMidnight;

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
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.validator.PersonValidator;

import java.math.BigDecimal;

import java.util.Locale;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class PersonManagementController {

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

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
    }


    @RequestMapping(value = "/staff/new", method = RequestMethod.GET)
    public String newPersonForm(Model model) {

        if (!sessionService.isOffice()) {
            return ControllerConstants.ERROR_JSP;
        }

        model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());
        model.addAttribute("personForm", new PersonForm());
        model.addAttribute("weekDays", Day.values());

        return PersonConstants.PERSON_FORM_JSP;
    }


    @RequestMapping(value = "/staff/new", method = RequestMethod.POST)
    public String newPerson(@ModelAttribute("personForm") PersonForm personForm, Errors errors, Model model) {

        if (!sessionService.isOffice()) {
            return ControllerConstants.ERROR_JSP;
        }

        // validate login name
        validator.validateLogin(personForm.getLoginName(), errors);
        validator.validate(personForm, errors);

        if (errors.hasGlobalErrors()) {
            model.addAttribute("errors", errors);
        }

        if (errors.hasErrors()) {
            model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());
            model.addAttribute("personForm", personForm);
            model.addAttribute("weekDays", Day.values());

            return PersonConstants.PERSON_FORM_JSP;
        }

        personInteractionService.create(personForm);

        return "redirect:/web/staff/";
    }


    @RequestMapping(value = "/staff/{personId}/edit", method = RequestMethod.GET)
    public String editPersonForm(@PathVariable("personId") Integer personId,
        @RequestParam(value = ControllerConstants.YEAR, required = false) Integer year, Model model) {

        if (!sessionService.isOffice()) {
            return ControllerConstants.ERROR_JSP;
        }

        int yearOfHolidaysAccount;

        if (year != null) {
            yearOfHolidaysAccount = year;
        } else {
            yearOfHolidaysAccount = DateMidnight.now().getYear();
        }

        Person person = personService.getPersonByID(personId);

        if (person == null) {
            return ControllerConstants.ERROR_JSP;
        }

        Optional<Account> account = accountService.getHolidaysAccount(yearOfHolidaysAccount, person);
        Optional<WorkingTime> workingTime = workingTimeService.getCurrentOne(person);

        PersonForm personForm = new PersonForm(person, yearOfHolidaysAccount, account, workingTime,
                person.getPermissions(), person.getNotifications());

        model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());
        model.addAttribute("personForm", personForm);
        model.addAttribute("weekDays", Day.values());
        model.addAttribute("workingTimes", workingTimeService.getByPerson(person));

        return PersonConstants.PERSON_FORM_JSP;
    }


    @RequestMapping(value = "/staff/{personId}/edit", method = RequestMethod.PUT)
    public String editPerson(@PathVariable("personId") Integer personId,
        @ModelAttribute("personForm") PersonForm personForm, Errors errors, Model model) {

        if (!sessionService.isOffice()) {
            return ControllerConstants.ERROR_JSP;
        }

        Person personToUpdate = personService.getPersonByID(personId);

        validator.validate(personForm, errors);

        if (errors.hasGlobalErrors()) {
            model.addAttribute("errors", errors);
        }

        if (errors.hasErrors()) {
            model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());
            model.addAttribute("personForm", personForm);
            model.addAttribute("weekDays", Day.values());
            model.addAttribute("workingTimes", workingTimeService.getByPerson(personToUpdate));

            return PersonConstants.PERSON_FORM_JSP;
        }

        personInteractionService.update(personForm);

        return "redirect:/web/staff/";
    }
}
