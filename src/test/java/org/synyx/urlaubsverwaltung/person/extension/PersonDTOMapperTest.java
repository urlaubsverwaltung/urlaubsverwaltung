package org.synyx.urlaubsverwaltung.person.extension;


import de.focus_shift.urlaubsverwaltung.extension.api.person.MailNotificationDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.RoleDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

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
            assertThat(dto.getId()).isEqualTo(1);
            assertThat(dto.getUsername()).isEqualTo("muster");
            assertThat(dto.getLastName()).isEqualTo("Muster");
            assertThat(dto.getFirstName()).isEqualTo("Marlene");
            assertThat(dto.getEmail()).isEqualTo("muster@example.org");
            assertThat(dto.getPermissions()).containsOnly(RoleDTO.USER);
            assertThat(dto.getNotifications()).containsOnly(MailNotificationDTO.NOTIFICATION_EMAIL_APPLICATION_ALLOWED);
            assertThat(dto.isEnabled()).isTrue();
        }

        @Test
        void disabledPersonDTO() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
            person.setId(1L);
            person.setPermissions(Set.of(INACTIVE));
            person.setNotifications(Set.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

            final PersonDTO dto = PersonDTOMapper.toPersonDTO(person);

            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(1);
            assertThat(dto.getUsername()).isEqualTo("muster");
            assertThat(dto.getLastName()).isEqualTo("Muster");
            assertThat(dto.getFirstName()).isEqualTo("Marlene");
            assertThat(dto.getEmail()).isEqualTo("muster@example.org");
            assertThat(dto.getPermissions()).isEmpty();
            assertThat(dto.getNotifications()).containsOnly(MailNotificationDTO.NOTIFICATION_EMAIL_APPLICATION_ALLOWED);
            assertThat(dto.isEnabled()).isFalse();
        }
    }

    @Nested
    class ToPerson {
        @Test
        void activePerson() {

            final PersonDTO personDTO = PersonDTO.builder()
                .id(1L)
                .username("muster")
                .lastName("Muster")
                .firstName("Marlene")
                .email("muster@example.org")
                .enabled(true)
                .permissions(Set.of(RoleDTO.USER))
                .notifications(Set.of(MailNotificationDTO.NOTIFICATION_EMAIL_APPLICATION_ALLOWED))
                .build();

            final Person person = PersonDTOMapper.toPerson(personDTO);

            assertThat(person).isNotNull();
            assertThat(person.getId()).isEqualTo(1);
            assertThat(person.getUsername()).isEqualTo("muster");
            assertThat(person.getLastName()).isEqualTo("Muster");
            assertThat(person.getFirstName()).isEqualTo("Marlene");
            assertThat(person.getEmail()).isEqualTo("muster@example.org");
            assertThat(person.getPermissions()).containsOnly(Role.USER);
            assertThat(person.getNotifications()).containsOnly(NOTIFICATION_EMAIL_APPLICATION_ALLOWED);
            assertThat(person.isActive()).isTrue();
        }

        @Test
        void disabledPerson() {

            final PersonDTO personDTO = PersonDTO.builder()
                .id(1L)
                .username("muster")
                .lastName("Muster")
                .firstName("Marlene")
                .email("muster@example.org")
                .permissions(Set.of())
                .notifications(Set.of(MailNotificationDTO.NOTIFICATION_EMAIL_APPLICATION_ALLOWED))
                .build()
                .disable();

            final Person person = PersonDTOMapper.toPerson(personDTO);

            assertThat(person).isNotNull();
            assertThat(person.getId()).isEqualTo(1);
            assertThat(person.getUsername()).isEqualTo("muster");
            assertThat(person.getLastName()).isEqualTo("Muster");
            assertThat(person.getFirstName()).isEqualTo("Marlene");
            assertThat(person.getEmail()).isEqualTo("muster@example.org");
            assertThat(person.getPermissions()).containsOnly(INACTIVE);
            assertThat(person.getNotifications()).containsOnly(NOTIFICATION_EMAIL_APPLICATION_ALLOWED);
            assertThat(person.isActive()).isFalse();
        }
    }

}
