package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.ApplicationSettings;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;

@ExtendWith(MockitoExtension.class)
class SickNoteMailServiceTest {

    private SickNoteMailService sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private MailService mailService;

    @BeforeEach
    void setUp() {
        sut = new SickNoteMailService(settingsService, sickNoteService, mailService);
    }

    @Test
    void ensureSendEndOfSickPayNotification() {

        final Person person = new Person();
        person.setUsername("Hulk");

        final SickNote sickNoteA = new SickNote();
        sickNoteA.setId(1);
        sickNoteA.setPerson(person);

        final SickNote sickNoteB = new SickNote();
        sickNoteB.setId(2);
        sickNoteB.setPerson(person);

        when(sickNoteService.getSickNotesReachingEndOfSickPay()).thenReturn(asList(sickNoteA, sickNoteB));

        prepareSettingsWithMaximumSickPayDays(5);

        Map<String, Object> modelA = new HashMap<>();
        modelA.put("maximumSickPayDays", 5);
        modelA.put("sickNote", sickNoteA);

        Map<String, Object> modelB = new HashMap<>();
        modelB.put("maximumSickPayDays", 5);
        modelB.put("sickNote", sickNoteB);

        sut.sendEndOfSickPayNotification();

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(4)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(sickNoteA.getPerson()));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.sicknote.endOfSickPay");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("sicknote_end_of_sick_pay");
        assertThat(mails.get(0).getTemplateModel()).isEqualTo(modelA);
        assertThat(mails.get(1).getMailNotificationRecipients()).hasValue(NOTIFICATION_OFFICE);
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.sicknote.endOfSickPay");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("sicknote_end_of_sick_pay");
        assertThat(mails.get(1).getTemplateModel()).isEqualTo(modelA);
        assertThat(mails.get(2).getMailAddressRecipients()).hasValue(List.of(sickNoteB.getPerson()));
        assertThat(mails.get(2).getSubjectMessageKey()).isEqualTo("subject.sicknote.endOfSickPay");
        assertThat(mails.get(2).getTemplateName()).isEqualTo("sicknote_end_of_sick_pay");
        assertThat(mails.get(2).getTemplateModel()).isEqualTo(modelB);
        assertThat(mails.get(3).getMailNotificationRecipients()).hasValue(NOTIFICATION_OFFICE);
        assertThat(mails.get(3).getSubjectMessageKey()).isEqualTo("subject.sicknote.endOfSickPay");
        assertThat(mails.get(3).getTemplateName()).isEqualTo("sicknote_end_of_sick_pay");
        assertThat(mails.get(3).getTemplateModel()).isEqualTo(modelB);
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
