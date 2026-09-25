package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER;
import static org.synyx.urlaubsverwaltung.person.web.PersonNotificationsMapper.mapToMailNotifications;
import static org.synyx.urlaubsverwaltung.person.web.PersonNotificationsMapper.mapToPersonNotificationsDto;

class PersonNotificationsMapperTest {

    @Test
    void ensureOwnSickNoteNotificationsContainTheAcceptedSickNote() {

        final PersonNotificationsDto personNotificationsDto = new PersonNotificationsDto();
        personNotificationsDto.setOwnSickNoteSubmittedCreatedEditedCancelled(new PersonNotificationDto(true, true));

        assertThat(mapToMailNotifications(personNotificationsDto)).containsExactlyInAnyOrder(
            NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER,
            NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT,
            NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER,
            NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT,
            NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT
        );
    }

    @Test
    void ensureSavingTheShownNotificationsKeepsEveryNotification() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(Role.values()));
        person.setNotifications(List.of(MailNotification.values()));

        final PersonNotificationsDto personNotificationsDto = mapToPersonNotificationsDto(person, true);

        // every notification shown by a checkbox has to be saved with it again
        assertThat(mapToMailNotifications(personNotificationsDto)).containsExactlyInAnyOrder(MailNotification.values());
    }
}
