package org.synyx.urlaubsverwaltung.dev;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Role;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link DemoUser}.
 */
class TestUserDemo {

    @Test
    void ensureReturnsCorrectRolesForTestUser() {

        Role[] roles = DemoUser.USER.getRoles();

        assertThat(roles).hasSize(1);
        assertThat(roles[0]).isEqualTo(Role.USER);
    }


    @Test
    void ensureReturnsCorrectRolesForTestDepartmentHead() {

        Role[] roles = DemoUser.DEPARTMENT_HEAD.getRoles();

        assertThat(roles).hasSize(2);
        assertThat(roles[0]).isEqualTo(Role.USER);
        assertThat(roles[1]).isEqualTo(Role.DEPARTMENT_HEAD);
    }


    @Test
    void ensureReturnsCorrectRolesForTestDepartmentHeadSecondStageAuthority() {

        Role[] roles = DemoUser.SECOND_STAGE_AUTHORITY.getRoles();

        assertThat(roles).hasSize(2);
        assertThat(roles[0]).isEqualTo(Role.USER);
        assertThat(roles[1]).isEqualTo(Role.SECOND_STAGE_AUTHORITY);
    }


    @Test
    void ensureReturnsCorrectRolesForTestBoss() {

        Role[] roles = DemoUser.BOSS.getRoles();

        assertThat(roles).hasSize(2);
        assertThat(roles[0]).isEqualTo(Role.USER);
        assertThat(roles[1]).isEqualTo(Role.BOSS);
    }


    @Test
    void ensureReturnsCorrectRolesForTestOffice() {

        Role[] roles = DemoUser.OFFICE.getRoles();

        assertThat(roles).hasSize(3);
        assertThat(roles[0]).isEqualTo(Role.USER);
        assertThat(roles[1]).isEqualTo(Role.BOSS);
        assertThat(roles[2]).isEqualTo(Role.OFFICE);
    }
}
