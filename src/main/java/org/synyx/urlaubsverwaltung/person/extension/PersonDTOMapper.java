package org.synyx.urlaubsverwaltung.person.extension;

import de.focus_shift.urlaubsverwaltung.extension.api.person.MailNotificationDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.RoleDTO;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Set;
import java.util.stream.Collectors;

class PersonDTOMapper {

    private PersonDTOMapper() {
        // Utility classes should not have public constructors java:S1118
    }

    static Person toPerson(PersonDTO personDTO) {
        final Person person = new Person(personDTO.getUsername(), personDTO.getLastName(), personDTO.getFirstName(), personDTO.getEmail());
        person.setId(personDTO.getId());
        person.setPermissions(toRoles(personDTO));
        person.setNotifications(toMailNotifications(personDTO));
        return person;
    }

    static PersonDTO toPersonDTO(Person person) {
        return PersonDTO.builder()
            .id(person.getId())
            .username(person.getUsername())
            .lastName(person.getLastName())
            .firstName(person.getFirstName())
            .email(person.getEmail())
            .enabled(person.isActive())
            .permissions(toRoleDTOs(person))
            .notifications(toMailNotificationDTOs(person))
            .build();
    }

    private static Set<MailNotification> toMailNotifications(PersonDTO personDTO) {
        return personDTO.getNotifications().stream().map(dto -> MailNotification.valueOf(dto.name())).collect(Collectors.toSet());
    }

    private static Set<MailNotificationDTO> toMailNotificationDTOs(Person person) {
        return person.getNotifications().stream().map(domain -> MailNotificationDTO.valueOf(domain.name())).collect(Collectors.toSet());
    }

    static Set<Role> toRoles(PersonDTO personDTO) {
        if (!personDTO.isEnabled()) {
            return Set.of(Role.INACTIVE);
        }
        return personDTO.getPermissions()
            .stream()
            .map(roleDTO -> Role.valueOf(roleDTO.name()))
            .collect(Collectors.toSet());
    }

    private static Set<RoleDTO> toRoleDTOs(Person person) {
        return person.getPermissions()
            .stream()
            .filter(role -> !Role.INACTIVE.equals(role))
            .map(role -> RoleDTO.valueOf(role.name()))
            .collect(Collectors.toSet());
    }
}
