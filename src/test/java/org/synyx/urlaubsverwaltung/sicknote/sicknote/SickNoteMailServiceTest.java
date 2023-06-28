package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailRecipientService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class SickNoteMailServiceTest {

    private SickNoteMailService sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private PersonService personService;
    @Mock
    private MailRecipientService mailRecipientService;
    @Mock
    private MailService mailService;

    @BeforeEach
    void setUp() {
        final Clock fixedClock = Clock.fixed(Instant.parse("2022-04-01T00:00:00.00Z"), ZoneId.of("UTC"));
        sut = new SickNoteMailService(settingsService, sickNoteService, mailService, personService, mailRecipientService, fixedClock);
    }

    @Test
    void ensureSendEndOfSickPayNotification() {

        final Person person = new Person();
        person.setUsername("Hulk");

        final Person office = new Person();
        office.setUsername("office");
        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of(office));

        final SickNote sickNoteA = SickNote.builder()
            .id(1)
            .person(person)
            .startDate(LocalDate.of(2022, 4, 1))
            .endDate(LocalDate.of(2022, 4, 13))
            .build();

        final SickNote sickNoteB = SickNote.builder()
            .id(2)
            .person(person)
            .startDate(LocalDate.of(2022, 4, 10))
            .endDate(LocalDate.of(2022, 4, 20))
            .build();

        when(sickNoteService.getSickNotesReachingEndOfSickPay()).thenReturn(asList(sickNoteA, sickNoteB));

        prepareSettingsWithMaximumSickPayDays(5);

        final Map<String, Object> modelA = new HashMap<>();
        modelA.put("maximumSickPayDays", 5);
        modelA.put("endOfSickPayDays", LocalDate.of(2022, 4, 5));
        modelA.put("sickPayDaysEndedDaysAgo", 4L);
        modelA.put("sickNotePayFrom", sickNoteA.getStartDate());
        modelA.put("sickNotePayTo", LocalDate.of(2022, 4, 5));
        modelA.put("sickNote", sickNoteA);

        final Map<String, Object> modelB = new HashMap<>();
        modelB.put("maximumSickPayDays", 5);
        modelB.put("endOfSickPayDays", LocalDate.of(2022, 4, 14));
        modelB.put("sickPayDaysEndedDaysAgo", 13L);
        modelB.put("sickNotePayFrom", sickNoteB.getStartDate());
        modelB.put("sickNotePayTo", LocalDate.of(2022, 4, 14));
        modelB.put("sickNote", sickNoteB);

        sut.sendEndOfSickPayNotification();

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(4)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(sickNoteA.getPerson()));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.sicknote.endOfSickPay");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("sicknote_end_of_sick_pay");
        assertThat(mails.get(0).getTemplateModel()).isEqualTo(modelA);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(office));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.sicknote.endOfSickPay.office");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("sicknote_end_of_sick_pay_office");
        assertThat(mails.get(1).getTemplateModel()).isEqualTo(modelA);
        assertThat(mails.get(2).getMailAddressRecipients()).hasValue(List.of(sickNoteB.getPerson()));
        assertThat(mails.get(2).getSubjectMessageKey()).isEqualTo("subject.sicknote.endOfSickPay");
        assertThat(mails.get(2).getTemplateName()).isEqualTo("sicknote_end_of_sick_pay");
        assertThat(mails.get(2).getTemplateModel()).isEqualTo(modelB);
        assertThat(mails.get(3).getMailAddressRecipients()).hasValue(List.of(office));
        assertThat(mails.get(3).getSubjectMessageKey()).isEqualTo("subject.sicknote.endOfSickPay.office");
        assertThat(mails.get(3).getTemplateName()).isEqualTo("sicknote_end_of_sick_pay_office");
        assertThat(mails.get(3).getTemplateModel()).isEqualTo(modelB);

        verify(sickNoteService).setEndOfSickPayNotificationSend(sickNoteA);
        verify(sickNoteService).setEndOfSickPayNotificationSend(sickNoteB);
    }

    @Test
    void ensureNoSendWhenDeactivated() {

        boolean isInactive = false;
        prepareSettingsWithRemindForWaitingApplications(isInactive);

        sut.sendEndOfSickPayNotification();
        verifyNoInteractions(mailService);
    }

    @Test
    void ensureSendSickNoteCreatedByManagementToSickPerson() {

        final Person management = new Person("muster", "Muster", "Marlene", "muster@example.org");
        management.setId(1);
        management.setPermissions(List.of(USER, OFFICE));

        final Person person = new Person("person", "person", "theo", "theo@example.org");
        person.setId(2);
        person.setPermissions(Set.of(USER));
        person.setNotifications(Set.of(NOTIFICATION_EMAIL_SICK_NOTE_CREATED_BY_MANAGEMENT));

        final SickNote sickNote = SickNote.builder()
            .id(2)
            .person(person)
            .applier(management)
            .startDate(LocalDate.of(2022, 3, 10))
            .endDate(LocalDate.of(2022, 4, 20))
            .build();

        sut.sendCreatedSickPerson(sickNote);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(sickNote.getPerson()));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.sicknote.created.to_applicant_by_management");
        assertThat(mail.getTemplateName()).isEqualTo("sick_note_created_by_management_to_applicant");
        assertThat(mail.getTemplateModel()).isEqualTo(Map.of("sickNote", sickNote));
    }

    @Test
    void ensureSendSickNoteCreatedToColleagues() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);

        final SickNote sickNote = SickNote.builder()
            .id(2)
            .person(person)
            .startDate(LocalDate.of(2022, 4, 10))
            .endDate(LocalDate.of(2022, 4, 20))
            .build();

        final Person colleague = new Person("muster", "Muster", "Marlene", "muster@example.org");
        colleague.setId(1);
        colleague.setPermissions(Set.of(USER));
        colleague.setNotifications(Set.of(NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED));
        when(mailRecipientService.getColleagues(person, NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CREATED)).thenReturn(List.of(colleague));

        sut.sendCreatedToColleagues(sickNote);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(sickNote.getPerson()));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.sicknote.created.to_colleagues");
        assertThat(mail.getTemplateName()).isEqualTo("sick_note_created_to_colleagues");
        assertThat(mail.getTemplateModel()).isEqualTo(Map.of("sickNote", sickNote));
    }

    @Test
    void ensureSendSickNoteEditedByManagementToSickPerson() {

        final Person management = new Person("muster", "Muster", "Marlene", "muster@example.org");
        management.setId(1);
        management.setPermissions(List.of(USER, OFFICE));

        final Person person = new Person("person", "person", "theo", "theo@example.org");
        person.setId(2);
        person.setPermissions(Set.of(USER));
        person.setNotifications(Set.of(NOTIFICATION_EMAIL_SICK_NOTE_EDITED_BY_MANAGEMENT));

        final SickNote sickNote = SickNote.builder()
            .id(2)
            .person(person)
            .applier(management)
            .startDate(LocalDate.of(2022, 3, 10))
            .endDate(LocalDate.of(2022, 4, 20))
            .build();

        sut.sendEditedToSickPerson(sickNote);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(sickNote.getPerson()));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.sicknote.edited.to_applicant_by_management");
        assertThat(mail.getTemplateName()).isEqualTo("sick_note_edited_by_management_to_applicant");
        assertThat(mail.getTemplateModel()).isEqualTo(Map.of("sickNote", sickNote));
    }

    @Test
    void ensureSendSickNoteCancelledByManagementToSickPerson() {

        final Person management = new Person("muster", "Muster", "Marlene", "muster@example.org");
        management.setId(1);
        management.setPermissions(List.of(USER, OFFICE));

        final Person person = new Person("person", "person", "theo", "theo@example.org");
        person.setId(2);
        person.setPermissions(Set.of(USER));
        person.setNotifications(Set.of(NOTIFICATION_EMAIL_SICK_NOTE_CANCELLED_BY_MANAGEMENT));

        final SickNote sickNote = SickNote.builder()
            .id(2)
            .person(person)
            .applier(management)
            .startDate(LocalDate.of(2022, 3, 10))
            .endDate(LocalDate.of(2022, 4, 20))
            .build();

        sut.sendCancelledToSickPerson(sickNote);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(sickNote.getPerson()));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.sicknote.cancelled.to_applicant_by_management");
        assertThat(mail.getTemplateName()).isEqualTo("sick_note_cancelled_by_management_to_applicant");
        assertThat(mail.getTemplateModel()).isEqualTo(Map.of("sickNote", sickNote));
    }

    @Test
    void ensureSendSickNoteCancelToColleagues() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);

        final SickNote sickNote = SickNote.builder()
            .id(2)
            .person(person)
            .startDate(LocalDate.of(2022, 4, 10))
            .endDate(LocalDate.of(2022, 4, 20))
            .build();

        final Person colleague = new Person("muster", "Muster", "Marlene", "muster@example.org");
        colleague.setId(1);
        colleague.setPermissions(Set.of(USER));
        colleague.setNotifications(Set.of(NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED));
        when(mailRecipientService.getColleagues(person, NOTIFICATION_EMAIL_SICK_NOTE_COLLEAGUES_CANCELLED)).thenReturn(List.of(colleague));

        sut.sendCancelToColleagues(sickNote);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(sickNote.getPerson()));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.sicknote.cancelled.to_colleagues");
        assertThat(mail.getTemplateName()).isEqualTo("sick_note_cancel_to_colleagues");
        assertThat(mail.getTemplateModel()).isEqualTo(Map.of("sickNote", sickNote));
    }

    private void prepareSettingsWithRemindForWaitingApplications(Boolean isActive) {
        Settings settings = new Settings();
        ApplicationSettings applicationSettings = new ApplicationSettings();
        applicationSettings.setRemindForWaitingApplications(isActive);
        settings.setApplicationSettings(applicationSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private void prepareSettingsWithMaximumSickPayDays(Integer sickPayDays) {
        final Settings settings = new Settings();
        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setMaximumSickPayDays(sickPayDays);
        settings.setSickNoteSettings(sickNoteSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }
}
