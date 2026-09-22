package org.synyx.urlaubsverwaltung.person.extension;


import de.focus_shift.urlaubsverwaltung.extension.api.person.MailNotificationDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.RoleDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

class PersonDTOMapperTest {

    @Nested
    class ToDTO {
        @Test
        void enabledPersonDTO() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
            person.setId(1L);
            person.setPermissions(Set.of(USER));
            person.setNotifications(Set.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

            final PersonDTO dto = PersonDTOMapper.toPersonDTO(person);

            assertThat(dto).isNotNull();
            assertThat(dto.id()).isEqualTo(1);
            assertThat(dto.username()).isEqualTo("muster");
            assertThat(dto.lastName()).isEqualTo("Muster");
            assertThat(dto.firstName()).isEqualTo("Marlene");
            assertThat(dto.email()).isEqualTo("muster@example.org");
            assertThat(dto.permissions()).containsOnly(RoleDTO.USER);
            assertThat(dto.notifications()).containsOnly(MailNotificationDTO.NOTIFICATION_EMAIL_APPLICATION_ALLOWED);
            assertThat(dto.enabled()).isTrue();
        }

        @Test
        void disabledPersonDTO() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
            person.setId(1L);
            person.setPermissions(Set.of(INACTIVE));
            person.setNotifications(Set.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

            final PersonDTO dto = PersonDTOMapper.toPersonDTO(person);

            assertThat(dto).isNotNull();
            assertThat(dto.id()).isEqualTo(1);
            assertThat(dto.username()).isEqualTo("muster");
            assertThat(dto.lastName()).isEqualTo("Muster");
            assertThat(dto.firstName()).isEqualTo("Marlene");
            assertThat(dto.email()).isEqualTo("muster@example.org");
            assertThat(dto.permissions()).isEmpty();
            assertThat(dto.notifications()).containsOnly(MailNotificationDTO.NOTIFICATION_EMAIL_APPLICATION_ALLOWED);
            assertThat(dto.enabled()).isFalse();
        }
    }

}
