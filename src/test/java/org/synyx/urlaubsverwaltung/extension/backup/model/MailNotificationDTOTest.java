package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.person.MailNotification;

import static org.assertj.core.api.Assertions.assertThat;

class MailNotificationDTOTest {

    @ParameterizedTest
    @EnumSource(MailNotificationDTO.class)
    void mailNotificationDTOEnumMatchesMailNotification(MailNotificationDTO mailNotificationDTO) {
        MailNotification mailNotification = mailNotificationDTO.toMailNotification();
        assertThat(mailNotification).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(MailNotification.class)
    void mailNotificationDTOEnumMatchesMailNotification(MailNotification mailNotification) {
        MailNotificationDTO mailNotificationDTO = MailNotificationDTO.valueOf(mailNotification.name());
        assertThat(mailNotificationDTO).isNotNull();
    }
}
