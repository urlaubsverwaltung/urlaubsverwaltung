package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.mail.MailTemplateModelSupplier;

import java.util.Map;

@Service
class CalendarMailService {

    private static final String CALENDAR = "calendar";
    private static final String EXCEPTION = "exception";

    private final MailService mailService;

    CalendarMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * Send an email to the tool's manager if an error occurs during adding calendar event.
     *
     * @param calendarName that is used for syncing
     * @param absence      represents the absence of a person
     * @param exception    describes the error
     */
    @Async
    void sendCalendarSyncErrorNotification(String calendarName, Absence absence, String exception) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            CALENDAR, calendarName,
            "absence", absence,
            EXCEPTION, exception
        );

        final Mail mailToTechnical = Mail.builder()
            .withTechnicalRecipient(true)
            .withSubject("subject.error.calendar.sync")
            .withTemplate("calendar_error_sync", modelSupplier)
            .build();
        mailService.send(mailToTechnical);
    }

    /**
     * Send an email to the tool's manager if an error occurs during update of calendar event.
     *
     * @param calendarName that is used for syncing
     * @param absence      represents the absence of a person
     * @param eventId      unique calendar event id
     * @param exception    describes the error
     */
    @Async
    void sendCalendarUpdateErrorNotification(String calendarName, Absence absence, String eventId, String exception) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            CALENDAR, calendarName,
            "absence", absence,
            "eventId", eventId,
            EXCEPTION, exception
        );

        final Mail toTechnical = Mail.builder()
            .withTechnicalRecipient(true)
            .withSubject("subject.error.calendar.update")
            .withTemplate("calendar_error_update", modelSupplier)
            .build();
        mailService.send(toTechnical);
    }

    /**
     * Send an email to the tool's manager if an error occurs during syncing delete action to calendar.
     *
     * @param calendarName name of calendar that is used for syncing
     * @param eventId      id of event which should be deleted
     * @param exception    describes the error
     */
    @Async
    void sendCalendarDeleteErrorNotification(String calendarName, String eventId, String exception) {

        final MailTemplateModelSupplier modelSupplier = locale -> Map.of(
            CALENDAR, calendarName,
            "eventId", eventId,
            EXCEPTION, exception
        );

        final Mail toTechnical = Mail.builder()
            .withTechnicalRecipient(true)
            .withSubject("subject.error.calendar.delete")
            .withTemplate("calendar_error_delete", modelSupplier)
            .build();
        mailService.send(toTechnical);
    }
}
