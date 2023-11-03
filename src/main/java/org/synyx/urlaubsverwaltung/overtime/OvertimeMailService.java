package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailRecipientService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.mail.MailTemplateModelSupplier;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;
import java.util.Map;

import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED;

@Service
class OvertimeMailService {

    private final MailService mailService;
    private final MailRecipientService mailRecipientService;

    OvertimeMailService(MailService mailService, MailRecipientService mailRecipientService) {
        this.mailService = mailService;
        this.mailRecipientService = mailRecipientService;
    }

    void sendOvertimeNotificationToApplicantFromApplicant(Overtime overtime, OvertimeComment overtimeComment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            "overtime", overtime,
            "overtimeDurationHours", overtime.getDuration().toHours() + " Std.",
            "overtimeDurationMinutes", overtime.getDuration().toMinutesPart() + " Min.",
            "comment", overtimeComment
        );

        final Mail toPerson = Mail.builder()
            .withRecipient(overtime.getPerson(), NOTIFICATION_EMAIL_OVERTIME_APPLIED)
            .withSubject("subject.overtime.created.applicant")
            .withTemplate("overtime_to_applicant_from_applicant", modelSupplier)
            .build();
        mailService.send(toPerson);
    }

    void sendOvertimeNotificationToApplicantFromManagement(Overtime overtime, OvertimeComment overtimeComment, Person author) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            "overtime", overtime,
            "overtimeDurationHours", overtime.getDuration().toHours() + " Std.",
            "overtimeDurationMinutes", overtime.getDuration().toMinutesPart() + " Min.",
            "comment", overtimeComment,
            "author", author
        );

        final Mail toPerson = Mail.builder()
            .withRecipient(overtime.getPerson(), NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT)
            .withSubject("subject.overtime.created.applicant_from_management")
            .withTemplate("overtime_to_applicant_from_management", modelSupplier)
            .build();
        mailService.send(toPerson);
    }

    void sendOvertimeNotificationToManagement(Overtime overtime, OvertimeComment overtimeComment) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            "overtime", overtime,
            "overtimeDurationHours", overtime.getDuration().toHours() + " Std.",
            "overtimeDurationMinutes", overtime.getDuration().toMinutesPart() + " Min.",
            "comment", overtimeComment
        );

        // send overtime to all interested managers
        final List<Person> relevantRecipientsToInform = mailRecipientService.getRecipientsOfInterest(overtime.getPerson(), NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED);
        final Mail mailToRelevantPersons = Mail.builder()
            .withRecipient(relevantRecipientsToInform)
            .withSubject("subject.overtime.created.management", overtime.getPerson().getNiceName())
            .withTemplate("overtime_to_management", modelSupplier)
            .build();
        mailService.send(mailToRelevantPersons);
    }
}
