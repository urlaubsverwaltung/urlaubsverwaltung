package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL;

@Service
public class PersonMailService {

    private final MailService mailService;
    private final PersonService personService;

    PersonMailService(MailService mailService, PersonService personService) {
        this.mailService = mailService;
        this.personService = personService;
    }

    @EventListener
    public void sendPersonCreationNotification(PersonCreatedEvent event) {

        final Map<String, Object> model = new HashMap<>();
        model.put("personId", event.getPersonId());
        model.put("personNiceName", event.getPersonNiceName());

        final Mail toOffice = Mail.builder()
            .withRecipient(personService.getActivePersonsWithNotificationType(NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL))
            .withSubject("subject.person.created")
            .withTemplate("person_created_office", locale -> model)
            .build();

        mailService.send(toOffice);
    }

    public void sendPersonGainedMorePermissionsNotification(Person person, List<PersonPermissionsRoleDto> addedPermissions) {

        final Map<String, Object> model = Map.of(
            "person", person,
            "addedPermissions", addedPermissions
        );

        final Mail toPerson = Mail.builder()
            .withRecipient(person)
            .withSubject("subject.person.gained-permissions")
            .withTemplate("person_gained_permissions", locale -> model)
            .build();
        mailService.send(toPerson);
    }
}
