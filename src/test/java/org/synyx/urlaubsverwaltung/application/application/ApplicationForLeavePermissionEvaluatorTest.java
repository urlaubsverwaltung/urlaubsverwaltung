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
import java.util.function.BiPredicate;

import static org.assertj.core.api.Assertions.assertThat;
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
            assertThat(onUnmanagedPerson(WAITING, BOSS).may(sut::isAllowedToAllowWaiting)).isTrue();
        }

        @Test
        void ensureBossMayAllowOwnWaitingApplication() {
            final Person boss = person(SIGNED_IN_USER_ID, BOSS);
            assertThat(sut.isAllowedToAllowWaiting(boss, application(boss, WAITING))).isTrue();
        }

        @Test
        void ensureDepartmentHeadMayAllowWaitingApplicationOfManagedMember() {
            assertThat(onManagedPerson(WAITING, DEPARTMENT_HEAD).may(sut::isAllowedToAllowWaiting)).isTrue();
        }

        @Test
        void ensureSecondStageAuthorityMayAllowWaitingApplicationOfManagedMember() {
            assertThat(onManagedPerson(WAITING, SECOND_STAGE_AUTHORITY).may(sut::isAllowedToAllowWaiting)).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerMayNotAllowOwnWaitingApplication(Role role) {
            final Person manager = person(SIGNED_IN_USER_ID, role);
            assertThat(sut.isAllowedToAllowWaiting(manager, application(manager, WAITING))).isFalse();
        }

        @Test
        void ensureDepartmentHeadMayNotAllowApplicationOfOwnSecondStageAuthority() {

            final Person departmentHead = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD);
            final Person secondStageAuthority = person(OTHER_PERSON_ID, SECOND_STAGE_AUTHORITY);

            when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, secondStageAuthority)).thenReturn(true);
            when(departmentService.isSecondStageAuthorityAllowedToManagePerson(departmentHead, secondStageAuthority)).thenReturn(false);
            when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, departmentHead)).thenReturn(true);

            assertThat(sut.isAllowedToAllowWaiting(departmentHead, application(secondStageAuthority, WAITING))).isFalse();
        }

        @Test
        void ensureNobodyWithoutManagementRoleMayAllow() {
            assertThat(onUnmanagedPerson(WAITING, APPLICATION_ADD, APPLICATION_EDIT, APPLICATION_CANCEL).may(sut::isAllowedToAllowWaiting)).isFalse();
        }

        @Test
        void ensureOfficeMayNotAllow() {
            assertThat(onUnmanagedPerson(WAITING, OFFICE).may(sut::isAllowedToAllowWaiting)).isFalse();
        }

        @Test
        void ensureAllowIsOnlyPossibleForWaitingApplications() {
            assertThat(onUnmanagedPerson(ALLOWED, BOSS).may(sut::isAllowedToAllowWaiting)).isFalse();
            assertThat(onUnmanagedPerson(REJECTED, BOSS).may(sut::isAllowedToAllowWaiting)).isFalse();
        }

        @Test
        void ensureSecondStageAuthorityMayFinallyAllowTemporaryAllowedApplication() {
            assertThat(onManagedPerson(TEMPORARY_ALLOWED, SECOND_STAGE_AUTHORITY).may(sut::isAllowedToAllowTemporaryAllowed)).isTrue();
        }

        @Test
        void ensureDepartmentHeadMayNotFinallyAllowTemporaryAllowedApplication() {
            final Fixture fixture = onManagedPerson(TEMPORARY_ALLOWED, DEPARTMENT_HEAD);
            assertThat(fixture.may(sut::isAllowedToAllowTemporaryAllowed)).isFalse();
            // being responsible for the person is not the reason, rejecting it is allowed
            assertThat(fixture.may(sut::isAllowedToReject)).isTrue();
        }

        @Test
        void ensureDepartmentHeadAllowsTemporarilyOnlyWithTwoStageApproval() {

            final Person departmentHead = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD);
            final Person member = person(OTHER_PERSON_ID, USER);
            when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, member)).thenReturn(true);

            final Application twoStage = application(member, WAITING);
            twoStage.setTwoStageApproval(true);
            assertThat(sut.isAllowedToAllowTemporarily(departmentHead, twoStage)).isTrue();

            final Application oneStage = application(member, WAITING);
            assertThat(sut.isAllowedToAllowTemporarily(departmentHead, oneStage)).isFalse();
        }

        @Test
        void ensureBossNeverAllowsTemporarilyOnly() {

            final Person boss = person(SIGNED_IN_USER_ID, BOSS);
            final Application twoStage = application(person(OTHER_PERSON_ID, USER), WAITING);
            twoStage.setTwoStageApproval(true);

            assertThat(sut.isAllowedToAllowTemporarily(boss, twoStage)).isFalse();
        }
    }

    @Nested
    class Reject {

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerMayRejectApplicationOfManagedMember(Role role) {
            assertThat(onManagedPerson(WAITING, role).may(sut::isAllowedToReject)).isTrue();
        }

        @Test
        void ensureBossMayRejectEveryApplication() {
            assertThat(onUnmanagedPerson(WAITING, BOSS).may(sut::isAllowedToReject)).isTrue();
        }

        @Test
        void ensureNobodyMayRejectOwnApplication() {
            final Person boss = person(SIGNED_IN_USER_ID, BOSS);
            assertThat(sut.isAllowedToReject(boss, application(boss, WAITING))).isFalse();
        }

        @Test
        void ensureOfficeMayNotReject() {
            assertThat(onUnmanagedPerson(WAITING, OFFICE).may(sut::isAllowedToReject)).isFalse();
        }

        @Test
        void ensureRejectIsOnlyPossibleForWaitingOrTemporaryAllowedApplications() {
            assertThat(onUnmanagedPerson(TEMPORARY_ALLOWED, BOSS).may(sut::isAllowedToReject)).isTrue();
            assertThat(onUnmanagedPerson(ALLOWED, BOSS).may(sut::isAllowedToReject)).isFalse();
        }
    }

    @Nested
    class Comment {

        @Test
        void ensureOfficeMayComment() {
            assertThat(onUnmanagedPerson(WAITING, OFFICE).may(sut::isAllowedToComment)).isTrue();
        }

        @Test
        void ensureBossMayComment() {
            assertThat(onUnmanagedPerson(WAITING, BOSS).may(sut::isAllowedToComment)).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerMayCommentWithoutApplicationAddRole(Role role) {
            final Fixture fixture = onManagedPerson(WAITING, role);
            // whoever may reject an application has to be able to say why
            assertThat(fixture.may(sut::isAllowedToComment)).isTrue();
            assertThat(fixture.may(sut::isAllowedToReject)).isTrue();
        }

        @Test
        void ensureManagerOfAnotherDepartmentMayNotComment() {
            assertThat(onUnmanagedPerson(WAITING, DEPARTMENT_HEAD, APPLICATION_ADD).may(sut::isAllowedToComment)).isFalse();
        }

        @Test
        void ensureUserMayNotCommentApplicationOfSomebodyElse() {
            assertThat(onUnmanagedPerson(WAITING, USER).may(sut::isAllowedToComment)).isFalse();
        }
    }

    @Nested
    class Refer {

        @Test
        void ensureBossAndOfficeMayRefer() {
            assertThat(onUnmanagedPerson(WAITING, BOSS).may(sut::isAllowedToRefer)).isTrue();
            assertThat(onUnmanagedPerson(WAITING, OFFICE).may(sut::isAllowedToRefer)).isTrue();
        }

        @Test
        void ensureManagerMayReferApplicationOfManagedMember() {
            assertThat(onManagedPerson(WAITING, DEPARTMENT_HEAD).may(sut::isAllowedToRefer)).isTrue();
        }

        @Test
        void ensureManagerMayNotReferOwnApplication() {
            final Person departmentHead = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD);
            assertThat(sut.isAllowedToRefer(departmentHead, application(departmentHead, WAITING))).isFalse();
        }
    }

    @Nested
    class Edit {

        @Test
        void ensurePersonMayEditOwnWaitingApplication() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.isAllowedToEdit(person, application(person, WAITING))).isTrue();
        }

        @Test
        void ensurePersonMayNotEditOwnApplicationSomebodyDecidedAbout() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.isAllowedToEdit(person, application(person, ALLOWED))).isFalse();
        }

        @Test
        void ensureOfficeMayEdit() {
            assertThat(onUnmanagedPerson(ALLOWED, OFFICE).may(sut::isAllowedToEdit)).isTrue();
        }

        @Test
        void ensureManagerNeedsApplicationEditRoleToEdit() {
            assertThat(onManagedPerson(ALLOWED, DEPARTMENT_HEAD, APPLICATION_EDIT).may(sut::isAllowedToEdit)).isTrue();
            assertThat(onManagedPerson(ALLOWED, DEPARTMENT_HEAD).may(sut::isAllowedToEdit)).isFalse();
        }

        @Test
        void ensureBossMayNotEditWithoutApplicationEditRole() {
            assertThat(onUnmanagedPerson(ALLOWED, BOSS, APPLICATION_EDIT).may(sut::isAllowedToEdit)).isFalse();
        }
    }

    @Nested
    class CancelAndRevoke {

        @Test
        void ensurePersonMayRevokeOwnWaitingApplication() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.isAllowedToRevoke(person, applicationRequiringApprovalToCancel(person, WAITING))).isTrue();
        }

        @Test
        void ensureManagerNeedsApplicationCancelRoleToCancel() {
            assertThat(onManagedPerson(ALLOWED, DEPARTMENT_HEAD, APPLICATION_CANCEL).may(sut::isAllowedToCancel)).isTrue();
            assertThat(onManagedPerson(ALLOWED, DEPARTMENT_HEAD).may(sut::isAllowedToCancel)).isFalse();
        }

        @Test
        void ensureOfficeMayCancel() {
            assertThat(onUnmanagedPerson(ALLOWED, OFFICE).may(sut::isAllowedToCancel)).isTrue();
        }

        @Test
        void ensurePersonMayCancelOwnApplicationDirectlyIfNoApprovalIsRequired() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.isAllowedToCancelDirectly(person, application(person, ALLOWED))).isTrue();
        }

        @Test
        void ensurePersonMayNotCancelOwnApplicationDirectlyIfApprovalIsRequired() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.isAllowedToCancelDirectly(person, applicationRequiringApprovalToCancel(person, ALLOWED))).isFalse();
        }
    }

    @Nested
    class StartCancellationRequest {

        @Test
        void ensurePersonMayRequestCancellationOfOwnAllowedApplication() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.isAllowedToStartCancellationRequest(person, applicationRequiringApprovalToCancel(person, ALLOWED))).isTrue();
        }

        @Test
        void ensureUnrelatedUserMayNotRequestCancellationOfAnotherPersonsApplication() {

            final Person signedInUser = person(SIGNED_IN_USER_ID, USER);
            final Person other = person(OTHER_PERSON_ID, USER);

            assertThat(sut.isAllowedToStartCancellationRequest(signedInUser, applicationRequiringApprovalToCancel(other, ALLOWED))).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerOfAnotherDepartmentMayNotRequestCancellation(Role role) {
            final Person signedInUser = person(SIGNED_IN_USER_ID, role);
            final Person other = person(OTHER_PERSON_ID, USER);

            assertThat(sut.isAllowedToStartCancellationRequest(signedInUser, applicationRequiringApprovalToCancel(other, ALLOWED))).isFalse();
        }

        @Test
        void ensureManagerMayRequestCancellationForManagedMember() {

            final Person departmentHead = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD);
            final Person member = person(OTHER_PERSON_ID, USER);
            when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, member)).thenReturn(true);

            assertThat(sut.isAllowedToStartCancellationRequest(departmentHead, applicationRequiringApprovalToCancel(member, ALLOWED))).isTrue();
        }

        @Test
        void ensureWhoeverMayCancelDoesNotRequestCancellation() {
            final Person office = person(SIGNED_IN_USER_ID, OFFICE);
            final Application application = applicationRequiringApprovalToCancel(person(OTHER_PERSON_ID, USER), ALLOWED);

            assertThat(sut.isAllowedToCancel(office, application)).isTrue();
            assertThat(sut.isAllowedToStartCancellationRequest(office, application)).isFalse();
        }
    }

    @Nested
    class DeclineCancellationRequest {

        @Test
        void ensureManagerNeedsApplicationCancellationRequestedRole() {
            assertThat(onManagedPerson(ALLOWED_CANCELLATION_REQUESTED, DEPARTMENT_HEAD, APPLICATION_CANCELLATION_REQUESTED)
                .may(sut::isAllowedToDeclineCancellationRequest)).isTrue();
            assertThat(onManagedPerson(ALLOWED_CANCELLATION_REQUESTED, DEPARTMENT_HEAD)
                .may(sut::isAllowedToDeclineCancellationRequest)).isFalse();
        }

        @Test
        void ensureOfficeMayDecline() {
            assertThat(onUnmanagedPerson(ALLOWED_CANCELLATION_REQUESTED, OFFICE).may(sut::isAllowedToDeclineCancellationRequest)).isTrue();
        }
    }

    @Nested
    class Remind {

        @Test
        void ensurePersonMayRemindAboutOwnWaitingApplication() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.isAllowedToRemind(person, application(person, WAITING))).isTrue();
        }

        @Test
        void ensureManagementDoesNotRemindItself() {
            final Person boss = person(SIGNED_IN_USER_ID, BOSS);
            assertThat(sut.isAllowedToRemind(boss, application(boss, WAITING))).isFalse();
        }

        @Test
        void ensureNobodyRemindsAboutTheApplicationOfSomebodyElse() {
            assertThat(onUnmanagedPerson(WAITING, USER).may(sut::isAllowedToRemind)).isFalse();
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
        void ensureResponsibilitiesAreNotLookedUpForTheOwnWaitingApplication() {
            final Person person = person(SIGNED_IN_USER_ID, USER, DEPARTMENT_HEAD);
            assertThat(sut.isAllowedToEdit(person, application(person, WAITING))).isTrue();
            verifyNoInteractions(departmentService);
        }
    }

    private Fixture onManagedPerson(ApplicationStatus status, Role... roles) {

        final Person signedInUser = person(SIGNED_IN_USER_ID, roles);
        final Person person = person(OTHER_PERSON_ID, USER);

        final List<Role> rolesOfUser = List.of(roles);
        if (rolesOfUser.contains(DEPARTMENT_HEAD)) {
            when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person)).thenReturn(true);
        }
        if (rolesOfUser.contains(SECOND_STAGE_AUTHORITY)) {
            when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person)).thenReturn(true);
        }

        return new Fixture(signedInUser, application(person, status));
    }

    private Fixture onUnmanagedPerson(ApplicationStatus status, Role... roles) {
        return new Fixture(person(SIGNED_IN_USER_ID, roles), application(person(OTHER_PERSON_ID, USER), status));
    }

    /**
     * A signed in user and the application the permissions are asked for, see {@link #may(BiPredicate)}.
     */
    private record Fixture(Person signedInUser, Application application) {

        boolean may(BiPredicate<Person, Application> permission) {
            return permission.test(signedInUser, application);
        }
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
