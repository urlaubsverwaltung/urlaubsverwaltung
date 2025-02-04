package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.mail.MailRecipientService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class SickNoteMailServiceIT extends SingleTenantTestContainersBase {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private SickNoteMailService sut;
    @Autowired
    private PersonService personService;

    @MockitoBean
    private MailRecipientService mailRecipientService;
    @MockitoBean
    private SickNoteService sickNoteService;
    @MockitoBean
    private SickNoteCommentService sickNoteCommentService;

    @Test
    void sendEndOfSickPayNotification() throws MessagingException, IOException {

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(), List.of(OFFICE));

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .startDate(LocalDate.of(2022, 2, 1))
            .endDate(LocalDate.of(2022, 4, 1))
            .dayLength(DayLength.FULL)
            .build();

        final List<SickNote> sickNotes = List.of(sickNote);
        when(sickNoteService.getSickNotesReachingEndOfSickPay()).thenReturn(sickNotes);

        sut.sendEndOfSickPayNotification();

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
            });

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msgUser = inbox[0];
        assertThat(msgUser.getSubject()).isEqualTo("Ende deiner Lohnfortzahlung");
        assertThat(readPlainContent(msgUser)).isEqualTo("""
            Hallo Lieschen Müller,

            dein Anspruch auf Lohnfortzahlung von 42 Tagen endete am 14.03.2022.

                https://localhost:8080/web/sicknote/1

            Informationen zur Krankmeldung:

                Mitarbeiter:                  Lieschen Müller
                Zeitraum:                     01.02.2022 bis 01.04.2022, ganztägig
                Anspruch auf Lohnfortzahlung: 01.02.2022 bis 14.03.2022


            Hinweis:
            Der Anspruch auf Lohnfortzahlung durch den Arbeitgeber im Krankheitsfall besteht für maximal 42 Tage
            (fortlaufende Kalendertage ohne Rücksicht auf die Arbeitstage des erkrankten Arbeitnehmers, Sonn- oder Feiertage).
            Danach wird für gesetzlich Krankenversicherte in der Regel Krankengeld von der Krankenkasse gezahlt.""");

        // check email of office
        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        final Message msgOffice = inboxOffice[0];
        assertThat(msgOffice.getSubject()).isEqualTo("Ende der Lohnfortzahlung von Lieschen Müller");

        // check content of office email
        assertThat(readPlainContent(msgOffice)).isEqualTo("""
            Hallo Marlene Muster,

            der Anspruch auf Lohnfortzahlung von Lieschen Müller von 42 Tagen endete am 14.03.2022.

                https://localhost:8080/web/sicknote/1

            Informationen zur Krankmeldung:

                Mitarbeiter:                  Lieschen Müller
                Zeitraum:                     01.02.2022 bis 01.04.2022, ganztägig
                Anspruch auf Lohnfortzahlung: 01.02.2022 bis 14.03.2022


            Hinweis:
            Der Anspruch auf Lohnfortzahlung durch den Arbeitgeber im Krankheitsfall besteht für maximal 42 Tage
            (fortlaufende Kalendertage ohne Rücksicht auf die Arbeitstage des erkrankten Arbeitnehmers, Sonn- oder Feiertage).
            Danach wird für gesetzlich Krankenversicherte in der Regel Krankengeld von der Krankenkasse gezahlt.""");

        verify(sickNoteService).setEndOfSickPayNotificationSend(sickNote);
    }

    @Test
    void sendSickNoteCreatedByManagementToSickPerson() throws MessagingException, IOException {

        final Person person = personService.create("person", "Marlene", "Muster", "colleague@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT), List.of(USER));

        final Person management = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SICK_NOTE_CHILD);
        sickNoteTypeChild.setMessageKey("application.data.sicknotetype.sicknotechild");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .applier(management)
            .startDate(LocalDate.of(2022, 2, 1))
            .endDate(LocalDate.of(2022, 4, 1))
            .dayLength(DayLength.FULL)
            .sickNoteType(sickNoteTypeChild)
            .build();

        sut.sendCreatedToSickPerson(sickNote);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).isEqualTo("Eine neue Krankmeldung wurde für dich eingetragen");
        assertThat(readPlainContent(msgPerson)).isEqualTo("""
            Hallo Marlene Muster,

            Lieschen Müller hat eine neue Krankmeldung für dich eingetragen:

                https://localhost:8080/web/sicknote/1

            Informationen zur Krankmeldung:

                Zeitraum:             01.02.2022 bis 01.04.2022, ganztägig
                Art der Krankmeldung: Kind-Krankmeldung


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(person.getId()));
    }

    @Test
    void sendSickNoteEditedByManagementToSickPerson() throws MessagingException, IOException {

        final Person person = personService.create("person", "Marlene", "Muster", "colleague@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT), List.of(USER));

        final Person management = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SICK_NOTE_CHILD);
        sickNoteTypeChild.setMessageKey("application.data.sicknotetype.sicknotechild");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .applier(management)
            .startDate(LocalDate.of(2022, 2, 1))
            .endDate(LocalDate.of(2022, 4, 1))
            .dayLength(DayLength.FULL)
            .sickNoteType(sickNoteTypeChild)
            .build();

        sut.sendEditedToSickPerson(sickNote);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).isEqualTo("Eine Krankmeldung wurde für dich bearbeitet");
        assertThat(readPlainContent(msgPerson)).isEqualTo("""
            Hallo Marlene Muster,

            Lieschen Müller hat die folgende Krankmeldung für dich bearbeitet:

                https://localhost:8080/web/sicknote/1

            Informationen zur Krankmeldung:

                Zeitraum:             01.02.2022 bis 01.04.2022, ganztägig
                Art der Krankmeldung: Kind-Krankmeldung


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(person.getId()));
    }

    @Test
    void sendSickNoteCreatedToColleagues() throws MessagingException, IOException {

        final Person colleague = personService.create("colleague", "Marlene", "Muster", "colleague@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED), List.of(USER));

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .startDate(LocalDate.of(2022, 2, 1))
            .endDate(LocalDate.of(2022, 4, 1))
            .dayLength(DayLength.FULL)
            .build();

        when(mailRecipientService.getColleagues(sickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED))
            .thenReturn(List.of(colleague));

        sut.sendCreatedOrAcceptedToColleagues(sickNote);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(colleague.getEmail())).hasSize(1));

        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        final Message msgColleague = inboxColleague[0];
        assertThat(msgColleague.getSubject()).isEqualTo("Neue Abwesenheit von Lieschen Müller");
        assertThat(readPlainContent(msgColleague)).isEqualTo("""
            Hallo Marlene Muster,

            eine Abwesenheit von Lieschen Müller wurde erstellt:

                Zeitraum: 01.02.2022 bis 01.04.2022, ganztägig

            Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(colleague.getId()));
    }

    @Test
    void sendSickNoteCancelledByManagementToSickPerson() throws MessagingException, IOException {

        final Person person = personService.create("person", "Marlene", "Muster", "colleague@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT), List.of(USER));

        final Person management = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SICK_NOTE_CHILD);
        sickNoteTypeChild.setMessageKey("application.data.sicknotetype.sicknotechild");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .applier(management)
            .startDate(LocalDate.of(2022, 2, 1))
            .endDate(LocalDate.of(2022, 4, 1))
            .dayLength(DayLength.FULL)
            .sickNoteType(sickNoteTypeChild)
            .build();

        sut.sendCancelledToSickPerson(sickNote);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).isEqualTo("Eine Krankmeldung wurde für dich storniert");
        assertThat(readPlainContent(msgPerson)).isEqualTo("""
            Hallo Marlene Muster,

            Lieschen Müller hat die folgende Krankmeldung für dich storniert:

                https://localhost:8080/web/sicknote/1

            Informationen zur Krankmeldung:

                Zeitraum:             01.02.2022 bis 01.04.2022, ganztägig
                Art der Krankmeldung: Kind-Krankmeldung


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(person.getId()));
    }

    @Test
    void sendSickNoteCancelToColleagues() throws MessagingException, IOException {

        final Person colleague = personService.create("colleague", "Marlene", "Muster", "colleague@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED), List.of(USER));

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .startDate(LocalDate.of(2022, 2, 1))
            .endDate(LocalDate.of(2022, 4, 1))
            .dayLength(DayLength.FULL)
            .build();

        when(mailRecipientService.getColleagues(sickNote.getPerson(), NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED))
            .thenReturn(List.of(colleague));

        sut.sendCancelToColleagues(sickNote);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(colleague.getEmail())).hasSize(1));

        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        final Message msgColleague = inboxColleague[0];
        assertThat(msgColleague.getSubject()).isEqualTo("Abwesenheit von Lieschen Müller wurde zurückgenommen");
        assertThat(readPlainContent(msgColleague)).isEqualTo("""
            Hallo Marlene Muster,

            eine Abwesenheit von Lieschen Müller wurde zurückgenommen:

                Zeitraum: 01.02.2022 bis 01.04.2022, ganztägig

            Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(colleague.getId()));
    }

    @Test
    void sendSickNoteCreatedNotificationToOfficeAndResponsibleManagement() throws MessagingException, IOException {

        final Person person = personService.create("user", "Marlene", "Muster", "user@example.org", List.of(), List.of(USER));
        final Person office1 = personService.create("office1", "Lieschen", "Müller", "office1@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT_TO_MANAGEMENT), List.of(OFFICE));
        final Person office2 = personService.create("office2", "Hans", "Meyer", "office2@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT_TO_MANAGEMENT), List.of(OFFICE));

        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT_TO_MANAGEMENT))
            .thenReturn(List.of(office1, office2));

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("application.data.sicknotetype.sicknote");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .applier(office1)
            .startDate(LocalDate.of(2022, 2, 1))
            .endDate(LocalDate.of(2022, 4, 1))
            .aubStartDate(LocalDate.of(2022, 2, 2))
            .aubEndDate(LocalDate.of(2022, 4, 1))
            .dayLength(DayLength.FULL)
            .sickNoteType(sickNoteType)
            .build();
        final String comment = "Weiterführende Information";

        sut.sendSickNoteCreatedNotificationToOfficeAndResponsibleManagement(sickNote, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(office2.getEmail())).hasSize(1));

        final MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(office2.getEmail());
        final Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).isEqualTo("Eine neue Krankmeldung wurde für Marlene Muster eingetragen");
        assertThat(readPlainContent(msgPerson)).isEqualTo("""
            Hallo Hans Meyer,

            Lieschen Müller hat eine neue Krankmeldung für Marlene Muster eingetragen:

                https://localhost:8080/web/sicknote/1

            Informationen zur Krankmeldung:

                Zeitraum:             01.02.2022 bis 01.04.2022, ganztägig
                Zeitraum der AU:      02.02.2022 bis 01.04.2022
                Art der Krankmeldung: Krankmeldung

            Kommentar(e) zur Krankmeldung:

                Weiterführende Information


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(office2.getId()));
    }

    @Test
    void sendSickNoteSubmittedNotificationToSickPerson() throws MessagingException, IOException {

        final Person person = personService.create("person", "Marlene", "Muster", "user@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_USER), List.of(USER));

        final SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SICK_NOTE_CHILD);
        sickNoteTypeChild.setMessageKey("application.data.sicknotetype.sicknotechild");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .applier(person)
            .startDate(LocalDate.of(2022, 2, 1))
            .endDate(LocalDate.of(2022, 4, 1))
            .dayLength(DayLength.FULL)
            .sickNoteType(sickNoteTypeChild)
            .build();

        sut.sendSickNoteSubmittedNotificationToSickPerson(sickNote);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).isEqualTo("Du hast eine neue Krankmeldung eingereicht");
        assertThat(readPlainContent(msgPerson)).isEqualTo("""
            Hallo Marlene Muster,

            Du hast eine neue Krankmeldung eingereicht:

                https://localhost:8080/web/sicknote/1

            Informationen zur Krankmeldung:

                Zeitraum:             01.02.2022 bis 01.04.2022, ganztägig
                Art der Krankmeldung: Kind-Krankmeldung


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(person.getId()));
    }

    @Test
    void sendSickNoteSubmittedNotificationToOfficeAndResponsibleManagement() throws MessagingException, IOException {

        final Person person = personService.create("user", "Marlene", "Muster", "user@example.org", List.of(), List.of(USER));
        final Person office = personService.create("office", "Lieschen", "Müller", "office@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT), List.of(OFFICE));

        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_SICK_NOTE_SUBMITTED_BY_USER_TO_MANAGEMENT))
            .thenReturn(List.of(office));

        final SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SICK_NOTE_CHILD);
        sickNoteTypeChild.setMessageKey("application.data.sicknotetype.sicknotechild");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .applier(person)
            .startDate(LocalDate.of(2022, 2, 1))
            .endDate(LocalDate.of(2022, 4, 1))
            .dayLength(DayLength.FULL)
            .sickNoteType(sickNoteTypeChild)
            .build();

        sut.sendSickNoteSubmittedNotificationToOfficeAndResponsibleManagement(sickNote);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(1));

        final MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(office.getEmail());
        final Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).isEqualTo("Eine neue Krankmeldung wurde von Marlene Muster eingereicht");
        assertThat(readPlainContent(msgPerson)).isEqualTo("""
            Hallo Lieschen Müller,

            Marlene Muster hat eine neue Krankmeldung eingereicht:

                https://localhost:8080/web/sicknote/1

            Informationen zur Krankmeldung:

                Zeitraum:             01.02.2022 bis 01.04.2022, ganztägig
                Art der Krankmeldung: Kind-Krankmeldung

            Überblick aller eingereichten Krankmeldungen findest du unter https://localhost:8080/web/sicknote/submitted#sicknote-submitted


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(office.getId()));
    }

    @Test
    void sendSickNoteAcceptedNotificationToSickPerson() throws MessagingException, IOException {

        final Person person = personService.create("user", "Marlene", "Muster", "user@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_USER), List.of(USER));
        final Person office = personService.create("office", "Lieschen", "Müller", "office@example.org", List.of(), List.of(OFFICE));

        final SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SICK_NOTE_CHILD);
        sickNoteTypeChild.setMessageKey("application.data.sicknotetype.sicknotechild");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .applier(person)
            .startDate(LocalDate.of(2022, 2, 1))
            .endDate(LocalDate.of(2022, 4, 1))
            .dayLength(DayLength.FULL)
            .sickNoteType(sickNoteTypeChild)
            .build();

        sut.sendSickNoteAcceptedNotificationToSickPerson(sickNote, office);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).isEqualTo("Deine Krankmeldung wurde angenommen");
        assertThat(readPlainContent(msgPerson)).isEqualTo("""
            Hallo Marlene Muster,

            Lieschen Müller hat eine neue Krankmeldung für dich angenommen:

                https://localhost:8080/web/sicknote/1

            Informationen zur Krankmeldung:

                Zeitraum:             01.02.2022 bis 01.04.2022, ganztägig
                Art der Krankmeldung: Kind-Krankmeldung


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(person.getId()));
    }

    @Test
    void sendSickNoteAcceptedNotificationToOfficeAndResponsibleManagement() throws MessagingException, IOException {

        final Person person = personService.create("user", "Marlene", "Muster", "user@example.org", List.of(), List.of(USER));
        final Person office1 = personService.create("office1", "Lieschen", "Müller", "office1@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_MANAGEMENT), List.of(OFFICE));
        final Person office2 = personService.create("office2", "Hans", "Meyer", "office2@example.org", List.of(NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_MANAGEMENT), List.of(OFFICE));

        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_SICK_NOTE_ACCEPTED_BY_MANAGEMENT_TO_MANAGEMENT))
            .thenReturn(List.of(office1, office2));

        final SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SICK_NOTE_CHILD);
        sickNoteTypeChild.setMessageKey("application.data.sicknotetype.sicknotechild");

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .person(person)
            .applier(person)
            .startDate(LocalDate.of(2022, 2, 1))
            .endDate(LocalDate.of(2022, 4, 1))
            .dayLength(DayLength.FULL)
            .sickNoteType(sickNoteTypeChild)
            .build();

        sut.sendSickNoteAcceptedNotificationToOfficeAndResponsibleManagement(sickNote, office1);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(office2.getEmail())).hasSize(1));

        final MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(office2.getEmail());
        final Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).isEqualTo("Eine neue Krankmeldung wurde von Marlene Muster angenommen");
        assertThat(readPlainContent(msgPerson)).isEqualTo("""
            Hallo Hans Meyer,

            Lieschen Müller hat eine neue Krankmeldung von Marlene Muster angenommen:

                https://localhost:8080/web/sicknote/1

            Informationen zur Krankmeldung:

                Zeitraum:             01.02.2022 bis 01.04.2022, ganztägig
                Art der Krankmeldung: Kind-Krankmeldung

            Überblick aller eingereichten Krankmeldungen findest du unter https://localhost:8080/web/sicknote/submitted#sicknote-submitted


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(office2.getId()));
    }

    private String readPlainContent(Message message) throws MessagingException, IOException {
        return message.getContent().toString().replaceAll("\\r", "");
    }
}
