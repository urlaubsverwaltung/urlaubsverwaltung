package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.mail.Mail;
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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

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
    private MailService mailService;

    @BeforeEach
    void setUp() {
        final Clock fixedClock = Clock.fixed(Instant.parse("2022-04-01T00:00:00.00Z"), ZoneId.of("UTC"));
        sut = new SickNoteMailService(settingsService, sickNoteService, mailService, personService, fixedClock);
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
        modelA.put("sickPayDaysEndedDaysAgo", 4);
        modelA.put("sickNotePayFrom", sickNoteA.getStartDate());
        modelA.put("sickNotePayTo", LocalDate.of(2022, 4, 5));
        modelA.put("sickNote", sickNoteA);

        final Map<String, Object> modelB = new HashMap<>();
        modelB.put("maximumSickPayDays", 5);
        modelB.put("endOfSickPayDays", LocalDate.of(2022, 4, 14));
        modelB.put("sickPayDaysEndedDaysAgo", 13);
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
