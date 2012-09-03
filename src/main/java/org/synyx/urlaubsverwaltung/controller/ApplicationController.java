
package org.synyx.urlaubsverwaltung.controller;

import org.apache.commons.lang.StringUtils;

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

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Comment;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.legacy.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.legacy.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;
import org.synyx.urlaubsverwaltung.domain.VacationType;
import org.synyx.urlaubsverwaltung.service.ApplicationService;
import org.synyx.urlaubsverwaltung.service.CommentService;
import org.synyx.urlaubsverwaltung.service.legacy.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.service.MailService;
import org.synyx.urlaubsverwaltung.service.OverlapCase;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.util.GravatarUtil;
import org.synyx.urlaubsverwaltung.validator.ApplicationValidator;
import org.synyx.urlaubsverwaltung.view.AppForm;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.synyx.urlaubsverwaltung.jmx.JmxDemo;
import org.synyx.urlaubsverwaltung.util.NumberUtil;


/**
 * @author  Aljona Murygina
 *
 *          <p>This class contains all methods that are necessary for handling applications for leave, i.e. apply for
 *          leave, show and edit applications.</p>
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
    private static final String ACCOUNTS = "accounts";
    private static final String USED_DAYS = "usedDays";
    private static final String ENTITLEMENT = "entitlement";
    private static final String PERSON = "person";
    private static final String PERSONS = "persons"; // persons for selecting rep
    private static final String PERSON_LIST = "personList"; // office can apply for leave for this persons
    private static final String YEAR = "year";
    private static final String DATE = "date";
    private static final String VACTYPES = "vacTypes";
    private static final String FULL = "full";
    private static final String MORNING = "morning";
    private static final String NOON = "noon";
    private static final String NOTPOSSIBLE = "notpossible"; // is it possible for user to apply for leave? (no, if
                                                             // he/she has no account/entitlement)
    private static final String APRIL = "april";
    private static final String GRAVATAR = "gravatar";

    private static final String APPLICATION_ID = "applicationId";
    private static final String PERSON_ID = "personId";

    // jsps
    private static final String APP_LIST_JSP = APPLICATION + "/app_list";
    private static final String SHOW_APP_DETAIL_JSP = APPLICATION + "/app_detail";
    private static final String APP_FORM_JSP = APPLICATION + "/app_form";
    private static final String APP_FORM_OFFICE_JSP = APPLICATION + "/app_form_office";
    private static final String ERROR_JSP = "error";

    // login link
    private static final String LOGIN_LINK = "redirect:/login.jsp?login_error=1";

    // overview
    private static final String OVERVIEW = "/overview";

    // links start with...
    private static final String SHORT_PATH_APPLICATION = "/" + APPLICATION;
    private static final String LONG_PATH_APPLICATION = "/" + APPLICATION + "/{";

    // list of applications by state
    private static final String APP_LIST = SHORT_PATH_APPLICATION;
    private static final String ALL_APPS = SHORT_PATH_APPLICATION + "/all";
    private static final String WAITING_APPS = SHORT_PATH_APPLICATION + "/waiting";
    private static final String ALLOWED_APPS = SHORT_PATH_APPLICATION + "/allowed";
    private static final String CANCELLED_APPS = SHORT_PATH_APPLICATION + "/cancelled";
    private static final String REJECTED_APPS = SHORT_PATH_APPLICATION + "/rejected"; 

    // order applications by certain numbers
    private static final String STATE_NUMBER = "stateNumber";
    private static final int WAITING = 0;
    private static final int TO_CANCEL = 4;

    // applications' status
    // title in list jsp
    private static final String TITLE_APP = "titleApp";
    private static final String TITLE_WAITING = "waiting.app";
    private static final String TITLE_ALLOWED = "allow.app";
    private static final String TITLE_REJECTED = "reject.app";
    private static final String TITLE_CANCELLED = "cancel.app";
    
    private static final String TOUCHED_DATE = "touchedDate";
    private static final String DATE_OVERVIEW = "app.date.overview";
    private static final String DATE_APPLIED = "app.date.applied";
    private static final String DATE_ALLOWED = "app.date.allowed";
    private static final String DATE_REJECTED = "app.date.rejected";
    private static final String DATE_CANCELLED = "app.date.cancelled";
    
    private static final String CHECKBOXES = "showCheckboxes";

    // form to apply vacation
    private static final String NEW_APP = SHORT_PATH_APPLICATION + "/new"; // form for user
    private static final String NEW_APP_OFFICE = "/{" + PERSON_ID + "}/application/new"; // form for office

    // for user: the only way editing an application for user is to cancel it
    // (application may have state waiting or allowed)
    private static final String CANCEL_APP = LONG_PATH_APPLICATION + APPLICATION_ID + "}/cancel";

    // detailed view of application
    private static final String SHOW_APP = LONG_PATH_APPLICATION + APPLICATION_ID + "}";

    // allow or reject application
    private static final String ALLOW_APP = LONG_PATH_APPLICATION + APPLICATION_ID + "}/allow";
    private static final String REJECT_APP = LONG_PATH_APPLICATION + APPLICATION_ID + "}/reject";
    
    // refer application to other boss
    private static final String REFER_APP = LONG_PATH_APPLICATION + APPLICATION_ID + "}/refer";
    
    // remind boss to decide about application
    private static final String REMIND = LONG_PATH_APPLICATION + APPLICATION_ID + "}/remind";

    // audit logger: logs nontechnically occurences like 'user x applied for leave' or 'subtracted n days from
    // holidays account y'
    private static final Logger LOG = Logger.getLogger("audit");

    private PersonService personService;
    private ApplicationService applicationService;
    private HolidaysAccountService accountService;
    private CommentService commentService;
    private ApplicationValidator validator;
    private GravatarUtil gravatarUtil;
    private MailService mailService;
    private OwnCalendarService calendarService;

    public ApplicationController(PersonService personService, ApplicationService applicationService,
        HolidaysAccountService accountService, CommentService commentService, ApplicationValidator validator,
        GravatarUtil gravatarUtil, MailService mailService, OwnCalendarService calendarService) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.accountService = accountService;
        this.commentService = commentService;
        this.validator = validator;
        this.gravatarUtil = gravatarUtil;
        this.mailService = mailService;
        this.calendarService = calendarService;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor(locale));
    }
    
    
    /**
     * shows the default list, dependent on user role: if boss show waiting applications, if office show all applications
     * @param model 
     */
    @RequestMapping(value = APP_LIST, method = RequestMethod.GET)
    public String showDefault(Model model) {
        
        Role role = getLoggedUser().getRole();
        
        if(role == Role.BOSS) {
            return "redirect:/web" + WAITING_APPS;
        } else if (role == Role.OFFICE) {
            return "redirect:/web" + ALL_APPS;
        } else {
            return ERROR_JSP;
        }
    }


    /**
     * Prepares the model object for the showAll or showAllByYear methods.
     * 
     * @param year
     * @param model
     * @return 
     */
    private Model prepareModelForShowAllMethods(int year, Model model) {
        
            DateMidnight firstDay = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
            DateMidnight lastDay = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);
            
            List<Application> apps = applicationService.getApplicationsForACertainTime(firstDay, lastDay);
            
            List<Application> applications = new ArrayList<Application>();
        
            for(Application a : apps) {
            if(a.getStatus() != ApplicationStatus.CANCELLED) {
                applications.add(a);
            } else {
                if(a.isFormerlyAllowed() == true) {
                    applications.add(a);
                }
            }
            }

            model.addAttribute(APPLICATIONS, applications);
            setLoggedUser(model);
            model.addAttribute(TITLE_APP, "all.app");
            model.addAttribute(TOUCHED_DATE, DATE_OVERVIEW);
            model.addAttribute(YEAR, DateMidnight.now().getYear());
            
            return model;
    }
    
    /**
     * show a list of all {@link Application} for the current year not dependent on {@link ApplicationStatus}
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = ALL_APPS, method = RequestMethod.GET)
    public String showAll(Model model) {

        Role role = getLoggedUser().getRole();
        
        if (role == Role.BOSS || role == Role.OFFICE) {
            int year = DateMidnight.now().getYear();
            prepareModelForShowAllMethods(year, model);
            
            return APP_LIST_JSP;
        } else {
            return ERROR_JSP;
        }

    }
    
    
    /**
     * show a list of all {@link Application} for the given year not dependent on {@link ApplicationStatus}
     * @param year
     * @param model
     * @return 
     */
    @RequestMapping(value = ALL_APPS, params = YEAR, method = RequestMethod.GET)
    public String showAllByYear(@RequestParam(YEAR) int year, Model model) {
     
        Role role = getLoggedUser().getRole();
        
        if (role == Role.BOSS || role == Role.OFFICE) {
            prepareModelForShowAllMethods(year, model);
            
            return APP_LIST_JSP;
        } else {
            return ERROR_JSP;
        }
    }


    /**
     * This method prepares applications list view by the given ApplicationStatus.
     *
     * @param  state
     * @param  year
     * @param  model
     *
     * @return
     */
    private String prepareAppListView(ApplicationStatus state, int year, Model model) {

        String title = "";
        String touchedDate = "";

        if (state == ApplicationStatus.WAITING) {
            title = TITLE_WAITING;
            touchedDate = DATE_APPLIED;
        } else if (state == ApplicationStatus.ALLOWED) {
            title = TITLE_ALLOWED;
            touchedDate = DATE_ALLOWED;
        } else if (state == ApplicationStatus.CANCELLED) {
            title = TITLE_CANCELLED;
            touchedDate = DATE_CANCELLED;
        } else if (state == ApplicationStatus.REJECTED) {
            title = TITLE_REJECTED;
            touchedDate = DATE_REJECTED;
        }

        if (getLoggedUser().getRole() == Role.BOSS || getLoggedUser().getRole() == Role.OFFICE) {
            List<Application> applications;
            if(state == ApplicationStatus.CANCELLED) {
                applications = applicationService.getCancelledApplicationsByYearFormerlyAllowed(year);
            } else {
                applications = applicationService.getApplicationsByStateAndYear(state, year);
            }

            model.addAttribute(APPLICATIONS, applications);
            setLoggedUser(model);
            model.addAttribute(TITLE_APP, title);
            model.addAttribute(TOUCHED_DATE, touchedDate);
            model.addAttribute(YEAR, DateMidnight.now().getYear());

            return APP_LIST_JSP;
        } else {
            return ERROR_JSP;
        }
    }
    
    
    /**
     * used if you want to see all waiting applications of the given year
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = WAITING_APPS, params = YEAR, method = RequestMethod.GET)
    public String showWaitingByYear(@RequestParam(YEAR) int year, Model model) {

        return prepareAppListView(ApplicationStatus.WAITING, year, model);
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

        return prepareAppListView(ApplicationStatus.WAITING, DateMidnight.now().getYear(), model);
    }


    /**
     * used if you want to see all allowed applications of the given year
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = ALLOWED_APPS, params = YEAR, method = RequestMethod.GET)
    public String showAllowedByYear(@RequestParam(YEAR) int year, Model model) {

        model.addAttribute(CHECKBOXES, true);
        return prepareAppListView(ApplicationStatus.ALLOWED, year, model);
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

        model.addAttribute(CHECKBOXES, true);
        return prepareAppListView(ApplicationStatus.ALLOWED, DateMidnight.now().getYear(), model);
    }
    
    
    @RequestMapping(value = ALLOWED_APPS + "/{" + APPLICATION_ID + "}", method = RequestMethod.PUT)
    public String setAllowedApplicationToEdited(@PathVariable(APPLICATION_ID) Integer applicationId) {
        Application app = applicationService.getApplicationById(applicationId);
        
        if(app.isIsInCalendar() == false) {
            app.setIsInCalendar(true);
        } else {
            app.setIsInCalendar(false);
        }
        
        applicationService.simpleSave(app);
        return "redirect:/web" + ALLOWED_APPS;
    }


    /**
     * used if you want to see all cancelled applications of the given year
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = CANCELLED_APPS, params = YEAR, method = RequestMethod.GET)
    public String showCancelledByYear(@RequestParam(YEAR) int year, Model model) {

        return prepareAppListView(ApplicationStatus.CANCELLED, year, model);
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

        return prepareAppListView(ApplicationStatus.CANCELLED, DateMidnight.now().getYear(), model);
    }

    
    /**
     * show all rejected applications of the given year
     * @param year
     * @param model
     * @return 
     */
    @RequestMapping(value = REJECTED_APPS, params = YEAR, method = RequestMethod.GET)
    public String showRejectedByYear(@RequestParam(YEAR) int year, Model model) {

        return prepareAppListView(ApplicationStatus.REJECTED, year, model);
    }

    /**
     * show all rejected applications
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = REJECTED_APPS, method = RequestMethod.GET)
    public String showRejected(Model model) {

        return prepareAppListView(ApplicationStatus.REJECTED, DateMidnight.now().getYear(), model);
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

    
    public String newApplication(Person person, int force, AppForm appForm, boolean isOffice, Errors errors, Model model) {

        Person loggedUser = getLoggedUser();
        
        Person personForForm;
        
        if(isOffice) {
            personForForm = person;
        } else {
            personForForm = loggedUser;
        }

        validator.validate(appForm, errors);
        
        if(errors.hasErrors()) {
            prepareForm(personForForm, appForm, model);

            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            if(isOffice) {
                return APP_FORM_OFFICE_JSP;
            } else {
                return APP_FORM_JSP;
            }
        }

        validator.validatePast(appForm, errors, model);

        if (force != 1 && model.containsAttribute("timeError")) {
            prepareForm(personForForm, appForm, model);

            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            if(isOffice) {
                return APP_FORM_OFFICE_JSP;
            } else {
                return APP_FORM_JSP;
            }
        } 
        
            if (checkAndSaveApplicationForm(appForm, personForForm, isOffice, errors, model)) {
                int id = applicationService.getIdOfLatestApplication(personForForm, ApplicationStatus.WAITING);
                Application application = applicationService.getApplicationById(id);
                
                commentService.saveComment(new Comment(), loggedUser, application);
                
                return "redirect:/web/application/" + id;
            } else {
                model.addAttribute("setForce", 0);
            }
        

            prepareForm(personForForm, appForm, model);

        if (errors.hasGlobalErrors()) {
            model.addAttribute("errors", errors);
        }

        if(isOffice) {
                return APP_FORM_OFFICE_JSP;
            } else {
                return APP_FORM_JSP;
            }
    }
    
     /**
     * 
     * use this to save an application (will be in "waiting" state)
     * 
     * @param force is 0 to check if application's period is in the past, after reconfirming is application saved
     * @param appForm {@link AppForm}
     * @param isOffice false: user applies for leave for oneself
     * @param errors
     * @param model
     * @return if success returns the detail view of the new applied application
     */
    @RequestMapping(value = NEW_APP, params = "force", method = RequestMethod.POST)
    public String newApplicationByUser(@RequestParam("force") int force, @ModelAttribute(APPFORM) AppForm appForm, Errors errors, Model model) {
        
        return newApplication(getLoggedUser(), force, appForm, false, errors, model);
    }


    /**
     * This method checks if there are overlapping applications and if the user has enough vacation days to apply for
     * leave.
     *
     * @param  appForm
     * @param  loggedUser
     * @param  isOffice
     * @param  errors
     * @param  model
     *
     * @return  true if everything is alright and application can be saved, else false
     */
    private boolean checkAndSaveApplicationForm(AppForm appForm, Person person, boolean isOffice, Errors errors, Model model) {

        Application application = new Application();
        application = appForm.fillApplicationObject(application);

        application.setPerson(person);
        application.setApplicationDate(DateMidnight.now(GregorianChronology.getInstance()));
        application.setApplier(getLoggedUser());

        BigDecimal days = calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate());

        // check if the vacation would have more than 0 days
        if (days.compareTo(BigDecimal.ZERO) == 0) {
            errors.reject("check.zero");

            return false;
        }

        // check at first if there are existent application for the same period

        // checkOverlap
        // case 1: ok
        // case 2: new application is fully part of existent applications, useless to apply it
        // case 3: gaps in between - feature in later version, now only error message

        OverlapCase overlap = applicationService.checkOverlap(application);

        if (overlap == OverlapCase.FULLY_OVERLAPPING || overlap == OverlapCase.PARTLY_OVERLAPPING) {
            // in this version, these two cases are handled equal
            errors.reject("check.overlap");
        } else if (overlap == OverlapCase.NO_OVERLAPPING) {
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

                // if office applies for leave on behalf of a user
                if (isOffice == true) {
                    // application is signed by office's key
                    applicationService.signApplicationByUser(application, getLoggedUser());

                    LOG.info(" ID: " + application.getId()
                        + " Es wurde ein neuer Antrag von " + getLoggedUser().getFirstName() + " "
                        + getLoggedUser().getLastName() + " für " + person.getFirstName() + " " + person.getLastName()
                        + " angelegt.");

                    // mail to loggedUser of application that office has made an application for him/her
                    mailService.sendAppliedForLeaveByOfficeNotification(application);
                } else {
                    // if user himself applies for leave

                    // application is signed by user's key
                    applicationService.signApplicationByUser(application, person);

                    LOG.info(" ID: " + application.getId()
                        + " Es wurde ein neuer Antrag von " + person.getFirstName() + " " + person.getLastName()
                        + " angelegt.");

                    // mail to applicant
                    mailService.sendConfirmation(application);
                }

                // mail to boss
                mailService.sendNewApplicationNotification(application);

                return true;
            } else {
                if (isOffice == true) {
                    errors.reject("check.enough.office");
                } else {
                    errors.reject("check.enough");
                }

                if (application.getStartDate().getYear() != application.getEndDate().getYear()) {
                    model.addAttribute("daysApp", null);
                } else {
                    model.addAttribute("daysApp", days);
                }
            }
        }

        return false;
    }


    /**
     * This method is analogial to application form for user, but office is able to apply for leave on behalf of other
     * users.
     *
     * @param  personId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = NEW_APP_OFFICE, method = RequestMethod.GET)
    public String newApplicationFormForOffice(@PathVariable(PERSON_ID) Integer personId, Model model) {

        // only office may apply for leave on behalf of other users
        if (getLoggedUser().getRole() == Role.OFFICE) {
            Person person = personService.getPersonByID(personId); // get loggedUser

            // check if the loggedUser is active
            if (person.getRole() != Role.INACTIVE) {
                // check if the loggedUser has a current/valid holidays account and entitlement
                if (accountService.getHolidayEntitlement(DateMidnight.now().getYear(), person) == null
                        || accountService.getHolidaysAccount(DateMidnight.now().getYear(), person) == null) {
                    model.addAttribute(NOTPOSSIBLE, true); // not possible to apply for leave
                } else {
                    prepareForm(person, new AppForm(), model);
                    List<Person> persons = personService.getAllPersons(); // get all active persons
                    model.addAttribute(PERSON_LIST, persons);

                    prepareAccountsMap(persons, model);
                }
            } else {
                model.addAttribute("notpossible", true);
            }

            return APP_FORM_OFFICE_JSP;
        } else {
            return ERROR_JSP;
        }
    }


    /**
     * 
     * This method saves an application that is applied by the office on behalf of an user.
     * 
     * @param force is 0 to check if application's period is in the past, after reconfirming is application saved
     * @param personId person on behalf application is applied
     * @param appForm {@link AppForm}
     * @param errors
     * @param model
     * @return if success returns the detail view of the new applied application
     */
    @RequestMapping(value = NEW_APP_OFFICE, params = "force", method = RequestMethod.POST)
    public String newApplicationByOffice(@RequestParam("force") int force, @PathVariable(PERSON_ID) Integer personId,
        @ModelAttribute(APPFORM) AppForm appForm, Errors errors, Model model) {

        List<Person> persons = personService.getAllPersons(); // get all active persons
        model.addAttribute(PERSON_LIST, persons);

        prepareAccountsMap(persons, model);

        Person person = personService.getPersonByID(personId);
        
        return newApplication(person, force, appForm, true, errors, model);
    }

    public void prepareForm(Person person, AppForm appForm, Model model) {

        int april = 0;

        if (DateUtil.isBeforeApril(DateMidnight.now())) {
            april = 1;
        }

        List<Person> persons = personService.getAllPersonsExceptOne(person.getId());

        ListIterator<Person> itr = persons.listIterator();

        while (itr.hasNext()) {
            Person p = itr.next();

            if (StringUtils.isEmpty(p.getFirstName()) && (StringUtils.isEmpty(p.getLastName()))) {
                itr.remove();
            }
        }

        model.addAttribute(APRIL, april);

        model.addAttribute(PERSON, person);
        model.addAttribute(PERSONS, persons);
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
     * application detail view for office; link in
     *
     * @param  applicationId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = SHOW_APP, method = RequestMethod.GET)
    public String showApplicationDetail(HttpServletRequest request, @PathVariable(APPLICATION_ID) Integer applicationId, Model model) {

        Person loggedUser = getLoggedUser();
        Role role = loggedUser.getRole();
        
        Application application = applicationService.getApplicationById(applicationId);

        if (role == Role.OFFICE || role == Role.BOSS) {

            prepareDetailView(application, -1, model);
            
            return SHOW_APP_DETAIL_JSP;
        } else if (loggedUser.equals(application.getPerson())) {
            prepareDetailView(application, -1, model);

            return SHOW_APP_DETAIL_JSP;
        } else {
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
    public String allowApplication(@PathVariable(APPLICATION_ID) Integer applicationId, @ModelAttribute(COMMENT) Comment comment, Errors errors, Model model) {

        Person boss = getLoggedUser();
        Application application = applicationService.getApplicationById(applicationId);
        
        
        // only boss is able to allow an application but only if this application isn't his own one
        if(boss.getRole() == Role.BOSS && !application.getPerson().equals(boss)) {
            
            applicationService.allow(application, boss);
            
            commentService.saveComment(comment, boss, application);

            String bossName = boss.getFirstName() + " " + boss.getLastName();
            
            LOG.info(application.getApplicationDate() + " ID: " + application.getId() + "Der Antrag von "
                + application.getPerson().getFirstName() + " " + application.getPerson().getLastName()
                + " wurde am " + DateMidnight.now().toString(DATE_FORMAT) + " von " + bossName + " genehmigt.");

            mailService.sendAllowedNotification(application, comment);
            
            return "redirect:/web/application/" + applicationId;
        }
        
        else {
            return ERROR_JSP;
        }
    }
    
    /**
     * If a boss is not sure about the decision if an application should be allowed or rejected, he can ask another boss to decide about this application (an email is sent)
     * @param applicationId
     * @param model
     * @return 
     */
    @RequestMapping(value = REFER_APP, method = RequestMethod.PUT)
    public String referApplication(@PathVariable(APPLICATION_ID) Integer applicationId,
        @ModelAttribute("modelPerson") Person p) {
        
        Application application = applicationService.getApplicationById(applicationId);
        
        Person sender = getLoggedUser();
        String senderName = sender.getFirstName() + " " + sender.getLastName();
        Person reciever = personService.getPersonByLogin(p.getLoginName());
        mailService.sendReferApplicationNotification(application, reciever, senderName);
        
        return "redirect:/web/application/" + applicationId;
        
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

        Person boss = getLoggedUser();
        
        if(boss.getRole() == Role.BOSS) {
            
        Application application = applicationService.getApplicationById(applicationId);

        validator.validateComment(comment, errors);

        if (errors.hasErrors()) {
            prepareDetailView(application, WAITING, model);
            model.addAttribute("errors", errors);

            return SHOW_APP_DETAIL_JSP;
        } else {
            applicationService.reject(application, boss);
            
             commentService.saveComment(comment, boss, application);
             
             String bossName = comment.getNameOfCommentingPerson();

            LOG.info(application.getApplicationDate() + " ID: " + application.getId() + "Der Antrag von "
                + application.getPerson().getFirstName() + " " + application.getPerson().getLastName()
                + " wurde am " + DateMidnight.now().toString(DATE_FORMAT) + " von " + bossName
                + " abgelehnt.");

            // mail to applicant
            mailService.sendRejectedNotification(application, comment);
            
            return "redirect:/web/application/" + applicationId;
        }
        } else {
            return ERROR_JSP;
        }
    }


    /**
     * This method shows a confirm page with details about the application that user wants to cancel; the user has to
     * confirm that he really wants to cancel this application.
     *
     * @param  applicationId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = CANCEL_APP, method = RequestMethod.GET)
    public String cancelApplicationConfirm(@PathVariable(APPLICATION_ID) Integer applicationId, Model model) {

        Application application = applicationService.getApplicationById(applicationId);
        model.addAttribute(STATE_NUMBER, TO_CANCEL);
        
        Person loggedUser = getLoggedUser();

        // office may cancel waiting or allowed applications of other users
        if (loggedUser.getRole() == Role.OFFICE) {
            prepareDetailView(application, TO_CANCEL, model);

            return SHOW_APP_DETAIL_JSP;
        } else {
            // user may cancel only his own waiting applications
            if (loggedUser.equals(application.getPerson())) {
                if(loggedUser.getRole() == Role.BOSS && application.getStatus() == ApplicationStatus.WAITING) {
            List<Person> vips = personService.getPersonsByRole(loggedUser.getRole());
            model.addAttribute("vips", vips);
            model.addAttribute("modelPerson", new Person());
        }
                prepareDetailView(application, TO_CANCEL, model);

                return SHOW_APP_DETAIL_JSP;
            } else {
                return ERROR_JSP;
            }
        }
    }
    


    /**
     * After confirming by user: this method set an application to cancelled.
     *
     * @param  applicationId
     *
     * @return
     */
    @RequestMapping(value = CANCEL_APP, method = RequestMethod.PUT)
    public String cancelApplication(@PathVariable(APPLICATION_ID) Integer applicationId, @ModelAttribute(COMMENT) Comment comment, Errors errors, Model model) {

        Application application = applicationService.getApplicationById(applicationId);
        Person loggedUser = getLoggedUser();
        
        // security check: only user himself or the loggedUser that has the role 'office' must be able to cancel an application
        if (loggedUser.equals(application.getPerson()) || loggedUser.getRole() == Role.OFFICE) {
            
            
        if(!loggedUser.equals(application.getPerson()) || application.getStatus() == ApplicationStatus.ALLOWED) {    
        validator.validateComment(comment, errors);

        if (errors.hasErrors()) {
            prepareDetailView(application, WAITING, model);
            model.addAttribute("errors", errors);

            return SHOW_APP_DETAIL_JSP;
        }
        }
            
            
        boolean allowed = false;

        // if application had status allowed set field formerlyAllowed to true
        if (application.getStatus() == ApplicationStatus.ALLOWED) {
            allowed = true;
            application.setFormerlyAllowed(true);
        }
        
        application.setCanceller(loggedUser);
        applicationService.cancel(application);
        
        commentService.saveComment(comment, loggedUser, application);

            if (allowed) {
                // if application has status ALLOWED, office and bosses get an email
                mailService.sendCancelledNotification(application, false, comment);
            }

            // user has cancelled his own application
        if (loggedUser.equals(application.getPerson())) {
            LOG.info("Antrag-ID: " + application.getId() + "Der Antrag wurde vom Antragssteller ("
                + loggedUser.getFirstName() + " " + loggedUser.getLastName()
                + ") storniert.");

        } else {
            // application has been cancelled by office
            // applicant gets an mail regardless of which application status
            mailService.sendCancelledNotification(application, true, comment);
            LOG.info("Antrag-ID: " + application.getId() + "Der Antrag wurde vom Office ("
                + loggedUser.getFirstName() + " " + loggedUser.getLastName()
                + ") storniert.");
        }
        
        return "redirect:/web/application/" + applicationId;
    } else {
            return ERROR_JSP;
        }
    }


    private void prepareAccountsMap(List<Person> persons, Model model) {

        Map<Person, HolidaysAccount> accounts = new HashMap<Person, HolidaysAccount>();
        HolidaysAccount account;

        for (Person person : persons) {
            account = accountService.getHolidaysAccount(DateMidnight.now().getYear(), person);

            if (account != null) {
                accounts.put(person, account);
            }
        }

        model.addAttribute(ACCOUNTS, accounts);
    }


    private void prepareDetailView(Application application, int stateNumber, Model model) {

        Comment comment = commentService.getCommentByApplicationAndStatus(application, application.getStatus());
        
        if(comment != null) {
            // use this later maybe
            // Locale locale = RequestContextUtils.getLocale(request);
            model.addAttribute(COMMENT, comment);
            } else {
                 model.addAttribute(COMMENT, new Comment());
            }
        
        List<Comment> comments = commentService.getCommentsByApplication(application);
        
       model.addAttribute("comments", comments);
        
        Role role = getLoggedUser().getRole();
        
        if(application.getStatus() == ApplicationStatus.WAITING && role == Role.BOSS) {
            List<Person> vips = personService.getPersonsByRole(role);
            model.addAttribute("vips", vips);
            model.addAttribute("modelPerson", new Person());
        }
        
        setLoggedUser(model);
        model.addAttribute(APPLICATION, application);
        model.addAttribute(STATE_NUMBER, stateNumber);

        // get the number of vacation days that loggedUser has used in the given year
        BigDecimal numberOfUsedDays = applicationService.getUsedVacationDaysOfPersonForYear(application.getPerson(),
                application.getStartDate().getYear());
        model.addAttribute(USED_DAYS, numberOfUsedDays);

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

        // get url of loggedUser's gravatar image
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
    
    
    @RequestMapping(value = REMIND, method = RequestMethod.PUT)
    public String remindBoss(@PathVariable(APPLICATION_ID) Integer applicationId, Model model) {
        
        
        Application application = applicationService.getApplicationById(applicationId);
        DateMidnight remindDate = application.getRemindDate();
        
        if(remindDate != null) {
            if(remindDate.equals(DateMidnight.now())) {
            model.addAttribute("alreadySent", true);
            }
        }  else {
        
        long applicationDate = application.getApplicationDate().getMillis();
        long now = DateMidnight.now().getMillis();
        
        // minimal difference should be two days
        long minDifference = 2 * 24 * 60 * 60 * 1000;
        
        if(now - applicationDate > minDifference) {
            mailService.sendRemindBossNotification(application);
            application.setRemindDate(DateMidnight.now());
            applicationService.simpleSave(application);
            model.addAttribute("isSent", true);
        } else {
            model.addAttribute("noWay", true);
        }
        }
        
        prepareDetailView(application, WAITING, model);
        return SHOW_APP_DETAIL_JSP;
        
    }
}
