package org.synyx.urlaubsverwaltung.application.application;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.calendar.ICalService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailRecipientService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Locale.GERMAN;
import static java.util.Locale.JAPANESE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_UPCOMING;

@ExtendWith(MockitoExtension.class)
class ApplicationMailServiceTest {

    private ApplicationMailService sut;

    @Mock
    private MailService mailService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private MailRecipientService mailRecipientService;
    @Mock
    private ICalService iCalService;
    @Mock
    private SettingsService settingsService;

    private final Clock clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00.00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        sut = new ApplicationMailService(mailService, departmentService, mailRecipientService, iCalService, settingsService, clock);
    }

    @Test
    void ensureSendsAllowedNotificationToManagementAndColleague() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Settings settings = new Settings();
        settings.setApplicationSettings(new ApplicationSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final ByteArrayResource attachment = new ByteArrayResource("".getBytes());
        when(iCalService.getSingleAppointment(any(), any(), any())).thenReturn(attachment);

        final Person person = new Person();
        person.setId(0L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setDayLength(FULL);
        application.setPerson(person);
        application.setStartDate(LocalDate.of(2020, 12, 1));
        application.setEndDate(LocalDate.of(2020, 12, 2));
        application.setStatus(ALLOWED);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Person boss = new Person();
        boss.setId(1L);
        final Person office = new Person();
        office.setId(2L);
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED))
            .thenReturn(List.of(boss, office));

        final Person colleague = new Person();
        colleague.setId(3L);
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED))
            .thenReturn(List.of(colleague));

        sut.sendAllowedNotification(application, applicationComment);

        final Map<String, Object> model = Map.of(
            "application", application,
            "vacationTypeLabel", "vacation type label",
            "comment", applicationComment
        );

        final Map<String, Object> modelColleagues = Map.of("application", application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(3)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.allowed.user");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_allowed_to_applicant");
        assertThat(mails.get(0).getTemplateModel(locale)).isEqualTo(model);
        assertThat(mails.get(0).getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mails.get(0).getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(boss, office));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.allowed.management");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_allowed_to_management");
        assertThat(mails.get(1).getTemplateModel(locale)).isEqualTo(model);
        assertThat(mails.get(1).getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mails.get(1).getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
        assertThat(mails.get(2).getMailAddressRecipients()).hasValue(List.of(colleague));
        assertThat(mails.get(2).getSubjectMessageKey()).isEqualTo("subject.application.allowed.to_colleagues");
        assertThat(mails.get(2).getTemplateName()).isEqualTo("application_allowed_to_colleagues");
        assertThat(mails.get(2).getTemplateModel(locale)).isEqualTo(modelColleagues);
        assertThat(mails.get(2).getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mails.get(2).getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
    }

    @Test
    void sendRejectedNotification() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person person = new Person();
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_REJECTED));

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setDayLength(FULL);
        application.setPerson(person);
        application.setStartDate(LocalDate.of(2020, 12, 1));
        application.setEndDate(LocalDate.of(2020, 12, 2));
        application.setStatus(ALLOWED);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationTypeLabel", "vacation type label");
        model.put("comment", applicationComment);

        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED)).thenReturn(List.of(person));

        sut.sendRejectedNotification(application, applicationComment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.rejected");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_rejected_information_to_applicant");
        assertThat(mails.get(0).getTemplateModel(locale)).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.rejected_information");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_rejected_information_to_management");
        assertThat(mails.get(1).getTemplateModel(locale)).isEqualTo(model);
    }

    @Test
    void sendReferApplicationNotification() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person recipient = new Person();
        final Person sender = new Person();

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setDayLength(FULL);
        application.setPerson(recipient);
        application.setStartDate(LocalDate.of(2020, 12, 1));
        application.setEndDate(LocalDate.of(2020, 12, 2));
        application.setStatus(ALLOWED);

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationTypeLabel", "vacation type label");
        model.put("sender", sender);

        sut.sendReferredToManagementNotification(application, recipient, sender);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(recipient));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.refer");
        assertThat(mail.getTemplateName()).isEqualTo("application_referred_to_management");
        assertThat(mail.getTemplateModel(locale)).isEqualTo(model);
    }

    @Test
    void ensureToSendEditedApplicationNotificationIfApplicantIsEditor() {

        final Person editor = new Person();
        editor.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_EDITED));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(HOLIDAY)
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setDayLength(FULL);
        application.setPerson(editor);
        application.setStartDate(LocalDate.of(2020, 12, 1));
        application.setEndDate(LocalDate.of(2020, 12, 2));
        application.setStatus(ALLOWED);

        final Person relevantPerson = new Person();
        relevantPerson.setId(2L);
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED)).thenReturn(List.of(relevantPerson));

        sut.sendEditedNotification(application, editor);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mail = argument.getAllValues();
        assertThat(mail.get(0).getMailAddressRecipients()).hasValue(List.of(editor));
        assertThat(mail.get(0).getSubjectMessageKey()).isEqualTo("subject.application.edited.to_applicant_by_applicant");
        assertThat(mail.get(0).getTemplateName()).isEqualTo("application_edited_by_applicant_to_applicant");
        assertThat(mail.get(0).getTemplateModel(GERMAN)).isEqualTo(Map.<String, Object>of("application", application));
        assertThat(mail.get(1).getMailAddressRecipients()).hasValue(List.of(relevantPerson));
        assertThat(mail.get(1).getSubjectMessageKey()).isEqualTo("subject.application.edited.management");
        assertThat(mail.get(1).getTemplateName()).isEqualTo("application_edited_by_applicant_to_management");
        assertThat(mail.get(1).getTemplateModel(GERMAN)).isEqualTo(Map.of("application", application, "editor", editor));
    }

    @Test
    void ensureToSendEditedApplicationNotificationIfOfficeIsEditor() {

        final Person office = new Person("marlene", "Muster", "Marlene", "marlene@example.org");

        final Person applicant = new Person();
        applicant.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_EDITED));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(HOLIDAY)
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setDayLength(FULL);
        application.setPerson(applicant);
        application.setStartDate(LocalDate.of(2020, 12, 1));
        application.setEndDate(LocalDate.of(2020, 12, 2));
        application.setStatus(ALLOWED);

        final Person relevantPerson = new Person();
        relevantPerson.setId(2L);
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED)).thenReturn(List.of(relevantPerson));

        sut.sendEditedNotification(application, office);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mail = argument.getAllValues();
        assertThat(mail.get(0).getMailAddressRecipients()).hasValue(List.of(applicant));
        assertThat(mail.get(0).getSubjectMessageKey()).isEqualTo("subject.application.edited.to_applicant_by_management");
        assertThat(mail.get(0).getTemplateName()).isEqualTo("application_edited_by_management_to_applicant");
        assertThat(mail.get(0).getTemplateModel(GERMAN)).isEqualTo(Map.of("application", application, "editor", office));
        assertThat(mail.get(1).getMailAddressRecipients()).hasValue(List.of(relevantPerson));
        assertThat(mail.get(1).getSubjectMessageKey()).isEqualTo("subject.application.edited.management");
        assertThat(mail.get(1).getTemplateName()).isEqualTo("application_edited_by_applicant_to_management");
        assertThat(mail.get(1).getTemplateModel(GERMAN)).isEqualTo(Map.of("application", application, "editor", office));
    }

    @Test
    void sendDeclinedCancellationRequestApplicationNotification() {

        final Person person = new Person();
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Application application = new Application();
        application.setPerson(person);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", comment);

        final Person office = new Person();
        office.setId(1L);
        final Person relevantPerson = new Person();
        relevantPerson.setId(2L);
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED)).thenReturn(List.of(relevantPerson, office));

        sut.sendDeclinedCancellationRequestApplicationNotification(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.cancellationRequest.declined.applicant");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_cancellation_request_declined_to_applicant");
        assertThat(mails.get(0).getTemplateModel(GERMAN)).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(relevantPerson, office));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.cancellationRequest.declined.management");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_cancellation_request_declined_to_management");
        assertThat(mails.get(1).getTemplateModel(GERMAN)).isEqualTo(model);
    }

    @Test
    void ensureMailIsSentToAllRecipientsThatHaveAnEmailAddress() {

        final Person person = new Person();
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Application application = new Application();
        application.setPerson(person);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", applicationComment);

        final List<Person> relevantPersons = List.of(new Person());
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED)).thenReturn(relevantPersons);

        sut.sendCancellationRequest(application, applicationComment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.cancellationRequest.applicant");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_cancellation_request_to_applicant");
        assertThat(mails.get(0).getTemplateModel(GERMAN)).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(relevantPersons);
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.cancellationRequest");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_cancellation_request_to_management");
        assertThat(mails.get(1).getTemplateModel(GERMAN)).isEqualTo(model);
    }

    @Test
    void sendSickNoteConvertedToVacationNotification() {
        final Person person = new Person();
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CONVERTED));

        final Application application = new Application();
        application.setPerson(person);

        final Person relevantPerson = new Person();
        relevantPerson.setId(2L);
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED)).thenReturn(List.of(relevantPerson));

        sut.sendSickNoteConvertedToVacationNotification(application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.sicknote.converted");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("sicknote_converted");
        assertThat(mails.get(0).getTemplateModel(GERMAN)).isEqualTo(Map.<String, Object>of("application", application));
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(relevantPerson));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.sicknote.converted.management");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("sicknote_converted_to_management");
        assertThat(mails.get(1).getTemplateModel(GERMAN)).isEqualTo(Map.<String, Object>of("application", application));
    }

    @Test
    void notifyHolidayReplacementAllow() {

        final Settings settings = new Settings();
        settings.setApplicationSettings(new ApplicationSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final Person holidayReplacement = new Person();
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));

        final Person applicant = new Person();
        applicant.setFirstName("Theo");
        applicant.setLastName("Fritz");

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("awesome replacement note");

        final Application application = new Application();
        application.setPerson(applicant);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2020, 3, 5));
        application.setEndDate(LocalDate.of(2020, 3, 6));

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("holidayReplacement", holidayReplacement);
        model.put("holidayReplacementNote", "awesome replacement note");

        final ByteArrayResource attachment = new ByteArrayResource("".getBytes());
        when(iCalService.getSingleAppointment(any(), any(), any())).thenReturn(attachment);

        sut.notifyHolidayReplacementAllow(replacementEntity, application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.holidayReplacement.allow");
        assertThat(mail.getTemplateName()).isEqualTo("application_allowed_to_holiday_replacement");
        assertThat(mail.getTemplateModel(GERMAN)).isEqualTo(model);
        assertThat(mail.getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mail.getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
    }

    @Test
    void ensureToNotifyHolidayReplacementAboutEditAndStatusIsWaiting() {

        final Person holidayReplacement = new Person();
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("awesome note");

        final Person applicant = new Person();
        applicant.setFirstName("Theo");
        applicant.setLastName("Fritz");

        final Application application = new Application();
        application.setStatus(WAITING);
        application.setPerson(applicant);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setDayLength(FULL);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("holidayReplacement", holidayReplacement);
        model.put("holidayReplacementNote", "awesome note");

        sut.notifyHolidayReplacementAboutEdit(replacementEntity, application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.holidayReplacement.edit");
        assertThat(mail.getTemplateName()).isEqualTo("application_edited_to_holiday_replacement");
        assertThat(mail.getTemplateModel(GERMAN)).isEqualTo(model);
    }

    @Test
    void ensureToNotifyHolidayReplacementAboutEditAndStatusIsAllowed() {

        final Person holidayReplacement = new Person();
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("awesome note");

        final Person applicant = new Person();
        applicant.setFirstName("Theo");
        applicant.setLastName("Fritz");

        final Application application = new Application();
        application.setStatus(ALLOWED);
        application.setPerson(applicant);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setDayLength(FULL);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("holidayReplacement", holidayReplacement);
        model.put("holidayReplacementNote", "awesome note");

        sut.notifyHolidayReplacementAboutEdit(replacementEntity, application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.holidayReplacement.allow.edit");
        assertThat(mail.getTemplateName()).isEqualTo("application_edited_to_holiday_replacement");
        assertThat(mail.getTemplateModel(GERMAN)).isEqualTo(model);
    }

    @Test
    void notifyHolidayReplacementForApply() {

        final Person holidayReplacement = new Person();
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("awesome note");

        final Person applicant = new Person();
        applicant.setFirstName("Theo");
        applicant.setLastName("Fritz");

        final Application application = new Application();
        application.setPerson(applicant);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setDayLength(FULL);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("holidayReplacement", holidayReplacement);
        model.put("holidayReplacementNote", "awesome note");

        sut.notifyHolidayReplacementForApply(replacementEntity, application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.holidayReplacement.apply");
        assertThat(mail.getTemplateName()).isEqualTo("application_applied_to_holiday_replacement");
        assertThat(mail.getTemplateModel(GERMAN)).isEqualTo(model);
    }

    @Test
    void notifyHolidayReplacementAboutCancellation() {

        final Settings settings = new Settings();
        settings.setTimeSettings(new TimeSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final ByteArrayResource attachment = new ByteArrayResource("".getBytes());
        when(iCalService.getSingleAppointment(any(), any(), any())).thenReturn(attachment);

        final Person applicant = new Person();
        applicant.setFirstName("Thomas");

        final Person holidayReplacement = new Person();
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("awesome note");

        final Application application = new Application();
        application.setPerson(applicant);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2020, 10, 2));
        application.setEndDate(LocalDate.of(2020, 10, 3));

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("holidayReplacement", holidayReplacement);

        sut.notifyHolidayReplacementAboutCancellation(replacementEntity, application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.holidayReplacement.cancellation");
        assertThat(mail.getTemplateName()).isEqualTo("application_cancelled_to_holiday_replacement");
        assertThat(mail.getTemplateModel(GERMAN)).isEqualTo(model);
        assertThat(mail.getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mail.getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
    }

    @Test
    void sendConfirmation() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person person = new Person();
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2020, 12, 1));
        application.setEndDate(LocalDate.of(2020, 12, 2));
        application.setStatus(WAITING);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationTypeLabel", "vacation type label");
        model.put("comment", comment);

        sut.sendAppliedNotification(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.applied.user");
        assertThat(mail.getTemplateName()).isEqualTo("application_applied_by_applicant_to_applicant");
        assertThat(mail.getTemplateModel(locale)).isEqualTo(model);
    }

    @Test
    void sendConfirmationAllowedDirectly() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person person = new Person();
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .requiresApprovalToApply(false)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2020, 12, 1));
        application.setEndDate(LocalDate.of(2020, 12, 2));
        application.setStatus(WAITING);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Person colleague = new Person();
        colleague.setId(3L);
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED))
            .thenReturn(List.of(colleague));

        sut.sendConfirmationAllowedDirectly(application, comment);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationTypeLabel", "vacation type label");
        model.put("comment", comment);

        final Map<String, Object> modelColleagues = Map.of("application", application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.allowedDirectly.user");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_allowed_directly_to_applicant");
        assertThat(mails.get(0).getTemplateModel(locale)).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(colleague));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.allowed.to_colleagues");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_allowed_to_colleagues");
        assertThat(mails.get(1).getTemplateModel(locale)).isEqualTo(modelColleagues);
    }

    @Test
    void sendConfirmationAllowedDirectlyByManagement() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person person = new Person();
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .requiresApprovalToApply(false)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2020, 12, 1));
        application.setEndDate(LocalDate.of(2020, 12, 2));
        application.setStatus(WAITING);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Person colleague = new Person();
        colleague.setId(3L);
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED))
            .thenReturn(List.of(colleague));

        sut.sendConfirmationAllowedDirectlyByManagement(application, comment);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationTypeLabel", "vacation type label");
        model.put("comment", comment);

        final Map<String, Object> modelColleagues = Map.of("application", application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.allowedDirectly.management");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_allowed_directly_by_management_to_applicant");
        assertThat(mails.get(0).getTemplateModel(locale)).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(colleague));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.allowed.to_colleagues");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_allowed_to_colleagues");
        assertThat(mails.get(1).getTemplateModel(locale)).isEqualTo(modelColleagues);
    }

    @Test
    void sendNewDirectlyAllowedApplicationNotification() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person person = new Person();
        person.setFirstName("Lord");
        person.setLastName("Helmchen");

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .requiresApprovalToApply(false)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2020, 12, 1));
        application.setEndDate(LocalDate.of(2020, 12, 2));
        application.setStatus(WAITING);

        final List<Person> recipients = singletonList(person);
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED)).thenReturn(recipients);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationTypeLabel", "vacation type label");
        model.put("comment", comment);

        sut.sendDirectlyAllowedNotificationToManagement(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(recipients);
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.allowedDirectly.boss");
        assertThat(mail.getSubjectMessageArguments()[0]).isEqualTo("Lord Helmchen");
        assertThat(mail.getTemplateName()).isEqualTo("application_allowed_directly_to_management");
        assertThat(mail.getTemplateModel(locale)).isEqualTo(model);
    }

    @Test
    void notifyHolidayReplacementAboutDirectlyAllowedApplication() {

        final Settings settings = new Settings();
        settings.setTimeSettings(new TimeSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final Person holidayReplacement = new Person();
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("awesome note");

        final Person applicant = new Person();
        applicant.setFirstName("Theo");
        applicant.setLastName("Fritz");

        final Application application = new Application();
        application.setPerson(applicant);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2020, 10, 2));
        application.setEndDate(LocalDate.of(2020, 10, 3));

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("holidayReplacement", holidayReplacement);
        model.put("holidayReplacementNote", "awesome note");

        sut.notifyHolidayReplacementAboutDirectlyAllowedApplication(replacementEntity, application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.allowedDirectly.holidayReplacement");
        assertThat(mail.getTemplateName()).isEqualTo("application_allowed_directly_to_holiday_replacement");
        assertThat(mail.getTemplateModel(GERMAN)).isEqualTo(model);
    }

    @Test
    void sendAppliedForLeaveByOfficeNotification() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person person = new Person();
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(FULL);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationTypeLabel", "vacation type label");
        model.put("comment", comment);

        sut.sendAppliedByManagementNotification(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.applied.management");
        assertThat(mail.getTemplateName()).isEqualTo("application_applied_by_management_to_applicant");
        assertThat(mail.getTemplateModel(locale)).isEqualTo(model);
    }

    @Test
    void sendCancelledDirectlyInformationToRecipientOfInterest() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person person = new Person();

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(FULL);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationTypeLabel", "vacation type label");
        model.put("comment", comment);

        final Person recipientOfInterest = new Person();
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION))
            .thenReturn(List.of(recipientOfInterest));

        sut.sendCancelledDirectlyToManagement(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(recipientOfInterest));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.cancelledDirectly.information.recipients_of_interest");
        assertThat(mail.getTemplateName()).isEqualTo("application_cancelled_directly_to_management");
        assertThat(mail.getTemplateModel(locale)).isEqualTo(model);
    }

    @Test
    void sendCancelledDirectlyConfirmationByApplicant() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Settings settings = new Settings();
        settings.setTimeSettings(new TimeSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final ByteArrayResource attachment = new ByteArrayResource("".getBytes());
        when(iCalService.getSingleAppointment(any(), any(), any())).thenReturn(attachment);

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Person person = new Person();
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Application application = new Application();
        application.setStartDate(LocalDate.of(2022, 3, 3));
        application.setEndDate(LocalDate.of(2022, 3, 3));
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(FULL);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Person colleague = new Person();
        colleague.setId(3L);
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION))
            .thenReturn(List.of(colleague));

        sut.sendCancelledDirectlyConfirmationByApplicant(application, comment);

        final Map<String, Object> model = Map.of(
            "application", application,
            "vacationTypeLabel", "vacation type label",
            "comment", comment
        );

        final Map<String, Object> modelColleagues = Map.of("application", application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.cancelledDirectly.user");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_cancelled_directly_confirmation_by_applicant_to_applicant");
        assertThat(mails.get(0).getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mails.get(0).getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
        assertThat(mails.get(0).getTemplateModel(locale)).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(colleague));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.cancelled.to_colleagues");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_cancellation_to_colleagues");
        assertThat(mails.get(1).getTemplateModel(locale)).isEqualTo(modelColleagues);
        assertThat(mails.get(1).getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mails.get(1).getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
    }

    @Test
    void sendCancelledDirectlyConfirmationByOffice() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person person = new Person();
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(FULL);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Person colleague = new Person();
        colleague.setId(3L);
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION))
            .thenReturn(List.of(colleague));

        sut.sendCancelledDirectlyConfirmationByManagement(application, comment);

        final Map<String, Object> model = Map.of(
            "application", application,
            "vacationTypeLabel", "vacation type label",
            "comment", comment
        );

        final Map<String, Object> modelColleagues = Map.of("application", application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.cancelledDirectly.management");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_cancelled_directly_confirmation_by_management_to_applicant");
        assertThat(mails.get(0).getTemplateModel(locale)).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(colleague));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.cancelled.to_colleagues");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_cancellation_to_colleagues");
        assertThat(mails.get(1).getTemplateModel(locale)).isEqualTo(modelColleagues);
    }

    @Test
    void sendCancelledConfirmationByManagement() {

        final Settings settings = new Settings();
        settings.setTimeSettings(new TimeSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final ByteArrayResource attachment = new ByteArrayResource("".getBytes());
        when(iCalService.getSingleAppointment(any(), any(), any())).thenReturn(attachment);

        final Person person = new Person();
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Person office = new Person();

        final Application application = new Application();
        application.setPerson(person);
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2020, 10, 2));
        application.setEndDate(LocalDate.of(2020, 10, 3));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", comment);

        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION))
            .thenReturn(List.of(person, office));

        final Person colleague = new Person();
        colleague.setId(3L);
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION))
            .thenReturn(List.of(colleague));

        sut.sendCancelledConfirmationByManagement(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(3)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.cancelled.user");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_cancelled_by_management_to_applicant");
        assertThat(mails.get(0).getTemplateModel(GERMAN)).isEqualTo(model);
        assertThat(mails.get(0).getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mails.get(0).getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(person, office));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.cancelled.management");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_cancelled_by_management_to_management");
        assertThat(mails.get(1).getTemplateModel(GERMAN)).isEqualTo(model);
        assertThat(mails.get(1).getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mails.get(1).getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
        assertThat(mails.get(2).getMailAddressRecipients()).hasValue(List.of(colleague));
        assertThat(mails.get(2).getSubjectMessageKey()).isEqualTo("subject.application.cancelled.to_colleagues");
        assertThat(mails.get(2).getTemplateName()).isEqualTo("application_cancellation_to_colleagues");
        assertThat(mails.get(2).getTemplateModel(GERMAN)).isEqualTo(Map.of("application", application));
        assertThat(mails.get(2).getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mails.get(2).getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
    }

    @Test
    void sendNewApplicationNotification() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person person = new Person();
        person.setFirstName("Lord");
        person.setLastName("Helmchen");

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2020, 12, 1));
        application.setEndDate(LocalDate.of(2020, 12, 2));
        application.setStatus(WAITING);

        final List<Person> recipients = singletonList(person);
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(recipients);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Application applicationForLeave = new Application();
        final List<Application> applicationsForLeave = singletonList(applicationForLeave);
        when(departmentService.getApplicationsFromColleaguesOf(person, LocalDate.of(2020, 12, 1), LocalDate.of(2020, 12, 2))).thenReturn(applicationsForLeave);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationTypeLabel", "vacation type label");
        model.put("comment", comment);
        model.put("departmentVacations", applicationsForLeave);

        sut.sendAppliedNotificationToManagement(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(recipients);
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.applied.boss");
        assertThat(mail.getSubjectMessageArguments()[0]).isEqualTo("Lord Helmchen");
        assertThat(mail.getTemplateName()).isEqualTo("application_applied_to_management");
        assertThat(mail.getTemplateModel(locale)).isEqualTo(model);
    }

    @Test
    void sendTemporaryAllowedNotification() {

        final Locale locale = JAPANESE;
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage("application.data.vacationType.holiday", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person person = new Person();
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED));
        final List<Person> recipients = singletonList(person);

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .id(1L)
            .category(HOLIDAY)
            .messageKey("application.data.vacationType.holiday")
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2020, 12, 1));
        application.setEndDate(LocalDate.of(2020, 12, 2));
        application.setStatus(WAITING);
        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED)).thenReturn(recipients);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "");

        final Application applicationForLeave = new Application();
        final List<Application> applicationsForLeave = singletonList(applicationForLeave);
        when(departmentService.getApplicationsFromColleaguesOf(person, LocalDate.of(2020, 12, 1), LocalDate.of(2020, 12, 2))).thenReturn(applicationsForLeave);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", comment);

        final Map<String, Object> modelSecondStage = new HashMap<>();
        modelSecondStage.put("application", application);
        modelSecondStage.put("vacationTypeLabel", "vacation type label");
        modelSecondStage.put("comment", comment);
        modelSecondStage.put("departmentVacations", applicationsForLeave);

        sut.sendTemporaryAllowedNotification(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.temporaryAllowed.user");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_temporary_allowed_to_applicant");
        assertThat(mails.get(0).getTemplateModel(locale)).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(recipients);
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.temporaryAllowed.management");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_temporary_allowed_to_management");
        assertThat(mails.get(1).getTemplateModel(locale)).isEqualTo(modelSecondStage);
    }

    @Test
    void sendRemindForStartsSoonApplicationsReminderNotification() {

        final Person person = new Person();
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(HOLIDAY)
            .build();

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2022, 1, 2));
        application.setEndDate(LocalDate.of(2022, 1, 3));
        application.setStatus(ALLOWED);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("daysBeforeUpcomingApplication", 1L);

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application, application));

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.remind.upcoming");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_cron_remind_for_upcoming_application_to_applicant");
        assertThat(mails.get(0).getTemplateModel(GERMAN)).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.remind.upcoming");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_cron_remind_for_upcoming_application_to_applicant");
        assertThat(mails.get(1).getTemplateModel(GERMAN)).isEqualTo(model);
    }

    @Test
    void sendRemindForUpcomingHolidayReplacement() {

        final Person holidayReplacement = new Person();
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING));
        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("Note");

        final Person holidayReplacementTwo = new Person();
        holidayReplacementTwo.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING));
        final HolidayReplacementEntity holidayReplacementEntityTwo = new HolidayReplacementEntity();
        holidayReplacementEntityTwo.setPerson(holidayReplacementTwo);
        holidayReplacementEntityTwo.setNote("Note 2");

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(HOLIDAY)
            .build();

        final Person applicant = new Person();
        applicant.setFirstName("Senior");
        applicant.setLastName("Thomas");
        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(applicant);
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2022, 1, 3));
        application.setEndDate(LocalDate.of(2022, 1, 4));
        application.setStatus(ALLOWED);
        application.setHolidayReplacements(List.of(holidayReplacementEntity, holidayReplacementEntityTwo));

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("daysBeforeUpcomingHolidayReplacement", 2L);
        model.put("replacementNote", "Note");

        final Map<String, Object> modelTwo = new HashMap<>();
        modelTwo.put("application", application);
        modelTwo.put("daysBeforeUpcomingHolidayReplacement", 2L);
        modelTwo.put("replacementNote", "Note 2");

        sut.sendRemindForUpcomingHolidayReplacement(List.of(application));

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.remind.upcoming.holiday_replacement");
        assertThat(mails.get(0).getSubjectMessageArguments()[0]).isEqualTo("Senior Thomas");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_cron_upcoming_holiday_replacement_to_holiday_replacement");
        assertThat(mails.get(0).getTemplateModel(GERMAN)).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(holidayReplacementTwo));
        assertThat(mails.get(1).getSubjectMessageArguments()[0]).isEqualTo("Senior Thomas");
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.remind.upcoming.holiday_replacement");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_cron_upcoming_holiday_replacement_to_holiday_replacement");
        assertThat(mails.get(1).getTemplateModel(GERMAN)).isEqualTo(modelTwo);
    }
}
