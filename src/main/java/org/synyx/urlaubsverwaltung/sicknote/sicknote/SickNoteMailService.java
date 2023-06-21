package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailRecipientService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@Service
class SickNoteMailService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SettingsService settingsService;
    private final SickNoteService sickNoteService;
    private final MailService mailService;
    private final PersonService personService;
    private final MailRecipientService mailRecipientService;
    private final Clock clock;

    @Autowired
    SickNoteMailService(SettingsService settingsService, SickNoteService sickNoteService, MailService mailService,
                        PersonService personService, MailRecipientService mailRecipientService, Clock clock) {
        this.settingsService = settingsService;
        this.sickNoteService = sickNoteService;
        this.mailService = mailService;
        this.personService = personService;
        this.mailRecipientService = mailRecipientService;
        this.clock = clock;
    }

    /**
     * Sends mail to person and office if sick pay (gesetzliche Lohnfortzahlung im Krankheitsfall) is about to end.
     */
    @Async
    void sendEndOfSickPayNotification() {

        final List<SickNote> sickNotes = sickNoteService.getSickNotesReachingEndOfSickPay();

        LOG.info("Found {} sick notes reaching end of sick pay", sickNotes.size());

        final Integer maximumSickPayDays = settingsService.getSettings().getSickNoteSettings().getMaximumSickPayDays();

        for (SickNote sickNote : sickNotes) {

            // we need to subtract 1 day, because the start date is inclusive
            final LocalDate lastDayOfSickPayDays = sickNote.getStartDate()
                .plus(maximumSickPayDays.longValue(), DAYS)
                .minusDays(1);
            final long sickPayDaysEndedDaysAgo = LocalDate.now(clock).until(lastDayOfSickPayDays, DAYS);

            final Map<String, Object> model = new HashMap<>();
            model.put("maximumSickPayDays", maximumSickPayDays);
            model.put("endOfSickPayDays", lastDayOfSickPayDays);
            model.put("sickPayDaysEndedDaysAgo", sickPayDaysEndedDaysAgo);
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
                .withRecipient(personService.getActivePersonsByRole(OFFICE))
                .withSubject("subject.sicknote.endOfSickPay.office", sickNote.getPerson().getNiceName())
                .withTemplate("sicknote_end_of_sick_pay_office", model)
                .build();
            mailService.send(toOffice);
            sickNoteService.setEndOfSickPayNotificationSend(sickNote);
        }
    }

    /**
     * Sends information about a created sick note to the applicant
     *
     * @param sickNote that has been created
     */
    @Async
    void sendCreatedToApplicant(SickNote sickNote) {
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(sickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT)
            .withSubject("subject.sicknote.created.to_applicant_by_management")
            .withTemplate("sick_note_created_by_management_to_applicant", Map.of("sickNote", sickNote))
            .build();
        mailService.send(mailToApplicant);
    }

    /**
     * Sends information about an anonym sick note to the colleagues
     * to inform them about an absence
     *
     * @param sickNote that has been created
     */
    @Async
    void sendCreatedToColleagues(SickNote sickNote) {

        // Inform colleagues of applicant which are in same department
        final Map<String, Object> modelColleagues = Map.of("sickNote", sickNote);
        final List<Person> relevantColleaguesToInform = mailRecipientService.getColleagues(sickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED);
        final Mail mailToRelevantColleagues = Mail.builder()
            .withRecipient(relevantColleaguesToInform)
            .withSubject("subject.sicknote.created.to_colleagues", sickNote.getPerson().getNiceName())
            .withTemplate("sick_note_created_to_colleagues", modelColleagues)
            .build();
        mailService.send(mailToRelevantColleagues);
    }

    /**
     * Sends information about an edited sick note to the applicant
     *
     * @param sickNote that has been created
     */
    @Async
    void sendEditedToApplicant(SickNote sickNote) {
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(sickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT)
            .withSubject("subject.sicknote.edited.to_applicant_by_management")
            .withTemplate("sick_note_edited_by_management_to_applicant", Map.of("sickNote", sickNote))
            .build();
        mailService.send(mailToApplicant);
    }

    /**
     * Sends information about a cancelled sick note to the applicant
     *
     * @param sickNote that has been created
     */
    @Async
    void sendCancelledToApplicant(SickNote sickNote) {
        final Mail mailToRelevantColleagues = Mail.builder()
            .withRecipient(sickNote.getPerson(),NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT)
            .withSubject("subject.sicknote.cancelled.to_applicant_by_management")
            .withTemplate("sick_note_cancelled_by_management_to_applicant", Map.of("sickNote", sickNote))
            .build();
        mailService.send(mailToRelevantColleagues);
    }

    /**
     * Sends information about an anonym sick note to the colleagues
     * to inform them about an absence
     *
     * @param sickNote that has been created
     */
    @Async
    void sendCancelToColleagues(SickNote sickNote) {

        // Inform colleagues of applicant which are in same department
        final Map<String, Object> modelColleagues = Map.of("sickNote", sickNote);
        final List<Person> relevantColleaguesToInform = mailRecipientService.getColleagues(sickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED);
        final Mail mailToRelevantColleagues = Mail.builder()
            .withRecipient(relevantColleaguesToInform)
            .withSubject("subject.sicknote.cancelled.to_colleagues", sickNote.getPerson().getNiceName())
            .withTemplate("sick_note_cancel_to_colleagues", modelColleagues)
            .build();
        mailService.send(mailToRelevantColleagues);
    }
}
