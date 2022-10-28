package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;

@Service
class SickNoteMailService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SettingsService settingsService;
    private final SickNoteService sickNoteService;
    private final MailService mailService;
    private final Clock clock;

    @Autowired
    SickNoteMailService(SettingsService settingsService, SickNoteService sickNoteService, MailService mailService, Clock clock) {
        this.settingsService = settingsService;
        this.sickNoteService = sickNoteService;
        this.mailService = mailService;
        this.clock = clock;
    }

    /**
     * Sends mail to person and office if sick pay (gesetzliche Lohnfortzahlung im Krankheitsfall) is about to end.
     */
    void sendEndOfSickPayNotification() {

        final List<SickNote> sickNotes = sickNoteService.getSickNotesReachingEndOfSickPay();

        LOG.info("Found {} sick notes reaching end of sick pay", sickNotes.size());

        final Integer maximumSickPayDays = settingsService.getSettings().getSickNoteSettings().getMaximumSickPayDays();

        for (SickNote sickNote : sickNotes) {

            // we need to subtract 1 day, because the start date is inclusive
            final LocalDate lastDayOfSickPayDays = sickNote.getStartDate()
                .plus(maximumSickPayDays.longValue(), DAYS)
                .minusDays(1);
            final boolean isLastDayOfSickPayDaysInPast = lastDayOfSickPayDays.isBefore(LocalDate.now(clock));

            final Map<String, Object> model = new HashMap<>();
            model.put("maximumSickPayDays", maximumSickPayDays);
            model.put("endOfSickPayDays", lastDayOfSickPayDays);
            model.put("isLastDayOfSickPayDaysInPast", isLastDayOfSickPayDaysInPast);
            model.put("sickNotePayFrom", sickNote.getStartDate());
            model.put("sickNotePayTo", lastDayOfSickPayDays);
            model.put("sickNote", sickNote);

            final Mail toSickNotePerson = Mail.builder()
                .withRecipient(sickNote.getPerson())
                .withSubject("subject.sicknote.endOfSickPay")
                .withTemplate("sicknote_end_of_sick_pay", model)
                .build();
            mailService.send(toSickNotePerson);

            final Mail toOffice = Mail.builder()
                .withRecipient(NOTIFICATION_OFFICE)
                .withSubject("subject.sicknote.endOfSickPay.office", sickNote.getPerson().getNiceName())
                .withTemplate("sicknote_end_of_sick_pay_office", model)
                .build();
            mailService.send(toOffice);
            sickNoteService.setEndOfSickPayNotificationSend(sickNote);
        }
    }
}
