package org.synyx.urlaubsverwaltung.application.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.calendar.ICalService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.io.File;
import java.time.Clock;
import java.time.LocalDate;
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
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
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
        settings.setAbsenceSettings(new AbsenceSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        final File file = new File("");
        when(iCalService.getCalendar(any(), any())).thenReturn(file);

        when(messageSource.getMessage(any(), any(), any())).thenReturn("something");

        final Person person = new Person();

        final VacationType vacationType = new VacationType();
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
        assertThat(mails.get(0).getMailAttachments().get().get(0).getFile()).isEqualTo(file);
        assertThat(mails.get(0).getMailAttachments().get().get(0).getName()).isEqualTo("calendar.ical");
        assertThat(mails.get(1).getMailNotificationRecipients()).hasValue(NOTIFICATION_OFFICE);
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.application.allowed.office");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("allowed_office");
        assertThat(mails.get(1).getTemplateModel()).isEqualTo(model);
    }


    @Test
    void sendRejectedNotification() {

        when(messageSource.getMessage(any(), any(), any())).thenReturn("something");

        final Person person = new Person();

        final VacationType vacationType = new VacationType();
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

        sut.sendRejectedNotification(application, applicationComment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.rejected");
        assertThat(mail.getTemplateName()).isEqualTo("rejected");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void sendReferApplicationNotification() {

        final Person recipient = new Person();
        final Person sender = new Person();

        final VacationType vacationType = new VacationType();
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

        final VacationType vacationType = new VacationType();
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
    void ensureMailIsSentToAllRecipientsThatHaveAnEmailAddress() {

        final Application application = new Application();
        final ApplicationComment applicationComment = new ApplicationComment(new Person(), clock);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", applicationComment);

        sut.sendCancellationRequest(application, applicationComment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailNotificationRecipients()).hasValue(NOTIFICATION_OFFICE);
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.cancellationRequest");
        assertThat(mail.getTemplateName()).isEqualTo("application_cancellation_request");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
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
    void notifyHolidayReplacement() {

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final Person holidayReplacement = new Person();

        final Application application = new Application();
        application.setHolidayReplacement(holidayReplacement);
        application.setDayLength(dayLength);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("dayLength", "FULL");

        sut.notifyHolidayReplacement(application);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(holidayReplacement));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.holidayReplacement");
        assertThat(mail.getTemplateName()).isEqualTo("notify_holiday_replacement");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void sendConfirmation() {

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final VacationCategory vacationCategory = HOLIDAY;
        when(messageSource.getMessage(eq(vacationCategory.getMessageKey()), any(), any())).thenReturn("HOLIDAY");

        final Person person = new Person();

        final VacationType vacationType = new VacationType();
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
    void sendAppliedForLeaveByOfficeNotification() {

        final DayLength dayLength = FULL;
        when(messageSource.getMessage(eq(dayLength.name()), any(), any())).thenReturn("FULL");

        final VacationCategory vacationCategory = HOLIDAY;
        when(messageSource.getMessage(eq(vacationCategory.getMessageKey()), any(), any())).thenReturn("HOLIDAY");

        final Person person = new Person();

        final VacationType vacationType = new VacationType();
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

        final Person person = new Person();

        final Application application = new Application();
        application.setPerson(person);

        final ApplicationComment comment = new ApplicationComment(person, clock);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", comment);

        sut.sendCancelledByOfficeNotification(application, comment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.application.cancelled.user");
        assertThat(mail.getTemplateName()).isEqualTo("cancelled_by_office");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
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

        final VacationType vacationType = new VacationType();
        vacationType.setCategory(vacationCategory);

        final Application application = new Application();
        application.setVacationType(vacationType);
        application.setPerson(person);
        application.setDayLength(dayLength);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setStatus(WAITING);

        final List<Person> recipients = singletonList(person);
        when(applicationRecipientService.getRecipientsForAllowAndRemind(application)).thenReturn(recipients);

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

        final VacationType vacationType = new VacationType();
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
}
