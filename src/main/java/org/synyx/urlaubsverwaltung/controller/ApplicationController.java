
package org.synyx.urlaubsverwaltung.controller;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.springframework.security.core.context.SecurityContextHolder;

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

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Comment;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;
import org.synyx.urlaubsverwaltung.domain.VacationType;
import org.synyx.urlaubsverwaltung.service.ApplicationService;
import org.synyx.urlaubsverwaltung.service.CommentService;
import org.synyx.urlaubsverwaltung.service.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.service.MailService;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.util.GravatarUtil;
import org.synyx.urlaubsverwaltung.validator.ApplicationValidator;
import org.synyx.urlaubsverwaltung.view.AppForm;

import java.util.List;
import java.util.Locale;


/**
 * @author  Aljona Murygina
 */
@Controller
public class ApplicationController {

    // attribute names
    private static final String LOGGED_USER = "loggedUser";
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final String COMMENT = "comment";
    private static final String APPFORM = "appForm";
    private static final String APPLICATION = "application";
    private static final String APPLICATIONS = "applications";
    private static final String ACCOUNT = "account";
    private static final String ENTITLEMENT = "entitlement";
    private static final String PERSON = "person";
    private static final String PERSONS = "persons";
    private static final String YEAR = "year";
    private static final String DATE = "date";
    private static final String VACTYPES = "vacTypes";
    private static final String FULL = "full";
    private static final String MORNING = "morning";
    private static final String NOON = "noon";
    private static final String NOTPOSSIBLE = "notpossible"; // is it possible for user to apply for leave? (no, if
                                                             // he/she has no account/entitlement)
    private static final String NO_APPS = "noapps"; // are there any applications to show?
    private static final String APRIL = "april";
    private static final String GRAVATAR = "gravatar";

    private static final String APPLICATION_ID = "applicationId";

    // jsps
    private static final String SHOW_APP_DETAIL = APPLICATION + "/app_detail";
    private static final String APP_LIST_JSP = APPLICATION + "/list";
    private static final String APP_FORM_JSP = APPLICATION + "/app_form";
    private static final String ERROR_JSP = "error";

    // login link
    private static final String LOGIN_LINK = "redirect:/login.jsp?login_error=1";

    // overview
    private static final String OVERVIEW = "/overview";

    // links start with...
    private static final String SHORT_PATH_APPLICATION = "/" + APPLICATION;
    private static final String LONG_PATH_APPLICATION = "/" + APPLICATION + "/{";

    // list of applications by state
    private static final String WAITING_APPS = SHORT_PATH_APPLICATION + "/waiting";
    private static final String ALLOWED_APPS = SHORT_PATH_APPLICATION + "/allowed";
    private static final String CANCELLED_APPS = SHORT_PATH_APPLICATION + "/cancelled";
    private static final String REJECTED_APPS = SHORT_PATH_APPLICATION + "/rejected"; // not used now, but maybe useful

    // order applications by certain numbers
    private static final String STATE_NUMBER = "stateNumber";
    private static final int WAITING = 0;
    private static final int ALLOWED = 1;
    private static final int CANCELLED = 2;
    private static final int TO_CANCEL = 4;

    // form to apply vacation
    private static final String NEW_APP = SHORT_PATH_APPLICATION + "/new";

    // for user: the only way editing an application for user is to cancel it
    // (application may have state waiting or allowed)
    private static final String CANCEL_APP = LONG_PATH_APPLICATION + APPLICATION_ID + "}/cancel";

    // detailed view of application
    private static final String SHOW_APP = LONG_PATH_APPLICATION + APPLICATION_ID + "}";

    // allow or reject application
    private static final String ALLOW_APP = LONG_PATH_APPLICATION + APPLICATION_ID + "}/allow";
    private static final String REJECT_APP = LONG_PATH_APPLICATION + APPLICATION_ID + "}/reject";

    // add sick days to application
    private static final String SICK_DAYS = LONG_PATH_APPLICATION + APPLICATION_ID + "}/sick";

    // logger
    private static final Logger LOG = Logger.getLogger(PersonController.class);

    private PersonService personService;
    private ApplicationService applicationService;
    private HolidaysAccountService accountService;
    private CommentService commentService;
    private ApplicationValidator validator;
    private GravatarUtil gravatarUtil;
    private MailService mailService;

    public ApplicationController(PersonService personService, ApplicationService applicationService,
        HolidaysAccountService accountService, CommentService commentService, ApplicationValidator validator,
        GravatarUtil gravatarUtil, MailService mailService) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.accountService = accountService;
        this.commentService = commentService;
        this.validator = validator;
        this.gravatarUtil = gravatarUtil;
        this.mailService = mailService;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor(locale));
    }


    private void setApplications(List<Application> applications, Model model) {

        if (applications.isEmpty()) {
            model.addAttribute(NO_APPS, true);
        } else {
            model.addAttribute(APPLICATIONS, applications);
        }
    }


    /**
     * used if you want to see all waiting applications
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = WAITING_APPS, method = RequestMethod.GET)
    public String showWaiting(Model model) {

        if (getLoggedUser().getRole() == Role.BOSS) {
            List<Application> applications = applicationService.getApplicationsByState(ApplicationStatus.WAITING);

            setApplications(applications, model);

            model.addAttribute(STATE_NUMBER, WAITING);
            setLoggedUser(model);

            return APP_LIST_JSP;
        } else {
            prepareErrorJsp(null, model);

            return ERROR_JSP;
        }
    }


    /**
     * used if you want to see all allowed applications
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = ALLOWED_APPS, method = RequestMethod.GET)
    public String showAllowed(Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE || getLoggedUser().getRole() == Role.BOSS) {
            List<Application> applications = applicationService.getApplicationsByState(ApplicationStatus.ALLOWED);

            setApplications(applications, model);

            model.addAttribute(STATE_NUMBER, ALLOWED);
            setLoggedUser(model);

            return APP_LIST_JSP;
        } else {
            prepareErrorJsp(null, model);

            return ERROR_JSP;
        }
    }


    /**
     * used if you want to see all cancelled applications
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = CANCELLED_APPS, method = RequestMethod.GET)
    public String showCancelled(Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE || getLoggedUser().getRole() == Role.BOSS) {
            List<Application> applications = applicationService.getApplicationsByState(ApplicationStatus.CANCELLED);

            setApplications(applications, model);

            model.addAttribute(STATE_NUMBER, CANCELLED);
            setLoggedUser(model);

            return APP_LIST_JSP;
        } else {
            prepareErrorJsp(null, model);

            return ERROR_JSP;
        }
    }


    /**
     * used if you want to see all rejected requests
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = REJECTED_APPS, method = RequestMethod.GET)
    public String showRejected(Model model) {

        if (getLoggedUser().getRole() == Role.BOSS) {
            List<Application> applications = applicationService.getApplicationsByState(ApplicationStatus.CANCELLED);

            setApplications(applications, model);

            setLoggedUser(model);

            return APP_LIST_JSP;
        } else {
            prepareErrorJsp(null, model);

            return ERROR_JSP;
        }
    }


    /**
     * used if you want to apply an application for leave (shows formular)
     *
     * @param  personId  id of the logged-in user
     * @param  model  the datamodel
     *
     * @return
     */
    @RequestMapping(value = NEW_APP, method = RequestMethod.GET)
    public String newApplicationForm(Model model) {

        if (getLoggedUser().getRole() != Role.INACTIVE) {
            Person person = getLoggedUser();

            // check if this is a new user without account and/or entitlement or a user that has no active account and
            // entitlement for current year

            if (accountService.getHolidayEntitlement(DateMidnight.now().getYear(), person) == null
                    || accountService.getHolidaysAccount(DateMidnight.now().getYear(), person) == null) {
                model.addAttribute(NOTPOSSIBLE, true);
            } else {
                prepareForm(person, new AppForm(), model);
            }

            return APP_FORM_JSP;
        } else {
            return LOGIN_LINK;
        }
    }


    /**
     * use this to save an application (will be in "waiting" state)
     *
     * @param  personId  the id of the employee who made this application
     * @param  application  the application-object created by the form-entries
     * @param  model
     *
     * @return  returns the path to a success-site ("your application is being processed") or the main-page
     */
    @RequestMapping(value = NEW_APP, method = RequestMethod.POST)
    public String newApplication(@ModelAttribute(APPFORM) AppForm appForm, Errors errors, Model model) {

        Person person = getLoggedUser();

        if (person.getRole() == Role.USER || person.getRole() == Role.BOSS) {
            validator.validateForUser(appForm, errors);
        }

        validator.validate(appForm, errors);

        if (errors.hasErrors()) {
            prepareForm(person, appForm, model);

            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            return APP_FORM_JSP;
        } else {
            Application application = new Application();
            application = appForm.fillApplicationObject(application);

            application.setPerson(person);
            application.setApplicationDate(DateMidnight.now(GregorianChronology.getInstance()));

            // check at first if there are existent application for the same period

            // checkOverlap
            // case 1: ok
            // case 2: new application is fully part of existent applications, useless to apply it
            // case 3: gaps in between - feature in later version, now only error message

            int overlap = applicationService.checkOverlap(application);

            if (overlap == 2 || overlap == 3) {
                // in this version, these two cases are handled equal
                errors.reject("check.overlap");
            } else if (overlap == 1) {
                // if there is no overlap go to next check but only if vacation type is holiday, else you don't have to
                // check if there are enough days on user's holidays account
                boolean enoughDays = false;

                if (application.getVacationType() == VacationType.HOLIDAY) {
                    enoughDays = applicationService.checkApplication(application);
                }

                // enough days to apply for leave
                if (enoughDays || (application.getVacationType() != VacationType.HOLIDAY)) {
                    // save the application
                    applicationService.save(application);

                    // and sign it
                    applicationService.signApplicationByUser(application, person);

                    LOG.info(application.getApplicationDate() + " ID: " + application.getId()
                        + " Es wurde ein neuer Antrag von " + person.getLastName() + " " + person.getFirstName()
                        + " angelegt.");

                    // mail to applicant
                    mailService.sendConfirmation(application);

                    // mail to boss
                    mailService.sendNewApplicationNotification(application);

                    return "redirect:/web" + OVERVIEW;
                } else {
                    errors.reject("check.enough");
                    model.addAttribute("daysApp", application.getDays());
                }
            }
        }

        prepareForm(person, appForm, model);

        if (errors.hasGlobalErrors()) {
            model.addAttribute("errors", errors);
        }

        return APP_FORM_JSP;
    }


    public void prepareForm(Person person, AppForm appForm, Model model) {

        int april = 0;

        if (DateUtil.isBeforeApril(DateMidnight.now())) {
            april = 1;
        }

        model.addAttribute(APRIL, april);

        model.addAttribute(PERSON, person);
        model.addAttribute(PERSONS, personService.getAllPersonsExceptOne(person.getId()));
        model.addAttribute(DATE, DateMidnight.now(GregorianChronology.getInstance()));
        model.addAttribute(YEAR, DateMidnight.now(GregorianChronology.getInstance()).getYear());
        model.addAttribute(APPFORM, appForm);
        model.addAttribute(ACCOUNT,
            accountService.getHolidaysAccount(DateMidnight.now(GregorianChronology.getInstance()).getYear(), person));
        model.addAttribute(VACTYPES, VacationType.values());
        model.addAttribute(FULL, DayLength.FULL);
        model.addAttribute(MORNING, DayLength.MORNING);
        model.addAttribute(NOON, DayLength.NOON);
        setLoggedUser(model);
    }


    /**
     * view for boss who has to decide if he allows or rejects the application office is able to add sick days that
     * occured during a holiday
     *
     * @param  applicationId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = SHOW_APP, params = "state", method = RequestMethod.GET)
    public String showApplicationDetail(@PathVariable(APPLICATION_ID) Integer applicationId,
        @RequestParam("state") int state, Model model) {

        if (getLoggedUser().getRole() == Role.OFFICE || getLoggedUser().getRole() == Role.BOSS) {
            Application application = applicationService.getApplicationById(applicationId);

            prepareDetailView(application, state, model);
            model.addAttribute(APPFORM, new AppForm());
            model.addAttribute(COMMENT, new Comment());

            return SHOW_APP_DETAIL;
        } else {
            prepareErrorJsp("message.curious", model);

            return ERROR_JSP;
        }
    }


    /**
     * used if you want to allow an existing request (boss only)
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = ALLOW_APP, method = RequestMethod.PUT)
    public String allowApplication(@PathVariable(APPLICATION_ID) Integer applicationId, Model model) {

        Application application = applicationService.getApplicationById(applicationId);

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Person boss = personService.getPersonByLogin(name);

        Integer person_id = application.getPerson().getId();
        Integer boss_id = boss.getId();

        // boss may only allow an application if this application isn't his own one
        if (!person_id.equals(boss_id)) {
            applicationService.allow(application, boss);

            LOG.info(application.getApplicationDate() + " ID: " + application.getId() + "Der Antrag von "
                + application.getPerson().getFirstName() + " " + application.getPerson().getLastName()
                + " wurde am " + DateMidnight.now().toString(DATE_FORMAT) + " von " + boss.getFirstName() + " "
                + boss.getLastName() + " genehmigt.");

            mailService.sendAllowedNotification(application);

            return "redirect:/web" + WAITING_APPS;
        } else {
            prepareErrorJsp("message.boss", model);

            return ERROR_JSP;
        }
    }


    /**
     * used if you want to reject a request (boss only)
     *
     * @param  applicationId  the id of the to declining request
     * @param  reason  the reason of the rejection
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = REJECT_APP, method = RequestMethod.PUT)
    public String rejectApplication(@PathVariable(APPLICATION_ID) Integer applicationId,
        @ModelAttribute(COMMENT) Comment comment, Errors errors, Model model) {

        Application application = applicationService.getApplicationById(applicationId);

        validator.validateComment(comment, errors);

        if (errors.hasErrors()) {
            prepareDetailView(application, WAITING, model);

            return SHOW_APP_DETAIL;
        } else {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            Person boss = personService.getPersonByLogin(name);
            String nameOfCommentingPerson = boss.getLastName() + " " + boss.getFirstName();

            comment.setNameOfCommentingPerson(nameOfCommentingPerson);
            comment.setApplication(application);
            comment.setDateOfComment(DateMidnight.now());
            commentService.saveComment(comment);

            applicationService.reject(application, boss);

            LOG.info(application.getApplicationDate() + " ID: " + application.getId() + "Der Antrag von "
                + application.getPerson().getFirstName() + " " + application.getPerson().getLastName()
                + " wurde am " + DateMidnight.now().toString(DATE_FORMAT) + " von " + nameOfCommentingPerson
                + " abgelehnt.");

            // mail to applicant
            mailService.sendRejectedNotification(application);

            return "redirect:/web" + WAITING_APPS;
        }
    }


    @RequestMapping(value = CANCEL_APP, method = RequestMethod.GET)
    public String cancelApplicationConfirm(@PathVariable(APPLICATION_ID) Integer applicationId, Model model) {

        Application application = applicationService.getApplicationById(applicationId);

        if (getLoggedUser().equals(application.getPerson())) {
            prepareDetailView(application, TO_CANCEL, model);

            return SHOW_APP_DETAIL;
        } else {
            prepareErrorJsp(null, model);

            return ERROR_JSP;
        }
    }


    @RequestMapping(value = CANCEL_APP, method = RequestMethod.PUT)
    public String cancelApplication(@PathVariable(APPLICATION_ID) Integer applicationId) {

        Application application = applicationService.getApplicationById(applicationId);

        applicationService.cancel(application);

        LOG.info(application.getApplicationDate() + " ID: " + application.getId() + "Der Antrag von "
            + application.getPerson().getFirstName() + " " + application.getPerson().getLastName()
            + " wurde am " + DateMidnight.now().toString(DATE_FORMAT) + " storniert.");

        // if application has status ALLOWED, office gets an email
        mailService.sendCancelledNotification(application, false);

        // should boss get an email if application's status is WAITING?
        // mailService.sendCancelledNotification(application, true);

        return "redirect:/web" + OVERVIEW;
    }


    @RequestMapping(value = SICK_DAYS, method = RequestMethod.PUT)
    public String addSickDays(@PathVariable(APPLICATION_ID) Integer applicationId,
        @ModelAttribute(APPFORM) AppForm appForm, Errors errors, Model model) {

        Application application = applicationService.getApplicationById(applicationId);

        validator.validateSickDays(appForm, application.getDays(), errors);

        if (errors.hasErrors()) {
            // shows error in Frontend
            prepareDetailView(application, ALLOWED, model);
            model.addAttribute(APPFORM, appForm);
            model.addAttribute("errors", errors);

            return SHOW_APP_DETAIL;
        } else {
            // sick days are smaller than vacation days (resp. equals)
            application.setSickDays(appForm.getSickDays());
            application.setDateOfAddingSickDays(DateMidnight.now());
            applicationService.simpleSave(application);
            applicationService.addSickDaysOnHolidaysAccount(application);

            return "redirect:/web" + ALLOWED_APPS;
        }
    }


    private void prepareDetailView(Application application, int stateNumber, Model model) {

        setLoggedUser(model);
        model.addAttribute(APPLICATION, application);
        model.addAttribute(STATE_NUMBER, stateNumber);

        int year = application.getEndDate().getYear();

        HolidaysAccount account = accountService.getHolidaysAccount(year, application.getPerson());
        HolidayEntitlement entitlement = accountService.getHolidayEntitlement(year, application.getPerson());
        int april = 0;

        if (DateUtil.isBeforeApril(application.getEndDate())) {
            april = 1;
        }

        model.addAttribute(ACCOUNT, account);
        model.addAttribute(ENTITLEMENT, entitlement);
        model.addAttribute(YEAR, year);
        model.addAttribute(APRIL, april);

        // get url of person's gravatar image
        String url = gravatarUtil.createImgURL(application.getPerson().getEmail());
        model.addAttribute(GRAVATAR, url);
    }


    private void setLoggedUser(Model model) {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        Person loggedUser = personService.getPersonByLogin(user);

        model.addAttribute(LOGGED_USER, loggedUser);
    }


    private Person getLoggedUser() {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        return personService.getPersonByLogin(user);
    }


    private void prepareErrorJsp(String message, Model model) {

        setLoggedUser(model);

        if (message != null) {
            model.addAttribute("message", message);
        }
    }
}
