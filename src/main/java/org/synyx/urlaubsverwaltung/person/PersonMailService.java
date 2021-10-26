package org.synyx.urlaubsverwaltung.person;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;

import java.util.HashMap;
import java.util.Map;

import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;

@Service
class PersonMailService {

    private final MailService mailService;

    PersonMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @EventListener
    public void sendPersonCreationNotification(PersonCreatedEvent event) {

        final Map<String, Object> model = new HashMap<>();
        model.put("personId", event.getPersonId());
        model.put("personNiceName", event.getPersonNiceName());

        final Mail toOffice = Mail.builder()
            .withRecipient(NOTIFICATION_OFFICE)
            .withSubject("subject.person.created")
            .withTemplate("person_created_office", model)
            .build();

        mailService.send(toOffice);
    }
}
