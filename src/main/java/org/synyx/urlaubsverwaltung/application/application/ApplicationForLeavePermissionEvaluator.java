package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCEL;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

/**
 * Single source of truth for the question "who may interact with an application for leave?".
 *
 * <p>Two tiers of permissions exist:
 *
 * <ul>
 *     <li><em>deciding</em> about an application - allowing, rejecting, referring and commenting it - is reserved for
 *     the management of the person, which is {@link org.synyx.urlaubsverwaltung.person.Role#BOSS} or the
 *     {@link org.synyx.urlaubsverwaltung.person.Role#DEPARTMENT_HEAD} /
 *     {@link org.synyx.urlaubsverwaltung.person.Role#SECOND_STAGE_AUTHORITY} of that person. Deciding needs no further
 *     role, it is what these roles are for.</li>
 *     <li><em>administering</em> an application of somebody else - adding, editing, cancelling it and declining a
 *     cancellation request - additionally requires the matching {@code APPLICATION_*} role.
 *     {@link org.synyx.urlaubsverwaltung.person.Role#OFFICE} may administer the applications of everyone.</li>
 * </ul>
 *
 * <p>Everyone may apply for leave for themselves, edit their own application as long as nobody decided about it and ask
 * for the cancellation of an application that was allowed already.
 */
@Component
public class ApplicationForLeavePermissionEvaluator {

    private final DepartmentService departmentService;

    public ApplicationForLeavePermissionEvaluator(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * Whether the given user may apply for leave for the given person. Everyone may apply for themselves, applying for
     * somebody else is administering and therefore requires
     * {@link org.synyx.urlaubsverwaltung.person.Role#APPLICATION_ADD} on top of being responsible for that person.
     *
     * @param signedInUser user asking for permissions
     * @param person       person the application would belong to
     * @return {@code true} if the user may apply for leave for the person, {@code false} otherwise
     */
    public boolean isAllowedToApplyForPerson(Person signedInUser, Person person) {
        return signedInUser.equals(person)
            || signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(APPLICATION_ADD)
            && (signedInUser.hasRole(BOSS)
            || departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)
            || departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person)));
    }

    /**
     * Whether the user may allow a waiting application. A boss decides about every application, including their own. A
     * department head does not decide about the application of a person that is their own second stage authority,
     * because that person decides about the department head. Depending on the department, a department head may allow it
     * temporarily only, see {@link #isAllowedToAllowTemporarily(Person, Application)}.
     *
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may allow the waiting application, {@code false} otherwise
     */
    public boolean isAllowedToAllowWaiting(Person signedInUser, Application application) {
        return application.hasStatus(WAITING)
            && (signedInUser.hasRole(BOSS)
            || (isSecondStageAuthorityOfPerson(signedInUser, application) && isNotOwnApplication(signedInUser, application))
            || (isDepartmentHeadOfPerson(signedInUser, application) && isNotOwnApplication(signedInUser, application)
            && !isPersonSecondStageAuthorityOfSignedInUser(signedInUser, application)));
    }

    /**
     * Whether the user may finally allow an application that a department head allowed temporarily.
     *
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may allow the temporary allowed application, {@code false} otherwise
     */
    public boolean isAllowedToAllowTemporaryAllowed(Person signedInUser, Application application) {
        return application.hasStatus(TEMPORARY_ALLOWED)
            && (signedInUser.hasRole(BOSS)
            || (isSecondStageAuthorityOfPerson(signedInUser, application) && isNotOwnApplication(signedInUser, application)));
    }

    /**
     * Whether allowing the application results in a temporary approval only, which is the case for a department head of
     * a department with two stage approval.
     *
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may allow the application temporarily only, {@code false} otherwise
     */
    public boolean isAllowedToAllowTemporarily(Person signedInUser, Application application) {
        return application.isTwoStageApproval()
            && isAllowedToAllowWaiting(signedInUser, application)
            && !signedInUser.hasRole(BOSS)
            && !isSecondStageAuthorityOfPerson(signedInUser, application)
            && isDepartmentHeadOfPerson(signedInUser, application);
    }

    /**
     * Nobody rejects their own application, withdrawing it is revoking, see
     * {@link #isAllowedToRevoke(Person, Application)}.
     *
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may reject the application, {@code false} otherwise
     */
    public boolean isAllowedToReject(Person signedInUser, Application application) {
        return (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED))
            && isNotOwnApplication(signedInUser, application)
            && (signedInUser.hasRole(BOSS)
            || isDepartmentHeadOfPerson(signedInUser, application)
            || isSecondStageAuthorityOfPerson(signedInUser, application));
    }

    /**
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may refer the application to somebody else, {@code false} otherwise
     */
    public boolean isAllowedToRefer(Person signedInUser, Application application) {
        return (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED))
            && (signedInUser.hasRole(BOSS)
            || signedInUser.hasRole(OFFICE)
            || ((isDepartmentHeadOfPerson(signedInUser, application) || isSecondStageAuthorityOfPerson(signedInUser, application))
            && isNotOwnApplication(signedInUser, application)));
    }

    /**
     * Commenting is part of deciding about an application and therefore needs no {@code APPLICATION_*} role - whoever
     * may reject an application has to be able to say why.
     *
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may comment the application, {@code false} otherwise
     */
    public boolean isAllowedToComment(Person signedInUser, Application application) {
        return signedInUser.hasRole(OFFICE)
            || signedInUser.hasRole(BOSS)
            || isDepartmentHeadOfPerson(signedInUser, application)
            || isSecondStageAuthorityOfPerson(signedInUser, application);
    }

    /**
     * Revoking is withdrawing an application nobody decided about yet.
     *
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may revoke the application, {@code false} otherwise
     */
    public boolean isAllowedToRevoke(Person signedInUser, Application application) {
        return application.hasStatus(WAITING)
            && requiresApprovalToCancel(application)
            && (isOwnApplication(signedInUser, application) || signedInUser.hasRole(OFFICE));
    }

    /**
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may cancel the already allowed application, {@code false} otherwise
     */
    public boolean isAllowedToCancel(Person signedInUser, Application application) {
        return (application.hasStatus(ALLOWED) || application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
            && isAllowedToAdminister(signedInUser, application, APPLICATION_CANCEL);
    }

    /**
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may cancel the application without asking anybody, {@code false} otherwise
     */
    public boolean isAllowedToCancelDirectly(Person signedInUser, Application application) {
        return (application.hasStatus(WAITING) || application.hasStatus(ALLOWED) || application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
            && !requiresApprovalToCancel(application)
            && (isOwnApplication(signedInUser, application) || isAllowedToAdminister(signedInUser, application, APPLICATION_CANCEL));
    }

    /**
     * Whether the user may ask for the cancellation of an already allowed application. Only the person itself and the
     * management of that person may ask for it, and only if they may not cancel it right away.
     *
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may request the cancellation of the application, {@code false} otherwise
     */
    public boolean isAllowedToStartCancellationRequest(Person signedInUser, Application application) {
        return (application.hasStatus(ALLOWED) || application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
            && requiresApprovalToCancel(application)
            && (isOwnApplication(signedInUser, application)
            || signedInUser.hasRole(BOSS)
            || isDepartmentHeadOfPerson(signedInUser, application)
            || isSecondStageAuthorityOfPerson(signedInUser, application))
            && !isAllowedToCancel(signedInUser, application);
    }

    /**
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may decline a cancellation request of the application, {@code false} otherwise
     */
    public boolean isAllowedToDeclineCancellationRequest(Person signedInUser, Application application) {
        return application.hasStatus(ALLOWED_CANCELLATION_REQUESTED)
            && isAllowedToAdminister(signedInUser, application, APPLICATION_CANCELLATION_REQUESTED);
    }

    /**
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may edit the application, {@code false} otherwise
     */
    public boolean isAllowedToEdit(Person signedInUser, Application application) {
        return (application.hasStatus(WAITING) && isOwnApplication(signedInUser, application))
            || signedInUser.hasRole(OFFICE)
            || ((isDepartmentHeadOfPerson(signedInUser, application) || isSecondStageAuthorityOfPerson(signedInUser, application))
            && signedInUser.hasRole(APPLICATION_EDIT));
    }

    /**
     * Whether the user may remind the management about the application, which only the waiting person itself may do.
     *
     * @param signedInUser user asking for permissions
     * @param application  application the permission is asked for
     * @return {@code true} if the user may remind about the application, {@code false} otherwise
     */
    public boolean isAllowedToRemind(Person signedInUser, Application application) {
        return (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED))
            && isOwnApplication(signedInUser, application)
            && !(signedInUser.hasRole(BOSS)
            || isDepartmentHeadOfPerson(signedInUser, application)
            || isSecondStageAuthorityOfPerson(signedInUser, application));
    }

    private boolean isAllowedToAdminister(Person signedInUser, Application application, Role role) {
        return signedInUser.hasRole(OFFICE)
            || ((signedInUser.hasRole(BOSS)
            || isDepartmentHeadOfPerson(signedInUser, application)
            || isSecondStageAuthorityOfPerson(signedInUser, application))
            && signedInUser.hasRole(role));
    }

    private static boolean requiresApprovalToCancel(Application application) {
        return application.getVacationType().isRequiresApprovalToCancel();
    }

    private static boolean isOwnApplication(Person signedInUser, Application application) {
        return signedInUser.equals(application.getPerson());
    }

    private static boolean isNotOwnApplication(Person signedInUser, Application application) {
        return !isOwnApplication(signedInUser, application);
    }

    private boolean isDepartmentHeadOfPerson(Person signedInUser, Application application) {
        return departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, application.getPerson());
    }

    private boolean isSecondStageAuthorityOfPerson(Person signedInUser, Application application) {
        return departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, application.getPerson());
    }

    /**
     * Whether the person of the application is a second stage authority of the signed in user - that person decides
     * about the signed in user, therefore the signed in user does not decide about them.
     */
    private boolean isPersonSecondStageAuthorityOfSignedInUser(Person signedInUser, Application application) {
        return departmentService.isSecondStageAuthorityAllowedToManagePerson(application.getPerson(), signedInUser);
    }
}
