package org.synyx.urlaubsverwaltung.application.application;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.calendar.ICalService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;

@ExtendWith(MockitoExtension.class)
class ApplicationMailServiceTest {

    private ApplicationMailService sut;

    @Mock
    private MailService mailService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ApplicationRecipientService applicationRecipientService;
    @Mock
    private MessageSource messageSource;
    @Mock
    private ICalService iCalService;
    @Mock
    private SettingsService settingsService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new ApplicationMailService(mailService, departmentService, applicationRecipientService, iCalService, messageSource, settingsService);
    }

    @Test
    void ensureSendsAllowedNotificationToOffice() {

        final Settings settings = new Settings();
        settings.setApplicationSettings(new ApplicationSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final ByteArrayResource attachment = new ByteArrayResource("".getBytes());
        when(iCalService.getSingleAppointment(any(), any())).thenReturn(attachment);

        when(messageSource.getMessage(any(), any(), any())).thenReturn("something");

        final Person person = new Person();

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(HOLIDAY);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setDayLength(FULL);
        application.setPerson(person);
        application.setStartDate(LocalDate.of(2020, 12, 1));
        application.setEndDate(LocalDate.of(2020, 12, 2));
        application.setStatus(ALLOWED);

        final ApplicationComment applicationComment = new ApplicationComment(person, clock);

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", "something");
        model.put("dayLength", "something");
        model.put("comment", applicationComment);

        sut.sendAllowedNotification(application, applicationComment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.allowed.user");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("allowed_user");
        assertThat(mails.get(0).getTemplateModel()).isEqualTo(model);
        assertThat(mails.get(0).getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mails.get(0).getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
        assertThat(mails.get(1).getMailNotificationRecipients()).hasValue(NOTIFICATION_OFFICE);
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.allowed.office");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("allowed_office");
        assertThat(mails.get(1).getTemplateModel()).isEqualTo(model);
    }


    @Test
    void sendRejectedNotification() {

        when(messageSource.getMessage(any(), any(), any())).thenReturn("something");

        final Person person = new Person();

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(HOLIDAY);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setDayLength(FULL);
        application.setPerson(person);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setStatus(ALLOWED);

        final ApplicationComment applicationComment = new ApplicationComment(person, clock);

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", "something");
        model.put("dayLength", "something");
        model.put("comment", applicationComment);

        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(List.of(person));

        sut.sendRejectedNotification(application, applicationComment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.rejected");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("rejected");
        assertThat(mails.get(0).getTemplateModel()).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.rejected_information");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("rejected_information");
        assertThat(mails.get(1).getTemplateModel()).isEqualTo(model);
    }

    @Test
    void sendReferApplicationNotification() {

        final Person recipient = new Person();
        final Person sender = new Person();

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(HOLIDAY);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setDayLength(FULL);
        application.setPerson(recipient);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setStatus(ALLOWED);

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("recipient", recipient);
        model.put("sender", sender);

        sut.sendReferApplicationNotification(application, recipient, sender);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(recipient));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.refer");
        assertThat(mail.getTemplateName()).isEqualTo("refer");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void sendEditedApplicationNotification() {

        final Person recipient = new Person();

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(HOLIDAY);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setDayLength(FULL);
        application.setPerson(recipient);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setStatus(ALLOWED);

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("recipient", recipient);

        sut.sendEditedApplicationNotification(application, recipient);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(recipient));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.edited");
        assertThat(mail.getTemplateName()).isEqualTo("edited");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void sendDeclinedCancellationRequestApplicationNotification() {

        final Application application = new Application();
        final Person person = new Person();
        application.setPerson(person);

        final ApplicationComment comment = new ApplicationComment(person, clock);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", comment);

        final Person office = new Person();
        office.setId(1);
        final List<Person> officeWorkers = List.of(office);
        when(applicationRecipientService.getRecipientsWithOfficeNotifications()).thenReturn(officeWorkers);

        final Person relevantPerson = new Person();
        relevantPerson.setId(2);
        final List<Person> relevantPersons = new ArrayList<>();
        relevantPersons.add(relevantPerson);
        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(relevantPersons);

        sut.sendDeclinedCancellationRequestApplicationNotification(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.cancellationRequest.declined.applicant");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_cancellation_request_declined_applicant");
        assertThat(mails.get(0).getTemplateModel()).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(relevantPerson, office));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.cancellationRequest.declined.management");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_cancellation_request_declined_management");
        assertThat(mails.get(1).getTemplateModel()).isEqualTo(model);
    }

    @Test
    void ensureMailIsSentToAllRecipientsThatHaveAnEmailAddress() {

        final Application application = new Application();
        final Person person = new Person();
        application.setPerson(person);
        final ApplicationComment applicationComment = new ApplicationComment(person, clock);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", applicationComment);

        final List<Person> relevantPersons = List.of(new Person());
        when(applicationRecipientService.getRecipientsWithOfficeNotifications()).thenReturn(relevantPersons);

        sut.sendCancellationRequest(application, applicationComment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.cancellationRequest.applicant");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("application_cancellation_request_applicant");
        assertThat(mails.get(0).getTemplateModel()).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(relevantPersons);
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.cancellationRequest");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("application_cancellation_request");
        assertThat(mails.get(1).getTemplateModel()).isEqualTo(model);
    }

    @Test
    void sendSickNoteConvertedToVacationNotification() {
        final Person person = new Person();

        final Application application = new Application();
        application.setPerson(person);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);

        sut.sendSickNoteConvertedToVacationNotification(application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.sicknote.converted");
        assertThat(mail.getTemplateName()).isEqualTo("sicknote_converted");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void notifyHolidayReplacementAllow() {

        final Settings settings = new Settings();
        settings.setApplicationSettings(new ApplicationSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final Person holidayReplacement = new Person();
        final Person applicant = new Person();
        applicant.setFirstName("Theo");
        applicant.setLastName("Fritz");

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("awesome replacement note");

        final Application application = new Application();
        application.setPerson(applicant);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setDayLength(dayLength);
        application.setStartDate(LocalDate.of(2020, 3, 5));
        application.setEndDate(LocalDate.of(2020, 3, 6));

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("holidayReplacement", holidayReplacement);
        model.put("holidayReplacementNote", "awesome replacement note");
        model.put("dayLength", "FULL");

        final ByteArrayResource attachment = new ByteArrayResource("".getBytes());
        when(iCalService.getSingleAppointment(any(), any())).thenReturn(attachment);

        sut.notifyHolidayReplacementAllow(replacementEntity, application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.holidayReplacement.allow");
        assertThat(mail.getTemplateName()).isEqualTo("notify_holiday_replacement_allow");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
        assertThat(mail.getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mail.getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
    }

    @Test
    void notifyHolidayReplacementAboutEdit() {

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final Person holidayReplacement = new Person();

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("awesome note");

        final Person applicant = new Person();
        applicant.setFirstName("Theo");
        applicant.setLastName("Fritz");

        final Application application = new Application();
        application.setPerson(applicant);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setDayLength(dayLength);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("holidayReplacement", holidayReplacement);
        model.put("holidayReplacementNote", "awesome note");
        model.put("dayLength", "FULL");

        sut.notifyHolidayReplacementAboutEdit(replacementEntity, application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.holidayReplacement.edit");
        assertThat(mail.getTemplateName()).isEqualTo("notify_holiday_replacement_edit");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void notifyHolidayReplacementForApply() {

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final Person holidayReplacement = new Person();

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("awesome note");

        final Person applicant = new Person();
        applicant.setFirstName("Theo");
        applicant.setLastName("Fritz");

        final Application application = new Application();
        application.setPerson(applicant);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setDayLength(dayLength);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("holidayReplacement", holidayReplacement);
        model.put("holidayReplacementNote", "awesome note");
        model.put("dayLength", "FULL");

        sut.notifyHolidayReplacementForApply(replacementEntity, application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.holidayReplacement.apply");
        assertThat(mail.getTemplateName()).isEqualTo("notify_holiday_replacement_apply");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void notifyHolidayReplacementAboutCancellation() {

        final Settings settings = new Settings();
        settings.setTimeSettings(new TimeSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final ByteArrayResource attachment = new ByteArrayResource("".getBytes());
        when(iCalService.getSingleAppointment(any(), any())).thenReturn(attachment);

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final Person applicant = new Person();
        applicant.setFirstName("Thomas");
        final Person holidayReplacement = new Person();

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("awesome note");

        final Application application = new Application();
        application.setPerson(applicant);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setDayLength(dayLength);
        application.setStartDate(LocalDate.of(2020, 10, 2));
        application.setEndDate(LocalDate.of(2020, 10, 3));

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("holidayReplacement", holidayReplacement);
        model.put("dayLength", "FULL");

        sut.notifyHolidayReplacementAboutCancellation(replacementEntity, application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.holidayReplacement.cancellation");
        assertThat(mail.getTemplateName()).isEqualTo("notify_holiday_replacement_cancellation");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
        assertThat(mail.getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mail.getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
    }

    @Test
    void sendConfirmation() {

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final VacationCategory vacationCategory = HOLIDAY;
        when(messageSource.getMessage(eq(vacationCategory.getMessageKey()), any(), any())).thenReturn("HOLIDAY");

        final Person person = new Person();

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(vacationCategory);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(dayLength);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setStatus(WAITING);

        final ApplicationComment comment = new ApplicationComment(person, clock);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", "HOLIDAY");
        model.put("dayLength", "FULL");
        model.put("comment", comment);

        sut.sendConfirmation(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.applied.user");
        assertThat(mail.getTemplateName()).isEqualTo("confirm");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void sendConfirmationAllowedDirectly() {

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final VacationCategory vacationCategory = HOLIDAY;
        when(messageSource.getMessage(eq(vacationCategory.getMessageKey()), any(), any())).thenReturn("HOLIDAY");

        final Person person = new Person();

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setRequiresApproval(false);
        vacationType.setCategory(vacationCategory);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(dayLength);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setStatus(WAITING);

        final ApplicationComment comment = new ApplicationComment(person, clock);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", "HOLIDAY");
        model.put("dayLength", "FULL");
        model.put("comment", comment);

        sut.sendConfirmationAllowedDirectly(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.allowedDirectly.user");
        assertThat(mail.getTemplateName()).isEqualTo("confirm_allowed_directly");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void sendConfirmationAllowedDirectlyByOffice() {

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final VacationCategory vacationCategory = HOLIDAY;
        when(messageSource.getMessage(eq(vacationCategory.getMessageKey()), any(), any())).thenReturn("HOLIDAY");

        final Person person = new Person();

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setRequiresApproval(false);
        vacationType.setCategory(vacationCategory);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(dayLength);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setStatus(WAITING);

        final ApplicationComment comment = new ApplicationComment(person, clock);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", "HOLIDAY");
        model.put("dayLength", "FULL");
        model.put("comment", comment);

        sut.sendConfirmationAllowedDirectlyByOffice(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.allowedDirectly.office");
        assertThat(mail.getTemplateName()).isEqualTo("new_directly_allowed_applications_by_office");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void sendNewDirectlyAllowedApplicationNotification() {

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final VacationCategory vacationCategory = HOLIDAY;
        when(messageSource.getMessage(eq(vacationCategory.getMessageKey()), any(), any())).thenReturn("HOLIDAY");

        final Person person = new Person();
        person.setFirstName("Lord");
        person.setLastName("Helmchen");

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setRequiresApproval(false);
        vacationType.setCategory(vacationCategory);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(dayLength);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setStatus(WAITING);

        final List<Person> recipients = singletonList(person);
        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(recipients);

        final ApplicationComment comment = new ApplicationComment(person, clock);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", "HOLIDAY");
        model.put("dayLength", "FULL");
        model.put("comment", comment);

        sut.sendNewDirectlyAllowedApplicationNotification(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(recipients);
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.allowedDirectly.boss");
        assertThat(mail.getSubjectMessageArguments()[0]).isEqualTo("Lord Helmchen");
        assertThat(mail.getTemplateName()).isEqualTo("new_directly_allowed_applications");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void notifyHolidayReplacementAboutDirectlyAllowedApplication() {

        final Settings settings = new Settings();
        settings.setTimeSettings(new TimeSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final Person holidayReplacement = new Person();

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("awesome note");

        final Person applicant = new Person();
        applicant.setFirstName("Theo");
        applicant.setLastName("Fritz");

        final Application application = new Application();
        application.setPerson(applicant);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setDayLength(dayLength);
        application.setStartDate(LocalDate.of(2020, 10, 2));
        application.setEndDate(LocalDate.of(2020, 10, 3));

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("holidayReplacement", holidayReplacement);
        model.put("holidayReplacementNote", "awesome note");
        model.put("dayLength", "FULL");

        sut.notifyHolidayReplacementAboutDirectlyAllowedApplication(replacementEntity, application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.allowedDirectly.holidayReplacement");
        assertThat(mail.getTemplateName()).isEqualTo("notify_holiday_replacement_directly_allow");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void sendAppliedForLeaveByOfficeNotification() {

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final VacationCategory vacationCategory = HOLIDAY;
        when(messageSource.getMessage(eq(vacationCategory.getMessageKey()), any(), any())).thenReturn("HOLIDAY");

        final Person person = new Person();

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(vacationCategory);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(dayLength);

        final ApplicationComment comment = new ApplicationComment(person, clock);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", "HOLIDAY");
        model.put("dayLength", "FULL");
        model.put("comment", comment);

        sut.sendAppliedForLeaveByOfficeNotification(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.appliedByOffice");
        assertThat(mail.getTemplateName()).isEqualTo("new_application_by_office");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void sendCancelledByOfficeNotification() {

        final Settings settings = new Settings();
        settings.setTimeSettings(new TimeSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final ByteArrayResource attachment = new ByteArrayResource("".getBytes());
        when(iCalService.getSingleAppointment(any(), any())).thenReturn(attachment);

        final Person person = new Person();
        final Person office = new Person();

        final Application application = new Application();
        application.setPerson(person);
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.of(2020, 10, 2));
        application.setEndDate(LocalDate.of(2020, 10, 3));

        final ApplicationComment comment = new ApplicationComment(person, clock);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", comment);

        final List<Person> relevantPersons = new ArrayList<>();
        relevantPersons.add(person);

        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(relevantPersons);
        when(applicationRecipientService.getRecipientsWithOfficeNotifications()).thenReturn(List.of(office));

        sut.sendCancelledByOfficeNotification(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.cancelled.user");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("cancelled_by_office");
        assertThat(mails.get(0).getTemplateModel()).isEqualTo(model);
        assertThat(mails.get(0).getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mails.get(0).getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(person, office));
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.cancelled.management");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("cancelled_by_office_management");
        assertThat(mails.get(1).getTemplateModel()).isEqualTo(model);
        assertThat(mails.get(1).getMailAttachments().get().get(0).getContent()).isEqualTo(attachment);
        assertThat(mails.get(1).getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ics");
    }


    @Test
    void sendNewApplicationNotification() {

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final VacationCategory vacationCategory = HOLIDAY;
        when(messageSource.getMessage(eq(vacationCategory.getMessageKey()), any(), any())).thenReturn("HOLIDAY");

        final Person person = new Person();
        person.setFirstName("Lord");
        person.setLastName("Helmchen");

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(vacationCategory);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(dayLength);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setStatus(WAITING);

        final List<Person> recipients = singletonList(person);
        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(recipients);

        final ApplicationComment comment = new ApplicationComment(person, clock);

        final Application applicationForLeave = new Application();
        final List<Application> applicationsForLeave = singletonList(applicationForLeave);
        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, LocalDate.MIN, LocalDate.MAX)).thenReturn(applicationsForLeave);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", "HOLIDAY");
        model.put("dayLength", "FULL");
        model.put("comment", comment);
        model.put("departmentVacations", applicationsForLeave);

        sut.sendNewApplicationNotification(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(recipients);
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.applied.boss");
        assertThat(mail.getSubjectMessageArguments()[0]).isEqualTo("Lord Helmchen");
        assertThat(mail.getTemplateName()).isEqualTo("new_applications");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }


    @Test
    void sendTemporaryAllowedNotification() {

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final VacationCategory vacationCategory = HOLIDAY;
        when(messageSource.getMessage(eq(vacationCategory.getMessageKey()), any(), any())).thenReturn("HOLIDAY");

        final Person person = new Person();
        final List<Person> recipients = singletonList(person);

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(vacationCategory);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(dayLength);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setStatus(WAITING);
        when(applicationRecipientService.getRecipientsForTemporaryAllow(application)).thenReturn(recipients);

        final ApplicationComment comment = new ApplicationComment(person, clock);

        final Application applicationForLeave = new Application();
        final List<Application> applicationsForLeave = singletonList(applicationForLeave);
        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, LocalDate.MIN, LocalDate.MAX)).thenReturn(applicationsForLeave);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("dayLength", "FULL");
        model.put("comment", comment);

        final Map<String, Object> modelSecondStage = new HashMap<>();
        modelSecondStage.put("application", application);
        modelSecondStage.put("vacationType", "HOLIDAY");
        modelSecondStage.put("dayLength", "FULL");
        modelSecondStage.put("comment", comment);
        modelSecondStage.put("departmentVacations", applicationsForLeave);

        sut.sendTemporaryAllowedNotification(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.temporaryAllowed.user");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("temporary_allowed_user");
        assertThat(mails.get(0).getTemplateModel()).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(recipients);
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.temporaryAllowed.secondStage");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("temporary_allowed_second_stage_authority");
        assertThat(mails.get(1).getTemplateModel()).isEqualTo(modelSecondStage);
    }

    @Test
    void sendRemindForStartsSoonApplicationsReminderNotification() {

        final Person person = new Person();
        final List<Person> recipients = singletonList(person);

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(HOLIDAY);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setStatus(ALLOWED);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("daysBeforeUpcomingApplication", 1);

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application, application), 1);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.remind.upcoming");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("remind_application_upcoming");
        assertThat(mails.get(0).getTemplateModel()).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(recipients);
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.remind.upcoming");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("remind_application_upcoming");
        assertThat(mails.get(1).getTemplateModel()).isEqualTo(model);
    }


    @Test
    void sendRemindForUpcomingHolidayReplacement() {

        final Person holidayReplacement = new Person();
        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("Note");

        final Person holidayReplacementTwo = new Person();
        final HolidayReplacementEntity holidayReplacementEntityTwo = new HolidayReplacementEntity();
        holidayReplacementEntityTwo.setPerson(holidayReplacementTwo);
        holidayReplacementEntityTwo.setNote("Note 2");

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(HOLIDAY);

        final Person applicant = new Person();
        applicant.setFirstName("Senior");
        applicant.setLastName("Thomas");
        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(applicant);
        application.setDayLength(FULL);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setStatus(ALLOWED);
        application.setHolidayReplacements(List.of(holidayReplacementEntity, holidayReplacementEntityTwo));

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("daysBeforeUpcomingHolidayReplacement", 1);
        model.put("replacementNote", "Note");

        final Map<String, Object> modelTwo = new HashMap<>();
        modelTwo.put("application", application);
        modelTwo.put("daysBeforeUpcomingHolidayReplacement", 1);
        modelTwo.put("replacementNote", "Note 2");

        sut.sendRemindForUpcomingHolidayReplacement(List.of(application), 1);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService, times(2)).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.application.remind.upcoming.holiday_replacement");
        assertThat(mails.get(0).getSubjectMessageArguments()[0]).isEqualTo("Senior Thomas");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("remind_holiday_replacement_upcoming");
        assertThat(mails.get(0).getTemplateModel()).isEqualTo(model);
        assertThat(mails.get(1).getMailAddressRecipients()).hasValue(List.of(holidayReplacementTwo));
        assertThat(mails.get(1).getSubjectMessageArguments()[0]).isEqualTo("Senior Thomas");
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.remind.upcoming.holiday_replacement");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("remind_holiday_replacement_upcoming");
        assertThat(mails.get(1).getTemplateModel()).isEqualTo(modelTwo);
    }
}
