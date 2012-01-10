
package org.synyx.urlaubsverwaltung.controller;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;

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
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.VacationType;
import org.synyx.urlaubsverwaltung.service.ApplicationService;
import org.synyx.urlaubsverwaltung.service.CommentService;
import org.synyx.urlaubsverwaltung.service.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.view.AppForm;

import java.util.List;
import java.util.Locale;


/**
 * @author  Aljona Murygina
 */
@Controller
public class ApplicationController {

    // jsps
    private static final String SHOW_APP_DETAIL = "application/app_detail";
    private static final String APP_LIST_JSP = "application/list";
    private static final String APP_FORM_JSP = "application/app_form";
    private static final String OVERVIEW_JSP = "/overview";

    // attribute names
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final String COMMENT = "comment";
    private static final String APPFORM = "appForm";
    private static final String APPLICATION = "application";
    private static final String APPLICATIONS = "applications";
    private static final String ACCOUNT = "account";
    private static final String PERSON = "person";
    private static final String PERSONS = "persons";
    private static final String YEAR = "year";
    private static final String DATE = "date";

    private static final String APPLICATION_ID = "applicationId";
    private static final String PERSON_ID = "personId";

    // list of applications by state
    private static final String WAITING_APPS = "/application/waiting";
    private static final String ALLOWED_APPS = "/application/allowed";
    private static final String CANCELLED_APPS = "/application/cancelled";
    private static final String REJECTED_APPS = "/application/rejected"; // not used now, but maybe useful someday
    private static final String STATE_NUMBER = "stateNumber";
    private static final int WAITING = 0;
    private static final int ALLOWED = 1;
    private static final int CANCELLED = 2;
    private static final int BY_PERSON = 3;

    // list of applications by person
    private static final String APPS_BY_PERSON = "/{" + PERSON_ID + "}/application";

    // form to apply vacation
    private static final String NEW_APP = "/application/new";

    // for user: the only way editing an application for user is to cancel it
    // (application may have state waiting or allowed)
    private static final String CANCEL_APP = "/application/{" + APPLICATION_ID + "}/cancel";

    // detailed view of application
    private static final String SHOW_APP = "/application/{" + APPLICATION_ID + "}";

    // allow or reject application
    private static final String ALLOW_APP = "/application/{" + APPLICATION_ID + "}/allow";
    private static final String REJECT_APP = "/application/{" + APPLICATION_ID + "}/reject";

    // logger
    private static final Logger LOG = Logger.getLogger(PersonController.class);

    private PersonService personService;
    private ApplicationService applicationService;
    private HolidaysAccountService accountService;
    private CommentService commentService;

    public ApplicationController(PersonService personService, ApplicationService applicationService,
        HolidaysAccountService accountService, CommentService commentService) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.accountService = accountService;
        this.commentService = commentService;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor(locale));
    }


    /**
     * show List<Application> of one person
     *
     * @param  personId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = APPS_BY_PERSON, method = RequestMethod.GET)
    public String showApplicationsByPerson(@PathVariable(PERSON_ID) Integer personId, Model model) {

        Person person = personService.getPersonByID(personId);
        List<Application> applications = applicationService.getApplicationsByPerson(person);

        model.addAttribute(PERSON, person);
        model.addAttribute(APPLICATIONS, applications);
        model.addAttribute(STATE_NUMBER, BY_PERSON);
        setLoggedUser(model);

        return APP_LIST_JSP;
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

        List<Application> applications = applicationService.getApplicationsByState(ApplicationStatus.WAITING);
        model.addAttribute(APPLICATIONS, applications);
        model.addAttribute(STATE_NUMBER, WAITING);
        setLoggedUser(model);

        return APP_LIST_JSP;
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

        List<Application> applications = applicationService.getApplicationsByState(ApplicationStatus.ALLOWED);
        model.addAttribute(APPLICATIONS, applications);
        model.addAttribute(STATE_NUMBER, ALLOWED);
        setLoggedUser(model);

        return APP_LIST_JSP;
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

        List<Application> applications = applicationService.getApplicationsByState(ApplicationStatus.CANCELLED);
        model.addAttribute(APPLICATIONS, applications);
        model.addAttribute(STATE_NUMBER, CANCELLED);
        setLoggedUser(model);

        return APP_LIST_JSP;
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

        List<Application> applications = applicationService.getApplicationsByState(ApplicationStatus.CANCELLED);
        model.addAttribute(APPLICATIONS, applications);
        setLoggedUser(model);

        return APP_LIST_JSP;
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

        Person person = getPersonByLogin();

        List<Person> persons = personService.getAllPersonsExceptOne(person.getId());

        DateMidnight date = DateMidnight.now(GregorianChronology.getInstance());
        String stringDate = date.getDayOfMonth() + "." + date.getMonthOfYear() + "." + date.getYear();
        int year = date.getYear();

        HolidaysAccount account = accountService.getHolidaysAccount(year, person);

        model.addAttribute(PERSON, person);
        model.addAttribute(PERSONS, persons);
        model.addAttribute(DATE, stringDate);
        model.addAttribute(YEAR, year);
        model.addAttribute(APPFORM, new AppForm());
        model.addAttribute(ACCOUNT, account);
        model.addAttribute("vacTypes", VacationType.values());
        model.addAttribute("full", DayLength.FULL);
        model.addAttribute("morning", DayLength.MORNING);
        model.addAttribute("noon", DayLength.NOON);
        setLoggedUser(model);

        return APP_FORM_JSP;
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
    public String newApplication(@ModelAttribute(APPFORM) AppForm appForm) {

        Person person = getPersonByLogin();

        Application application = new Application();
        application = appForm.fillApplicationObject(application);

        application.setPerson(person);
        application.setApplicationDate(DateMidnight.now(GregorianChronology.getInstance()));

        applicationService.save(application);

//        cryptoService and mailService must be modified
//        applicationService.signApplicationByUser(application, person);

        LOG.info(application.getApplicationDate() + " ID: " + application.getId() + " Es wurde ein neuer Antrag von "
            + person.getLastName() + " " + person.getFirstName() + " angelegt.");

        return "redirect:/web" + OVERVIEW_JSP;
    }


    /**
     * view for boss who has to decide if he allows or rejects the application
     *
     * @param  applicationId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = SHOW_APP, params = "state", method = RequestMethod.GET)
    public String showApplicationDetail(@PathVariable(APPLICATION_ID) Integer applicationId,
        @RequestParam("state") int state, Model model) {

        Application application = applicationService.getApplicationById(applicationId);

        model.addAttribute(APPLICATION, application);
        model.addAttribute(STATE_NUMBER, state);
        model.addAttribute(COMMENT, new Comment());
        setLoggedUser(model);

        return SHOW_APP_DETAIL;
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

        applicationService.allow(application, boss);

        LOG.info(application.getApplicationDate() + " ID: " + application.getId() + "Der Antrag von "
            + application.getPerson().getFirstName() + " " + application.getPerson().getLastName()
            + " wurde am " + DateMidnight.now().toString(DATE_FORMAT) + " von " + boss.getFirstName() + " "
            + boss.getLastName() + " genehmigt.");

        return "redirect:/web" + WAITING_APPS;
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
        @ModelAttribute(COMMENT) Comment comment) {

        Application application = applicationService.getApplicationById(applicationId);

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

        return "redirect:/web" + WAITING_APPS;
    }


    @RequestMapping(value = CANCEL_APP, method = RequestMethod.PUT)
    public String cancelApplication(@PathVariable(APPLICATION_ID) Integer applicationId) {

        Application application = applicationService.getApplicationById(applicationId);

        applicationService.cancel(application);

        LOG.info(application.getApplicationDate() + " ID: " + application.getId() + "Der Antrag von "
            + application.getPerson().getFirstName() + " " + application.getPerson().getLastName()
            + " wurde am " + DateMidnight.now().toString(DATE_FORMAT) + " storniert.");

        return "redirect:/web" + OVERVIEW_JSP;
    }


    private void setLoggedUser(Model model) {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        Person loggedUser = personService.getPersonByLogin(user);

        model.addAttribute("loggedUser", loggedUser);
    }


    private Person getPersonByLogin() {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        return personService.getPersonByLogin(user);
    }
}
