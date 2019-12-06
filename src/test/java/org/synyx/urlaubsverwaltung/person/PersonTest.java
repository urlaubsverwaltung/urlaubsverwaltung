package org.synyx.urlaubsverwaltung.person;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;


public class PersonTest {

    @Test
    public void ensureReturnsFirstAndLastNameAsNiceName() {

        Person person = new Person("muster", "Muster", "Max", "");

        assertThat(person.getNiceName()).isEqualTo("Max Muster");
    }


    @Test
    public void ensureReturnsDummyAsNiceNameIfFirstAndLastNameAreNotSet() {

        Person person = new Person("muster", "", "", "");

        assertThat(person.getNiceName()).isEqualTo("---");

    }

    @Test
    public void ensureReturnsFirstNameAsNiceNameIfLastNameIsNotSet() {

        Person person = new Person("muster", "Muster", "", "");

        assertThat(person.getNiceName()).isEqualTo("Muster");
    }

    @Test
    public void ensureReturnsLastNameAsNiceNameIfFirstNameIsNotSet() {

        Person person = new Person("muster", "", "Max", "");

        assertThat(person.getNiceName()).isEqualTo("Max");
    }


    @Test
    public void ensureReturnsTrueIfPersonHasTheGivenRole() {

        Person person = createPerson();
        person.setPermissions(Arrays.asList(USER, BOSS));

        assertThat(person.hasRole(BOSS)).isTrue();
    }


    @Test
    public void ensureReturnsFalseIfPersonHasNotTheGivenRole() {

        Person person = createPerson();
        person.setPermissions(singletonList(USER));

        assertThat(person.hasRole(BOSS)).isFalse();
    }


    @Test
    public void ensureReturnsTrueIfPersonHasTheGivenNotificationType() {

        Person person = createPerson();
        person.setNotifications(Arrays.asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        assertThat(person.hasNotificationType(NOTIFICATION_BOSS_ALL)).isTrue();
    }


    @Test
    public void ensureReturnsFalseIfPersonHasNotTheGivenNotificationType() {

        Person person = createPerson();
        person.setNotifications(singletonList(NOTIFICATION_USER));

        assertThat(person.hasNotificationType(NOTIFICATION_BOSS_ALL)).isFalse();
    }


    @Test
    public void ensureReturnsEmptyStringAsGravatarURLIfEmailIsEmpty() {

        Person person = createPerson();
        person.setEmail(null);

        assertThat(person.getGravatarURL()).isSameAs("");
    }


    @Test
    public void ensureCanReturnGravatarURL() {

        Person person = createPerson();
        person.setEmail("muster@test.de");

        assertThat(person.getGravatarURL()).isNotEqualTo("");
        assertThat(person.getEmail()).isNotEqualTo(person.getGravatarURL());
    }


    @Test
    public void ensurePermissionsAreUnmodifiable() {

        List<Role> modifiableList = new ArrayList<>();
        modifiableList.add(USER);

        Person person = createPerson();
        person.setPermissions(modifiableList);

        try {
            person.getPermissions().add(BOSS);
            Assert.fail("Permissions should be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }
    }


    @Test
    public void ensureNotificationsAreUnmodifiable() {

        List<MailNotification> modifiableList = new ArrayList<>();
        modifiableList.add(NOTIFICATION_USER);

        Person person = createPerson();
        person.setNotifications(modifiableList);

        try {
            person.getNotifications().add(NOTIFICATION_BOSS_ALL);
            Assert.fail("Notifications should be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }
    }

    @Test
    public void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10);
        person.setPassword("Theo");
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_USER));

        final String personToString = person.toString();
        assertThat(personToString).isEqualTo("Person{id='10'}");
        assertThat(personToString).doesNotContain("Theo", "USER", "NOTIFICATION_USER");
    }
}
