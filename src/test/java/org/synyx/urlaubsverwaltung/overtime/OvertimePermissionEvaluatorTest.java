package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeType.EXTERNAL;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeType.UV_INTERNAL;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Truth table of "who may interact with overtime of a person?".
 */
@ExtendWith(MockitoExtension.class)
class OvertimePermissionEvaluatorTest {

    private static final long SIGNED_IN_USER_ID = 1L;
    private static final long OTHER_PERSON_ID = 2L;

    private OvertimePermissionEvaluator sut;

    @Mock
    private DepartmentService departmentService;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new OvertimePermissionEvaluator(departmentService, settingsService);
    }

    @Nested
    class View {

        @Test
        void ensureEveryoneMaySeeOwnOvertime() {
            final Person person = person(SIGNED_IN_USER_ID, USER);
            settings(true, false, false);
            assertThat(sut.of(person, person).isAllowedToView()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
        void ensureOfficeAndBossMaySeeOvertimeOfEveryone(Role role) {
            assertThat(permissionsOnUnmanagedPerson(role).isAllowedToView()).isTrue();
        }

        @Test
        void ensureDepartmentHeadMaySeeOvertimeOfManagedMember() {
            assertThat(permissionsOnManagedPerson(DEPARTMENT_HEAD).isAllowedToView()).isTrue();
        }

        @Test
        void ensureSecondStageAuthorityMaySeeOvertimeOfManagedMember() {
            assertThat(permissionsOnManagedPerson(SECOND_STAGE_AUTHORITY).isAllowedToView()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureNobodyElseMaySeeOvertimeOfUnmanagedPerson(Role role) {
            assertThat(permissionsOnUnmanagedPerson(role).isAllowedToView()).isFalse();
        }
    }

    @Nested
    class Add {

        @Test
        void ensureOfficeMayAddForEveryone() {
            assertThat(permissionsOn(OTHER_PERSON_ID, false, false, OFFICE).isAllowedToAdd()).isTrue();
        }

        @Test
        void ensureNobodyMayAddWhenOvertimeIsNotActive() {
            assertThat(permissionsOn(OTHER_PERSON_ID, false, false, OFFICE, false, false).isAllowedToAdd()).isFalse();
        }

        @Test
        void ensureNobodyMayAddWhenOvertimeSyncIsActive() {
            assertThat(permissionsOn(OTHER_PERSON_ID, false, false, OFFICE, true, true).isAllowedToAdd()).isFalse();
        }

        @Test
        void ensurePersonMayAddOwnOvertimeWithoutPrivilegedRestriction() {
            assertThat(permissionsOn(SIGNED_IN_USER_ID, false, false, USER).isAllowedToAdd()).isTrue();
        }

        @Test
        void ensurePersonMayNotAddOwnOvertimeWithPrivilegedRestriction() {
            assertThat(permissionsOnPrivilegedOnly(SIGNED_IN_USER_ID, false, false, USER).isAllowedToAdd()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "OFFICE"})
        void ensurePrivilegedPersonMayAddOwnOvertimeWithPrivilegedRestriction(Role role) {
            assertThat(permissionsOnPrivilegedOnly(SIGNED_IN_USER_ID, false, false, role).isAllowedToAdd()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "USER"})
        void ensureNobodyMayAddForOthersWithoutPrivilegedRestriction(Role role) {
            assertThat(permissionsOn(OTHER_PERSON_ID, false, false, role).isAllowedToAdd()).isFalse();
        }

        @Test
        void ensureBossMayAddForEveryoneWithPrivilegedRestriction() {
            assertThat(permissionsOnPrivilegedOnly(OTHER_PERSON_ID, false, false, BOSS).isAllowedToAdd()).isTrue();
        }

        @Test
        void ensureDepartmentHeadMayAddForManagedMemberWithPrivilegedRestriction() {
            assertThat(permissionsOnPrivilegedOnly(OTHER_PERSON_ID, true, false, DEPARTMENT_HEAD).isAllowedToAdd()).isTrue();
        }

        @Test
        void ensureSecondStageAuthorityMayAddForManagedMemberWithPrivilegedRestriction() {
            assertThat(permissionsOnPrivilegedOnly(OTHER_PERSON_ID, false, true, SECOND_STAGE_AUTHORITY).isAllowedToAdd()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerMayNotAddForPersonOutsideOfDepartmentWithPrivilegedRestriction(Role role) {
            assertThat(permissionsOnPrivilegedOnly(OTHER_PERSON_ID, false, false, role).isAllowedToAdd()).isFalse();
        }
    }

    @Nested
    class Edit {

        @Test
        void ensureOfficeMayEditForEveryone() {
            assertThat(permissionsOn(OTHER_PERSON_ID, false, false, OFFICE).isAllowedToEdit(overtime(UV_INTERNAL))).isTrue();
        }

        @Test
        void ensureNobodyMayEditAnExternalOvertime() {
            assertThat(permissionsOn(OTHER_PERSON_ID, false, false, OFFICE).isAllowedToEdit(overtime(EXTERNAL))).isFalse();
        }

        @Test
        void ensureEditIsAllowedWhenOvertimeSyncIsActive() {
            assertThat(permissionsOn(OTHER_PERSON_ID, false, false, OFFICE, true, true).isAllowedToEdit(overtime(UV_INTERNAL))).isTrue();
        }

        @Test
        void ensureNobodyMayEditWhenOvertimeIsNotActive() {
            assertThat(permissionsOn(OTHER_PERSON_ID, false, false, OFFICE, false, false).isAllowedToEdit(overtime(UV_INTERNAL))).isFalse();
        }

        @Test
        void ensureDepartmentHeadMayEditForManagedMemberWithPrivilegedRestriction() {
            assertThat(permissionsOnPrivilegedOnly(OTHER_PERSON_ID, true, false, DEPARTMENT_HEAD).isAllowedToEdit(overtime(UV_INTERNAL))).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerMayNotEditForPersonOutsideOfDepartmentWithPrivilegedRestriction(Role role) {
            assertThat(permissionsOnPrivilegedOnly(OTHER_PERSON_ID, false, false, role).isAllowedToEdit(overtime(UV_INTERNAL))).isFalse();
        }
    }

    @Nested
    class Comment {

        @Test
        void ensureOfficeMayCommentForEveryone() {
            assertThat(permissionsOn(OTHER_PERSON_ID, false, false, OFFICE).isAllowedToComment()).isTrue();
        }

        @Test
        void ensureCommentIsAllowedWhenOvertimeSyncIsActive() {
            assertThat(permissionsOn(OTHER_PERSON_ID, false, false, OFFICE, true, true).isAllowedToComment()).isTrue();
        }

        @Test
        void ensureNobodyMayCommentWhenOvertimeIsNotActive() {
            assertThat(permissionsOn(OTHER_PERSON_ID, false, false, OFFICE, false, false).isAllowedToComment()).isFalse();
        }

        @Test
        void ensurePersonMayCommentOwnOvertimeWithoutPrivilegedRestriction() {
            assertThat(permissionsOn(SIGNED_IN_USER_ID, false, false, USER).isAllowedToComment()).isTrue();
        }

        @Test
        void ensureDepartmentHeadMayCommentForManagedMemberWithPrivilegedRestriction() {
            assertThat(permissionsOnPrivilegedOnly(OTHER_PERSON_ID, true, false, DEPARTMENT_HEAD).isAllowedToComment()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureManagerMayNotCommentForPersonOutsideOfDepartmentWithPrivilegedRestriction(Role role) {
            assertThat(permissionsOnPrivilegedOnly(OTHER_PERSON_ID, false, false, role).isAllowedToComment()).isFalse();
        }
    }

    @Nested
    class CreateForOtherPersons {

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS"})
        void ensureOnlyOfficeMayCreateForOthersWithoutPrivilegedRestriction(Role role) {
            settings(true, false, false);
            assertThat(sut.isAllowedToCreateOvertimeForOtherPersons(person(SIGNED_IN_USER_ID, USER, role))).isFalse();
        }

        @Test
        void ensureOfficeMayCreateForOthersWithoutPrivilegedRestriction() {
            settings(true, false, false);
            assertThat(sut.isAllowedToCreateOvertimeForOtherPersons(person(SIGNED_IN_USER_ID, USER, OFFICE))).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "OFFICE"})
        void ensurePrivilegedMayCreateForOthersWithPrivilegedRestriction(Role role) {
            settings(true, false, true);
            assertThat(sut.isAllowedToCreateOvertimeForOtherPersons(person(SIGNED_IN_USER_ID, USER, role))).isTrue();
        }

        @Test
        void ensureNobodyMayCreateForOthersWhenOvertimeIsNotActive() {
            settings(false, false, true);
            assertThat(sut.isAllowedToCreateOvertimeForOtherPersons(person(SIGNED_IN_USER_ID, USER, OFFICE))).isFalse();
        }

        @Test
        void ensureNobodyMayCreateForOthersWhenOvertimeSyncIsActive() {
            settings(true, true, true);
            assertThat(sut.isAllowedToCreateOvertimeForOtherPersons(person(SIGNED_IN_USER_ID, USER, OFFICE))).isFalse();
        }
    }

    @Nested
    class CreateForAnyPerson {

        @Test
        void ensureEveryoneMayCreateOvertimeWithoutPrivilegedRestriction() {
            settings(true, false, false);
            assertThat(sut.isAllowedToCreateOvertimeForAnyPerson(person(SIGNED_IN_USER_ID, USER))).isTrue();
        }

        @Test
        void ensureUserMayNotCreateOvertimeWithPrivilegedRestriction() {
            settings(true, false, true);
            assertThat(sut.isAllowedToCreateOvertimeForAnyPerson(person(SIGNED_IN_USER_ID, USER))).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "OFFICE"})
        void ensurePrivilegedMayCreateOvertimeWithPrivilegedRestriction(Role role) {
            settings(true, false, true);
            assertThat(sut.isAllowedToCreateOvertimeForAnyPerson(person(SIGNED_IN_USER_ID, USER, role))).isTrue();
        }

        @Test
        void ensureNobodyMayCreateOvertimeWhenOvertimeIsNotActive() {
            settings(false, false, false);
            assertThat(sut.isAllowedToCreateOvertimeForAnyPerson(person(SIGNED_IN_USER_ID, USER, OFFICE))).isFalse();
        }

        @Test
        void ensureNobodyMayCreateOvertimeWhenOvertimeSyncIsActive() {
            settings(true, true, false);
            assertThat(sut.isAllowedToCreateOvertimeForAnyPerson(person(SIGNED_IN_USER_ID, USER, OFFICE))).isFalse();
        }
    }

    @Nested
    class ViewOvertimeOfAllPersons {

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
        void ensureOfficeAndBossMayViewOvertimeOfAllPersons(Role role) {
            settings(true, false, false);
            assertThat(sut.isAllowedToViewOvertimeOfAllPersons(person(SIGNED_IN_USER_ID, USER, role))).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureNobodyElseMayViewOvertimeOfAllPersons(Role role) {
            settings(true, false, false);
            assertThat(sut.isAllowedToViewOvertimeOfAllPersons(person(SIGNED_IN_USER_ID, USER, role))).isFalse();
        }

        @Test
        void ensureNobodyMayViewOvertimeOfAllPersonsWhenOvertimeIsNotActive() {
            settings(false, false, false);
            assertThat(sut.isAllowedToViewOvertimeOfAllPersons(person(SIGNED_IN_USER_ID, USER, OFFICE))).isFalse();
        }
    }

    @Nested
    class DepartmentLookups {

        @Test
        void ensureDepartmentMembershipsAndSettingsAreLookedUpOnlyOnce() {
            final Person signedInUser = person(SIGNED_IN_USER_ID, USER, DEPARTMENT_HEAD);
            final Person overtimePerson = person(OTHER_PERSON_ID, USER);

            settings(true, false, true);
            when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, overtimePerson)).thenReturn(true);
            when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, overtimePerson)).thenReturn(false);

            final OvertimePermissions permissions = sut.of(signedInUser, overtimePerson);
            permissions.isAllowedToView();
            permissions.isAllowedToAdd();
            permissions.isAllowedToEdit(overtime(UV_INTERNAL));
            permissions.isAllowedToComment();

            verify(departmentService).isDepartmentHeadAllowedToManagePerson(signedInUser, overtimePerson);
            verify(departmentService).isSecondStageAuthorityAllowedToManagePerson(signedInUser, overtimePerson);
            verify(settingsService).getSettings();
        }
    }

    private OvertimePermissions permissionsOnManagedPerson(Role role) {
        return permissionsOn(OTHER_PERSON_ID, role == DEPARTMENT_HEAD, role == SECOND_STAGE_AUTHORITY, role);
    }

    private OvertimePermissions permissionsOnUnmanagedPerson(Role role) {
        return permissionsOn(OTHER_PERSON_ID, false, false, role);
    }

    private OvertimePermissions permissionsOn(long overtimePersonId, boolean departmentHead, boolean secondStageAuthority, Role role) {
        return permissionsOn(overtimePersonId, departmentHead, secondStageAuthority, role, true, false, false);
    }

    private OvertimePermissions permissionsOn(long overtimePersonId, boolean departmentHead, boolean secondStageAuthority,
                                              Role role, boolean overtimeActive, boolean syncActive) {
        return permissionsOn(overtimePersonId, departmentHead, secondStageAuthority, role, overtimeActive, syncActive, false);
    }

    private OvertimePermissions permissionsOnPrivilegedOnly(long overtimePersonId, boolean departmentHead,
                                                            boolean secondStageAuthority, Role role) {
        return permissionsOn(overtimePersonId, departmentHead, secondStageAuthority, role, true, false, true);
    }

    private OvertimePermissions permissionsOn(long overtimePersonId, boolean departmentHead, boolean secondStageAuthority,
                                              Role role, boolean overtimeActive, boolean syncActive, boolean writePrivilegedOnly) {

        final Person signedInUser = person(SIGNED_IN_USER_ID, USER, role);
        final Person overtimePerson = overtimePersonId == SIGNED_IN_USER_ID ? signedInUser : person(overtimePersonId, USER);

        settings(overtimeActive, syncActive, writePrivilegedOnly);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, overtimePerson)).thenReturn(departmentHead);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, overtimePerson)).thenReturn(secondStageAuthority);

        return sut.of(signedInUser, overtimePerson);
    }

    private static Overtime overtime(OvertimeType type) {
        return new Overtime(new OvertimeId(1L), new PersonId(OTHER_PERSON_ID),
            new DateRange(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 1)),
            Duration.ofHours(1), type, Instant.now());
    }

    private void settings(boolean overtimeActive, boolean syncActive, boolean writePrivilegedOnly) {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(overtimeActive);
        settings.getOvertimeSettings().setOvertimeSyncActive(syncActive);
        settings.getOvertimeSettings().setOvertimeWritePrivilegedOnly(writePrivilegedOnly);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private static Person person(long id, Role... roles) {
        final Person person = new Person();
        person.setId(id);
        person.setPermissions(List.of(roles));
        return person;
    }
}
