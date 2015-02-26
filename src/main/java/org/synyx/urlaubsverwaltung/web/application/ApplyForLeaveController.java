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
import org.synyx.urlaubsverwaltung.core.application.domain.OverlapCase;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.application.service.OverlapService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.util.CalcUtil;
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
    private ApplicationValidator applicationValidator;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationInteractionService applicationInteractionService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private OverlapService overlapService;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor(locale));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    /**
     * Show form to apply an application for leave.
     *
     * @param  personId
     * @param  model
     *
     * @return  form to apply an application for leave
     */
    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newApplicationForm(@RequestParam(value = "personId", required = false) Integer personId,
        Model model) {

        if (sessionService.isInactive()) {
            return ControllerConstants.ERROR_JSP;
        }

        Person person;
        Person applicant;

        if (personId == null) {
            person = sessionService.getLoggedUser();
            applicant = person;
        } else {
            person = personService.getPersonByID(personId);
            applicant = sessionService.getLoggedUser();
        }

        boolean isApplyingForOneSelf = person.equals(applicant);

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

        Person person;

        if (personId == null) {
            person = sessionService.getLoggedUser();
        } else {
            person = personService.getPersonByID(personId);
        }

        return newApplication(person, appForm, true, errors, model);
    }


    private String newApplication(Person person, AppForm appForm, boolean isOffice, Errors errors, Model model) {

        Person personForForm;

        if (isOffice) {
            personForForm = person;
        } else {
            personForForm = sessionService.getLoggedUser();
        }

        applicationValidator.validate(appForm, errors);

        if (errors.hasErrors()) {
            prepareApplicationForLeaveForm(personForForm, appForm, model);

            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            return ControllerConstants.APPLICATION + "/app_form";
        }

        if (checkAndSaveApplicationForm(appForm, errors)) {
            int id = applicationService.getIdOfLatestApplication(personForForm, ApplicationStatus.WAITING);

            return "redirect:/web/application/" + id;
        } else {
            prepareApplicationForLeaveForm(personForForm, appForm, model);

            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            return ControllerConstants.APPLICATION + "/app_form";
        }
    }


    /**
     * This method checks if there are overlapping applications and if the user has enough vacation days to apply for
     * leave.
     *
     * @param  appForm
     * @param  errors
     *
     * @return  true if everything is alright and application can be saved, else false
     */
    private boolean checkAndSaveApplicationForm(AppForm appForm, Errors errors) {

        Application application = appForm.createApplicationObject();

        BigDecimal days = applicationInteractionService.getNumberOfVacationDays(application);

        // ensure that no one applies for leave for a vacation of 0 days
        if (CalcUtil.isZero(days)) {
            errors.reject("check.zero");

            return false;
        }

        // check at first if there are existent application for the same period

        // checkOverlap
        // case 1: ok
        // case 2: new application is fully part of existent applications, useless to apply it
        // case 3: gaps in between - feature in later version, now only error message

        OverlapCase overlap = overlapService.checkOverlap(application);

        boolean isOverlapping = overlap == OverlapCase.FULLY_OVERLAPPING || overlap == OverlapCase.PARTLY_OVERLAPPING;

        if (isOverlapping) {
            // in this version, these two cases are handled equal
            errors.reject("check.overlap");

            return false;
        }

        // if there is no overlap go to next check but only if vacation type is holiday, else you don't have to
        // check if there are enough days on user's holidays account
        boolean enoughDays = false;
        boolean isHoliday = application.getVacationType() == VacationType.HOLIDAY;

        if (isHoliday) {
            enoughDays = calculationService.checkApplication(application);
        }

        boolean mayApplyForLeave = (isHoliday && enoughDays) || !isHoliday;

        if (mayApplyForLeave) {
            applicationInteractionService.apply(application, sessionService.getLoggedUser());

            return true;
        } else {
            errors.reject("check.enough");

            return false;
        }
    }
}
