package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCEL;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Truth table of "who may interact with an application for leave?".
 *
 * <p>Deciding about an application - allowing, rejecting, referring, commenting - is reserved for the management of the
 * person and needs no {@code APPLICATION_*} role. Administering the application of somebody else - adding, editing,
 * cancelling, declining a cancellation request - additionally requires the matching {@code APPLICATION_*} role.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationForLeavePermissionEvaluatorTest {

    private static final long SIGNED_IN_USER_ID = 1L;
    private static final long OTHER_PERSON_ID = 2L;

    private ApplicationForLeavePermissionEvaluator sut;

    @Mock
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeavePermissionEvaluator(departmentService);
    }

    @Nested
    class Allow {

        @Test
        void ensureBossMayAllowEveryWaitingApplication() {
            assertThat(permissionsOnUnmanagedPerson(WAITING, BOSS).isAllowedToAllowWaiting()).isTrue();
        }

        @Test
        void ensureBossMayAllowOwnWaitingApplication() {
            final Person boss = person(SIGNED_IN_USER_ID, BOSS);
            assertThat(sut.of(boss, application(boss, WAITING)).isAllowedToAllowWaiting()).isTrue();
        }

        @Test
        void ensureDepartmentHeadMayAllowWaitingApplicationOfManagedMember() {
            assertThat(permissionsOnManagedPerson(WAITING, DEPARTMENT_HEAD).isAllowedToAllowWaiting()).isTrue();
        }

        @Test
        void ensureSecondStageAuthorityMayAllowWaitingApplicationOfManagedMember() {
            assertThat(permissionsOnManagedPerson(WAITING, SECOND_STAGE_AUTHORITY).isAllowedToAllowWaiting()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerMayNotAllowOwnWaitingApplication(Role role) {
            final Person manager = person(SIGNED_IN_USER_ID, role);
            assertThat(sut.of(manager, application(manager, WAITING)).isAllowedToAllowWaiting()).isFalse();
        }

        @Test
        void ensureDepartmentHeadMayNotAllowApplicationOfOwnSecondStageAuthority() {

            final Person departmentHead = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD);
            final Person secondStageAuthority = person(OTHER_PERSON_ID, SECOND_STAGE_AUTHORITY);

            when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, secondStageAuthority)).thenReturn(true);
            when(departmentService.isSecondStageAuthorityAllowedToManagePerson(departmentHead, secondStageAuthority)).thenReturn(false);
            when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, departmentHead)).thenReturn(true);

            assertThat(sut.of(departmentHead, application(secondStageAuthority, WAITING)).isAllowedToAllowWaiting()).isFalse();
        }

        @Test
        void ensureNobodyWithoutManagementRoleMayAllow() {
            assertThat(permissionsOnUnmanagedPerson(WAITING, APPLICATION_ADD, APPLICATION_EDIT, APPLICATION_CANCEL).isAllowedToAllowWaiting()).isFalse();
        }

        @Test
        void ensureOfficeMayNotAllow() {
            assertThat(permissionsOnUnmanagedPerson(WAITING, OFFICE).isAllowedToAllowWaiting()).isFalse();
        }

        @Test
        void ensureAllowIsOnlyPossibleForWaitingApplications() {
            assertThat(permissionsOnUnmanagedPerson(ALLOWED, BOSS).isAllowedToAllowWaiting()).isFalse();
            assertThat(permissionsOnUnmanagedPerson(REJECTED, BOSS).isAllowedToAllowWaiting()).isFalse();
        }

        @Test
        void ensureSecondStageAuthorityMayFinallyAllowTemporaryAllowedApplication() {
            assertThat(permissionsOnManagedPerson(TEMPORARY_ALLOWED, SECOND_STAGE_AUTHORITY).isAllowedToAllowTemporaryAllowed()).isTrue();
        }

        @Test
        void ensureDepartmentHeadMayNotFinallyAllowTemporaryAllowedApplication() {
            final ApplicationForLeavePermissions permissions = permissionsOnManagedPerson(TEMPORARY_ALLOWED, DEPARTMENT_HEAD);
            assertThat(permissions.isAllowedToAllowTemporaryAllowed()).isFalse();
            // being responsible for the person is not the reason, rejecting it is allowed
            assertThat(permissions.isAllowedToReject()).isTrue();
        }

        @Test
        void ensureDepartmentHeadAllowsTemporarilyOnlyWithTwoStageApproval() {

            final Person departmentHead = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD);
            final Person member = person(OTHER_PERSON_ID, USER);
            when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, member)).thenReturn(true);

            final Application twoStage = application(member, WAITING);
            twoStage.setTwoStageApproval(true);
            assertThat(sut.of(departmentHead, twoStage).isAllowedToAllowTemporarily()).isTrue();

            final Application oneStage = application(member, WAITING);
            assertThat(sut.of(departmentHead, oneStage).isAllowedToAllowTemporarily()).isFalse();
        }

        @Test
        void ensureBossNeverAllowsTemporarilyOnly() {

            final Person boss = person(SIGNED_IN_USER_ID, BOSS);
            final Application twoStage = application(person(OTHER_PERSON_ID, USER), WAITING);
            twoStage.setTwoStageApproval(true);

            assertThat(sut.of(boss, twoStage).isAllowedToAllowTemporarily()).isFalse();
        }
    }

    @Nested
    class AllowInAnyWay {

        @Test
        void ensureManagerMayAllowAWaitingApplication() {
            assertThat(permissionsOnManagedPerson(WAITING, SECOND_STAGE_AUTHORITY).isAllowedToAllowInAnyWay()).isTrue();
        }

        @Test
        void ensureManagerMayAllowATemporaryAllowedApplication() {
            assertThat(permissionsOnManagedPerson(TEMPORARY_ALLOWED, SECOND_STAGE_AUTHORITY).isAllowedToAllowInAnyWay()).isTrue();
        }

        @Test
        void ensureNobodyAllowsAnAlreadyAllowedApplication() {
            assertThat(permissionsOnManagedPerson(ALLOWED, SECOND_STAGE_AUTHORITY).isAllowedToAllowInAnyWay()).isFalse();
        }

        @Test
        void ensureUnrelatedUserMayNotAllow() {
            assertThat(permissionsOnUnmanagedPerson(WAITING, USER).isAllowedToAllowInAnyWay()).isFalse();
        }
    }

    @Nested
    class Reject {

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerMayRejectApplicationOfManagedMember(Role role) {
            assertThat(permissionsOnManagedPerson(WAITING, role).isAllowedToReject()).isTrue();
        }

        @Test
        void ensureBossMayRejectEveryApplication() {
            assertThat(permissionsOnUnmanagedPerson(WAITING, BOSS).isAllowedToReject()).isTrue();
        }

        @Test
        void ensureNobodyMayRejectOwnApplication() {
            final Person boss = person(SIGNED_IN_USER_ID, BOSS);
            assertThat(sut.of(boss, application(boss, WAITING)).isAllowedToReject()).isFalse();
        }

        @Test
        void ensureOfficeMayNotReject() {
            assertThat(permissionsOnUnmanagedPerson(WAITING, OFFICE).isAllowedToReject()).isFalse();
        }

        @Test
        void ensureRejectIsOnlyPossibleForWaitingOrTemporaryAllowedApplications() {
            assertThat(permissionsOnUnmanagedPerson(TEMPORARY_ALLOWED, BOSS).isAllowedToReject()).isTrue();
            assertThat(permissionsOnUnmanagedPerson(ALLOWED, BOSS).isAllowedToReject()).isFalse();
        }

        @Test
        void ensureDepartmentHeadMayNotRejectApplicationOfOwnSecondStageAuthority() {

            final Person departmentHead = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD);
            final Person secondStageAuthority = person(OTHER_PERSON_ID, SECOND_STAGE_AUTHORITY);

            when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, secondStageAuthority)).thenReturn(true);
            when(departmentService.isSecondStageAuthorityAllowedToManagePerson(departmentHead, secondStageAuthority)).thenReturn(false);
            when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, departmentHead)).thenReturn(true);

            final ApplicationForLeavePermissions permissions = sut.of(departmentHead, application(secondStageAuthority, WAITING));

            // refusing is deciding just as granting is
            assertThat(permissions.isAllowedToReject()).isFalse();
            assertThat(permissions.isAllowedToAllowWaiting()).isFalse();

            // but the application can still be handed on to somebody who decides about it, with a reason
            assertThat(permissions.isAllowedToRefer()).isTrue();
            assertThat(permissions.isAllowedToComment()).isTrue();
        }

        @Test
        void ensureSecondStageAuthorityOfThePersonMayRejectEvenIfThatPersonIsTheirOwnApprover() {

            final Person signedInUser = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY);
            final Person other = person(OTHER_PERSON_ID, SECOND_STAGE_AUTHORITY);

            when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, other)).thenReturn(true);
            when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, other)).thenReturn(true);
            when(departmentService.isSecondStageAuthorityAllowedToManagePerson(other, signedInUser)).thenReturn(true);

            // the rule holds back a department head, not somebody who is a second stage authority themselves
            assertThat(sut.of(signedInUser, application(other, WAITING)).isAllowedToReject()).isTrue();
        }
    }

    @Nested
    class Comment {

        @Test
        void ensureOfficeMayComment() {
            assertThat(permissionsOnUnmanagedPerson(WAITING, OFFICE).isAllowedToComment()).isTrue();
        }

        @Test
        void ensureBossMayComment() {
            assertThat(permissionsOnUnmanagedPerson(WAITING, BOSS).isAllowedToComment()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerMayCommentWithoutApplicationAddRole(Role role) {
            final ApplicationForLeavePermissions permissions = permissionsOnManagedPerson(WAITING, role);
            // whoever may reject an application has to be able to say why
            assertThat(permissions.isAllowedToComment()).isTrue();
            assertThat(permissions.isAllowedToReject()).isTrue();
        }

        @Test
        void ensureManagerOfAnotherDepartmentMayNotComment() {
            assertThat(permissionsOnUnmanagedPerson(WAITING, DEPARTMENT_HEAD, APPLICATION_ADD).isAllowedToComment()).isFalse();
        }

        @Test
        void ensureUserMayNotCommentApplicationOfSomebodyElse() {
            assertThat(permissionsOnUnmanagedPerson(WAITING, USER).isAllowedToComment()).isFalse();
        }
    }

    @Nested
    class Refer {

        @Test
        void ensureBossAndOfficeMayRefer() {
            assertThat(permissionsOnUnmanagedPerson(WAITING, BOSS).isAllowedToRefer()).isTrue();
            assertThat(permissionsOnUnmanagedPerson(WAITING, OFFICE).isAllowedToRefer()).isTrue();
        }

        @Test
        void ensureManagerMayReferApplicationOfManagedMember() {
            assertThat(permissionsOnManagedPerson(WAITING, DEPARTMENT_HEAD).isAllowedToRefer()).isTrue();
        }

        @Test
        void ensureManagerMayNotReferOwnApplication() {
            final Person departmentHead = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD);
            assertThat(sut.of(departmentHead, application(departmentHead, WAITING)).isAllowedToRefer()).isFalse();
        }
    }

    @Nested
    class Edit {

        @Test
        void ensurePersonMayEditOwnWaitingApplication() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(person, application(person, WAITING)).isAllowedToEdit()).isTrue();
        }

        @Test
        void ensurePersonMayNotEditOwnApplicationSomebodyDecidedAbout() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(person, application(person, ALLOWED)).isAllowedToEdit()).isFalse();
        }

        @Test
        void ensureOfficeMayEdit() {
            assertThat(permissionsOnUnmanagedPerson(ALLOWED, OFFICE).isAllowedToEdit()).isTrue();
        }

        @Test
        void ensureManagerNeedsApplicationEditRoleToEdit() {
            assertThat(permissionsOnManagedPerson(ALLOWED, DEPARTMENT_HEAD, APPLICATION_EDIT).isAllowedToEdit()).isTrue();
            assertThat(permissionsOnManagedPerson(ALLOWED, DEPARTMENT_HEAD).isAllowedToEdit()).isFalse();
        }

        @Test
        void ensureBossMayNotEditWithoutApplicationEditRole() {
            assertThat(permissionsOnUnmanagedPerson(ALLOWED, BOSS, APPLICATION_EDIT).isAllowedToEdit()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = ApplicationStatus.class, names = {"REVOKED", "REJECTED", "CANCELLED"})
        void ensureOfficeMayNotEditAnInactiveApplication(ApplicationStatus status) {
            assertThat(permissionsOnUnmanagedPerson(status, OFFICE).isAllowedToEdit()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = ApplicationStatus.class, names = {"REVOKED", "REJECTED", "CANCELLED"})
        void ensureManagerMayNotEditAnInactiveApplication(ApplicationStatus status) {
            assertThat(permissionsOnManagedPerson(status, DEPARTMENT_HEAD, APPLICATION_EDIT).isAllowedToEdit()).isFalse();
        }
    }

    @Nested
    class CancelAndRevoke {

        @Test
        void ensurePersonMayRevokeOwnWaitingApplication() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(person, applicationRequiringApprovalToCancel(person, WAITING)).isAllowedToRevoke()).isTrue();
        }

        @Test
        void ensureManagerNeedsApplicationCancelRoleToCancel() {
            assertThat(permissionsOnManagedPerson(ALLOWED, DEPARTMENT_HEAD, APPLICATION_CANCEL).isAllowedToCancel()).isTrue();
            assertThat(permissionsOnManagedPerson(ALLOWED, DEPARTMENT_HEAD).isAllowedToCancel()).isFalse();
        }

        @Test
        void ensureOfficeMayCancel() {
            assertThat(permissionsOnUnmanagedPerson(ALLOWED, OFFICE).isAllowedToCancel()).isTrue();
        }

        @Test
        void ensurePersonMayCancelOwnApplicationDirectlyIfNoApprovalIsRequired() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(person, application(person, ALLOWED)).isAllowedToCancelDirectly()).isTrue();
        }

        @Test
        void ensurePersonMayNotCancelOwnApplicationDirectlyIfApprovalIsRequired() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(person, applicationRequiringApprovalToCancel(person, ALLOWED)).isAllowedToCancelDirectly()).isFalse();
        }
    }

    @Nested
    class StartCancellationRequest {

        @Test
        void ensurePersonMayRequestCancellationOfOwnAllowedApplication() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(person, applicationRequiringApprovalToCancel(person, ALLOWED)).isAllowedToStartCancellationRequest()).isTrue();
        }

        @Test
        void ensureUnrelatedUserMayNotRequestCancellationOfAnotherPersonsApplication() {

            final Person signedInUser = person(SIGNED_IN_USER_ID, USER);
            final Person other = person(OTHER_PERSON_ID, USER);

            assertThat(sut.of(signedInUser, applicationRequiringApprovalToCancel(other, ALLOWED)).isAllowedToStartCancellationRequest()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerOfAnotherDepartmentMayNotRequestCancellation(Role role) {
            final Person signedInUser = person(SIGNED_IN_USER_ID, role);
            final Person other = person(OTHER_PERSON_ID, USER);

            assertThat(sut.of(signedInUser, applicationRequiringApprovalToCancel(other, ALLOWED)).isAllowedToStartCancellationRequest()).isFalse();
        }

        @Test
        void ensureManagerMayRequestCancellationForManagedMember() {

            final Person departmentHead = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD);
            final Person member = person(OTHER_PERSON_ID, USER);
            when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, member)).thenReturn(true);

            assertThat(sut.of(departmentHead, applicationRequiringApprovalToCancel(member, ALLOWED)).isAllowedToStartCancellationRequest()).isTrue();
        }

        @Test
        void ensureWhoeverMayCancelDoesNotRequestCancellation() {
            final Person office = person(SIGNED_IN_USER_ID, OFFICE);
            final Application application = applicationRequiringApprovalToCancel(person(OTHER_PERSON_ID, USER), ALLOWED);

            final ApplicationForLeavePermissions permissions = sut.of(office, application);
            assertThat(permissions.isAllowedToCancel()).isTrue();
            assertThat(permissions.isAllowedToStartCancellationRequest()).isFalse();
        }
    }

    @Nested
    class CancelInAnyWay {

        @Test
        void ensurePersonMayCancelOwnWaitingApplicationRightAway() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            final ApplicationForLeavePermissions permissions = sut.of(person, applicationRequiringApprovalToCancel(person, WAITING));
            assertThat(permissions.isAllowedToCancelRightAway()).isTrue();
            assertThat(permissions.isAllowedToCancelInAnyWay()).isTrue();
        }

        @Test
        void ensurePersonAskingForCancellationMayNotCancelRightAway() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            final ApplicationForLeavePermissions permissions = sut.of(person, applicationRequiringApprovalToCancel(person, ALLOWED));
            assertThat(permissions.isAllowedToCancelRightAway()).isFalse();
            assertThat(permissions.isAllowedToCancelInAnyWay()).isTrue();
        }

        @Test
        void ensureUnrelatedUserMayNotCancelAtAll() {
            final Person signedInUser = person(SIGNED_IN_USER_ID, USER);
            final Application application = applicationRequiringApprovalToCancel(person(OTHER_PERSON_ID, USER), ALLOWED);

            final ApplicationForLeavePermissions permissions = sut.of(signedInUser, application);
            assertThat(permissions.isAllowedToCancelRightAway()).isFalse();
            assertThat(permissions.isAllowedToCancelInAnyWay()).isFalse();
        }
    }

    @Nested
    class CommentMandatoryToCancel {

        @Test
        void ensureNoCommentIsNeededToCancelAnOwnApplicationDirectly() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(person, application(person, ALLOWED)).isCommentMandatoryToCancel()).isFalse();
        }

        @Test
        void ensureCommentIsNeededToRequestCancellationOfAnOwnApplication() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(person, applicationRequiringApprovalToCancel(person, ALLOWED)).isCommentMandatoryToCancel()).isTrue();
        }

        @Test
        void ensureCommentIsNeededToCancelTheApplicationOfSomebodyElse() {
            final Person office = person(SIGNED_IN_USER_ID, OFFICE);
            final Application application = applicationRequiringApprovalToCancel(person(OTHER_PERSON_ID, USER), ALLOWED);
            assertThat(sut.of(office, application).isCommentMandatoryToCancel()).isTrue();
        }
    }

    @Nested
    class DeclineCancellationRequest {

        @Test
        void ensureManagerNeedsApplicationCancellationRequestedRole() {
            assertThat(permissionsOnManagedPerson(ALLOWED_CANCELLATION_REQUESTED, DEPARTMENT_HEAD, APPLICATION_CANCELLATION_REQUESTED)
                .isAllowedToDeclineCancellationRequest()).isTrue();
            assertThat(permissionsOnManagedPerson(ALLOWED_CANCELLATION_REQUESTED, DEPARTMENT_HEAD)
                .isAllowedToDeclineCancellationRequest()).isFalse();
        }

        @Test
        void ensureOfficeMayDecline() {
            assertThat(permissionsOnUnmanagedPerson(ALLOWED_CANCELLATION_REQUESTED, OFFICE).isAllowedToDeclineCancellationRequest()).isTrue();
        }
    }

    @Nested
    class Remind {

        @Test
        void ensurePersonMayRemindAboutOwnWaitingApplication() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(person, application(person, WAITING)).isAllowedToRemind()).isTrue();
        }

        @Test
        void ensureManagementDoesNotRemindItself() {
            final Person boss = person(SIGNED_IN_USER_ID, BOSS);
            assertThat(sut.of(boss, application(boss, WAITING)).isAllowedToRemind()).isFalse();
        }

        @Test
        void ensureNobodyRemindsAboutTheApplicationOfSomebodyElse() {
            assertThat(permissionsOnUnmanagedPerson(WAITING, USER).isAllowedToRemind()).isFalse();
        }
    }

    @Nested
    class ApplyForPerson {

        @Test
        void ensureEveryoneMayApplyForThemselves() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.isAllowedToApplyForPerson(person, person)).isTrue();
            verifyNoInteractions(departmentService);
        }

        @Test
        void ensureOfficeMayApplyForEveryone() {
            assertThat(sut.isAllowedToApplyForPerson(person(SIGNED_IN_USER_ID, OFFICE), person(OTHER_PERSON_ID, USER))).isTrue();
            verifyNoInteractions(departmentService);
        }

        @Test
        void ensureBossNeedsApplicationAddRoleToApplyForSomebodyElse() {
            assertThat(sut.isAllowedToApplyForPerson(person(SIGNED_IN_USER_ID, BOSS, APPLICATION_ADD), person(OTHER_PERSON_ID, USER))).isTrue();
            assertThat(sut.isAllowedToApplyForPerson(person(SIGNED_IN_USER_ID, BOSS), person(OTHER_PERSON_ID, USER))).isFalse();
        }

        @Test
        void ensureManagerMayApplyForManagedMemberOnly() {

            final Person departmentHead = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD, APPLICATION_ADD);
            final Person member = person(OTHER_PERSON_ID, USER);
            when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, member)).thenReturn(true);

            assertThat(sut.isAllowedToApplyForPerson(departmentHead, member)).isTrue();
        }

        @Test
        void ensureUserMayNotApplyForSomebodyElse() {
            assertThat(sut.isAllowedToApplyForPerson(person(SIGNED_IN_USER_ID, USER, APPLICATION_ADD), person(OTHER_PERSON_ID, USER))).isFalse();
        }
    }

    @Nested
    class ResponsibilityLookups {

        @Test
        void ensureTheOwnSecondStageAuthorityIsLookedUpForADepartmentHeadOnly() {

            final Person secondStageAuthority = person(SIGNED_IN_USER_ID, SECOND_STAGE_AUTHORITY);
            final Person member = person(OTHER_PERSON_ID, USER);

            sut.of(secondStageAuthority, application(member, WAITING));

            // only a department head is held back by the application of their own second stage authority
            verify(departmentService).isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, member);
            verify(departmentService, never()).isSecondStageAuthorityAllowedToManagePerson(member, secondStageAuthority);
        }

        @Test
        void ensureResponsibilitiesAreLookedUpOnlyOnce() {

            final Person departmentHead = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD, APPLICATION_CANCEL);
            final Person member = person(OTHER_PERSON_ID, USER);
            when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, member)).thenReturn(true);

            final ApplicationForLeavePermissions permissions = sut.of(departmentHead, application(member, ALLOWED));
            permissions.isAllowedToCancel();
            permissions.isAllowedToComment();
            permissions.isAllowedToEdit();

            verify(departmentService).isDepartmentHeadAllowedToManagePerson(departmentHead, member);
        }

        @Test
        void ensureResponsibilitiesOfAListOfApplicationsAreResolvedAtOnce() {

            final Person departmentHead = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD);
            final Person memberOne = person(OTHER_PERSON_ID, USER);
            final Person memberTwo = person(3L, USER);

            when(departmentService.getMembersForDepartmentHead(departmentHead)).thenReturn(List.of(memberOne, memberTwo));

            final List<Application> applications = List.of(application(memberOne, WAITING), application(memberTwo, WAITING));
            final var permissionsOf = sut.of(departmentHead, applications);

            assertThat(applications).allSatisfy(application ->
                assertThat(permissionsOf.apply(application).isAllowedToAllowWaiting()).isTrue());

            verify(departmentService).getMembersForDepartmentHead(departmentHead);
            verify(departmentService, never()).isDepartmentHeadAllowedToManagePerson(any(), any());
        }
    }

    private ApplicationForLeavePermissions permissionsOnManagedPerson(ApplicationStatus status, Role... roles) {

        final Person signedInUser = person(SIGNED_IN_USER_ID, roles);
        final Person person = person(OTHER_PERSON_ID, USER);

        final List<Role> rolesOfUser = List.of(roles);
        if (rolesOfUser.contains(DEPARTMENT_HEAD)) {
            when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)).thenReturn(true);
        }
        if (rolesOfUser.contains(SECOND_STAGE_AUTHORITY)) {
            when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person)).thenReturn(true);
        }

        return sut.of(signedInUser, application(person, status));
    }

    private ApplicationForLeavePermissions permissionsOnUnmanagedPerson(ApplicationStatus status, Role... roles) {
        return sut.of(person(SIGNED_IN_USER_ID, roles), application(person(OTHER_PERSON_ID, USER), status));
    }

    private static Person person(long id, Role... roles) {
        final Person person = new Person();
        person.setId(id);
        person.setPermissions(List.of(roles));
        return person;
    }

    private static Application application(Person person, ApplicationStatus status) {
        return applicationWith(person, status, false);
    }

    private static Application applicationRequiringApprovalToCancel(Person person, ApplicationStatus status) {
        return applicationWith(person, status, true);
    }

    private static Application applicationWith(Person person, ApplicationStatus status, boolean requiresApprovalToCancel) {

        final VacationType<?> vacationType = ProvidedVacationType.builder(new org.springframework.context.support.StaticMessageSource())
            .id(1L)
            .category(VacationCategory.HOLIDAY)
            .messageKey("vacationTypeMessageKey")
            .requiresApprovalToCancel(requiresApprovalToCancel)
            .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(person);
        application.setStatus(status);
        application.setVacationType(vacationType);
        return application;
    }
}
