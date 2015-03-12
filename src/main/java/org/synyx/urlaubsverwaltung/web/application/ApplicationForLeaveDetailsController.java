package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.application.service.CommentService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.util.GravatarUtil;
import org.synyx.urlaubsverwaltung.web.validator.CommentValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Controller to manage applications for leave.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@RequestMapping("/application")
@Controller
public class ApplicationForLeaveDetailsController {

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
    private CommentService commentService;

    @Autowired
    private CommentValidator commentValidator;

    @Autowired
    private MailService mailService;

    @RequestMapping(value = "/{applicationId}", method = RequestMethod.GET)
    public String showApplicationDetail(@PathVariable("applicationId") Integer applicationId, Model model) {

        Person loggedUser = sessionService.getLoggedUser();

        Application application = applicationService.getApplicationById(applicationId);

        if (loggedUser.equals(application.getPerson()) || (sessionService.isBoss() || sessionService.isOffice())) {
            prepareDetailView(application, model);

            return ControllerConstants.APPLICATIONS_URL + "/app_detail";
        }

        return ControllerConstants.ERROR_JSP;
    }


    private void prepareDetailView(Application application, Model model) {

        model.addAttribute("comment", new Comment());

        List<Comment> comments = commentService.getCommentsByApplication(application);

        model.addAttribute("comments", comments);

        Map<Comment, String> gravatarUrls = new HashMap<>();

        for (Comment comment : comments) {
            String gravatarUrl = GravatarUtil.createImgURL(comment.getPerson().getEmail());

            if (gravatarUrl != null) {
                gravatarUrls.put(comment, gravatarUrl);
            }
        }

        model.addAttribute(PersonConstants.GRAVATAR_URLS, gravatarUrls);

        if (application.getStatus() == ApplicationStatus.WAITING && sessionService.isBoss()) {
            // get all persons that have the Boss Role
            List<Person> bosses = personService.getPersonsByRole(Role.BOSS);
            model.addAttribute("bosses", bosses);
            model.addAttribute("modelPerson", new Person());
        }

        model.addAttribute(PersonConstants.LOGGED_USER, sessionService.getLoggedUser());
        model.addAttribute("application", application);

        int year = application.getStartDate().getYear();

        Account account = accountService.getHolidaysAccount(year, application.getPerson());

        if (account != null) {
            model.addAttribute("vacationDaysLeft", calculationService.getVacationDaysLeft(account));
            model.addAttribute(PersonConstants.BEFORE_APRIL, DateUtil.isBeforeApril(DateMidnight.now()));
        }

        model.addAttribute("account", account);
        model.addAttribute(ControllerConstants.YEAR, year);

        // get url of loggedUser's gravatar image
        String url = GravatarUtil.createImgURL(application.getPerson().getEmail());
        model.addAttribute("gravatar", url);
    }


    /**
     * Allow a not yet allowed application for leave (Boss only!).
     */
    @RequestMapping(value = "/{applicationId}/allow", method = RequestMethod.PUT)
    public String allowApplication(@PathVariable("applicationId") Integer applicationId,
        @ModelAttribute("comment") Comment comment, Errors errors, RedirectAttributes redirectAttributes) {

        Person boss = sessionService.getLoggedUser();
        Application application = applicationService.getApplicationById(applicationId);

        if (sessionService.isBoss()) {
            comment.setMandatory(false);
            commentValidator.validate(comment, errors);

            if (errors.hasErrors()) {
                redirectAttributes.addFlashAttribute("errors", errors);
                redirectAttributes.addFlashAttribute("action", "allow");
            } else {
                applicationInteractionService.allow(application, boss, comment);
                redirectAttributes.addFlashAttribute("allowSuccess", true);
            }

            return "redirect:/web/application/" + applicationId;
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    /**
     * If a boss is not sure about the decision if an application should be allowed or rejected, he can ask another boss
     * to decide about this application (an email is sent).
     */
    @RequestMapping(value = "/{applicationId}/refer", method = RequestMethod.PUT)
    public String referApplication(@PathVariable("applicationId") Integer applicationId,
        @ModelAttribute("modelPerson") Person p, RedirectAttributes redirectAttributes) {

        Application application = applicationService.getApplicationById(applicationId);

        Person sender = sessionService.getLoggedUser();
        Person recipient = personService.getPersonByLogin(p.getLoginName());
        mailService.sendReferApplicationNotification(application, recipient, sender);

        redirectAttributes.addFlashAttribute("referSuccess", true);

        return "redirect:/web/application/" + applicationId;
    }


    /**
     * Reject an application for leave (Boss only!).
     */
    @RequestMapping(value = "/{applicationId}/reject", method = RequestMethod.PUT)
    public String rejectApplication(@PathVariable("applicationId") Integer applicationId,
        @ModelAttribute("comment") Comment comment, Errors errors, RedirectAttributes redirectAttributes) {

        Person boss = sessionService.getLoggedUser();

        if (sessionService.isBoss()) {
            Application application = applicationService.getApplicationById(applicationId);

            comment.setMandatory(true);
            commentValidator.validate(comment, errors);

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
     * Cancel an application for leave. Cancelling an application for leave on behalf for someone is allowed only for
     * Office.
     */
    @RequestMapping(value = "/{applicationId}/cancel", method = RequestMethod.PUT)
    public String cancelApplication(@PathVariable("applicationId") Integer applicationId,
        @ModelAttribute("comment") Comment comment, Errors errors, RedirectAttributes redirectAttributes) {

        Application application = applicationService.getApplicationById(applicationId);
        Person loggedUser = sessionService.getLoggedUser();

        boolean isWaiting = application.hasStatus(ApplicationStatus.WAITING);
        boolean isAllowed = application.hasStatus(ApplicationStatus.ALLOWED);

        // security check: only two cases where cancelling is possible
        // 1: user can cancel his own applications for leave if they have the state waiting
        // 2: office can cancel all applications for leave that has the state waiting or allowed, even for other persons
        if (loggedUser.equals(application.getPerson()) && isWaiting) {
            // user can cancel only his own waiting applications, so the comment is NOT mandatory
            comment.setMandatory(false);
        } else if (sessionService.isOffice() && (isWaiting || isAllowed)) {
            // office cancels application of other users, state can be waiting or allowed, so the comment is mandatory
            comment.setMandatory(true);
        } else {
            return ControllerConstants.ERROR_JSP;
        }

        commentValidator.validate(comment, errors);

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", errors);
            redirectAttributes.addFlashAttribute("action", "cancel");
        } else {
            applicationInteractionService.cancel(application, loggedUser, comment);
        }

        return "redirect:/web/application/" + applicationId;
    }


    /**
     * Remind the bosses about the decision of an application for leave.
     */
    @RequestMapping(value = "/{applicationId}/remind", method = RequestMethod.PUT)
    public String remindBoss(@PathVariable("applicationId") Integer applicationId,
        RedirectAttributes redirectAttributes) {

        // TODO: move this to a service method

        Application application = applicationService.getApplicationById(applicationId);
        DateMidnight remindDate = application.getRemindDate();

        if (remindDate != null) {
            if (remindDate.isEqualNow()) {
                redirectAttributes.addFlashAttribute("remindAlreadySent", true);
            }
        } else {
            int minDaysToWait = 2;

            DateMidnight minDateForNotification = application.getApplicationDate().plusDays(minDaysToWait);

            if (minDateForNotification.isAfterNow()) {
                redirectAttributes.addFlashAttribute("remindNoWay", true);
            } else {
                mailService.sendRemindBossNotification(application);
                application.setRemindDate(DateMidnight.now());
                applicationService.save(application);
                redirectAttributes.addFlashAttribute("remindIsSent", true);
            }
        }

        return "redirect:/web/application/" + applicationId;
    }
}
