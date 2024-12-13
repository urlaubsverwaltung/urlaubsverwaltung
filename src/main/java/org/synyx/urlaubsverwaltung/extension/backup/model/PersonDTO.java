package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

public record PersonDTO(Long id, String externalId, String firstName, String lastName, String email, boolean active,
                        List<RoleDTO> permissions, List<MailNotificationDTO> mailNotification,
                        PersonBaseDataDTO personBaseData, List<AccountDTO> accounts,
                        List<WorkingTimeDTO> workingTimes,
                        UserSettingsDTO userSettings) {


    public Person toPerson() {
        Person person = new Person();
        person.setUsername(externalId);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmail(email);
        person.setPermissions(permissions.stream().map(RoleDTO::toRole).toList());
        person.setNotifications(mailNotification.stream().map(MailNotificationDTO::toMailNotification).toList());
        return person;
    }
}
