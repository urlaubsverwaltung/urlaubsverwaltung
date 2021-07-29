package org.synyx.urlaubsverwaltung.sicknote;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettingsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;

@Service
public class SickNoteMailService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SickNoteSettingsService sickNoteSettingsService;
    private final SickNoteService sickNoteService;
    private final MailService mailService;

    @Autowired
    public SickNoteMailService(SickNoteSettingsService sickNoteSettingsService, SickNoteService sickNoteService, MailService mailService) {
        this.sickNoteSettingsService = sickNoteSettingsService;
        this.sickNoteService = sickNoteService;
        this.mailService = mailService;
    }

    /**
     * Sends mail to person and office if sick pay (gesetzliche Lohnfortzahlung im Krankheitsfall) is about to end.
     */
    void sendEndOfSickPayNotification() {

        final List<SickNote> sickNotes = sickNoteService.getSickNotesReachingEndOfSickPay();

        LOG.info("Found {} sick notes reaching end of sick pay", sickNotes.size());

        final String subjectMessageKey = "subject.sicknote.endOfSickPay";
        final String templateName = "sicknote_end_of_sick_pay";
        final Integer maximumSickPayDays = sickNoteSettingsService.getSettings().getMaximumSickPayDays();

        for (SickNote sickNote : sickNotes) {

            Map<String, Object> model = new HashMap<>();
            model.put("maximumSickPayDays", maximumSickPayDays);
            model.put("sickNote", sickNote);

            final Mail toSickNotePerson = Mail.builder()
                .withRecipient(sickNote.getPerson())
                .withSubject(subjectMessageKey)
                .withTemplate(templateName, model)
                .build();
            mailService.send(toSickNotePerson);

            final Mail toOffice = Mail.builder()
                .withRecipient(NOTIFICATION_OFFICE)
                .withSubject(subjectMessageKey)
                .withTemplate(templateName, model)
                .build();
            mailService.send(toOffice);
            sickNoteService.setEndOfSickPayNotificationSend(sickNote);
        }
    }
}
