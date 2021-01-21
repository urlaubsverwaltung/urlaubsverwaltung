package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.overtime.Overtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


class PersonTest {

    @Test
    void ensureReturnsFirstAndLastNameAsNiceName() {

        Person person = new Person("muster", "Muster", "Max", "");
        assertThat(person.getNiceName()).isEqualTo("Max Muster");
    }

    @Test
    void ensureReturnsDummyAsNiceNameIfFirstAndLastNameAreNotSet() {

        Person person = new Person("muster", "", "", "");
        assertThat(person.getNiceName()).isEqualTo("---");
    }

    @Test
    void ensureReturnsFirstNameAsNiceNameIfLastNameIsNotSet() {

        Person person = new Person("muster", "Muster", "", "");
        assertThat(person.getNiceName()).isEqualTo("Muster");
    }

    @Test
    void ensureReturnsLastNameAsNiceNameIfFirstNameIsNotSet() {

        Person person = new Person("muster", "", "Max", "");
        assertThat(person.getNiceName()).isEqualTo("Max");
    }

    @Test
    void ensureReturnsTrueIfPersonHasTheGivenRole() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(Arrays.asList(USER, BOSS));
        assertThat(person.hasRole(BOSS)).isTrue();
    }

    @Test
    void ensureReturnsFalseIfPersonHasNotTheGivenRole() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(singletonList(USER));
        assertThat(person.hasRole(BOSS)).isFalse();
    }

    @Test
    void ensureReturnsTrueIfPersonHasTheGivenNotificationType() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setNotifications(Arrays.asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));
        assertThat(person.hasNotificationType(NOTIFICATION_BOSS_ALL)).isTrue();
    }

    @Test
    void ensureReturnsFalseIfPersonHasNotTheGivenNotificationType() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setNotifications(singletonList(NOTIFICATION_USER));
        assertThat(person.hasNotificationType(NOTIFICATION_BOSS_ALL)).isFalse();
    }

    @Test
    void ensureReturnsEmptyStringAsGravatarURLIfEmailIsEmpty() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setEmail(null);
        assertThat(person.getGravatarURL()).isSameAs("");
    }

    @Test
    void ensureCanReturnGravatarURL() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setEmail("muster@example.org");
        assertThat(person.getGravatarURL()).isNotEqualTo("");
        assertThat(person.getEmail()).isNotEqualTo(person.getGravatarURL());
    }

    @Test
    void ensurePermissionsAreUnmodifiable() {

        List<Role> modifiableList = new ArrayList<>();
        modifiableList.add(USER);

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(modifiableList);

        final Collection<Role> permissions = person.getPermissions();
        assertThatThrownBy(() -> permissions.add(BOSS)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void ensureNotificationsAreUnmodifiable() {

        List<MailNotification> modifiableList = new ArrayList<>();
        modifiableList.add(NOTIFICATION_USER);

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setNotifications(modifiableList);

        final Collection<MailNotification> notifications = person.getNotifications();
        assertThatThrownBy(() -> notifications.add(NOTIFICATION_BOSS_ALL)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10);
        person.setPassword("Theo");
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_USER));

        final String personToString = person.toString();
        assertThat(personToString)
            .isEqualTo("Person{id='10'}")
            .doesNotContain("Theo", "USER", "NOTIFICATION_USER");
    }

    @Test
    void equals() {
        final Person personOne = new Person();
        personOne.setId(1);

        final Person personOneOne = new Person();
        personOneOne.setId(1);

        final Person personTwo = new Person();
        personTwo.setId(2);

        assertThat(personOne)
            .isEqualTo(personOne)
            .isEqualTo(personOneOne)
            .isNotEqualTo(personTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final Person personOne = new Person();
        personOne.setId(1);

        assertThat(personOne.hashCode()).isEqualTo(32);
    }
}
