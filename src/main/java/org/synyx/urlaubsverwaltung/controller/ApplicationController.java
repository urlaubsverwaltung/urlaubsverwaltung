
package org.synyx.urlaubsverwaltung.controller;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.VacationType;
import org.synyx.urlaubsverwaltung.service.ApplicationService;
import org.synyx.urlaubsverwaltung.service.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.service.PersonService;

import java.util.List;


/**
 * @author  Aljona Murygina
 */
@Controller
public class ApplicationController {

    // jsps
    private static final String SHOW_APP_JSP = "application/show";
    private static final String SHOW_APP_CHEF_JSP = "application/show_chef";
    private static final String SHOW_APP_OFFICE_JSP = "application/show_office";
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
    private static final String WAITING_APPS = "/application/waiting";
    private static final String ALLOWED_APPS = "/application/allowed";
    private static final String CANCELLED_APPS = "/application/cancelled";
    private static final String REJECTED_APPS = "/application/rejected";
    private static final String APPS_BY_PERSON = "/application/{" + PERSON_ID + "}";
    private static final String NEW_APP = "/application/{" + PERSON_ID + "}/new";
    private static final String EDIT_APP = "/application/{" + APPLICATION_ID + "}/edit";
    private static final String SHOW_APP = "/application/{" + APPLICATION_ID + "}";

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

        Application application = new Application();

        model.addAttribute(PERSON, person);
        model.addAttribute(PERSONS, persons);
        model.addAttribute(DATE, stringDate);
        model.addAttribute(YEAR, year);
        model.addAttribute(APPLICATION, application);
        model.addAttribute(ACCOUNT, account);
        model.addAttribute("vacTypes", VacationType.values());
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
        @ModelAttribute(APPLICATION) Application application, Model model) {

        Person person = personService.getPersonByID(personId);

        application.setPerson(person);
        application.setStatus(ApplicationStatus.WAITING);

        applicationService.save(application);

        applicationService.signApplicationByUser(application, person);

        LOG.info(application.getApplicationDate() + " ID: " + application.getId() + " Es wurde ein neuer Antrag von "
            + person.getLastName() + " " + person.getFirstName() + " angelegt.");

        return "redirect:";
    }


//    /**
//     * view for chef who has to decide if he allows or rejects the application
//     *
//     * @param  applicationId
//     * @param  model
//     *
//     * @return
//     */
//    @RequestMapping(value = "/application/{applicationId}/chef", method = RequestMethod.GET)
//    public String showapplicationDetailChef(@PathVariable(application_ID) Integer applicationId, Model model) {
//
////        Application application = applicationService.getRequestById(applicationId);
//
////        model.addAttribute(application_ATTRIBUTE_NAME, application);
    // setLoggedUser(model);
//
//        return "applications/applicationdetailchef";
//    }

//    /**
//     * view for office who can print or edit the request
//     *
//     * @param  applicationId
//     * @param  model
//     *
//     * @return
//     */
//    @RequestMapping(value = "/application/{applicationId}/office", method = RequestMethod.GET)
//    public String showapplicationDetailOffice(@PathVariable(APPLICATION_ID) Integer applicationId, Model model) {
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
//    /**
//     * used if you want to allow an existing request (boss only)
//     *
//     * @param  model
//     *
//     * @return
//     */
//    @RequestMapping(value = "/application/{applicationId}/genehmigen", method = RequestMethod.PUT)
//    public String approveapplication(@PathVariable(APPLICATION_ID) Integer applicationId, Model model) {
//
////        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        // irgendwie an LoginNamen kommen via security stuff
//        String login = "";
//
////        Application application = applicationService.getRequestById(applicationId);
//        Person boss = personService.getPersonByLogin(login);
//
//        // über die logik sollten wir nochmal nachdenken...
//// applicationService.allow(application);
//
//// try {
//// // Application mit Boss Daten signieren
//////            applicationService.signApplicationByBoss(application, boss);
////        } catch (NoSuchAlgorithmException ex) {
////            java.util.logging.Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
////        } catch (InvalidKeySpecException ex) {
////            java.util.logging.Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
////        }
//
//        LOG.info("Ein application wurde genehmigt.");
//
//        // benachrichtigungs-zeugs
//
//        return "";
//    }
//
//
//    /**
//     * used if you want to reject a request (boss only)
//     *
//     * @param  applicationId  the id of the to declining request
//     * @param  reason  the reason of the rejection
//     * @param  model
//     *
//     * @return
//     */
//    @RequestMapping(value = "/application/{applicationId}/ablehnen", method = RequestMethod.PUT)
//    public String declineapplication(@PathVariable(APPLICATION_ID) Integer applicationId,
//        @ModelAttribute("reasonForDeclining") String reasonForDeclining, Model model) {
//
//        // ueber security zeugs an login name kommen
//        String login = "";
//
//        Person boss = personService.getPersonByLogin(login);
////        Application application = applicationService.getRequestById(applicationId);
////
////        applicationService.reject(application, boss, reasonForDeclining);
////
////        try {
////            applicationService.signApplicationByBoss(application, boss);
////        } catch (NoSuchAlgorithmException ex) {
////            java.util.logging.Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
////        } catch (InvalidKeySpecException ex) {
////            java.util.logging.Logger.getLogger(ApplicationController.class.getName()).log(Level.SEVERE, null, ex);
////        }
//
//        LOG.info("Ein application wurde abgelehnt.");
//
//        // benachrichtigungs-zeugs
//
//        return "";
//    }

    private void setLoggedUser(Model model) {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        Person loggedUser = personService.getPersonByLogin(user);

        model.addAttribute("loggedUser", loggedUser);
    }
}
