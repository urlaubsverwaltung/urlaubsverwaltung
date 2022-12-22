package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
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
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));
        assertThat(person.hasNotificationType(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).isTrue();
    }

    @Test
    void ensureReturnsFalseIfPersonHasNotTheGivenNotificationType() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));
        assertThat(person.hasNotificationType(NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED)).isFalse();
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
        modifiableList.add(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setNotifications(modifiableList);

        final Collection<MailNotification> notifications = person.getNotifications();
        assertThatThrownBy(() -> notifications.add(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void isPrivilegedAsUserIsFalse() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setPermissions(List.of(USER));

        assertThat(person.isPrivileged()).isFalse();
    }

    @Test
    void isPrivilegedAsBossIsFalse() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setPermissions(List.of(BOSS));

        assertThat(person.isPrivileged()).isTrue();
    }

    @Test
    void ensureIsInactiveReturnsTrueWhenPersonHasRoleInactive() {
        final Person person = new Person();
        person.setPermissions(List.of(INACTIVE));

        assertThat(person.isInactive()).isTrue();
    }

    @Test
    void ensureIsInactiveReturnsFalseWhenPersonDoesNotHaveRoleInactive() {
        final Person person = new Person();
        person.setPermissions(List.of());

        assertThat(person.isInactive()).isFalse();
    }

    @Test
    void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10L);
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final String personToString = person.toString();
        assertThat(personToString)
            .isEqualTo("Person{id='10'}")
            .doesNotContain("Theo", "USER", "NOTIFICATION_APPLICATION_MANAGEMENT_ALL");
    }

    @Test
    void equals() {
        final Person personOne = new Person();
        personOne.setId(1L);

        final Person personOneOne = new Person();
        personOneOne.setId(1L);

        final Person personTwo = new Person();
        personTwo.setId(2L);

        assertThat(personOne)
            .isEqualTo(personOne)
            .isEqualTo(personOneOne)
            .isNotEqualTo(personTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void ensureHasAnyRoleIsTrue() {
        final Person personOne = new Person();
        personOne.setPermissions(List.of(DEPARTMENT_HEAD, BOSS));
        personOne.setId(1L);

        assertThat(personOne.hasAnyRole(DEPARTMENT_HEAD)).isTrue();
    }

    @Test
    void ensureHasAnyRoleFalse() {
        final Person personOne = new Person();
        personOne.setPermissions(List.of(SECOND_STAGE_AUTHORITY, OFFICE));
        personOne.setId(1L);

        assertThat(personOne.hasAnyRole(DEPARTMENT_HEAD)).isFalse();
    }

    @Test
    void hashCodeTest() {
        final Person personOne = new Person();
        personOne.setId(1L);

        assertThat(personOne.hashCode()).isEqualTo(32);
    }
}
