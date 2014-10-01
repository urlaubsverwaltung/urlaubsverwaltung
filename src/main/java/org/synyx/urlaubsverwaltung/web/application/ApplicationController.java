package org.synyx.urlaubsverwaltung.web.application;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.chrono.GregorianChronology;

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

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.OverlapCase;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.application.service.CommentService;
import org.synyx.urlaubsverwaltung.core.application.service.OverlapService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.sicknote.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.web.util.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.util.GravatarUtil;
import org.synyx.urlaubsverwaltung.web.validator.ApplicationValidator;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Controller for management of {@link Application} entities.
 *
 * @author  Aljona Murygina
 */
@Controller
public class ApplicationController {

    private static final Logger LOG = Logger.getLogger(ApplicationController.class);

    // links start with...
    private static final String SHORT_PATH_APPLICATION = "/" + ControllerConstants.APPLICATION;

    private static final String LONG_PATH_APPLICATION = "/" + ControllerConstants.APPLICATION + "/{";

    // list of applications by state
    private static final String APP_LIST = SHORT_PATH_APPLICATION;
    private static final String ALL_APPS = SHORT_PATH_APPLICATION + "/all";
    private static final String WAITING_APPS = SHORT_PATH_APPLICATION + "/waiting";
    private static final String ALLOWED_APPS = SHORT_PATH_APPLICATION + "/allowed";
    private static final String CANCELLED_APPS = SHORT_PATH_APPLICATION + "/cancelled";

    private static final String REJECTED_APPS = SHORT_PATH_APPLICATION + "/rejected";

    // form to apply vacation
    private static final String NEW_APP = SHORT_PATH_APPLICATION + "/new"; // form for user

    private static final String NEW_APP_OFFICE = "/{" + ApplicationConstants.PERSON_ID + "}/application/new"; // form for office

    // for user: the only way editing an application for user is to cancel it
    // (application may have state waiting or allowed)
    private static final String CANCEL_APP = LONG_PATH_APPLICATION + ApplicationConstants.APPLICATION_ID + "}/cancel";

    // detailed view of application
    private static final String SHOW_APP = LONG_PATH_APPLICATION + ApplicationConstants.APPLICATION_ID + "}";

    // allow or reject application
    private static final String ALLOW_APP = LONG_PATH_APPLICATION + ApplicationConstants.APPLICATION_ID + "}/allow";

    private static final String REJECT_APP = LONG_PATH_APPLICATION + ApplicationConstants.APPLICATION_ID + "}/reject";

    // refer application to other boss
    private static final String REFER_APP = LONG_PATH_APPLICATION + ApplicationConstants.APPLICATION_ID + "}/refer";

    // remind boss to decide about application
    private static final String REMIND = LONG_PATH_APPLICATION + ApplicationConstants.APPLICATION_ID + "}/remind";

    @Autowired
    private PersonService personService;

    @Autowired
    private ApplicationInteractionService applicationInteractionService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OverlapService overlapService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ApplicationValidator validator;

    @Autowired
    private MailService mailService;

    @Autowired
    private SessionService sessionService;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor(locale));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    /**
     * shows the default list, dependent on user role: if boss show waiting applications, if office show all
     * applications
     *
     * @param  model
     */
    @RequestMapping(value = APP_LIST, method = RequestMethod.GET)
    public String showDefault() {

        if (sessionService.isBoss() || sessionService.isOffice()) {
            return "redirect:/web" + WAITING_APPS;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Prepares the model object for the showAll or showAllByYear methods.
     *
     * @param  year
     * @param  model
     *
     * @return
     */
    private Model prepareModelForShowAllMethods(int year, Model model) {

        DateMidnight firstDay = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDay = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        Map<Application, String> gravatarUrls = new HashMap<>();
        List<Application> apps = applicationService.getApplicationsForACertainPeriod(firstDay, lastDay);

        List<Application> applications = new ArrayList<Application>();

        for (Application a : apps) {

            boolean isNotCancelled = a.getStatus() != ApplicationStatus.CANCELLED;
            boolean isCancelledButWasAllowed = a.getStatus() == ApplicationStatus.CANCELLED && a.isFormerlyAllowed();

            if(isNotCancelled || isCancelledButWasAllowed) {
                applications.add(a);

                String gravatarUrl = GravatarUtil.createImgURL(a.getPerson().getEmail());

                if (gravatarUrl != null) {
                    gravatarUrls.put(a, gravatarUrl);
                }

            }

        }

        model.addAttribute(ControllerConstants.APPLICATIONS, applications);
        model.addAttribute(PersonConstants.GRAVATAR_URLS, gravatarUrls);
        sessionService.setLoggedUser(model);
        model.addAttribute(ApplicationConstants.TITLE_APP, "applications.all");
        model.addAttribute(ApplicationConstants.TOUCHED_DATE, ApplicationConstants.DATE_OVERVIEW);
        model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());

        return model;
    }


    /**
     * show a list of all {@link Application} for the current year not dependent on {@link ApplicationStatus}.
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = ALL_APPS, method = RequestMethod.GET)
    public String showAll(Model model) {

        if (sessionService.isBoss() || sessionService.isOffice()) {
            int year = DateMidnight.now().getYear();
            prepareModelForShowAllMethods(year, model);

            return ApplicationConstants.APP_LIST_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * show a list of all {@link Application} for the given year not dependent on {@link ApplicationStatus}.
     *
     * @param  year
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = ALL_APPS, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showAllByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        if (sessionService.isBoss() || sessionService.isOffice()) {
            prepareModelForShowAllMethods(year, model);

            return ApplicationConstants.APP_LIST_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
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
            title = ApplicationConstants.TITLE_WAITING;
            touchedDate = ApplicationConstants.DATE_APPLIED;
        } else if (state == ApplicationStatus.ALLOWED) {
            title = ApplicationConstants.TITLE_ALLOWED;
            touchedDate = ApplicationConstants.DATE_ALLOWED;
        } else if (state == ApplicationStatus.CANCELLED) {
            title = ApplicationConstants.TITLE_CANCELLED;
            touchedDate = ApplicationConstants.DATE_CANCELLED;
        } else if (state == ApplicationStatus.REJECTED) {
            title = ApplicationConstants.TITLE_REJECTED;
            touchedDate = ApplicationConstants.DATE_REJECTED;
        }

        if (sessionService.isBoss() || sessionService.isOffice()) {
            List<Application> applications;

            if (state == ApplicationStatus.CANCELLED) {
                applications = applicationService.getCancelledApplicationsByYearFormerlyAllowed(year);
            } else {
                applications = applicationService.getApplicationsByStateAndYear(state, year);
            }

            Map<Application, String>  gravatarUrls = new HashMap<>();

            for(Application app : applications) {
                String gravatarUrl = GravatarUtil.createImgURL(app.getPerson().getEmail());

                if (gravatarUrl != null) {
                    gravatarUrls.put(app, gravatarUrl);
                }
            }

            model.addAttribute(PersonConstants.GRAVATAR_URLS, gravatarUrls);
            model.addAttribute(ControllerConstants.APPLICATIONS, applications);
            sessionService.setLoggedUser(model);
            model.addAttribute(ApplicationConstants.TITLE_APP, title);
            model.addAttribute(ApplicationConstants.TOUCHED_DATE, touchedDate);
            model.addAttribute(ControllerConstants.YEAR, DateMidnight.now().getYear());

            return ApplicationConstants.APP_LIST_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * used if you want to see all waiting applications of the given year.
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = WAITING_APPS, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showWaitingByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        return prepareAppListView(ApplicationStatus.WAITING, year, model);
    }


    /**
     * used if you want to see all waiting applications.
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
     * used if you want to see all allowed applications of the given year.
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = ALLOWED_APPS, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showAllowedByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        model.addAttribute(ApplicationConstants.CHECKBOXES, true);

        return prepareAppListView(ApplicationStatus.ALLOWED, year, model);
    }


    /**
     * used if you want to see all allowed applications.
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = ALLOWED_APPS, method = RequestMethod.GET)
    public String showAllowed(Model model) {

        model.addAttribute(ApplicationConstants.CHECKBOXES, true);

        return prepareAppListView(ApplicationStatus.ALLOWED, DateMidnight.now().getYear(), model);
    }


    /**
     * used if you want to see all cancelled applications of the given year.
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = CANCELLED_APPS, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showCancelledByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        return prepareAppListView(ApplicationStatus.CANCELLED, year, model);
    }


    /**
     * used if you want to see all cancelled applications.
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
     * show all rejected applications of the given year.
     *
     * @param  year
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = REJECTED_APPS, params = ControllerConstants.YEAR, method = RequestMethod.GET)
    public String showRejectedByYear(@RequestParam(ControllerConstants.YEAR) int year, Model model) {

        return prepareAppListView(ApplicationStatus.REJECTED, year, model);
    }


    /**
     * show all rejected applications.
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = REJECTED_APPS, method = RequestMethod.GET)
    public String showRejected(Model model) {

        return prepareAppListView(ApplicationStatus.REJECTED, DateMidnight.now().getYear(), model);
    }


    /**
     * used if you want to apply an application for leave (shows formular).
     *
     * @param  model  the datamodel
     *
     * @return
     */
    @RequestMapping(value = NEW_APP, method = RequestMethod.GET)
    public String newApplicationForm(Model model) {

        if (sessionService.isInactive()) {
            return ControllerConstants.LOGIN_LINK;
        } else {
            Person person = sessionService.getLoggedUser();

            if (accountService.getHolidaysAccount(DateMidnight.now().getYear(), person) == null) {
                model.addAttribute(ApplicationConstants.NOTPOSSIBLE, true);
            } else {
                prepareForm(person, new AppForm(), model);
            }

            return ApplicationConstants.APP_FORM_JSP;
        }
    }


    public String newApplication(Person person, AppForm appForm, boolean isOffice, Errors errors, Model model) {

        Person loggedUser = sessionService.getLoggedUser();

        Person personForForm;

        if (isOffice) {
            personForForm = person;
        } else {
            personForForm = loggedUser;
        }

        validator.validate(appForm, errors);

        if (errors.hasErrors()) {
            prepareForm(personForForm, appForm, model);

            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            return ApplicationConstants.APP_FORM_JSP;
        }

        validator.validatePast(appForm, errors, model);

        if (model.containsAttribute("timeError")) {
            prepareForm(personForForm, appForm, model);

            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            return ApplicationConstants.APP_FORM_JSP;
        }

        if (checkAndSaveApplicationForm(appForm, personForForm, isOffice, errors, model)) {
            int id = applicationService.getIdOfLatestApplication(personForForm, ApplicationStatus.WAITING);

            return "redirect:/web/application/" + id;
        } else {
            prepareForm(personForForm, appForm, model);

            if (errors.hasGlobalErrors()) {
                model.addAttribute("errors", errors);
            }

            return ApplicationConstants.APP_FORM_JSP;
        }
    }


    /**
     * use this to save an application (will be in "waiting" state).
     *
     * @param  appForm {@link AppForm}
     * @param  errors
     * @param  model
     *
     * @return  if success returns the detail view of the new applied application
     */
    @RequestMapping(value = NEW_APP, method = RequestMethod.POST)
    public String newApplicationByUser(@ModelAttribute(ApplicationConstants.APPFORM) AppForm appForm, Errors errors,
        Model model) {

        return newApplication(sessionService.getLoggedUser(), appForm, false, errors, model);
    }


    /**
     * This method checks if there are overlapping applications and if the user has enough vacation days to apply for
     * leave.
     *
     * @param  appForm
     * @param  isOffice
     * @param  errors
     * @param  model
     *
     * @return  true if everything is alright and application can be saved, else false
     */
    private boolean checkAndSaveApplicationForm(AppForm appForm, Person person, boolean isOffice, Errors errors,
        Model model) {

        Application application = appForm.createApplicationObject();

        BigDecimal days = applicationInteractionService.getNumberOfVacationDays(application);

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

        OverlapCase overlap = overlapService.checkOverlap(application);

        if (overlap == OverlapCase.FULLY_OVERLAPPING || overlap == OverlapCase.PARTLY_OVERLAPPING) {
            // in this version, these two cases are handled equal
            errors.reject("check.overlap");
        } else if (overlap == OverlapCase.NO_OVERLAPPING) {
            // if there is no overlap go to next check but only if vacation type is holiday, else you don't have to
            // check if there are enough days on user's holidays account
            boolean enoughDays = false;

            if (application.getVacationType() == VacationType.HOLIDAY) {
                enoughDays = calculationService.checkApplication(application);
            }

            // enough days to apply for leave
            if (enoughDays || (application.getVacationType() != VacationType.HOLIDAY)) {
                applicationInteractionService.apply(application, sessionService.getLoggedUser());

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
    public String newApplicationFormForOffice(@PathVariable(ApplicationConstants.PERSON_ID) Integer personId,
        Model model) {

        // only office may apply for leave on behalf of other users
        if (sessionService.isOffice()) {
            Person person = personService.getPersonByID(personId); // get loggedUser

            // check if the loggedUser is active
            if (sessionService.isInactive()) {
                model.addAttribute("notpossible", true);
            } else {
                // check if the loggedUser has a current/valid holidays account
                if (accountService.getHolidaysAccount(DateMidnight.now().getYear(), person) == null) {
                    model.addAttribute(ApplicationConstants.NOTPOSSIBLE, true); // not possible to apply for leave
                } else {
                    prepareForm(person, new AppForm(), model);

                    List<Person> persons = personService.getAllPersons(); // get all active persons
                    model.addAttribute(ApplicationConstants.PERSON_LIST, persons);
                }
            }

            return ApplicationConstants.APP_FORM_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * This method saves an application that is applied by the office on behalf of an user.
     *
     * @param  personId  person on behalf application is applied
     * @param  appForm {@link AppForm}
     * @param  errors
     * @param  model
     *
     * @return  if success returns the detail view of the new applied application
     */
    @RequestMapping(value = NEW_APP_OFFICE, method = RequestMethod.POST)
    public String newApplicationByOffice(@PathVariable(ApplicationConstants.PERSON_ID) Integer personId,
        @ModelAttribute(ApplicationConstants.APPFORM) AppForm appForm, Errors errors, Model model) {

        List<Person> persons = personService.getAllPersons(); // get all active persons
        model.addAttribute(ApplicationConstants.PERSON_LIST, persons);

        prepareAccountsMap(persons, model);

        Person person = personService.getPersonByID(personId);

        return newApplication(person, appForm, true, errors, model);
    }


    public void prepareForm(Person person, AppForm appForm, Model model) {

        List<Person> persons = personService.getAllPersonsExcept(person);

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
        model.addAttribute(ApplicationConstants.APPFORM, appForm);
        model.addAttribute(ControllerConstants.ACCOUNT, account);
        model.addAttribute("vacTypes", VacationType.values());
        model.addAttribute("full", DayLength.FULL);
        model.addAttribute("morning", DayLength.MORNING);
        model.addAttribute("noon", DayLength.NOON);
        sessionService.setLoggedUser(model);
    }


    /**
     * application detail view for office; link in.
     *
     * @param  applicationId
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = SHOW_APP, method = RequestMethod.GET)
    public String showApplicationDetail(@PathVariable(ApplicationConstants.APPLICATION_ID) Integer applicationId,
        Model model) {

        Person loggedUser = sessionService.getLoggedUser();

        Application application = applicationService.getApplicationById(applicationId);

        if (sessionService.isBoss() || sessionService.isOffice()) {
            prepareDetailView(application, -1, model);

            return ApplicationConstants.SHOW_APP_DETAIL_JSP;
        } else if (loggedUser.equals(application.getPerson())) {
            prepareDetailView(application, -1, model);

            return ApplicationConstants.SHOW_APP_DETAIL_JSP;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * Allow a not yet allowed application for leave (Boss only!).
     */
    @RequestMapping(value = ALLOW_APP, method = RequestMethod.PUT)
    public String allowApplication(@PathVariable(ApplicationConstants.APPLICATION_ID) Integer applicationId,
        @ModelAttribute(ApplicationConstants.COMMENT) Comment comment, RedirectAttributes redirectAttributes) {

        Person boss = sessionService.getLoggedUser();
        Application application = applicationService.getApplicationById(applicationId);

        if (sessionService.isBoss()) {
            applicationInteractionService.allow(application, boss, comment);

            redirectAttributes.addFlashAttribute("allowSuccess", true);

            return "redirect:/web/application/" + applicationId;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * If a boss is not sure about the decision if an application should be allowed or rejected, he can ask another boss
     * to decide about this application (an email is sent).
     *
     * @param  applicationId
     *
     * @return
     */
    @RequestMapping(value = REFER_APP, method = RequestMethod.PUT)
    public String referApplication(@PathVariable(ApplicationConstants.APPLICATION_ID) Integer applicationId,
        @ModelAttribute("modelPerson") Person p, RedirectAttributes redirectAttributes) {

        Application application = applicationService.getApplicationById(applicationId);

        Person sender = sessionService.getLoggedUser();
        String senderName = sender.getFirstName() + " " + sender.getLastName();
        Person recipient = personService.getPersonByLogin(p.getLoginName());
        mailService.sendReferApplicationNotification(application, recipient, senderName);

        redirectAttributes.addFlashAttribute("referSuccess", true);

        return "redirect:/web/application/" + applicationId;
    }


    /**
     * Reject an application for leave (Boss only!).
     */
    @RequestMapping(value = REJECT_APP, method = RequestMethod.PUT)
    public String rejectApplication(@PathVariable(ApplicationConstants.APPLICATION_ID) Integer applicationId,
        @ModelAttribute(ApplicationConstants.COMMENT) Comment comment, Errors errors, RedirectAttributes redirectAttributes) {

        Person boss = sessionService.getLoggedUser();

        if (sessionService.isBoss()) {
            Application application = applicationService.getApplicationById(applicationId);

            validator.validateComment(comment, errors, true);

            if (errors.hasErrors()) {
                redirectAttributes.addFlashAttribute("errors", errors);
                redirectAttributes.addFlashAttribute("action", "reject");
            } else {
                applicationInteractionService.reject(application, boss, comment);
                redirectAttributes.addFlashAttribute("rejectSuccess", true);
            }

            return "redirect:/web/application/" + applicationId;
        } else {
            return ControllerConstants.ERROR_JSP;
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
    public String cancelApplication(@PathVariable(ApplicationConstants.APPLICATION_ID) Integer applicationId,
        @ModelAttribute(ApplicationConstants.COMMENT) Comment comment, Errors errors, RedirectAttributes redirectAttributes) {

        Application application = applicationService.getApplicationById(applicationId);
        Person loggedUser = sessionService.getLoggedUser();
        ApplicationStatus status = application.getStatus();

        // security check: only two cases where cancelling is possible
        // 1: office can cancel all applications for leave that has the state waiting or allowed, even for other persons
        // 2: user can cancel his own applications for leave if they have the state waiting
        boolean officeIsCancelling = sessionService.isOffice()
            && (status == ApplicationStatus.WAITING || status == ApplicationStatus.ALLOWED);
        boolean userIsCancelling = loggedUser.equals(application.getPerson()) && status == ApplicationStatus.WAITING;

        if (officeIsCancelling || userIsCancelling) {
            // user can cancel only his own waiting applications, so the comment is NOT mandatory
            if (userIsCancelling) {
                validator.validateComment(comment, errors, false);
            }
            // office cancels application of other users, state can be waiting or allowed, so the comment is mandatory
            else if (officeIsCancelling) {
                validator.validateComment(comment, errors, true);
            }

            if (errors.hasErrors()) {
                redirectAttributes.addFlashAttribute("errors", errors);
                redirectAttributes.addFlashAttribute("action", "cancel");
            } else {
                applicationInteractionService.cancel(application, loggedUser, comment);
            }

            return "redirect:/web/application/" + applicationId;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    private void prepareAccountsMap(List<Person> persons, Model model) {

        Map<Person, Account> accounts = new HashMap<Person, Account>();
        Account account;

        for (Person person : persons) {
            account = accountService.getHolidaysAccount(DateMidnight.now().getYear(), person);

            if (account != null) {
                accounts.put(person, account);
            }
        }

        model.addAttribute(ControllerConstants.ACCOUNTS, accounts);
    }


    private void prepareDetailView(Application application, int stateNumber, Model model) {

        model.addAttribute(ApplicationConstants.COMMENT, new Comment());

        List<Comment> comments = commentService.getCommentsByApplication(application);

        model.addAttribute("comments", comments);

        if (application.getStatus() == ApplicationStatus.WAITING && sessionService.isBoss()) {
            // get all persons that have the Boss Role
            List<Person> bosses = personService.getPersonsByRole(Role.BOSS);
            model.addAttribute("bosses", bosses);
            model.addAttribute("modelPerson", new Person());
        }

        sessionService.setLoggedUser(model);
        model.addAttribute(ControllerConstants.APPLICATION, application);
        model.addAttribute(ApplicationConstants.STATE_NUMBER, stateNumber);

        int year = application.getStartDate().getYear();

        Account account = accountService.getHolidaysAccount(year, application.getPerson());

        if (account != null) {
            BigDecimal vacationDaysLeft = calculationService.calculateLeftVacationDays(account);
            BigDecimal remainingVacationDaysLeft = calculationService.calculateLeftRemainingVacationDays(account);
            model.addAttribute(PersonConstants.LEFT_DAYS, vacationDaysLeft);
            model.addAttribute(PersonConstants.REM_LEFT_DAYS, remainingVacationDaysLeft);
            model.addAttribute(PersonConstants.BEFORE_APRIL, DateUtil.isBeforeApril(DateMidnight.now()));
        }

        model.addAttribute(ControllerConstants.ACCOUNT, account);
        model.addAttribute(ControllerConstants.YEAR, year);

        // get url of loggedUser's gravatar image
        String url = GravatarUtil.createImgURL(application.getPerson().getEmail());
        model.addAttribute("gravatar", url);
    }


    @RequestMapping(value = REMIND, method = RequestMethod.PUT)
    public String remindBoss(@PathVariable(ApplicationConstants.APPLICATION_ID) Integer applicationId, RedirectAttributes redirectAttributes) {

        Application application = applicationService.getApplicationById(applicationId);
        DateMidnight remindDate = application.getRemindDate();

        if (remindDate != null) {
            if (remindDate.equals(DateMidnight.now())) {
                redirectAttributes.addFlashAttribute("remindAlreadySent", true);
            }
        } else {
            long applicationDate = application.getApplicationDate().getMillis();
            long now = DateMidnight.now().getMillis();

            // minimal difference should be two days
            long minDifference = 2 * 24 * 60 * 60 * 1000;

            if (now - applicationDate > minDifference) {
                mailService.sendRemindBossNotification(application);
                application.setRemindDate(DateMidnight.now());
                applicationService.save(application);
                redirectAttributes.addFlashAttribute("remindIsSent", true);
            } else {
                redirectAttributes.addFlashAttribute("remindNoWay", true);
            }
        }

        return "redirect:/web/application/" + applicationId;
    }
}
