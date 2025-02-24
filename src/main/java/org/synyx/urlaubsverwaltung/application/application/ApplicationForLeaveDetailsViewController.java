package org.synyx.urlaubsverwaltung.application.application;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.HolidayAccountVacationDays;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentForm;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentService;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentValidator;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.ResponsiblePersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNullElse;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToAllowTemporaryAllowedApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToAllowWaitingApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToCancelApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToCancelDirectlyApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToDeclineCancellationRequest;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToEditApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToReferApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToRejectApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToRemindApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToRevokeApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToStartCancellationRequest;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_DEPARTMENT_HEAD_OR_SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_PRIVILEGED_USER;

/**
 * Controller to manage applications for leave.
 */
@RequestMapping("/web/application")
@Controller
class ApplicationForLeaveDetailsViewController implements HasLaunchpad {

    private static final String REDIRECT_WEB_APPLICATION = "redirect:/web/application/";
    private static final String ATTRIBUTE_ERRORS = "errors";

    private final PersonService personService;
    private final ResponsiblePersonService responsiblePersonService;
    private final AccountService accountService;
    private final ApplicationService applicationService;
    private final ApplicationInteractionService applicationInteractionService;
    private final VacationDaysService vacationDaysService;
    private final ApplicationCommentService commentService;
    private final WorkDaysCountService workDaysCountService;
    private final ApplicationCommentValidator commentValidator;
    private final DepartmentService departmentService;
    private final WorkingTimeService workingTimeService;
    private final Clock clock;

    @Autowired
    ApplicationForLeaveDetailsViewController(
        VacationDaysService vacationDaysService, PersonService personService,
        ResponsiblePersonService responsiblePersonService,
        AccountService accountService, ApplicationService applicationService,
        ApplicationInteractionService applicationInteractionService,
        ApplicationCommentService commentService, WorkDaysCountService workDaysCountService,
        ApplicationCommentValidator commentValidator,
        DepartmentService departmentService, WorkingTimeService workingTimeService, Clock clock
    ) {
        this.vacationDaysService = vacationDaysService;
        this.personService = personService;
        this.responsiblePersonService = responsiblePersonService;
        this.accountService = accountService;
        this.applicationService = applicationService;
        this.applicationInteractionService = applicationInteractionService;
        this.commentService = commentService;
        this.workDaysCountService = workDaysCountService;
        this.commentValidator = commentValidator;
        this.departmentService = departmentService;
        this.workingTimeService = workingTimeService;
        this.clock = clock;
    }

    @GetMapping("/{applicationId}")
    public String showApplicationDetail(@PathVariable("applicationId") Long applicationId,
                                        @RequestParam(value = "year", required = false) Integer requestedYear,
                                        @RequestParam(value = "action", required = false) String action,
                                        @RequestParam(value = "shortcut", required = false) boolean shortcut,
                                        Model model, Locale locale)
        throws UnknownApplicationForLeaveException {

        final Application application = applicationService.getApplicationById(applicationId)
            .orElseThrow(() -> new UnknownApplicationForLeaveException(applicationId));

        final Person signedInUser = personService.getSignedInUser();
        final Person person = application.getPerson();

        if (!departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(format("User '%s' has not the correct permissions to see application for " +
                "leave of user '%s'", signedInUser.getId(), person.getId()));
        }

        final int year = requestedYear == null ? application.getEndDate().getYear() : requestedYear;
        prepareDetailView(application, year, action, shortcut, model, locale, signedInUser);

        return "application/application-detail";
    }

    /*
     * Allow a not yet allowed application for leave (Privileged user only!).
     */
    @PreAuthorize(IS_BOSS_OR_DEPARTMENT_HEAD_OR_SECOND_STAGE_AUTHORITY)
    @PostMapping("/{applicationId}/allow")
    public String allowApplication(@PathVariable("applicationId") Long applicationId,
                                   @ModelAttribute("comment") ApplicationCommentForm comment, Errors errors,
                                   @RequestParam(value = "redirect", required = false) String redirectUrl,
                                   RedirectAttributes redirectAttributes) throws UnknownApplicationForLeaveException {

        final Application application = applicationService.getApplicationById(applicationId)
            .orElseThrow(() -> new UnknownApplicationForLeaveException(applicationId));

        final Person signedInUser = personService.getSignedInUser();
        final Person person = application.getPerson();

        final boolean isDepartmentHead = departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person);
        final boolean isSecondStageAuthority = departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person);
        final boolean allowedToAllowWaitingApplication = isAllowedToAllowWaitingApplication(application, signedInUser, isDepartmentHead, isSecondStageAuthority);
        final boolean allowedToAllowTemporaryAllowedApplication = isAllowedToAllowTemporaryAllowedApplication(application, signedInUser, isSecondStageAuthority);
        if (!(allowedToAllowWaitingApplication || allowedToAllowTemporaryAllowedApplication)) {
            throw new AccessDeniedException(format("User '%s' has not the correct permissions to allow application for leave of user '%s'",
                signedInUser.getId(), person.getId()));
        }

        comment.setMandatory(false);
        commentValidator.validate(comment, errors);

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute(ATTRIBUTE_ERRORS, errors);
            return REDIRECT_WEB_APPLICATION + applicationId + "?action=allow";
        }

        final Application allowedApplicationForLeave;
        try {
            allowedApplicationForLeave = applicationInteractionService.allow(application, signedInUser, Optional.ofNullable(comment.getText()));
        } catch (NotPrivilegedToApproveException e) {
            redirectAttributes.addFlashAttribute("notPrivilegedToApprove", true);
            return REDIRECT_WEB_APPLICATION + applicationId;
        }

        if (allowedApplicationForLeave.hasStatus(ALLOWED)) {
            redirectAttributes.addFlashAttribute("allowSuccess", true);
        } else if (allowedApplicationForLeave.hasStatus(TEMPORARY_ALLOWED)) {
            redirectAttributes.addFlashAttribute("temporaryAllowSuccess", true);
        }

        if (redirectUrl != null && redirectUrl.equals("/web/application")) {
            return "redirect:" + redirectUrl;
        }

        return REDIRECT_WEB_APPLICATION + applicationId;
    }

    /*
     * If a boss is not sure about the decision if an application should be allowed or rejected,
     * he can ask another boss to decide about this application (an email is sent).
     */
    @PreAuthorize(IS_PRIVILEGED_USER)
    @PostMapping("/{applicationId}/refer")
    public String referApplication(@PathVariable("applicationId") Long applicationId,
                                   @ModelAttribute("referredPerson") ReferredPerson referredPerson, RedirectAttributes redirectAttributes)
        throws UnknownApplicationForLeaveException, UnknownPersonException {

        final Application application = applicationService.getApplicationById(applicationId)
            .orElseThrow(() -> new UnknownApplicationForLeaveException(applicationId));

        final String referUsername = referredPerson.getUsername();
        final Person personToRefer = personService.getPersonByUsername(referUsername)
            .orElseThrow(() -> new UnknownPersonException(referUsername));

        final Person personOfApplication = application.getPerson();
        final Person signedInUser = personService.getSignedInUser();

        final boolean isDepartmentHead = departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, personOfApplication);
        final boolean isSecondStageAuthority = departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, personOfApplication);
        final boolean allowedToReferApplication = isAllowedToReferApplication(application, signedInUser, isDepartmentHead, isSecondStageAuthority);
        final List<Person> responsibleManagersOf = getPossibleManagersToRefer(personOfApplication, signedInUser, application);
        if (!allowedToReferApplication || !responsibleManagersOf.contains(personToRefer)) {
            throw new AccessDeniedException(format("User '%s' has not the correct permissions to refer application for " +
                "leave to user '%s'", signedInUser.getId(), referUsername));
        }

        applicationInteractionService.refer(application, personToRefer, signedInUser);
        redirectAttributes.addFlashAttribute("referSuccess", true);
        return REDIRECT_WEB_APPLICATION + applicationId;
    }

    @PreAuthorize(IS_BOSS_OR_DEPARTMENT_HEAD_OR_SECOND_STAGE_AUTHORITY)
    @PostMapping("/{applicationId}/reject")
    public String rejectApplication(@PathVariable("applicationId") Long applicationId,
                                    @ModelAttribute("comment") ApplicationCommentForm comment, Errors errors,
                                    @RequestParam(value = "redirect", required = false) String redirectUrl,
                                    RedirectAttributes redirectAttributes) throws UnknownApplicationForLeaveException {

        final Application application = applicationService.getApplicationById(applicationId)
            .orElseThrow(() -> new UnknownApplicationForLeaveException(applicationId));

        final Person person = application.getPerson();
        final Person signedInUser = personService.getSignedInUser();

        final boolean isDepartmentHead = departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person);
        final boolean isSecondStageAuthority = departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person);
        final boolean allowedToRejectApplication = isAllowedToRejectApplication(application, signedInUser, isDepartmentHead, isSecondStageAuthority);
        if (!allowedToRejectApplication) {
            throw new AccessDeniedException(format("User '%s' has not the correct permissions to reject application for " +
                "leave of user '%s'", signedInUser.getId(), person.getId()));
        }

        comment.setMandatory(true);
        commentValidator.validate(comment, errors);

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute(ATTRIBUTE_ERRORS, errors);

            if (redirectUrl != null) {
                return REDIRECT_WEB_APPLICATION + applicationId + "?action=reject&shortcut=true";
            }

            return REDIRECT_WEB_APPLICATION + applicationId + "?action=reject";
        }

        applicationInteractionService.reject(application, signedInUser, Optional.ofNullable(comment.getText()));
        redirectAttributes.addFlashAttribute("rejectSuccess", true);

        if (redirectUrl != null && redirectUrl.equals("/web/application")) {
            return "redirect:" + redirectUrl;
        }

        return REDIRECT_WEB_APPLICATION + applicationId;
    }

    /*
     * Cancel an application for leave.
     *
     * Cancelling an application for leave on behalf for someone is allowed only for Office.
     */
    @PostMapping("/{applicationId}/cancel")
    public String cancelApplication(@PathVariable("applicationId") Long applicationId,
                                    @ModelAttribute("comment") ApplicationCommentForm comment, Errors errors,
                                    RedirectAttributes redirectAttributes)
        throws UnknownApplicationForLeaveException {

        final Application application = applicationService.getApplicationById(applicationId)
            .orElseThrow(() -> new UnknownApplicationForLeaveException(applicationId));

        final Person person = application.getPerson();
        final Person signedInUser = personService.getSignedInUser();

        final boolean isDepartmentHead = departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person);
        final boolean isSecondStageAuthority = departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person);

        final boolean requiresApprovalToCancel = application.getVacationType().isRequiresApprovalToCancel();

        final boolean allowedToRevokeApplication = isAllowedToRevokeApplication(application, signedInUser, requiresApprovalToCancel);
        final boolean allowedToCancelApplication = isAllowedToCancelApplication(application, signedInUser, isDepartmentHead, isSecondStageAuthority);
        final boolean allowedToCancelDirectlyApplication = isAllowedToCancelDirectlyApplication(application, signedInUser, isDepartmentHead, isSecondStageAuthority, requiresApprovalToCancel);
        final boolean allowedToStartCancellationRequest = isAllowedToStartCancellationRequest(application, signedInUser, isDepartmentHead, isSecondStageAuthority, requiresApprovalToCancel);
        if (!(allowedToRevokeApplication || allowedToCancelApplication || allowedToCancelDirectlyApplication || allowedToStartCancellationRequest)) {
            throw new AccessDeniedException(format("User '%s' has not the correct permissions to cancel or revoke application " +
                "for leave of user '%s'", signedInUser.getId(), application.getPerson().getId()));
        }

        // comment is mandatory if cancel for another user or cancellation request of own
        final boolean isCommentMandatory = allowedToStartCancellationRequest || !signedInUser.equals(application.getPerson());
        comment.setMandatory(isCommentMandatory);

        commentValidator.validate(comment, errors);
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute(ATTRIBUTE_ERRORS, errors);
            return REDIRECT_WEB_APPLICATION + applicationId + "?action=cancel";
        }

        if (requiresApprovalToCancel) {
            applicationInteractionService.cancel(application, signedInUser, Optional.ofNullable(comment.getText()));
        } else {
            applicationInteractionService.directCancel(application, signedInUser, Optional.ofNullable(comment.getText()));
        }
        return REDIRECT_WEB_APPLICATION + applicationId;
    }

    /*
     * Cancel the cancellation request of an application for leave.
     */
    @PostMapping("/{applicationId}/decline-cancellation-request")
    public String declineCancellationRequestApplication(@PathVariable("applicationId") Long applicationId,
                                                        @ModelAttribute("comment") ApplicationCommentForm comment, Errors errors,
                                                        RedirectAttributes redirectAttributes)
        throws UnknownApplicationForLeaveException {

        final Application application = applicationService.getApplicationById(applicationId)
            .orElseThrow(() -> new UnknownApplicationForLeaveException(applicationId));

        final Person person = application.getPerson();
        final Person signedInUser = personService.getSignedInUser();

        final boolean isDepartmentHead = departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person);
        final boolean isSecondStageAuthority = departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person);
        final boolean allowedToDeclineCancellationRequest = isAllowedToDeclineCancellationRequest(application, signedInUser, isDepartmentHead, isSecondStageAuthority);
        if (!allowedToDeclineCancellationRequest) {
            throw new AccessDeniedException(format("User '%s' has not the correct permissions to cancel a cancellation request of " +
                "application for leave of user '%s'", signedInUser.getId(), application.getPerson().getId()));
        }

        comment.setMandatory(true);

        commentValidator.validate(comment, errors);
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute(ATTRIBUTE_ERRORS, errors);
            return REDIRECT_WEB_APPLICATION + applicationId + "?action=decline-cancellation-request";
        }

        applicationInteractionService.declineCancellationRequest(application, signedInUser, Optional.ofNullable(comment.getText()));
        return REDIRECT_WEB_APPLICATION + applicationId;
    }

    /*
     * Remind the bosses about the decision of an application for leave.
     */
    @PostMapping("/{applicationId}/remind")
    public String remindBoss(@PathVariable("applicationId") Long applicationId,
                             RedirectAttributes redirectAttributes) throws UnknownApplicationForLeaveException {

        final Application application = applicationService.getApplicationById(applicationId)
            .orElseThrow(() -> new UnknownApplicationForLeaveException(applicationId));

        final Person person = application.getPerson();
        final Person signedInUser = personService.getSignedInUser();

        final boolean isDepartmentHead = departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person);
        final boolean isSecondStageAuthority = departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person);
        final boolean allowedToRemindApplication = isAllowedToRemindApplication(application, signedInUser, isDepartmentHead, isSecondStageAuthority);
        if (!allowedToRemindApplication) {
            throw new AccessDeniedException(format("User '%s' has not the correct permissions to remind application for " +
                "leave of user '%s'", signedInUser.getId(), person.getId()));
        }

        try {
            applicationInteractionService.remind(application);
            redirectAttributes.addFlashAttribute("remindIsSent", true);
        } catch (RemindAlreadySentException ex) {
            redirectAttributes.addFlashAttribute("remindAlreadySent", true);
        } catch (ImpatientAboutApplicationForLeaveProcessException ex) {
            redirectAttributes.addFlashAttribute("remindNoWay", true);
        }

        return REDIRECT_WEB_APPLICATION + applicationId;
    }

    private void prepareDetailView(Application application, int year, String action, boolean shortcut, Model model, Locale locale, Person signedInUser) {

        // signed in user
        model.addAttribute("signedInUser", signedInUser);

        // person information with departments
        model.addAttribute("departmentsOfPerson", departmentService.getAssignedDepartmentsOfMember(application.getPerson()));

        // COMMENTS
        final List<ApplicationComment> comments = commentService.getCommentsByApplication(application);
        model.addAttribute("comment", new ApplicationCommentForm());
        model.addAttribute("comments", comments);
        model.addAttribute("lastComment", comments.getLast());

        // APPLICATION FOR LEAVE
        final ApplicationForLeave applicationForLeave = new ApplicationForLeave(application, workDaysCountService);
        model.addAttribute("app", applicationForLeaveDetailDto(applicationForLeave, locale));

        final Map<DateRange, WorkingTime> workingTime = workingTimeService.getWorkingTimesByPersonAndDateRange(
                application.getPerson(), new DateRange(application.getStartDate(), application.getEndDate())).entrySet().stream()
            .sorted(Map.Entry.comparingByKey(comparing(DateRange::startDate)))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new));
        model.addAttribute("dateRangeWorkingTimes", workingTime);

        // DEPARTMENT APPLICATIONS FOR LEAVE
        final List<Application> departmentApplications = departmentService.getApplicationsFromColleaguesOf(application.getPerson(), application.getStartDate(), application.getEndDate());
        model.addAttribute("departmentApplications", departmentApplications);

        // HOLIDAY ACCOUNT
        final Optional<Account> maybeAccount = accountService.getHolidaysAccount(year, application.getPerson());
        if (maybeAccount.isPresent()) {
            final Account account = maybeAccount.get();

            final List<Account> accountNextYear = accountService.getHolidaysAccount(year + 1, application.getPerson()).stream().toList();
            final Map<Account, HolidayAccountVacationDays> accountHolidayAccountVacationDaysMap = vacationDaysService.getVacationDaysLeft(List.of(account), Year.of(year), accountNextYear);
            final VacationDaysLeft vacationDaysLeft = accountHolidayAccountVacationDaysMap.get(account).vacationDaysYear();
            model.addAttribute("vacationDaysLeft", vacationDaysLeft);

            final LocalDate now = LocalDate.now(clock);
            final LocalDate expiryDate = account.getExpiryDate();
            final BigDecimal expiredRemainingVacationDays = vacationDaysLeft.getExpiredRemainingVacationDays(now, expiryDate);
            model.addAttribute("expiredRemainingVacationDays", expiredRemainingVacationDays);
            model.addAttribute("doRemainingVacationDaysExpire", account.doRemainingVacationDaysExpire());
            model.addAttribute("expiryDate", expiryDate);

            model.addAttribute("account", account);
            model.addAttribute("isBeforeExpiryDate", now.isBefore(expiryDate));
        }

        // Signed in person is allowed to manage
        final boolean isDepartmentHeadOfPerson = departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, application.getPerson());
        final boolean isSecondStageAuthorityOfPerson = departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, application.getPerson());
        final boolean requiresApprovalToCancel = application.getVacationType().isRequiresApprovalToCancel();

        model.addAttribute("isAllowedToAllowWaitingApplication", isAllowedToAllowWaitingApplication(application, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson));
        model.addAttribute("isAllowedToAllowTemporaryAllowedApplication", isAllowedToAllowTemporaryAllowedApplication(application, signedInUser, isSecondStageAuthorityOfPerson));

        model.addAttribute("isAllowedToRejectApplication", isAllowedToRejectApplication(application, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson));

        model.addAttribute("isAllowedToRevokeApplication", isAllowedToRevokeApplication(application, signedInUser, requiresApprovalToCancel));
        model.addAttribute("isAllowedToCancelApplication", isAllowedToCancelApplication(application, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson));
        model.addAttribute("isAllowedToCancelDirectlyApplication", isAllowedToCancelDirectlyApplication(application, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson, requiresApprovalToCancel));
        model.addAttribute("isAllowedToStartCancellationRequest", isAllowedToStartCancellationRequest(application, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson, requiresApprovalToCancel));

        model.addAttribute("isAllowedToDeclineCancellationRequest", isAllowedToDeclineCancellationRequest(application, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson));

        model.addAttribute("isAllowedToEditApplication", isAllowedToEditApplication(application, signedInUser));
        model.addAttribute("isAllowedToRemindApplication", isAllowedToRemindApplication(application, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson));

        final boolean allowedToReferApplication = isAllowedToReferApplication(application, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson);
        model.addAttribute("isAllowedToReferApplication", allowedToReferApplication);
        if (allowedToReferApplication) {
            model.addAttribute("availablePersonsToRefer", getPossibleManagersToRefer(application.getPerson(), signedInUser, application));
            model.addAttribute("referredPerson", new ReferredPerson());
        }

        model.addAttribute("isDepartmentHeadOfPerson", isDepartmentHeadOfPerson);
        model.addAttribute("isSecondStageAuthorityOfPerson", isSecondStageAuthorityOfPerson);
        model.addAttribute("isBoss", signedInUser.hasRole(BOSS));
        model.addAttribute("isOffice", signedInUser.hasRole(OFFICE));

        // UNSPECIFIC ATTRIBUTES
        model.addAttribute("selectedYear", year);
        model.addAttribute("currentYear", Year.now(clock).getValue());
        model.addAttribute("action", requireNonNullElse(action, ""));
        model.addAttribute("shortcut", shortcut);
    }

    private List<Person> getPossibleManagersToRefer(Person personOfInterest, Person signedInUser, Application application) {
        return responsiblePersonService.getResponsibleManagersOf(personOfInterest)
            .stream()
            .filter(not(isEqual(signedInUser)))
            .filter(person -> person.hasRole(BOSS) || person.hasRole(SECOND_STAGE_AUTHORITY) || (person.hasRole(DEPARTMENT_HEAD) && application.hasStatus(WAITING)))
            .toList();
    }

    private ApplicationForLeaveDetailDto applicationForLeaveDetailDto(ApplicationForLeave applicationForLeave, Locale locale) {
        final ApplicationForLeaveDetailDto dto = new ApplicationForLeaveDetailDto();
        dto.setId(applicationForLeave.getId());
        dto.setPerson(applicationForLeave.getPerson());
        dto.setStatus(applicationForLeave.getStatus());
        dto.setVacationType(applicationForLeaveDetailVacationTypeDto(applicationForLeave.getVacationType(), locale));
        dto.setApplicationDate(applicationForLeave.getApplicationDate());
        dto.setStartDate(applicationForLeave.getStartDate());
        dto.setEndDate(applicationForLeave.getEndDate());
        dto.setStartTime(applicationForLeave.getStartTime());
        dto.setEndTime(applicationForLeave.getEndTime());
        dto.setWeekDayOfStartDate(applicationForLeave.getWeekDayOfStartDate());
        dto.setWeekDayOfEndDate(applicationForLeave.getWeekDayOfEndDate());
        dto.setDayLength(applicationForLeave.getDayLength());
        dto.setWorkDays(applicationForLeave.getWorkDays());
        dto.setHours(applicationForLeave.getHours());
        dto.setEditedDate(applicationForLeave.getEditedDate());
        dto.setCancelDate(applicationForLeave.getCancelDate());
        dto.setTwoStageApproval(applicationForLeave.isTwoStageApproval());
        dto.setReason(applicationForLeave.getReason());
        dto.setHolidayReplacements(applicationForLeave.getHolidayReplacements());
        dto.setTeamInformed(applicationForLeave.isTeamInformed());
        dto.setAddress(applicationForLeave.getAddress());
        return dto;
    }

    private ApplicationForLeaveDetailVacationTypeDto applicationForLeaveDetailVacationTypeDto(VacationType<?> vacationType, Locale locale) {
        return new ApplicationForLeaveDetailVacationTypeDto(
            vacationType.getLabel(locale),
            vacationType.getCategory(),
            vacationType.getColor(),
            vacationType.isRequiresApprovalToCancel()
        );
    }
}
