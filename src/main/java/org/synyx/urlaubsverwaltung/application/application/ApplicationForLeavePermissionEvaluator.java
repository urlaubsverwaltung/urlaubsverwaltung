package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.person.Person;

import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCEL;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

class ApplicationForLeavePermissionEvaluator {

    private ApplicationForLeavePermissionEvaluator() {
        // ok
    }

    static boolean isAllowedToAllowWaitingApplication(Application application, Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return application.hasStatus(WAITING)
            && (signedInUser.hasRole(BOSS) || ((isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson) && !application.getPerson().equals(signedInUser)));
    }

    static boolean isAllowedToAllowTemporaryAllowedApplication(Application application, Person signedInUser, boolean isSecondStageAuthorityOfPerson) {
        return application.hasStatus(TEMPORARY_ALLOWED)
            && (signedInUser.hasRole(BOSS) || (isSecondStageAuthorityOfPerson && !application.getPerson().equals(signedInUser)));
    }

    static boolean isAllowedToRejectApplication(Application application, Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED))
            && !application.getPerson().equals(signedInUser)
            && (signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson);
    }

    static boolean isAllowedToRevokeApplication(Application application, Person signedInUser, boolean requiresApprovalToCancel) {
        return application.hasStatus(WAITING)
            && requiresApprovalToCancel
            && (application.getPerson().equals(signedInUser) || signedInUser.hasRole(OFFICE));
    }

    static boolean isAllowedToCancelApplication(Application application, Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return (application.hasStatus(ALLOWED) || application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
            && (signedInUser.hasRole(OFFICE) || ((signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson) && signedInUser.hasRole(APPLICATION_CANCEL)));
    }

    static boolean isAllowedToCancelDirectlyApplication(Application application, Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson, boolean requiresApprovalToCancel) {
        return (application.hasStatus(WAITING) || application.hasStatus(ALLOWED) || application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
            && !requiresApprovalToCancel
            && (application.getPerson().equals(signedInUser) || signedInUser.hasRole(OFFICE) || ((signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson) && signedInUser.hasRole(APPLICATION_CANCEL)));
    }

    static boolean isAllowedToStartCancellationRequest(Application application, Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson, boolean requiresApprovalToCancel) {
        return (application.hasStatus(ALLOWED) || application.hasStatus(TEMPORARY_ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
            && requiresApprovalToCancel
            && !(signedInUser.hasRole(OFFICE) || ((signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson) && signedInUser.hasRole(APPLICATION_CANCEL)));
    }

    static boolean isAllowedToDeclineCancellationRequest(Application application, Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return application.hasStatus(ALLOWED_CANCELLATION_REQUESTED)
            && (signedInUser.hasRole(OFFICE) || ((signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson) && signedInUser.hasRole(APPLICATION_CANCELLATION_REQUESTED)));
    }

    static boolean isAllowedToEditApplication(Application application, Person signedInUser) {
        return (application.hasStatus(WAITING) && application.getPerson().equals(signedInUser)) || signedInUser.hasRole(OFFICE);
    }

    static boolean isAllowedToRemindApplication(Application application, Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED))
            && (application.getPerson().equals(signedInUser) && !(signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson));
    }

    static boolean isAllowedToReferApplication(Application application, Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED))
            && (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE) || ((isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson) && !application.getPerson().equals(signedInUser)));
    }
}
