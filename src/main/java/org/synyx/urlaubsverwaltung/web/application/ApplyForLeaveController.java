package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.*;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.sicknote.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.web.util.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.validator.ApplicationValidator;

import java.math.BigDecimal;

import java.util.List;
import java.util.Locale;


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
    private AccountService accountService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationInteractionService applicationInteractionService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private ApplicationValidator applicationValidator;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor(locale));
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

        Account holidaysAccount = accountService.getHolidaysAccount(DateMidnight.now().getYear(), person);

        if (holidaysAccount == null) {
            model.addAttribute("notpossible", true);
        } else {
            prepareApplicationForLeaveForm(person, new AppForm(), model);
        }

        return ControllerConstants.APPLICATION + "/app_form";
    }


    private void prepareApplicationForLeaveForm(Person person, AppForm appForm, Model model) {

        List<Person> persons = personService.getActivePersons();

        Account account = accountService.getHolidaysAccount(DateMidnight.now(GregorianChronology.getInstance())
                .getYear(), person);

        if (account != null) {
            BigDecimal vacationDaysLeft = calculationService.calculateLeftVacationDays(account);
            BigDecimal remainingVacationDaysLeft = calculationService.calculateLeftRemainingVacationDays(account);
            model.addAttribute(PersonConstants.LEFT_DAYS, vacationDaysLeft);
            model.addAttribute(PersonConstants.REM_LEFT_DAYS, remainingVacationDaysLeft);
            model.addAttribute(PersonConstants.BEFORE_APRIL, DateUtil.isBeforeApril(DateMidnight.now()));
        }

        model.addAttribute(ControllerConstants.PERSON, person);
        model.addAttribute(ControllerConstants.PERSONS, persons);
        model.addAttribute("date", DateMidnight.now(GregorianChronology.getInstance()));
        model.addAttribute(ControllerConstants.YEAR, DateMidnight.now(GregorianChronology.getInstance()).getYear());
        model.addAttribute("appForm", appForm);
        model.addAttribute(ControllerConstants.ACCOUNT, account);
        model.addAttribute("vacTypes", VacationType.values());
        model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());
    }


    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String newApplication(@RequestParam(value = "personId", required = false) Integer personId,
        @ModelAttribute("appForm") AppForm appForm, Errors errors, Model model) {

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

            return ControllerConstants.APPLICATION + "/app_form";
        }

        Application application = appForm.createApplicationObject();

        applicationInteractionService.apply(application, applier);

        return "redirect:/web/application/"
            + applicationService.getIdOfLatestApplication(personToApplyForLeave, ApplicationStatus.WAITING);
    }
}
