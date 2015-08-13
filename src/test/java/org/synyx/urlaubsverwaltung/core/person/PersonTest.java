package org.synyx.urlaubsverwaltung.core.person;

import org.junit.Assert;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.mail.MailNotification;
import org.synyx.urlaubsverwaltung.security.Role;

import java.util.Arrays;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonTest {

    @Test
    public void ensureReturnsFirstAndLastNameAsNiceName() {

        Person person = new Person("muster", "Muster", "Max", "");

        Assert.assertEquals("Wrong nice name", "Max Muster", person.getNiceName());
    }


    @Test
    public void ensureReturnsLoginNameAsNiceNameIfFirstAndLastNameAreNotSet() {

        Person person = new Person("muster", "", "", "");

        Assert.assertEquals("Wrong nice name", "muster", person.getNiceName());
    }


    @Test
    public void ensureReturnsTrueIfPersonHasTheGivenRole() {

        Person person = new Person();
        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Assert.assertTrue("Should return true if the person has the given role", person.hasRole(Role.BOSS));
    }


    @Test
    public void ensureReturnsFalseIfPersonHasNotTheGivenRole() {

        Person person = new Person();
        person.setPermissions(Arrays.asList(Role.USER));

        Assert.assertFalse("Should return false if the person has not the given role", person.hasRole(Role.BOSS));
    }


    @Test
    public void ensureReturnsTrueIfPersonHasTheGivenNotificationType() {

        Person person = new Person();
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS));

        Assert.assertTrue("Should return true if the person has the given notification type",
            person.hasNotificationType(MailNotification.NOTIFICATION_BOSS));
    }


    @Test
    public void ensureReturnsFalseIfPersonHasNotTheGivenNotificationType() {

        Person person = new Person();
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER));

        Assert.assertFalse("Should return false if the person has not the given notification type",
            person.hasNotificationType(MailNotification.NOTIFICATION_BOSS));
    }


    @Test
    public void ensureDepartmentHeadIsAPrivilegedUser() {

        Person departmentHead = new Person();
        departmentHead.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD));

        Assert.assertTrue("Department head should be a privileged user", departmentHead.isPrivilegedUser());
    }


    @Test
    public void ensureBossIsAPrivilegedUser() {

        Person boss = new Person();
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Assert.assertTrue("Boss should be a privileged user", boss.isPrivilegedUser());
    }


    @Test
    public void ensureOfficeIsAPrivilegedUser() {

        Person office = new Person();
        office.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        Assert.assertTrue("Office should be a privileged user", office.isPrivilegedUser());
    }


    @Test
    public void ensureUserIsNotAPrivilegedUser() {

        Person user = new Person();
        user.setPermissions(Arrays.asList(Role.USER));

        Assert.assertFalse("User should not be a privileged user", user.isPrivilegedUser());
    }


    @Test
    public void ensureInactiveUserIsNotAPrivilegedUser() {

        Person inactive = new Person();
        inactive.setPermissions(Arrays.asList(Role.INACTIVE));

        Assert.assertFalse("Inactive user should not be a privileged user", inactive.isPrivilegedUser());
    }
}
