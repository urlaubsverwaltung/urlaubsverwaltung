package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailRecipientService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.mail.MailTemplateModelSupplier;
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
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT_TO_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@Service
public class SickNoteMailService {

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
    public void sendEndOfSickPayNotification() {

        final List<SickNote> sickNotes = sickNoteService.getSickNotesReachingEndOfSickPay();

        LOG.info("Found {} sick notes reaching end of sick pay", sickNotes.size());

        final Integer maximumSickPayDays = settingsService.getSettings().getSickNoteSettings().getMaximumSickPayDays();

        for (SickNote sickNote : sickNotes) {

            // we need to subtract 1 day, because the start date is inclusive
            final LocalDate lastDayOfSickPayDays = sickNote.getStartDate().plusDays(maximumSickPayDays.longValue())
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
                .withTemplate("sicknote_end_of_sick_pay", locale -> model)
                .build();
            mailService.send(toSickNotePerson);

            final Mail toOffice = Mail.builder()
                .withRecipient(personService.getActivePersonsByRole(OFFICE))
                .withSubject("subject.sicknote.endOfSickPay.office", sickNote.getPerson().getNiceName())
                .withTemplate("sicknote_end_of_sick_pay_office", locale -> model)
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
    void sendCreatedToSickPerson(SickNote sickNote) {
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(sickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT)
            .withSubject("subject.sicknote.created.to_applicant_by_management")
            .withTemplate("sick_note_created_by_management_to_applicant", locale -> Map.of("sickNote", sickNote))
            .build();
        mailService.send(mailToApplicant);
    }

    /**
     * Sends information about an anonym sick note to the colleagues
     * to inform them about an absence
     *
     * @param sickNote that has been accepted or created
     */
    @Async
    void sendCreatedOrAcceptedToColleagues(SickNote sickNote) {

        // Inform colleagues of applicant which are in same department
        final MailTemplateModelSupplier modelColleaguesSupplier = locale -> Map.of("sickNote", sickNote);
        final List<Person> relevantColleaguesToInform = mailRecipientService.getColleagues(sickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED);
        final Mail mailToRelevantColleagues = Mail.builder()
            .withRecipient(relevantColleaguesToInform)
            .withSubject("subject.sicknote.createdOrAccepted.to_colleagues", sickNote.getPerson().getNiceName())
            .withTemplate("sick_note_created_or_accepted_to_colleagues", modelColleaguesSupplier)
            .build();
        mailService.send(mailToRelevantColleagues);
    }

    /**
     * Sends information about an edited sick note to the applicant
     *
     * @param sickNote that has been created
     */
    @Async
    void sendEditedToSickPerson(SickNote sickNote) {
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(sickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT)
            .withSubject("subject.sicknote.edited.to_applicant_by_management")
            .withTemplate("sick_note_edited_by_management_to_applicant", locale -> Map.of("sickNote", sickNote))
            .build();
        mailService.send(mailToApplicant);
    }

    /**
     * Sends information about a cancelled sick note to the applicant
     *
     * @param sickNote that has been created
     */
    @Async
    void sendCancelledToSickPerson(SickNote sickNote) {
        final Mail mailToRelevantColleagues = Mail.builder()
            .withRecipient(sickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT)
            .withSubject("subject.sicknote.cancelled.to_applicant_by_management")
            .withTemplate("sick_note_cancelled_by_management_to_applicant", locale -> Map.of("sickNote", sickNote))
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
        final MailTemplateModelSupplier modelColleaguesSupplier = locale -> Map.of("sickNote", sickNote);
        final List<Person> relevantColleaguesToInform = mailRecipientService.getColleagues(sickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED);
        final Mail mailToRelevantColleagues = Mail.builder()
            .withRecipient(relevantColleaguesToInform)
            .withSubject("subject.sicknote.cancelled.to_colleagues", sickNote.getPerson().getNiceName())
            .withTemplate("sick_note_cancel_to_colleagues", modelColleaguesSupplier)
            .build();
        mailService.send(mailToRelevantColleagues);
    }

    @Async
    void sendSickNoteSubmittedNotificationToSickPerson(SickNote submittedSickNote) {
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(submittedSickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER)
            .withSubject("subject.sicknote.submitted_by_user.to_applicant")
            .withTemplate("sick_note_submitted_by_user_to_applicant", locale -> Map.of("sickNote", submittedSickNote))
            .build();
        mailService.send(mailToApplicant);
    }

    @Async
    void sendSickNoteAcceptedNotificationToSickPerson(SickNote acceptedSickNote, Person maintainer) {
        final Mail mailToApplicant = Mail.builder()
            .withRecipient(acceptedSickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER)
            .withSubject("subject.sicknote.accepted_by_management.to_applicant")
            .withTemplate("sick_note_accepted_by_management_to_applicant", locale -> Map.of("sickNote", acceptedSickNote, "maintainer", maintainer))
            .build();
        mailService.send(mailToApplicant);
    }

    @Async
    void sendSickNoteSubmittedNotificationToOfficeAndResponsibleManagement(SickNote submittedSickNote) {

        final List<Person> recipients =
            mailRecipientService.getRecipientsOfInterest(submittedSickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT);
        final Mail mailToOfficeAndResponsibleManagement = Mail.builder()
            .withRecipient(recipients)
            .withSubject("subject.sicknote.submitted_by_user.to_management", submittedSickNote.getPerson().getNiceName())
            .withTemplate("sick_note_submitted_by_user_to_management", locale -> Map.of("sickNote", submittedSickNote))
            .build();

        mailService.send(mailToOfficeAndResponsibleManagement);
    }

    @Async
    void sendSickNoteCreatedNotificationToOfficeAndResponsibleManagement(SickNote createdSickNote, String comment) {

        final List<Person> recipientsWithoutApplier =
            mailRecipientService.getRecipientsOfInterest(createdSickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT_TO_MANAGEMENT).stream()
                .filter(recipient -> !recipient.equals(createdSickNote.getApplier())).toList();

        final Mail mailToOfficeAndResponsibleManagement = Mail.builder()
            .withRecipient(recipientsWithoutApplier)
            .withSubject("subject.sicknote.created_by_management.to_management", createdSickNote.getPerson().getNiceName())
            .withTemplate("sick_note_created_by_management_to_management", locale -> Map.of("sickNote", createdSickNote, "comment", comment))
            .build();

        mailService.send(mailToOfficeAndResponsibleManagement);
    }


    @Async
    void sendSickNoteAcceptedNotificationToOfficeAndResponsibleManagement(SickNote acceptedSickNote, Person maintainer) {
        final List<Person> recipients =
            mailRecipientService.getRecipientsOfInterest(acceptedSickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_MANAGEMENT)
                .stream().filter(recipient -> !recipient.equals(maintainer))
                .toList();
        final Mail mailToOfficeAndResponsibleManagement = Mail.builder()
            .withRecipient(recipients)
            .withSubject("subject.sicknote.accepted_by_management.to_management", acceptedSickNote.getPerson().getNiceName())
            .withTemplate("sick_note_accepted_by_management_to_management", locale -> Map.of("sickNote", acceptedSickNote, "maintainer", maintainer))
            .build();

        mailService.send(mailToOfficeAndResponsibleManagement);
    }
}
