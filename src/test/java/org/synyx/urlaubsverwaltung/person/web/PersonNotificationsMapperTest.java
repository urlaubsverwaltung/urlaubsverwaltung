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
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL;

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

    @Test
    void ensureExpiredRemainingVacationDaysNotificationIsVisibleAndActiveForOffice() {

        final Person office = new Person();
        office.setId(1L);
        office.setPermissions(List.of(Role.USER, Role.OFFICE));
        office.setNotifications(List.of(NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL));

        final PersonNotificationsDto personNotificationsDto = mapToPersonNotificationsDto(office, false);

        assertThat(personNotificationsDto.getRemainingVacationDaysExpiredManagementAll().isVisible()).isTrue();
        assertThat(personNotificationsDto.getRemainingVacationDaysExpiredManagementAll().isActive()).isTrue();
    }

    @Test
    void ensureExpiredRemainingVacationDaysNotificationIsNotVisibleWithoutOffice() {

        final Person user = new Person();
        user.setId(2L);
        user.setPermissions(List.of(Role.USER, Role.BOSS));
        user.setNotifications(List.of());

        final PersonNotificationsDto personNotificationsDto = mapToPersonNotificationsDto(user, false);

        assertThat(personNotificationsDto.getRemainingVacationDaysExpiredManagementAll().isVisible()).isFalse();
    }

    @Test
    void ensureActiveExpiredRemainingVacationDaysNotificationIsSaved() {

        final PersonNotificationsDto personNotificationsDto = new PersonNotificationsDto();
        personNotificationsDto.setRemainingVacationDaysExpiredManagementAll(new PersonNotificationDto(true, true));

        assertThat(mapToMailNotifications(personNotificationsDto)).containsExactly(NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL);
    }
}
