package org.synyx.urlaubsverwaltung.web.application;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.validator.ApplicationValidator;

import java.util.Comparator;
import java.util.List;


/**
 * Controller to apply for leave.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@RequestMapping("/application")
@Controller
public class ApplyForLeaveController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PersonService personService;

    @Autowired
    private AccountInteractionService accountInteractionService;

    @Autowired
    private ApplicationInteractionService applicationInteractionService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private ApplicationValidator applicationValidator;

    @InitBinder
    public void initBinder(DataBinder binder) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    /**
     * Show form to apply for leave.
     *
     * @param  personId  of the person that applies for leave
     * @param  model  to be filled
     *
     * @return  form to apply for leave
     */
    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newApplicationForm(@RequestParam(value = "personId", required = false) Integer personId,
        Model model) {

        if (sessionService.isInactive()) {
            return ControllerConstants.ERROR_JSP;
        }

        Person person;
        Person applier;

        if (personId == null) {
            person = sessionService.getLoggedUser();
            applier = person;
        } else {
            person = personService.getPersonByID(personId);
            applier = sessionService.getLoggedUser();
        }

        boolean isApplyingForOneSelf = person.equals(applier);

        // only office may apply for leave on behalf of other users
        if (!isApplyingForOneSelf && !sessionService.isOffice()) {
            return ControllerConstants.ERROR_JSP;
        }

        Account holidaysAccount = accountInteractionService.getHolidaysAccount(DateMidnight.now().getYear(), person);

        if (holidaysAccount == null) {
            model.addAttribute("notpossible", true);
        } else {
            prepareApplicationForLeaveForm(person, new ApplicationForLeaveForm(), model);
        }

        return ControllerConstants.APPLICATIONS_URL + "/app_form";
    }


    private void prepareApplicationForLeaveForm(Person person, ApplicationForLeaveForm appForm, Model model) {

        List<Person> persons = FluentIterable.from(personService.getActivePersons()).toSortedList(
                new Comparator<Person>() {

                    @Override
                    public int compare(Person p1, Person p2) {

                        String niceName1 = p1.getNiceName();
                        String niceName2 = p2.getNiceName();

                        return niceName1.toLowerCase().compareTo(niceName2.toLowerCase());
                    }
                });

        Account account = accountInteractionService.getHolidaysAccount(DateMidnight.now(
                    GregorianChronology.getInstance()).getYear(), person);

        if (account != null) {
            model.addAttribute("vacationDaysLeft", calculationService.getVacationDaysLeft(account));
            model.addAttribute(PersonConstants.BEFORE_APRIL, DateUtil.isBeforeApril(DateMidnight.now()));
        }

        model.addAttribute("person", person);
        model.addAttribute("persons", persons);
        model.addAttribute("date", DateMidnight.now(GregorianChronology.getInstance()));
        model.addAttribute(ControllerConstants.YEAR, DateMidnight.now(GregorianChronology.getInstance()).getYear());
        model.addAttribute("appForm", appForm);
        model.addAttribute("account", account);
        model.addAttribute("vacTypes", VacationType.values());
        model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());
    }


    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String newApplication(@RequestParam(value = "personId", required = false) Integer personId,
        @ModelAttribute("appForm") ApplicationForLeaveForm appForm, Errors errors, Model model) {

        Person applier = sessionService.getLoggedUser();
        Person personToApplyForLeave;

        if (personId == null) {
            personToApplyForLeave = applier;
        } else {
            personToApplyForLeave = personService.getPersonByID(personId);
        }

        applicationValidator.validate(appForm, errors);

        if (errors.hasErrors()) {
            prepareApplicationForLeaveForm(personToApplyForLeave, appForm, model);

            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            return ControllerConstants.APPLICATIONS_URL + "/app_form";
        }

        Application application = appForm.generateApplicationForLeave();

        Application savedApplicationForLeave = applicationInteractionService.apply(application, applier,
                Optional.fromNullable(appForm.getComment()));

        return "redirect:/web/application/" + savedApplicationForLeave.getId();
    }
}
