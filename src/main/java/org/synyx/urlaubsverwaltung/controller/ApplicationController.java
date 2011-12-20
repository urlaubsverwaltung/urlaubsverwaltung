
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

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.VacationType;
import org.synyx.urlaubsverwaltung.service.ApplicationService;
import org.synyx.urlaubsverwaltung.service.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.DateMidnightPropertyEditor;

import java.util.List;
import java.util.Locale;


/**
 * @author  Aljona Murygina
 */
@Controller
public class ApplicationController {

    // jsps
    private static final String SHOW_APP_JSP = "application/show";
    private static final String SHOW_APP_CHEF_JSP = "application/app_chef";
    private static final String SHOW_APP_OFFICE_JSP = "application/app_office";
    private static final String PRINT_VIEW_APP_JSP = "application/print";
    private static final String APP_LIST_JSP = "application/list";
    private static final String APP_FORM_JSP = "application/app_form";

    // attribute names
    private static final String APPLICATION = "application";
    private static final String APPLICATIONS = "applications";
    private static final String ACCOUNT = "account";
    private static final String PERSON = "person";
    private static final String PERSONS = "persons";
    private static final String YEAR = "year";
    private static final String DATE = "date";

    private static final String APPLICATION_ID = "applicationId";
    private static final String PERSON_ID = "personId";

    // links
    private static final String WEB = "web/";

    // list of applications by state
    private static final String WAITING_APPS = "/application/waiting";
    private static final String ALLOWED_APPS = "/application/allowed";
    private static final String CANCELLED_APPS = "/application/cancelled";
    private static final String REJECTED_APPS = "/application/rejected";

    // list of applications by person
    private static final String APPS_BY_PERSON = "/application/{" + PERSON_ID + "}";

    // form to apply vacation
    private static final String NEW_APP = "/application/{" + PERSON_ID + "}/new";

    // for user: if application has state waiting, user may change various attributes
    // if application has state allowed, user is able to cancel application, but not to edit it
    private static final String EDIT_APP = "/application/{" + APPLICATION_ID + "}/edit";

    // detailed view of application for chef
    private static final String SHOW_APP = "/application/{" + APPLICATION_ID + "}";

    // allow or reject application
    private static final String ALLOW_APP = "/application/{" + APPLICATION_ID + "}/allow";
    private static final String REJECT_APP = "/application/{" + APPLICATION_ID + "}/reject";

    // logger
    private static final Logger LOG = Logger.getLogger(PersonController.class);

    private PersonService personService;
    private ApplicationService applicationService;
    private HolidaysAccountService accountService;

    public ApplicationController(PersonService personService, ApplicationService applicationService,
        HolidaysAccountService accountService) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.accountService = accountService;
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
        List<Application> applications = applicationService.getAllApplicationsForPerson(person);

        model.addAttribute(PERSON, person);
        model.addAttribute(APPLICATIONS, applications);
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

        List<Application> applications = applicationService.getAllApplicationsByState(ApplicationStatus.WAITING);
        model.addAttribute(APPLICATIONS, applications);
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

        List<Application> applications = applicationService.getAllApplicationsByState(ApplicationStatus.ALLOWED);
        model.addAttribute(APPLICATIONS, applications);
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

        List<Application> applications = applicationService.getAllApplicationsByState(ApplicationStatus.CANCELLED);
        model.addAttribute(APPLICATIONS, applications);
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

        List<Application> applications = applicationService.getAllApplicationsByState(ApplicationStatus.CANCELLED);
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
    public String newApplicationForm(@PathVariable(PERSON_ID) Integer personId, Model model) {

        Person person = personService.getPersonByID(personId);
        List<Person> persons = personService.getAllPersons();

        DateMidnight date = DateMidnight.now(GregorianChronology.getInstance());
        String stringDate = date.getDayOfMonth() + "." + date.getMonthOfYear() + "." + date.getYear();
        int year = date.getYear();

        HolidaysAccount account = accountService.getHolidaysAccount(year, person);

        model.addAttribute(PERSON, person);
        model.addAttribute(PERSONS, persons);
        model.addAttribute(DATE, stringDate);
        model.addAttribute(YEAR, year);
        model.addAttribute(APPLICATION, new Application());
        model.addAttribute(ACCOUNT, account);
        model.addAttribute("vacTypes", VacationType.values());
        model.addAttribute("daylength", DayLength.values());
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
    public String newApplication(@PathVariable(PERSON_ID) Integer personId,
        @ModelAttribute(APPLICATION) Application application) {

        Person person = personService.getPersonByID(personId);

        application.setPerson(person);

        DateMidnight date = DateMidnight.now(GregorianChronology.getInstance());
        application.setApplicationDate(date);

        applicationService.save(application);

//        cryptoService and mailService must be modified
//        applicationService.signApplicationByUser(application, person);

        LOG.info(application.getApplicationDate() + " ID: " + application.getId() + " Es wurde ein neuer Antrag von "
            + person.getLastName() + " " + person.getFirstName() + " angelegt.");

        return "redirect:/web/staff/" + personId + "/overview";
    }


    /**
     * view for chef who has to decide if he allows or rejects the application
     *
     * @param  applicationId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = SHOW_APP, method = RequestMethod.GET)
    public String showApplicationDetailChef(@PathVariable(APPLICATION_ID) Integer applicationId, Model model) {

        Application application = applicationService.getApplicationById(applicationId);

        model.addAttribute(APPLICATION, application);
        setLoggedUser(model);

        return SHOW_APP_CHEF_JSP;
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
            + " wurde am " + DateMidnight.now().toString("dd.MM.yyyy") + " von " + boss.getFirstName() + " "
            + boss.getLastName() + " genehmigt.");

        return "";
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
        @ModelAttribute("reasonForRejecting") String reasonForRejecting, Model model) {

        Application application = applicationService.getApplicationById(applicationId);

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Person boss = personService.getPersonByLogin(name);

        applicationService.reject(application, boss, reasonForRejecting);

        LOG.info(application.getApplicationDate() + " ID: " + application.getId() + "Der Antrag von "
            + application.getPerson().getFirstName() + " " + application.getPerson().getLastName()
            + " wurde am " + DateMidnight.now().toString("dd.MM.yyyy") + " von " + boss.getFirstName() + " "
            + boss.getLastName() + " abgelehnt.");

        return "";
    }


    private void setLoggedUser(Model model) {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        Person loggedUser = personService.getPersonByLogin(user);

        model.addAttribute("loggedUser", loggedUser);
    }

    // /**
// * view for office who can print or edit the request
// *
// * @param  applicationId
// * @param  model
// *
// * @return
// */
// @RequestMapping(value = "/application/{applicationId}/office", method = RequestMethod.GET)
// public String showapplicationDetailOffice(@PathVariable(APPLICATION_ID) Integer applicationId, Model model) {
//
////        Application application = applicationService.getRequestById(applicationId);
////
////        model.addAttribute(application_ATTRIBUTE_NAME, application);
    // setLoggedUser(model);
//
//        return "applications/applicationdetailoffice";
//    }
//
//
//    /**
//     * used if you want to cancel an existing request (owner only/maybe office)
//     *
//     * @param  model
//     *
//     * @return
//     */
//    @RequestMapping(value = "/application/{applicationId}/stornieren", method = RequestMethod.PUT)
//    public String cancelapplication(@PathVariable(APPLICATION_ID) Integer applicationId, Model model) {
//
////        // über die logik sollten wir nochmal nachdenken...
////        applicationService.cancel(applicationService.getRequestById(applicationId));
//
//        LOG.info("Ein application wurde storniert.");
//
//        return "";
//    }
//
//
}
