package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCEL;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

/**
 * Permissions of a user on one certain application for leave, see
 * {@link ApplicationForLeavePermissionEvaluator#of(Person, Application)}.
 *
 * <p>Besides the roles of the user the rules depend on facts that cannot be derived from a {@link Person}: whether the
 * user is a department head or a second stage authority of the person of the application and whether that person is the
 * second stage authority of the user. They are passed in as a snapshot taken by the evaluator, therefore an instance is
 * meant to live no longer than one request.
 */
public final class ApplicationForLeavePermissions {

    private final Person signedInUser;
    private final Application application;
    private final boolean departmentHeadOfPerson;
    private final boolean secondStageAuthorityOfPerson;
    private final boolean personIsSecondStageAuthorityOfSignedInUser;

    ApplicationForLeavePermissions(Person signedInUser, Application application, boolean departmentHeadOfPerson,
                                   boolean secondStageAuthorityOfPerson, boolean personIsSecondStageAuthorityOfSignedInUser) {
        this.signedInUser = signedInUser;
        this.application = application;
        this.departmentHeadOfPerson = departmentHeadOfPerson;
        this.secondStageAuthorityOfPerson = secondStageAuthorityOfPerson;
        this.personIsSecondStageAuthorityOfSignedInUser = personIsSecondStageAuthorityOfSignedInUser;
    }

    /**
     * Whether the user may allow a waiting application. A boss decides about every application, including their own. A
     * department head does not decide about the application of a person that is their own second stage authority, see
     * {@link #isNotDecidingAboutTheirOwnApprover()}. Depending on the department, a department head may allow it
     * temporarily only, see {@link #isAllowedToAllowTemporarily()}.
     *
     * @return {@code true} if the user may allow the waiting application, {@code false} otherwise
     */
    public boolean isAllowedToAllowWaiting() {
        return application.hasStatus(WAITING)
            && (signedInUser.hasRole(BOSS)
            || (secondStageAuthorityOfPerson && isNotOwnApplication())
            || (departmentHeadOfPerson && isNotOwnApplication() && isNotDecidingAboutTheirOwnApprover()));
    }

    /**
     * Whether the user may finally allow an application that a department head allowed temporarily.
     *
     * @return {@code true} if the user may allow the temporary allowed application, {@code false} otherwise
     */
    public boolean isAllowedToAllowTemporaryAllowed() {
        return application.hasStatus(TEMPORARY_ALLOWED)
            && (signedInUser.hasRole(BOSS) || (secondStageAuthorityOfPerson && isNotOwnApplication()));
    }

    /**
     * Whether the user may allow the application at all, no matter whether it is waiting or allowed temporarily
     * already. Guards everything that leads to an allowed application, the allow form of the detail page as well as
     * the approve buttons of the application lists.
     *
     * @return {@code true} if the user may allow the application in any way, {@code false} otherwise
     */
    public boolean isAllowedToAllowInAnyWay() {
        return isAllowedToAllowWaiting() || isAllowedToAllowTemporaryAllowed();
    }

    /**
     * Whether allowing the application results in a temporary approval only, which is the case for a department head of
     * a department with two stage approval.
     *
     * @return {@code true} if the user may allow the application temporarily only, {@code false} otherwise
     */
    public boolean isAllowedToAllowTemporarily() {
        return application.isTwoStageApproval()
            && isAllowedToAllowWaiting()
            && !signedInUser.hasRole(BOSS)
            && !secondStageAuthorityOfPerson
            && departmentHeadOfPerson;
    }

    /**
     * Nobody rejects their own application, withdrawing it is revoking, see {@link #isAllowedToRevoke()}. Refusing an
     * application is deciding about it just as granting it is, so a department head does not reject the application of
     * their own second stage authority either, see {@link #isNotDecidingAboutTheirOwnApprover()}.
     *
     * @return {@code true} if the user may reject the application, {@code false} otherwise
     */
    public boolean isAllowedToReject() {
        return (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED))
            && isNotOwnApplication()
            && (signedInUser.hasRole(BOSS)
            || secondStageAuthorityOfPerson
            || (departmentHeadOfPerson && isNotDecidingAboutTheirOwnApprover()));
    }

    /**
     * @return {@code true} if the user may refer the application to somebody else, {@code false} otherwise
     */
    public boolean isAllowedToRefer() {
        return (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED))
            && (signedInUser.hasRole(BOSS)
            || signedInUser.hasRole(OFFICE)
            || (isManagerOfPerson() && isNotOwnApplication()));
    }

    /**
     * Commenting is part of deciding about an application and therefore needs no {@code APPLICATION_*} role - whoever
     * may reject an application has to be able to say why.
     *
     * @return {@code true} if the user may comment the application, {@code false} otherwise
     */
    public boolean isAllowedToComment() {
        return signedInUser.hasRole(OFFICE)
            || signedInUser.hasRole(BOSS)
            || isManagerOfPerson();
    }

    /**
     * Revoking is withdrawing an application nobody decided about yet.
     *
     * @return {@code true} if the user may revoke the application, {@code false} otherwise
     */
    public boolean isAllowedToRevoke() {
        return application.hasStatus(WAITING)
            && requiresApprovalToCancel()
            && (isOwnApplication() || signedInUser.hasRole(OFFICE));
    }

    /**
     * @return {@code true} if the user may cancel the already allowed application, {@code false} otherwise
     */
    public boolean isAllowedToCancel() {
        return (application.hasStatus(ALLOWED) || application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
            && isAllowedToAdminister(APPLICATION_CANCEL);
    }

    /**
     * @return {@code true} if the user may cancel the application without asking anybody, {@code false} otherwise
     */
    public boolean isAllowedToCancelDirectly() {
        return (application.hasStatus(WAITING) || application.hasStatus(ALLOWED) || application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
            && !requiresApprovalToCancel()
            && (isOwnApplication() || isAllowedToAdminister(APPLICATION_CANCEL));
    }

    /**
     * Whether the user may end the application right away, no matter how - by revoking it, by cancelling an allowed
     * one or by cancelling it without asking anybody. The counterpart is
     * {@link #isAllowedToStartCancellationRequest()}, which the user has to fall back to when this one does not hold.
     *
     * @return {@code true} if the user may cancel or revoke the application without asking, {@code false} otherwise
     */
    public boolean isAllowedToCancelRightAway() {
        return isAllowedToRevoke() || isAllowedToCancel() || isAllowedToCancelDirectly();
    }

    /**
     * Whether the user may ask for the cancellation of an already allowed application. Only the person itself and the
     * management of that person may ask for it, and only if they may not cancel it right away.
     *
     * @return {@code true} if the user may request the cancellation of the application, {@code false} otherwise
     */
    public boolean isAllowedToStartCancellationRequest() {
        return (application.hasStatus(ALLOWED) || application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
            && requiresApprovalToCancel()
            && (isOwnApplication() || signedInUser.hasRole(BOSS) || isManagerOfPerson())
            && !isAllowedToCancel();
    }

    /**
     * Whether a comment has to be given to cancel the application. Cancelling the application of somebody else has to
     * be explained, and so has asking for the cancellation of an own one - whoever decides about the request has to
     * know what it is about. Cancelling an own application without asking anybody needs no comment.
     *
     * @return {@code true} if a comment is mandatory to cancel the application, {@code false} otherwise
     */
    public boolean isCommentMandatoryToCancel() {
        return isAllowedToStartCancellationRequest() || isNotOwnApplication();
    }

    /**
     * Whether the user may get rid of the application at all - either right away or by asking somebody to cancel it.
     * Guards everything that leads to the cancellation of an application, the cancel form of the detail page as well
     * as the delete buttons of the application lists.
     *
     * @return {@code true} if the user may cancel the application in any way, {@code false} otherwise
     */
    public boolean isAllowedToCancelInAnyWay() {
        return isAllowedToCancelRightAway() || isAllowedToStartCancellationRequest();
    }

    /**
     * @return {@code true} if the user may decline a cancellation request of the application, {@code false} otherwise
     */
    public boolean isAllowedToDeclineCancellationRequest() {
        return application.hasStatus(ALLOWED_CANCELLATION_REQUESTED)
            && isAllowedToAdminister(APPLICATION_CANCELLATION_REQUESTED);
    }

    /**
     * Whether the user may edit the application. Nobody edits an application that is not active any more, see
     * {@link #isStillActive()} - a revoked, rejected or cancelled application cannot be reanimated, therefore changing
     * its data would have no effect. Saying something about it is still possible, see {@link #isAllowedToComment()}.
     *
     * @return {@code true} if the user may edit the application, {@code false} otherwise
     */
    public boolean isAllowedToEdit() {
        return isStillActive()
            && ((application.hasStatus(WAITING) && isOwnApplication())
            || signedInUser.hasRole(OFFICE)
            || (isManagerOfPerson() && signedInUser.hasRole(APPLICATION_EDIT)));
    }

    /**
     * Whether the user may remind the management about the application, which only the waiting person itself may do.
     *
     * @return {@code true} if the user may remind about the application, {@code false} otherwise
     */
    public boolean isAllowedToRemind() {
        return (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED))
            && isOwnApplication()
            && !(signedInUser.hasRole(BOSS) || isManagerOfPerson());
    }

    private boolean isAllowedToAdminister(Role role) {
        return signedInUser.hasRole(OFFICE)
            || ((signedInUser.hasRole(BOSS) || isManagerOfPerson()) && signedInUser.hasRole(role));
    }

    /**
     * Whether the person of the application is <em>not</em> the second stage authority that decides about the
     * applications of the signed in user. A department head neither grants nor refuses the leave of their own
     * approver.
     *
     * <p>The rule stops at deciding. A department head may still refer the application to somebody who can decide
     * about it and say why, see {@link #isAllowedToRefer()} and {@link #isAllowedToComment()} - taking those away
     * would leave them without a way to hand the application on.
     */
    private boolean isNotDecidingAboutTheirOwnApprover() {
        return !personIsSecondStageAuthorityOfSignedInUser;
    }

    /**
     * Whether the application still takes effect, which is the case for the
     * {@link ApplicationStatus#activeStatuses() active statuses} only.
     */
    private boolean isStillActive() {
        return ApplicationStatus.activeStatuses().stream().anyMatch(application::hasStatus);
    }

    private boolean isManagerOfPerson() {
        return departmentHeadOfPerson || secondStageAuthorityOfPerson;
    }

    private boolean requiresApprovalToCancel() {
        return application.getVacationType().isRequiresApprovalToCancel();
    }

    private boolean isOwnApplication() {
        return signedInUser.equals(application.getPerson());
    }

    private boolean isNotOwnApplication() {
        return !isOwnApplication();
    }
}
