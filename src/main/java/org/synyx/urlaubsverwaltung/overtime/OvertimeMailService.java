package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;
import static org.synyx.urlaubsverwaltung.person.MailNotification.OVERTIME_NOTIFICATION_OFFICE;

@Service
class OvertimeMailService {

    private final MailService mailService;
    private final PersonService personService;

    OvertimeMailService(MailService mailService, PersonService personService) {
        this.mailService = mailService;
        this.personService = personService;
    }

    void sendOvertimeNotification(Overtime overtime, OvertimeComment overtimeComment) {

        final Map<String, Object> model = new HashMap<>();
        model.put("overtime", overtime);
        model.put("overtimeDurationHours", overtime.getDuration().toHours() + " Std.");
        model.put("overtimeDurationMinutes", overtime.getDuration().toMinutesPart() + " Min.");

        model.put("comment", overtimeComment);

        final Mail toOffice = Mail.builder()
            .withRecipient(personService.findRecipients(OVERTIME_NOTIFICATION_OFFICE))
            .withSubject("subject.overtime.created")
            .withTemplate("overtime_office", model)
            .build();

        mailService.send(toOffice);
    }
}
