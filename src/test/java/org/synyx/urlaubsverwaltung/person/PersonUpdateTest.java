package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

class PersonUpdateTest {

    @Test
    void ensureOfPersonalDataOnlyDescribesPersonalData() {

        final PersonUpdate personUpdate = PersonUpdate.ofPersonalData("muster", "Marlene", "Muster", "muster@example.org");

        assertThat(personUpdate.personalData())
            .hasValue(new PersonUpdate.PersonalData("muster", "Marlene", "Muster", "muster@example.org"));
        assertThat(personUpdate.permissions()).isEmpty();
        assertThat(personUpdate.notifications()).isEmpty();
    }

    @Test
    void ensureOfPermissionsOnlyDescribesPermissions() {

        final PersonUpdate personUpdate = PersonUpdate.ofPermissions(List.of(USER, OFFICE));

        assertThat(personUpdate.permissions()).hasValue(List.of(USER, OFFICE));
        assertThat(personUpdate.personalData()).isEmpty();
        assertThat(personUpdate.notifications()).isEmpty();
    }

    @Test
    void ensureOfNotificationsOnlyDescribesNotifications() {

        final PersonUpdate personUpdate = PersonUpdate.ofNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

        assertThat(personUpdate.notifications()).hasValue(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        assertThat(personUpdate.personalData()).isEmpty();
        assertThat(personUpdate.permissions()).isEmpty();
    }

    @Test
    void ensureAspectsCanBeCombined() {

        final PersonUpdate personUpdate = PersonUpdate
            .ofPersonalData("muster", "Marlene", "Muster", "muster@example.org")
            .withPermissions(List.of(USER))
            .withNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

        assertThat(personUpdate.personalData())
            .hasValue(new PersonUpdate.PersonalData("muster", "Marlene", "Muster", "muster@example.org"));
        assertThat(personUpdate.permissions()).hasValue(List.of(USER));
        assertThat(personUpdate.notifications()).hasValue(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
    }

    @Test
    void ensureWithersDoNotModifyTheGivenPersonUpdate() {

        final PersonUpdate personUpdate = PersonUpdate.ofPermissions(List.of(USER));

        personUpdate.withNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        personUpdate.withPersonalData("muster", "Marlene", "Muster", "muster@example.org");

        assertThat(personUpdate.notifications()).isEmpty();
        assertThat(personUpdate.personalData()).isEmpty();
    }

    @Test
    void ensureEqualsAndHashCode() {

        final PersonUpdate personUpdate = PersonUpdate.ofPermissions(List.of(USER));

        assertThat(personUpdate)
            .isEqualTo(PersonUpdate.ofPermissions(List.of(USER)))
            .hasSameHashCodeAs(PersonUpdate.ofPermissions(List.of(USER)))
            .isNotEqualTo(PersonUpdate.ofPermissions(List.of(OFFICE)))
            .isNotEqualTo(PersonUpdate.ofNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED)));
    }
}
