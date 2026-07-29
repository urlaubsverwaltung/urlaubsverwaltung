package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.extension.backup.model.MailNotificationDTO.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;

class PersonDTOTest {

    @Test
    void personDTOHandlesValidData() {
        List<RoleDTO> roles = List.of(RoleDTO.USER);
        List<MailNotificationDTO> notifications = List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        PersonBaseDataDTO baseData = new PersonBaseDataDTO("12345", "Some info");
        List<AccountDTO> accounts = List.of();
        List<WorkingTimeDTO> workingTimes = List.of();
        UserSettingsDTO userSettings = new UserSettingsDTO(null, null, null, null, null);
        final Instant createdAt = Instant.parse("2021-01-01T00:00:00Z");

        PersonDTO dto = new PersonDTO(1L, "externalId", "John", "Doe", "john.doe@example.com", true, createdAt, roles, notifications, baseData, accounts, workingTimes, userSettings);

        final Person person = dto.toPerson();

        assertThat(person.getUsername()).isEqualTo(dto.externalId());
        assertThat(person.getFirstName()).isEqualTo(dto.firstName());
        assertThat(person.getLastName()).isEqualTo(dto.lastName());
        assertThat(person.getEmail()).isEqualTo(dto.email());
        assertThat(person.getCreatedAt()).isEqualTo(createdAt);
        assertThat(person.getPermissions()).containsOnly(Role.USER);
        assertThat(person.getNotifications()).containsOnly(MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
    }
}
