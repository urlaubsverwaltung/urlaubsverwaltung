package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_CANCEL;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_COMMENT;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.SUBMITTED;

/**
 * Truth table of "who may interact with a sick note?".
 *
 * <p>The rule under test: {@link Role#SICK_NOTE_VIEW} grants sight of sick notes of persons one does not manage.
 * {@link Role#OFFICE} sees everyone, {@link Role#BOSS} needs {@code SICK_NOTE_VIEW} to see everyone,
 * {@link Role#DEPARTMENT_HEAD} / {@link Role#SECOND_STAGE_AUTHORITY} always see their own members, everyone sees their
 * own sick notes. Maintaining a sick note additionally requires the matching {@code SICK_NOTE_*} role.
 */
@ExtendWith(MockitoExtension.class)
class SickNotePermissionEvaluatorTest {

    private static final long SIGNED_IN_USER_ID = 1L;
    private static final long OTHER_PERSON_ID = 2L;

    private SickNotePermissionEvaluator sut;

    private final Settings settings = new Settings();

    @Mock
    private DepartmentService departmentService;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new SickNotePermissionEvaluator(departmentService, settingsService);
        lenient().when(settingsService.getSettings()).thenReturn(settings);
    }

    @Nested
    class View {

        @Test
        void ensureEveryoneMaySeeOwnSickNotes() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(person, person).isAllowedToView()).isTrue();
        }

        @Test
        void ensureOfficeMaySeeSickNotesOfEveryone() {
            assertThat(permissionsOnUnmanagedPerson(OFFICE).isAllowedToView()).isTrue();
        }

        @Test
        void ensureBossMaySeeSickNotesOfEveryoneWithSickNoteViewRole() {
            assertThat(permissionsOnUnmanagedPerson(BOSS, SICK_NOTE_VIEW).isAllowedToView()).isTrue();
        }

        @Test
        void ensureBossMayNotSeeSickNotesWithoutSickNoteViewRole() {
            assertThat(permissionsOnUnmanagedPerson(BOSS).isAllowedToView()).isFalse();
        }

        @Test
        void ensureDepartmentHeadMaySeeSickNotesOfManagedMemberWithoutSickNoteViewRole() {
            assertThat(permissionsOnManagedPerson(DEPARTMENT_HEAD).isAllowedToView()).isTrue();
        }

        @Test
        void ensureSecondStageAuthorityMaySeeSickNotesOfManagedMemberWithoutSickNoteViewRole() {
            assertThat(permissionsOnManagedPerson(SECOND_STAGE_AUTHORITY).isAllowedToView()).isTrue();
        }

        @Test
        void ensureDepartmentHeadMayNotSeeSickNotesOfPersonOutsideOfDepartment() {
            assertThat(permissionsOnUnmanagedPerson(DEPARTMENT_HEAD, SICK_NOTE_VIEW).isAllowedToView()).isFalse();
        }

        @Test
        void ensureSecondStageAuthorityMayNotSeeSickNotesOfPersonOutsideOfDepartment() {
            assertThat(permissionsOnUnmanagedPerson(SECOND_STAGE_AUTHORITY, SICK_NOTE_VIEW).isAllowedToView()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"USER", "SICK_NOTE_VIEW", "SICK_NOTE_ADD", "SICK_NOTE_EDIT", "SICK_NOTE_CANCEL", "SICK_NOTE_COMMENT"})
        void ensureSickNoteRoleAloneDoesNotAllowToSeeSickNotesOfOtherPersons(Role role) {
            assertThat(permissionsOnUnmanagedPerson(role).isAllowedToView()).isFalse();
        }
    }

    @Nested
    class Add {

        @Test
        void ensureOfficeMayAdd() {
            assertThat(permissionsOnUnmanagedPerson(OFFICE).isAllowedToAdd()).isTrue();
        }

        @Test
        void ensureBossWithSickNoteAddMayAddForEveryone() {
            assertThat(permissionsOnUnmanagedPerson(BOSS, SICK_NOTE_ADD).isAllowedToAdd()).isTrue();
        }

        @Test
        void ensureDepartmentHeadWithSickNoteAddMayAddForManagedMember() {
            assertThat(permissionsOnManagedPerson(DEPARTMENT_HEAD, SICK_NOTE_ADD).isAllowedToAdd()).isTrue();
        }

        @Test
        void ensureSecondStageAuthorityWithSickNoteAddMayAddForManagedMember() {
            assertThat(permissionsOnManagedPerson(SECOND_STAGE_AUTHORITY, SICK_NOTE_ADD).isAllowedToAdd()).isTrue();
        }

        @Test
        void ensureDepartmentHeadWithSickNoteAddMayNotAddForPersonOutsideOfDepartment() {
            assertThat(permissionsOnUnmanagedPerson(DEPARTMENT_HEAD, SICK_NOTE_ADD).isAllowedToAdd()).isFalse();
        }

        @Test
        void ensureDepartmentHeadWithoutSickNoteAddMayNotAddForManagedMember() {
            final SickNotePermissions permissions = permissionsOnManagedPerson(DEPARTMENT_HEAD);
            assertThat(permissions.isAllowedToAdd()).isFalse();
            // managing the person is not the reason - seeing the sick notes is allowed
            assertThat(permissions.isAllowedToView()).isTrue();
        }

        @Test
        void ensureSickNoteAddAloneDoesNotAllowToAdd() {
            final Person signedInUser = person(SIGNED_IN_USER_ID, USER, SICK_NOTE_ADD);
            assertThat(sut.of(signedInUser, signedInUser).isAllowedToAdd()).isFalse();
        }

        @Test
        void ensureSickNoteEditDoesNotAllowToAdd() {
            final SickNotePermissions permissions = permissionsOnManagedPerson(DEPARTMENT_HEAD, SICK_NOTE_EDIT);
            assertThat(permissions.isAllowedToAdd()).isFalse();
            assertThat(permissions.isAllowedToAccept()).isTrue();
        }
    }

    @Nested
    class Submit {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void ensureOnlyThePersonItselfMaySubmit(boolean submissionEnabled) {
            settingsWithSubmissionOfSickNotes(submissionEnabled);

            final Person signedInUser = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(signedInUser, signedInUser).isAllowedToSubmit()).isEqualTo(submissionEnabled);
        }

        @Test
        void ensureNobodyMaySubmitForSomeoneElse() {
            final Person signedInUser = person(SIGNED_IN_USER_ID, OFFICE, BOSS, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY, SICK_NOTE_ADD);
            assertThat(sut.of(signedInUser, person(OTHER_PERSON_ID, USER)).isAllowedToSubmit()).isFalse();
        }
    }

    @Nested
    class Accept {

        @Test
        void ensureOfficeMayAccept() {
            assertThat(permissionsOnUnmanagedPerson(OFFICE).isAllowedToAccept()).isTrue();
        }

        @Test
        void ensureBossWithSickNoteEditMayAccept() {
            assertThat(permissionsOnUnmanagedPerson(BOSS, SICK_NOTE_EDIT).isAllowedToAccept()).isTrue();
        }

        @Test
        void ensureDepartmentHeadWithSickNoteEditMayAcceptForManagedMember() {
            assertThat(permissionsOnManagedPerson(DEPARTMENT_HEAD, SICK_NOTE_EDIT).isAllowedToAccept()).isTrue();
        }

        @Test
        void ensureSickNoteAddDoesNotAllowToAccept() {
            final SickNotePermissions permissions = permissionsOnManagedPerson(DEPARTMENT_HEAD, SICK_NOTE_ADD);
            assertThat(permissions.isAllowedToAccept()).isFalse();
            assertThat(permissions.isAllowedToAdd()).isTrue();
        }

        @Test
        void ensurePersonMayNotAcceptOwnSickNote() {
            final Person signedInUser = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(signedInUser, signedInUser).isAllowedToAccept()).isFalse();
        }

        @Test
        void ensureAcceptingAnExtensionFollowsTheSameRuleAsAccepting() {
            final SickNotePermissions permissions = permissionsOnManagedPerson(DEPARTMENT_HEAD, SICK_NOTE_EDIT);
            assertThat(permissions.isAllowedToAcceptExtension()).isEqualTo(permissions.isAllowedToAccept());
        }

        @Test
        void ensureSickNoteAddDoesNotAllowToAcceptAnExtension() {
            final SickNotePermissions permissions = permissionsOnManagedPerson(DEPARTMENT_HEAD, SICK_NOTE_ADD);
            assertThat(permissions.isAllowedToAcceptExtension()).isFalse();
            assertThat(permissions.isAllowedToAdd()).isTrue();
        }
    }

    @Nested
    class Edit {

        @Test
        void ensureOfficeMayEdit() {
            assertThat(permissionsOnUnmanagedPerson(OFFICE).isAllowedToEdit(sickNote(OTHER_PERSON_ID, ACTIVE))).isTrue();
        }

        @Test
        void ensureBossWithSickNoteEditMayEdit() {
            assertThat(permissionsOnUnmanagedPerson(BOSS, SICK_NOTE_EDIT).isAllowedToEdit(sickNote(OTHER_PERSON_ID, ACTIVE))).isTrue();
        }

        @Test
        void ensureDepartmentHeadWithSickNoteEditMayEditForManagedMember() {
            assertThat(permissionsOnManagedPerson(DEPARTMENT_HEAD, SICK_NOTE_EDIT).isAllowedToEdit(sickNote(OTHER_PERSON_ID, ACTIVE))).isTrue();
        }

        @Test
        void ensurePersonMayEditOwnSubmittedSickNote() {
            final Person signedInUser = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(signedInUser, signedInUser).isAllowedToEdit(sickNote(SIGNED_IN_USER_ID, SUBMITTED))).isTrue();
        }

        @Test
        void ensurePersonMayNotEditOwnAcceptedSickNote() {
            final Person signedInUser = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(signedInUser, signedInUser).isAllowedToEdit(sickNote(SIGNED_IN_USER_ID, ACTIVE))).isFalse();
        }

        @Test
        void ensureSickNoteAddDoesNotAllowToEdit() {
            final SickNotePermissions permissions = permissionsOnManagedPerson(DEPARTMENT_HEAD, SICK_NOTE_ADD);
            assertThat(permissions.isAllowedToEdit(sickNote(OTHER_PERSON_ID, ACTIVE))).isFalse();
            assertThat(permissions.isAllowedToAdd()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = SickNoteStatus.class, names = {"CANCELLED", "CONVERTED_TO_VACATION"})
        void ensureNobodyMayEditInactiveSickNote(SickNoteStatus status) {
            assertThat(permissionsOnUnmanagedPerson(OFFICE).isAllowedToEdit(sickNote(OTHER_PERSON_ID, status))).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = SickNoteStatus.class, names = {"CANCELLED", "CONVERTED_TO_VACATION"})
        void ensurePersonMayNotEditOwnInactiveSickNote(SickNoteStatus status) {
            final Person signedInUser = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(signedInUser, signedInUser).isAllowedToEdit(sickNote(SIGNED_IN_USER_ID, status))).isFalse();
        }
    }

    @Nested
    class Convert {

        @Test
        void ensureOnlyOfficeMayConvert() {
            assertThat(permissionsOnUnmanagedPerson(OFFICE).isAllowedToConvert()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerWithEveryTaskRoleMayNotConvert(Role role) {
            final Person signedInUser = person(SIGNED_IN_USER_ID, role, SICK_NOTE_VIEW, SICK_NOTE_ADD, SICK_NOTE_EDIT, SICK_NOTE_CANCEL, SICK_NOTE_COMMENT);
            assertThat(sut.of(signedInUser, person(OTHER_PERSON_ID, USER)).isAllowedToConvert()).isFalse();
        }
    }

    @Nested
    class Cancel {

        @Test
        void ensureOfficeMayCancel() {
            assertThat(permissionsOnUnmanagedPerson(OFFICE).isAllowedToCancel(sickNote(OTHER_PERSON_ID, ACTIVE))).isTrue();
        }

        @Test
        void ensureBossWithSickNoteCancelMayCancel() {
            assertThat(permissionsOnUnmanagedPerson(BOSS, SICK_NOTE_CANCEL).isAllowedToCancel(sickNote(OTHER_PERSON_ID, ACTIVE))).isTrue();
        }

        @Test
        void ensureDepartmentHeadWithSickNoteCancelMayCancelForManagedMember() {
            assertThat(permissionsOnManagedPerson(DEPARTMENT_HEAD, SICK_NOTE_CANCEL).isAllowedToCancel(sickNote(OTHER_PERSON_ID, ACTIVE))).isTrue();
        }

        @Test
        void ensureSubmittedSickNoteMayBeCancelled() {
            assertThat(permissionsOnUnmanagedPerson(OFFICE).isAllowedToCancel(sickNote(OTHER_PERSON_ID, SUBMITTED))).isTrue();
        }

        @Test
        void ensureDepartmentHeadWithSickNoteCancelMayNotCancelForPersonOutsideOfDepartment() {
            assertThat(permissionsOnUnmanagedPerson(DEPARTMENT_HEAD, SICK_NOTE_CANCEL).isAllowedToCancel(sickNote(OTHER_PERSON_ID, ACTIVE))).isFalse();
        }

        @Test
        void ensureSickNoteEditDoesNotAllowToCancel() {
            final SickNotePermissions permissions = permissionsOnManagedPerson(DEPARTMENT_HEAD, SICK_NOTE_EDIT);
            assertThat(permissions.isAllowedToCancel(sickNote(OTHER_PERSON_ID, ACTIVE))).isFalse();
            assertThat(permissions.isAllowedToAccept()).isTrue();
        }

        @Test
        void ensurePersonMayNotCancelOwnSickNote() {
            final Person signedInUser = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(signedInUser, signedInUser).isAllowedToCancel(sickNote(SIGNED_IN_USER_ID, ACTIVE))).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = SickNoteStatus.class, names = {"CANCELLED", "CONVERTED_TO_VACATION"})
        void ensureNobodyMayCancelInactiveSickNote(SickNoteStatus status) {
            assertThat(permissionsOnUnmanagedPerson(OFFICE).isAllowedToCancel(sickNote(OTHER_PERSON_ID, status))).isFalse();
        }
    }

    @Nested
    class Comment {

        @Test
        void ensureOfficeMayComment() {
            assertThat(permissionsOnUnmanagedPerson(OFFICE).isAllowedToComment()).isTrue();
        }

        @Test
        void ensureBossWithSickNoteCommentMayComment() {
            assertThat(permissionsOnUnmanagedPerson(BOSS, SICK_NOTE_COMMENT).isAllowedToComment()).isTrue();
        }

        @Test
        void ensureDepartmentHeadWithSickNoteCommentMayCommentForManagedMember() {
            assertThat(permissionsOnManagedPerson(DEPARTMENT_HEAD, SICK_NOTE_COMMENT).isAllowedToComment()).isTrue();
        }

        @Test
        void ensureSickNoteEditDoesNotAllowToComment() {
            final SickNotePermissions permissions = permissionsOnManagedPerson(DEPARTMENT_HEAD, SICK_NOTE_EDIT);
            assertThat(permissions.isAllowedToComment()).isFalse();
            assertThat(permissions.isAllowedToAccept()).isTrue();
        }

        @Test
        void ensurePersonMayNotCommentOwnSickNote() {
            final Person signedInUser = person(SIGNED_IN_USER_ID, USER);
            assertThat(sut.of(signedInUser, signedInUser).isAllowedToComment()).isFalse();
        }
    }

    @Nested
    class SickNoteSubmissions {

        @Test
        void ensureOfficeMayAccessSickNoteSubmissions() {
            assertThat(sut.isAllowedToAccessSickNoteSubmissions(person(SIGNED_IN_USER_ID, OFFICE))).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerWithSickNoteEditMayAccessSickNoteSubmissions(Role role) {
            assertThat(sut.isAllowedToAccessSickNoteSubmissions(person(SIGNED_IN_USER_ID, role, SICK_NOTE_EDIT))).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerWithoutSickNoteEditMayNotAccessSickNoteSubmissions(Role role) {
            assertThat(sut.isAllowedToAccessSickNoteSubmissions(person(SIGNED_IN_USER_ID, role))).isFalse();
        }

        @Test
        void ensureSickNoteEditAloneDoesNotAllowToAccessSickNoteSubmissions() {
            assertThat(sut.isAllowedToAccessSickNoteSubmissions(person(SIGNED_IN_USER_ID, USER, SICK_NOTE_EDIT))).isFalse();
        }
    }

    @Nested
    class ViewSickNotesOfOtherPersons {

        @Test
        void ensureOfficeMayViewSickNotesOfAllPersons() {
            final Person office = person(SIGNED_IN_USER_ID, OFFICE);
            assertThat(sut.isAllowedToViewSickNotesOfAllPersons(office)).isTrue();
            assertThat(sut.isAllowedToViewSickNotesOfOtherPersons(office)).isTrue();
        }

        @Test
        void ensureBossNeedsSickNoteViewToViewSickNotesOfAllPersons() {
            assertThat(sut.isAllowedToViewSickNotesOfAllPersons(person(SIGNED_IN_USER_ID, BOSS, SICK_NOTE_VIEW))).isTrue();
            assertThat(sut.isAllowedToViewSickNotesOfAllPersons(person(SIGNED_IN_USER_ID, BOSS))).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerMayViewSickNotesOfOtherPersonsButNotOfAllPersons(Role role) {
            final Person manager = person(SIGNED_IN_USER_ID, role);
            assertThat(sut.isAllowedToViewSickNotesOfOtherPersons(manager)).isTrue();
            assertThat(sut.isAllowedToViewSickNotesOfAllPersons(manager)).isFalse();
        }

        @Test
        void ensureBossWithSickNoteViewIsNotLimitedToManagedMembers() {
            assertThat(sut.isAllowedToViewSickNotesOfOtherPersons(person(SIGNED_IN_USER_ID, BOSS, SICK_NOTE_VIEW))).isTrue();
        }

        @Test
        void ensureUserMayNotViewSickNotesOfOtherPersons() {
            final Person user = person(SIGNED_IN_USER_ID, USER, SICK_NOTE_VIEW);
            assertThat(sut.isAllowedToViewSickNotesOfOtherPersons(user)).isFalse();
            assertThat(sut.isAllowedToViewSickNotesOfAllPersons(user)).isFalse();
        }
    }

    @Nested
    class DepartmentLookups {

        @Test
        void ensureDepartmentMembershipsAreLookedUpOnlyOnce() {
            final Person signedInUser = person(SIGNED_IN_USER_ID, DEPARTMENT_HEAD, SICK_NOTE_EDIT, SICK_NOTE_CANCEL, SICK_NOTE_COMMENT);
            final Person sickNotePerson = person(OTHER_PERSON_ID, USER);

            when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, sickNotePerson)).thenReturn(true);

            final SickNotePermissions permissions = sut.of(signedInUser, sickNotePerson);
            permissions.isAllowedToView();
            permissions.isAllowedToAccept();
            permissions.isAllowedToCancel(sickNote(OTHER_PERSON_ID, ACTIVE));
            permissions.isAllowedToComment();

            verify(departmentService).isDepartmentHeadAllowedToManagePerson(signedInUser, sickNotePerson);
            verify(departmentService).isSecondStageAuthorityAllowedToManagePerson(signedInUser, sickNotePerson);
        }
    }

    private SickNotePermissions permissionsOnManagedPerson(Role... roles) {

        final Person signedInUser = person(SIGNED_IN_USER_ID, roles);
        final Person sickNotePerson = person(OTHER_PERSON_ID, USER);

        final List<Role> rolesOfUser = List.of(roles);
        if (rolesOfUser.contains(DEPARTMENT_HEAD)) {
            when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, sickNotePerson)).thenReturn(true);
        }
        if (rolesOfUser.contains(SECOND_STAGE_AUTHORITY)) {
            when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, sickNotePerson)).thenReturn(true);
        }

        return sut.of(signedInUser, sickNotePerson);
    }

    private SickNotePermissions permissionsOnUnmanagedPerson(Role... roles) {
        return sut.of(person(SIGNED_IN_USER_ID, roles), person(OTHER_PERSON_ID, USER));
    }

    private void settingsWithSubmissionOfSickNotes(boolean enabled) {
        settings.getSickNoteSettings().setUserIsAllowedToSubmitSickNotes(enabled);
    }

    private static Person person(long id, Role... roles) {
        final Person person = new Person();
        person.setId(id);
        person.setPermissions(List.of(roles));
        return person;
    }

    private static SickNote sickNote(long personId, SickNoteStatus status) {
        return SickNote.builder()
            .id(42L)
            .person(person(personId, USER))
            .status(status)
            .build();
    }
}
