package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.AccessDeniedException;
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
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.person.UnknownPersonException;

import java.util.List;
import java.util.Optional;


/**
 * Controller to manage applications for leave.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@RequestMapping("/web/application")
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

    @Autowired
    private WorkingTimeService workingTimeService;

    @RequestMapping(value = "/{applicationId}", method = RequestMethod.GET)
    public String showApplicationDetail(@PathVariable("applicationId") Integer applicationId,
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Integer requestedYear,
        @RequestParam(value = "action", required = false) String action,
        @RequestParam(value = "shortcut", required = false) boolean shortcut, Model model)
        throws UnknownApplicationForLeaveException, AccessDeniedException {

        Application application = applicationService.getApplicationById(applicationId).orElseThrow(() ->
                    new UnknownApplicationForLeaveException(applicationId));

        Person signedInUser = sessionService.getSignedInUser();
        Person person = application.getPerson();

        if (!sessionService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(String.format(
                    "User '%s' has not the correct permissions to see application for leave of user '%s'",
                    signedInUser.getLoginName(), person.getLoginName()));
        }

        Integer year = requestedYear == null ? application.getEndDate().getYear() : requestedYear;

        prepareDetailView(application, year, action, shortcut, model);

        return "application/app_detail";
    }


    private void prepareDetailView(Application application, int year, String action, boolean shortcut, Model model) {

        // COMMENTS
        List<ApplicationComment> comments = commentService.getCommentsByApplication(application);

        model.addAttribute("comment", new ApplicationCommentForm());
        model.addAttribute("comments", comments);
        model.addAttribute("lastComment", comments.get(comments.size() - 1));

        // SPECIAL ATTRIBUTES FOR BOSSES / DEPARTMENT HEADS
        Person signedInUser = sessionService.getSignedInUser();

        boolean isNotYetAllowed = application.hasStatus(ApplicationStatus.WAITING)
            || application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        boolean isPrivilegedUser = signedInUser.hasRole(Role.BOSS) || signedInUser.hasRole(Role.DEPARTMENT_HEAD)
            || signedInUser.hasRole(Role.SECOND_STAGE_AUTHORITY);

        if (isNotYetAllowed && isPrivilegedUser) {
            model.addAttribute("bosses", personService.getPersonsByRole(Role.BOSS));
            model.addAttribute("referredPerson", new ReferredPerson());
        }

        // APPLICATION FOR LEAVE
        model.addAttribute("application", new ApplicationForLeave(application, workDaysService));

        // WORKING TIME FOR VACATION PERIOD
        Optional<WorkingTime> optionalWorkingTime = workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(
                application.getPerson(), application.getStartDate());

        if (optionalWorkingTime.isPresent()) {
            model.addAttribute("workingTime", optionalWorkingTime.get());
        }

        // DEPARTMENT APPLICATIONS FOR LEAVE
        List<Application> departmentApplications =
            departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(application.getPerson(),
                application.getStartDate(), application.getEndDate());
        model.addAttribute("departmentApplications", departmentApplications);

        // HOLIDAY ACCOUNT
        Optional<Account> account = accountService.getHolidaysAccount(year, application.getPerson());

        if (account.isPresent()) {
            Account acc = account.get();
            model.addAttribute("vacationDaysLeft", vacationDaysService.getVacationDaysLeft(acc, accountService.getHolidaysAccount(year+1, application.getPerson())));
            model.addAttribute("account", acc);
            model.addAttribute(PersonConstants.BEFORE_APRIL_ATTRIBUTE, DateUtil.isBeforeApril(DateMidnight.now(), acc.getYear()));
        }

        // UNSPECIFIC ATTRIBUTES
        model.addAttribute(ControllerConstants.YEAR_ATTRIBUTE, year);
        model.addAttribute("action", action);
        model.addAttribute("shortcut", shortcut);
    }


    /**
     * Allow a not yet allowed application for leave (Privileged user only!).
     */
    @PreAuthorize(SecurityRules.IS_BOSS_OR_DEPARTMENT_HEAD_OR_SECOND_STAGE_AUTHORITY)
    @RequestMapping(value = "/{applicationId}/allow", method = RequestMethod.POST)
    public String allowApplication(@PathVariable("applicationId") Integer applicationId,
        @ModelAttribute("comment") ApplicationCommentForm comment,
        @RequestParam(value = "redirect", required = false) String redirectUrl, Errors errors,
        RedirectAttributes redirectAttributes) throws UnknownApplicationForLeaveException, AccessDeniedException {

        Application application = applicationService.getApplicationById(applicationId).orElseThrow(() ->
                    new UnknownApplicationForLeaveException(applicationId));

        Person signedInUser = sessionService.getSignedInUser();
        Person person = application.getPerson();

        boolean isBoss = signedInUser.hasRole(Role.BOSS);
        boolean isDepartmentHead = signedInUser.hasRole(Role.DEPARTMENT_HEAD)
            && departmentService.isDepartmentHeadOfPerson(signedInUser, person);
        boolean isSecondStageAuthority = signedInUser.hasRole(Role.SECOND_STAGE_AUTHORITY)
            && departmentService.isSecondStageAuthorityOfPerson(signedInUser, person);

        if (!isBoss && !isDepartmentHead && !isSecondStageAuthority) {
            throw new AccessDeniedException(String.format(
                    "User '%s' has not the correct permissions to allow application for leave of user '%s'",
                    signedInUser.getLoginName(), person.getLoginName()));
        }

        comment.setMandatory(false);
        commentValidator.validate(comment, errors);

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);

            return "redirect:/web/application/" + applicationId + "?action=allow";
        }

        Application allowedApplicationForLeave = applicationInteractionService.allow(application, signedInUser,
                Optional.ofNullable(comment.getText()));

        if (allowedApplicationForLeave.hasStatus(ApplicationStatus.ALLOWED)) {
            redirectAttributes.addFlashAttribute("allowSuccess", true);
        } else if (allowedApplicationForLeave.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)) {
            redirectAttributes.addFlashAttribute("temporaryAllowSuccess", true);
        }

        if (redirectUrl != null) {
            return "redirect:" + redirectUrl;
        }

        return "redirect:/web/application/" + applicationId;
    }


    /**
     * If a boss is not sure about the decision if an application should be allowed or rejected, he can ask another boss
     * to decide about this application (an email is sent).
     */
    @PreAuthorize(SecurityRules.IS_BOSS_OR_DEPARTMENT_HEAD)
    @RequestMapping(value = "/{applicationId}/refer", method = RequestMethod.POST)
    public String referApplication(@PathVariable("applicationId") Integer applicationId,
        @ModelAttribute("referredPerson") ReferredPerson referredPerson, RedirectAttributes redirectAttributes)
        throws UnknownApplicationForLeaveException, UnknownPersonException, AccessDeniedException {

        Application application = applicationService.getApplicationById(applicationId).orElseThrow(() ->
                    new UnknownApplicationForLeaveException(applicationId));

        String referLoginName = referredPerson.getLoginName();
        Person recipient = personService.getPersonByLogin(referLoginName).orElseThrow(() ->
                    new UnknownPersonException(referLoginName));

        Person sender = sessionService.getSignedInUser();

        boolean isBoss = sender.hasRole(Role.BOSS);
        boolean isDepartmentHead = departmentService.isDepartmentHeadOfPerson(sender, application.getPerson());

        if (isBoss || isDepartmentHead) {
            applicationInteractionService.refer(application, recipient, sender);

            redirectAttributes.addFlashAttribute("referSuccess", true);

            return "redirect:/web/application/" + applicationId;
        }

        throw new AccessDeniedException(String.format(
                "User '%s' has not the correct permissions to refer application for leave to user '%s'",
                sender.getLoginName(), referLoginName));
    }


    /**
     * Reject an application for leave (Boss only!).
     */
    @PreAuthorize(SecurityRules.IS_BOSS_OR_DEPARTMENT_HEAD_OR_SECOND_STAGE_AUTHORITY)
    @RequestMapping(value = "/{applicationId}/reject", method = RequestMethod.POST)
    public String rejectApplication(@PathVariable("applicationId") Integer applicationId,
        @ModelAttribute("comment") ApplicationCommentForm comment,
        @RequestParam(value = "redirect", required = false) String redirectUrl, Errors errors,
        RedirectAttributes redirectAttributes) throws UnknownApplicationForLeaveException, AccessDeniedException {

        Application application = applicationService.getApplicationById(applicationId).orElseThrow(() ->
                    new UnknownApplicationForLeaveException(applicationId));

        Person person = application.getPerson();
        Person signedInUser = sessionService.getSignedInUser();

        boolean isBoss = signedInUser.hasRole(Role.BOSS);
        boolean isDepartmentHead = departmentService.isDepartmentHeadOfPerson(signedInUser, person);
        boolean isSecondStageAuthority = departmentService.isSecondStageAuthorityOfPerson(signedInUser, person);

        if (isBoss || isDepartmentHead || isSecondStageAuthority) {
            comment.setMandatory(true);
            commentValidator.validate(comment, errors);

            if (errors.hasErrors()) {
                redirectAttributes.addFlashAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);

                if (redirectUrl != null) {
                    return "redirect:/web/application/" + applicationId + "?action=reject&shortcut=true";
                }

                return "redirect:/web/application/" + applicationId + "?action=reject";
            }

            applicationInteractionService.reject(application, signedInUser, Optional.ofNullable(comment.getText()));
            redirectAttributes.addFlashAttribute("rejectSuccess", true);

            if (redirectUrl != null) {
                return "redirect:" + redirectUrl;
            }

            return "redirect:/web/application/" + applicationId;
        }

        throw new AccessDeniedException(String.format(
                "User '%s' has not the correct permissions to reject application for leave of user '%s'",
                signedInUser.getLoginName(), person.getLoginName()));
    }


    /**
     * Cancel an application for leave. Cancelling an application for leave on behalf for someone is allowed only for
     * Office.
     */
    @RequestMapping(value = "/{applicationId}/cancel", method = RequestMethod.POST)
    public String cancelApplication(@PathVariable("applicationId") Integer applicationId,
        @ModelAttribute("comment") ApplicationCommentForm comment, Errors errors, RedirectAttributes redirectAttributes)
        throws UnknownApplicationForLeaveException, AccessDeniedException {

        Application application = applicationService.getApplicationById(applicationId).orElseThrow(() ->
                    new UnknownApplicationForLeaveException(applicationId));

        Person signedInUser = sessionService.getSignedInUser();

        boolean isWaiting = application.hasStatus(ApplicationStatus.WAITING);
        boolean isAllowed = application.hasStatus(ApplicationStatus.ALLOWED);
        boolean isTemporaryAllowed = application.hasStatus((ApplicationStatus.TEMPORARY_ALLOWED));

        // security check: only two cases where cancelling is possible
        // 1: user can cancel her own applications for leave if it has not been allowed yet
        // 2: user can request cancellation if the application is already allowed.
        // 3: office can cancel all applications for leave that has the state waiting or allowed, even for other persons
        if (signedInUser.equals(application.getPerson())) {
            // user can cancel only her own waiting applications, so the comment is NOT mandatory
            comment.setMandatory(false);
        } else if (signedInUser.hasRole(Role.OFFICE) && (isWaiting || isAllowed || isTemporaryAllowed)) {
            // office cancels application of other users, state can be waiting or allowed, so the comment is mandatory
            comment.setMandatory(true);
        } else {
            throw new AccessDeniedException(String.format(
                    "User '%s' has not the correct permissions to cancel application for leave of user '%s'",
                    signedInUser.getLoginName(), application.getPerson().getLoginName()));
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
    @RequestMapping(value = "/{applicationId}/remind", method = RequestMethod.POST)
    public String remindBoss(@PathVariable("applicationId") Integer applicationId,
        RedirectAttributes redirectAttributes) throws UnknownApplicationForLeaveException {

        Application application = applicationService.getApplicationById(applicationId).orElseThrow(() ->
                    new UnknownApplicationForLeaveException(applicationId));

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
