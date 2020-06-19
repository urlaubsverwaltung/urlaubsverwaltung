package org.synyx.urlaubsverwaltung.dev;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.person.Role;


/**
 * Unit test for {@link DemoUser}.
 */
public class TestUserDemo {

    @Test
    public void ensureReturnsCorrectRolesForTestUser() {

        Role[] roles = DemoUser.USER.getRoles();

        Assert.assertNotNull("Should not be null", roles);
        Assert.assertEquals("Wrong number of roles", 1, roles.length);
        Assert.assertEquals("Wrong role", Role.USER, roles[0]);
    }


    @Test
    public void ensureReturnsCorrectRolesForTestDepartmentHead() {

        Role[] roles = DemoUser.DEPARTMENT_HEAD.getRoles();

        Assert.assertNotNull("Should not be null", roles);
        Assert.assertEquals("Wrong number of roles", 2, roles.length);
        Assert.assertEquals("Wrong role", Role.USER, roles[0]);
        Assert.assertEquals("Wrong role", Role.DEPARTMENT_HEAD, roles[1]);
    }


    @Test
    public void ensureReturnsCorrectRolesForTestDepartmentHeadSecondStageAuthority() {

        Role[] roles = DemoUser.SECOND_STAGE_AUTHORITY.getRoles();

        Assert.assertNotNull("Should not be null", roles);
        Assert.assertEquals("Wrong number of roles", 2, roles.length);
        Assert.assertEquals("Wrong role", Role.USER, roles[0]);
        Assert.assertEquals("Wrong role", Role.SECOND_STAGE_AUTHORITY, roles[1]);
    }


    @Test
    public void ensureReturnsCorrectRolesForTestBoss() {

        Role[] roles = DemoUser.BOSS.getRoles();

        Assert.assertNotNull("Should not be null", roles);
        Assert.assertEquals("Wrong number of roles", 2, roles.length);
        Assert.assertEquals("Wrong role", Role.USER, roles[0]);
        Assert.assertEquals("Wrong role", Role.BOSS, roles[1]);
    }


    @Test
    public void ensureReturnsCorrectRolesForTestOffice() {

        Role[] roles = DemoUser.OFFICE.getRoles();

        Assert.assertNotNull("Should not be null", roles);
        Assert.assertEquals("Wrong number of roles", 3, roles.length);
        Assert.assertEquals("Wrong role", Role.USER, roles[0]);
        Assert.assertEquals("Wrong role", Role.BOSS, roles[1]);
        Assert.assertEquals("Wrong role", Role.OFFICE, roles[2]);
    }
}
