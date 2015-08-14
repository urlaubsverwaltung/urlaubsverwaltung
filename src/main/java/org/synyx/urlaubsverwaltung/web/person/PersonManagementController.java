
package org.synyx.urlaubsverwaltung.web.person;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;

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

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.calendar.Day;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonInteractionService;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.validator.PersonValidator;

import java.math.BigDecimal;

import java.util.Locale;
import java.util.Optional;


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
    private WorkingTimeService workingTimeService;

    @Autowired
    private DepartmentService departmentService;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/staff/new", method = RequestMethod.GET)
    public String newPersonForm(Model model) {

        model.addAttribute("personForm", new PersonForm());
        model.addAttribute("weekDays", Day.values());

        return PersonConstants.PERSON_FORM_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/staff/new", method = RequestMethod.POST)
    public String newPerson(@ModelAttribute("personForm") PersonForm personForm, Errors errors, Model model) {

        validator.validateLogin(personForm.getLoginName(), errors);
        validator.validate(personForm, errors);

        if (errors.hasGlobalErrors()) {
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
        }

        if (errors.hasErrors()) {
            model.addAttribute("personForm", personForm);
            model.addAttribute("weekDays", Day.values());

            return PersonConstants.PERSON_FORM_JSP;
        }

        personInteractionService.create(personForm);

        return "redirect:/web/staff/";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/staff/{personId}/edit", method = RequestMethod.GET)
    public String editPersonForm(@PathVariable("personId") Integer personId,
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Integer year, Model model) {

        int yearOfHolidaysAccount;

        if (year != null) {
            yearOfHolidaysAccount = year;
        } else {
            yearOfHolidaysAccount = DateMidnight.now().getYear();
        }

        java.util.Optional<Person> optionalPerson = personService.getPersonByID(personId);

        if (!optionalPerson.isPresent()) {
            return ControllerConstants.ERROR_JSP;
        }

        Person person = optionalPerson.get();
        Optional<Account> account = accountService.getHolidaysAccount(yearOfHolidaysAccount, person);
        Optional<WorkingTime> workingTime = workingTimeService.getCurrentOne(person);

        PersonForm personForm = new PersonForm(person, yearOfHolidaysAccount, account, workingTime,
                person.getPermissions(), person.getNotifications());

        model.addAttribute("personForm", personForm);
        model.addAttribute("weekDays", Day.values());
        model.addAttribute("workingTimes", workingTimeService.getByPerson(person));
        model.addAttribute("departments", departmentService.getDepartmentsOfPerson(person));

        return PersonConstants.PERSON_FORM_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/staff/{personId}/edit", method = RequestMethod.PUT)
    public String editPerson(@PathVariable("personId") Integer personId,
        @ModelAttribute("personForm") PersonForm personForm, Errors errors, Model model) {

        java.util.Optional<Person> personToUpdate = personService.getPersonByID(personId);

        if (!personToUpdate.isPresent()) {
            return ControllerConstants.ERROR_JSP;
        }

        validator.validate(personForm, errors);

        if (errors.hasGlobalErrors()) {
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
        }

        if (errors.hasErrors()) {
            model.addAttribute("personForm", personForm);
            model.addAttribute("weekDays", Day.values());
            model.addAttribute("workingTimes", workingTimeService.getByPerson(personToUpdate.get()));

            return PersonConstants.PERSON_FORM_JSP;
        }

        personInteractionService.update(personForm);

        return "redirect:/web/staff/";
    }
}
