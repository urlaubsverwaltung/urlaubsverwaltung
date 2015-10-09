package org.synyx.urlaubsverwaltung.core.startup;

import org.junit.Assert;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.person.Role;


/**
 * Unit test for {@link TestUser}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class TestUserTest {

    @Test
    public void ensureReturnsCorrectRolesForTestUser() {

        Role[] roles = TestUser.USER.getRoles();

        Assert.assertNotNull("Should not be null", roles);
        Assert.assertEquals("Wrong number of roles", 1, roles.length);
        Assert.assertEquals("Wrong role", Role.USER, roles[0]);
    }


    @Test
    public void ensureReturnsCorrectRolesForTestDepartmentHead() {

        Role[] roles = TestUser.DEPARTMENT_HEAD.getRoles();

        Assert.assertNotNull("Should not be null", roles);
        Assert.assertEquals("Wrong number of roles", 2, roles.length);
        Assert.assertEquals("Wrong role", Role.USER, roles[0]);
        Assert.assertEquals("Wrong role", Role.DEPARTMENT_HEAD, roles[1]);
    }


    @Test
    public void ensureReturnsCorrectRolesForTestBoss() {

        Role[] roles = TestUser.BOSS.getRoles();

        Assert.assertNotNull("Should not be null", roles);
        Assert.assertEquals("Wrong number of roles", 2, roles.length);
        Assert.assertEquals("Wrong role", Role.USER, roles[0]);
        Assert.assertEquals("Wrong role", Role.BOSS, roles[1]);
    }


    @Test
    public void ensureReturnsCorrectRolesForTestOffice() {

        Role[] roles = TestUser.OFFICE.getRoles();

        Assert.assertNotNull("Should not be null", roles);
        Assert.assertEquals("Wrong number of roles", 3, roles.length);
        Assert.assertEquals("Wrong role", Role.USER, roles[0]);
        Assert.assertEquals("Wrong role", Role.BOSS, roles[1]);
        Assert.assertEquals("Wrong role", Role.OFFICE, roles[2]);
    }
}
