package org.synyx.urlaubsverwaltung.person.extension;

import de.focus_shift.urlaubsverwaltung.extension.api.person.MailNotificationDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.RoleDTO;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonUpdate;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;

final class PersonDTOMapper {

    private static final Set<String> NOTIFICATIONS_OF_THE_EXTENSION_API = Arrays.stream(MailNotificationDTO.values())
        .map(Enum::name)
        .collect(Collectors.toUnmodifiableSet());

    private PersonDTOMapper() {
        // Utility classes should not have public constructors java:S1118
    }

    /**
     * @param personDTO                            the person as sent by the extension
     * @param notificationsUnknownToTheExtensionApi notifications of the person the extension cannot express and must
     *                                             therefore keep, see {@link #isKnownToTheExtensionApi(MailNotification)}
     */
    static PersonUpdate toPersonUpdate(PersonDTO personDTO, Collection<MailNotification> notificationsUnknownToTheExtensionApi) {

        final Set<MailNotification> notifications = new HashSet<>(toMailNotifications(personDTO));
        notifications.addAll(notificationsUnknownToTheExtensionApi);

        return PersonUpdate
            .ofPersonalData(personDTO.username(), personDTO.firstName(), personDTO.lastName(), personDTO.email())
            .withPermissions(toRoles(personDTO))
            .withNotifications(notifications);
    }

    /**
     * The extension api mirrors {@link MailNotification} in its own release cycle, so a new notification may not be
     * part of it yet.
     */
    static boolean isKnownToTheExtensionApi(MailNotification notification) {
        return NOTIFICATIONS_OF_THE_EXTENSION_API.contains(notification.name());
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
        return personDTO.notifications().stream().map(dto -> MailNotification.valueOf(dto.name())).collect(Collectors.toSet());
    }

    private static Set<MailNotificationDTO> toMailNotificationDTOs(Person person) {
        return person.getNotifications().stream()
            .filter(PersonDTOMapper::isKnownToTheExtensionApi)
            .map(domain -> MailNotificationDTO.valueOf(domain.name()))
            .collect(Collectors.toSet());
    }

    static Set<Role> toRoles(PersonDTO personDTO) {
        if (FALSE.equals(personDTO.enabled())) {
            return Set.of();
        }
        return Stream.concat(
                personDTO.permissions().stream().map(roleDTO -> Role.valueOf(roleDTO.name())),
                Stream.of(Role.USER)
            )
            .collect(Collectors.toSet());
    }

    private static Set<RoleDTO> toRoleDTOs(Person person) {
        return person.getPermissions()
            .stream()
            .map(role -> RoleDTO.valueOf(role.name()))
            .collect(Collectors.toSet());
    }
}
