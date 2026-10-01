package org.synyx.urlaubsverwaltung.person.extension;


import de.focus_shift.urlaubsverwaltung.extension.api.person.MailNotificationDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.RoleDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonUpdate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
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
            person.setPermissions(Set.of());
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

        @Test
        void personWithoutUserRoleIsDisabled() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
            person.setId(1L);
            person.setPermissions(Set.of(OFFICE));

            final PersonDTO dto = PersonDTOMapper.toPersonDTO(person);

            assertThat(dto.permissions()).containsOnly(RoleDTO.OFFICE);
            assertThat(dto.enabled()).isFalse();
        }
    }

    @Nested
    class ToPersonUpdate {

        @Test
        void enabledPersonKeepsPermissions() {

            final PersonUpdate personUpdate = PersonDTOMapper.toPersonUpdate(personDTO(true, Set.of(RoleDTO.USER, RoleDTO.OFFICE)));

            assertThat(personUpdate.permissions()).hasValueSatisfying(roles -> assertThat(roles).containsExactlyInAnyOrder(USER, OFFICE));
        }

        @Test
        void enabledPersonWithoutUserRoleGetsUserRole() {

            final PersonUpdate personUpdate = PersonDTOMapper.toPersonUpdate(personDTO(true, Set.of(RoleDTO.OFFICE)));

            assertThat(personUpdate.permissions()).hasValueSatisfying(roles -> assertThat(roles).containsExactlyInAnyOrder(USER, OFFICE));
        }

        @Test
        void disabledPersonLosesAllPermissions() {

            final PersonUpdate personUpdate = PersonDTOMapper.toPersonUpdate(personDTO(false, Set.of(RoleDTO.USER, RoleDTO.OFFICE)));

            assertThat(personUpdate.permissions()).hasValueSatisfying(roles -> assertThat(roles).isEmpty());
        }

        private PersonDTO personDTO(boolean enabled, Set<RoleDTO> permissions) {
            return PersonDTO.builder()
                .id(1L)
                .username("muster")
                .lastName("Muster")
                .firstName("Marlene")
                .email("muster@example.org")
                .enabled(enabled)
                .permissions(permissions)
                .notifications(Set.of())
                .build();
        }
    }

}
