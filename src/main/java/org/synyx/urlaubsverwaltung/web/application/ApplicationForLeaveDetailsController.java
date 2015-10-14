package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationCommentService;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.exception.ImpatientAboutApplicationForLeaveProcessException;
import org.synyx.urlaubsverwaltung.core.application.service.exception.RemindAlreadySentException;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;

import java.util.List;
import java.util.Optional;


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
    private VacationDaysService vacationDaysService;

    @Autowired
    private ApplicationCommentService commentService;

    @Autowired
    private WorkDaysService workDaysService;

    @Autowired
    private ApplicationCommentValidator commentValidator;

    @Autowired
    private DepartmentService departmentService;

    @RequestMapping(value = "/{applicationId}", method = RequestMethod.GET)
    public String showApplicationDetail(@PathVariable("applicationId") Integer applicationId,
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Integer requestedYear,
        @RequestParam(value = "action", required = false) String action,
        @RequestParam(value = "shortcut", required = false) boolean shortcut, Model model) {

        Optional<Application> applicationOptional = applicationService.getApplicationById(applicationId);

        if (!applicationOptional.isPresent()) {
            return ControllerConstants.ERROR_JSP;
        }

        Person signedInUser = sessionService.getSignedInUser();
        Application application = applicationOptional.get();
        Person person = application.getPerson();

        boolean samePerson = signedInUser.equals(person);
        boolean isBossOrOffice = signedInUser.hasRole(Role.BOSS) || signedInUser.hasRole(Role.OFFICE);
        boolean isDepartmentHead = departmentService.isDepartmentHeadOfPerson(signedInUser, person);

        if (samePerson || isBossOrOffice || isDepartmentHead) {
            Integer year = requestedYear == null ? application.getEndDate().getYear() : requestedYear;

            prepareDetailView(application, year, action, shortcut, model);

            return "application/app_detail";
        }

        return ControllerConstants.ERROR_JSP;
    }


    private void prepareDetailView(Application application, int year, String action, boolean shortcut, Model model) {

        // COMMENTS
        List<ApplicationComment> comments = commentService.getCommentsByApplication(application);

        model.addAttribute("comment", new ApplicationCommentForm());
        model.addAttribute("comments", comments);

        // SPECIAL ATTRIBUTES FOR BOSSES / DEPARTMENT HEADS
        Person signedInUser = sessionService.getSignedInUser();

        if (application.getStatus() == ApplicationStatus.WAITING
                && (signedInUser.hasRole(Role.BOSS) || signedInUser.hasRole(Role.DEPARTMENT_HEAD))) {
            model.addAttribute("bosses", personService.getPersonsByRole(Role.BOSS));
            model.addAttribute("personToRefer", new Person());
        }

        // APPLICATION FOR LEAVE
        model.addAttribute("application", new ApplicationForLeave(application, workDaysService));

        // DEPARTMENT APPLICATIONS FOR LEAVE
        List<Application> departmentApplications =
            departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(application.getPerson(),
                application.getStartDate(), application.getEndDate());
        model.addAttribute("departmentApplications", departmentApplications);

        // HOLIDAY ACCOUNT
        Optional<Account> account = accountService.getHolidaysAccount(year, application.getPerson());

        if (account.isPresent()) {
            model.addAttribute("vacationDaysLeft", vacationDaysService.getVacationDaysLeft(account.get()));
            model.addAttribute("account", account.get());
            model.addAttribute(PersonConstants.BEFORE_APRIL_ATTRIBUTE, DateUtil.isBeforeApril(DateMidnight.now()));
        }

        // UNSPECIFIC ATTRIBUTES
        model.addAttribute(ControllerConstants.YEAR_ATTRIBUTE, year);
        model.addAttribute("action", action);
        model.addAttribute("shortcut", shortcut);
    }


    /**
     * Allow a not yet allowed application for leave (Boss only!).
     */
    @PreAuthorize(SecurityRules.IS_BOSS_OR_DEPARTMENT_HEAD)
    @RequestMapping(value = "/{applicationId}/allow", method = RequestMethod.PUT)
    public String allowApplication(@PathVariable("applicationId") Integer applicationId,
        @ModelAttribute("comment") ApplicationCommentForm comment,
        @RequestParam(value = "redirect", required = false) String redirectUrl, Errors errors,
        RedirectAttributes redirectAttributes) {

        Optional<Application> application = applicationService.getApplicationById(applicationId);

        if (!application.isPresent()) {
            return ControllerConstants.ERROR_JSP;
        }

        Person signedInUser = sessionService.getSignedInUser();
        Person person = application.get().getPerson();

        boolean isBoss = signedInUser.hasRole(Role.BOSS);
        boolean isDepartmentHead = departmentService.isDepartmentHeadOfPerson(signedInUser, person);

        if (isBoss || isDepartmentHead) {
            comment.setMandatory(false);
            commentValidator.validate(comment, errors);

            if (errors.hasErrors()) {
                redirectAttributes.addFlashAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);

                return "redirect:/web/application/" + applicationId + "?action=allow";
            }

            applicationInteractionService.allow(application.get(), signedInUser,
                Optional.ofNullable(comment.getText()));

            redirectAttributes.addFlashAttribute("allowSuccess", true);

            if (redirectUrl != null) {
                return "redirect:" + redirectUrl;
            }

            return "redirect:/web/application/" + applicationId;
        }

        return ControllerConstants.ERROR_JSP;
    }


    /**
     * If a boss is not sure about the decision if an application should be allowed or rejected, he can ask another boss
     * to decide about this application (an email is sent).
     */
    @PreAuthorize(SecurityRules.IS_BOSS_OR_DEPARTMENT_HEAD)
    @RequestMapping(value = "/{applicationId}/refer", method = RequestMethod.PUT)
    public String referApplication(@PathVariable("applicationId") Integer applicationId,
        @ModelAttribute("personToRefer") Person personToRefer, RedirectAttributes redirectAttributes) {

        Optional<Application> application = applicationService.getApplicationById(applicationId);
        java.util.Optional<Person> recipient = personService.getPersonByLogin(personToRefer.getLoginName());

        if (application.isPresent() && recipient.isPresent()) {
            Person sender = sessionService.getSignedInUser();

            boolean isBoss = sender.hasRole(Role.BOSS);
            boolean isDepartmentHead = departmentService.isDepartmentHeadOfPerson(sender,
                    application.get().getPerson());

            if (isBoss || isDepartmentHead) {
                applicationInteractionService.refer(application.get(), recipient.get(), sender);

                redirectAttributes.addFlashAttribute("referSuccess", true);

                return "redirect:/web/application/" + applicationId;
            }
        }

        return ControllerConstants.ERROR_JSP;
    }


    /**
     * Reject an application for leave (Boss only!).
     */
    @PreAuthorize(SecurityRules.IS_BOSS_OR_DEPARTMENT_HEAD)
    @RequestMapping(value = "/{applicationId}/reject", method = RequestMethod.PUT)
    public String rejectApplication(@PathVariable("applicationId") Integer applicationId,
        @ModelAttribute("comment") ApplicationCommentForm comment,
        @RequestParam(value = "redirect", required = false) String redirectUrl, Errors errors,
        RedirectAttributes redirectAttributes) {

        Optional<Application> application = applicationService.getApplicationById(applicationId);

        if (application.isPresent()) {
            Person person = application.get().getPerson();
            Person signedInUser = sessionService.getSignedInUser();

            boolean isBoss = signedInUser.hasRole(Role.BOSS);
            boolean isDepartmentHead = departmentService.isDepartmentHeadOfPerson(signedInUser, person);

            if (isBoss || isDepartmentHead) {
                comment.setMandatory(true);
                commentValidator.validate(comment, errors);

                if (errors.hasErrors()) {
                    redirectAttributes.addFlashAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);

                    if (redirectUrl != null) {
                        return "redirect:/web/application/" + applicationId + "?action=reject&shortcut=true";
                    }

                    return "redirect:/web/application/" + applicationId + "?action=reject";
                }

                applicationInteractionService.reject(application.get(), signedInUser,
                    Optional.ofNullable(comment.getText()));
                redirectAttributes.addFlashAttribute("rejectSuccess", true);

                if (redirectUrl != null) {
                    return "redirect:" + redirectUrl;
                }

                return "redirect:/web/application/" + applicationId;
            }
        }

        return ControllerConstants.ERROR_JSP;
    }


    /**
     * Cancel an application for leave. Cancelling an application for leave on behalf for someone is allowed only for
     * Office.
     */
    @RequestMapping(value = "/{applicationId}/cancel", method = RequestMethod.PUT)
    public String cancelApplication(@PathVariable("applicationId") Integer applicationId,
        @ModelAttribute("comment") ApplicationCommentForm comment, Errors errors,
        RedirectAttributes redirectAttributes) {

        Optional<Application> optionalApplication = applicationService.getApplicationById(applicationId);

        if (!optionalApplication.isPresent()) {
            return ControllerConstants.ERROR_JSP;
        }

        Person signedInUser = sessionService.getSignedInUser();
        Application application = optionalApplication.get();

        boolean isWaiting = application.hasStatus(ApplicationStatus.WAITING);
        boolean isAllowed = application.hasStatus(ApplicationStatus.ALLOWED);

        // security check: only two cases where cancelling is possible
        // 1: user can cancel his own applications for leave if they have the state waiting
        // 2: office can cancel all applications for leave that has the state waiting or allowed, even for other persons
        if (signedInUser.equals(application.getPerson()) && isWaiting) {
            // user can cancel only his own waiting applications, so the comment is NOT mandatory
            comment.setMandatory(false);
        } else if (signedInUser.hasRole(Role.OFFICE) && (isWaiting || isAllowed)) {
            // office cancels application of other users, state can be waiting or allowed, so the comment is mandatory
            comment.setMandatory(true);
        } else {
            return ControllerConstants.ERROR_JSP;
        }

        commentValidator.validate(comment, errors);

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);

            return "redirect:/web/application/" + applicationId + "?action=cancel";
        }

        applicationInteractionService.cancel(application, signedInUser, Optional.ofNullable(comment.getText()));

        return "redirect:/web/application/" + applicationId;
    }


    /**
     * Remind the bosses about the decision of an application for leave.
     */
    @RequestMapping(value = "/{applicationId}/remind", method = RequestMethod.PUT)
    public String remindBoss(@PathVariable("applicationId") Integer applicationId,
        RedirectAttributes redirectAttributes) {

        Optional<Application> optionalApplication = applicationService.getApplicationById(applicationId);

        if (!optionalApplication.isPresent()) {
            return ControllerConstants.ERROR_JSP;
        }

        Application application = optionalApplication.get();

        try {
            applicationInteractionService.remind(application);
            redirectAttributes.addFlashAttribute("remindIsSent", true);
        } catch (RemindAlreadySentException ex) {
            redirectAttributes.addFlashAttribute("remindAlreadySent", true);
        } catch (ImpatientAboutApplicationForLeaveProcessException ex) {
            redirectAttributes.addFlashAttribute("remindNoWay", true);
        }

        return "redirect:/web/application/" + applicationId;
    }
}
