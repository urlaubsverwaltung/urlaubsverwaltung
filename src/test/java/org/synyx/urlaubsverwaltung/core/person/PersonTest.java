package org.synyx.urlaubsverwaltung.core.person;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.core.mail.MailNotification;
import org.synyx.urlaubsverwaltung.security.Role;

import java.util.Arrays;

/**
 * @author Aljona Murygina - murygina@synyx.de
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

        Assert.assertTrue("Should return true if the person has the given notification type", person.hasNotificationType(MailNotification.NOTIFICATION_BOSS));

    }

    @Test
    public void ensureReturnsFalseIfPersonHasNotTheGivenNotificationType() {

        Person person = new Person();
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER));

        Assert.assertFalse("Should return false if the person has not the given notification type", person.hasNotificationType(MailNotification.NOTIFICATION_BOSS));

    }
}
