package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
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

    private OvertimePermissions permissionsOnManagedPerson(Role role) {
        final Person signedInUser = person(SIGNED_IN_USER_ID, USER, role);
        final Person overtimePerson = person(OTHER_PERSON_ID, USER);
        settings(true, false, false);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, overtimePerson))
            .thenReturn(role == DEPARTMENT_HEAD);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, overtimePerson))
            .thenReturn(role == SECOND_STAGE_AUTHORITY);
        return sut.of(signedInUser, overtimePerson);
    }

    private OvertimePermissions permissionsOnUnmanagedPerson(Role role) {
        final Person signedInUser = person(SIGNED_IN_USER_ID, USER, role);
        final Person overtimePerson = person(OTHER_PERSON_ID, USER);
        settings(true, false, false);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, overtimePerson)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, overtimePerson)).thenReturn(false);
        return sut.of(signedInUser, overtimePerson);
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
