package org.synyx.urlaubsverwaltung.application.service;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMailServiceTest {

    private ApplicationMailService sut;

    @Mock
    private MailService mailService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ApplicationRecipientService applicationRecipientService;
    @Mock
    private MessageSource messageSource;

    @Before
    public void setUp() {
        sut = new ApplicationMailService(mailService, departmentService, applicationRecipientService, messageSource);
    }

    @Test
    public void ensureSendsAllowedNotificationToOffice() {

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

        final ApplicationComment applicationComment = new ApplicationComment(person);

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", "something");
        model.put("dayLength", "something");
        model.put("comment", applicationComment);

        sut.sendAllowedNotification(application, applicationComment);

        verify(mailService).sendMailTo(person, "subject.application.allowed.user", "allowed_user", model);
        verify(mailService).sendMailTo(NOTIFICATION_OFFICE, "subject.application.allowed.office", "allowed_office", model);
    }


    @Test
    public void sendRejectedNotification() {

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

        final ApplicationComment applicationComment = new ApplicationComment(person);

        Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", "something");
        model.put("dayLength", "something");
        model.put("comment", applicationComment);

        sut.sendRejectedNotification(application, applicationComment);

        verify(mailService).sendMailTo(person, "subject.application.rejected", "rejected", model);
    }

    @Test
    public void sendReferApplicationNotification() {

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

        verify(mailService).sendMailTo(recipient, "subject.application.refer", "refer", model);
    }

    @Test
    public void ensureMailIsSentToAllRecipientsThatHaveAnEmailAddress() {

        final Application application = new Application();
        final ApplicationComment applicationComment = new ApplicationComment(new Person());

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", applicationComment);

        sut.sendCancellationRequest(application, applicationComment);

        verify(mailService).sendMailTo(NOTIFICATION_OFFICE, "subject.application.cancellationRequest", "application_cancellation_request", model);
    }

    @Test
    public void sendSickNoteConvertedToVacationNotification() {
        final Person person = new Person();

        final Application application = new Application();
        application.setPerson(person);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);

        sut.sendSickNoteConvertedToVacationNotification(application);

        verify(mailService).sendMailTo(person, "subject.sicknote.converted", "sicknote_converted", model);
    }

    @Test
    public void notifyHolidayReplacement() {

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

        verify(mailService).sendMailTo(holidayReplacement, "subject.application.holidayReplacement", "notify_holiday_replacement", model);
    }

    @Test
    public void sendConfirmation() {

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

        final ApplicationComment comment = new ApplicationComment(person);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", "HOLIDAY");
        model.put("dayLength", "FULL");
        model.put("comment", comment);

        sut.sendConfirmation(application, comment);

        verify(mailService).sendMailTo(person, "subject.application.applied.user", "confirm", model);
    }


    @Test
    public void sendAppliedForLeaveByOfficeNotification() {

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

        final ApplicationComment comment = new ApplicationComment(person);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("vacationType", "HOLIDAY");
        model.put("dayLength", "FULL");
        model.put("comment", comment);

        sut.sendAppliedForLeaveByOfficeNotification(application, comment);

        verify(mailService).sendMailTo(person, "subject.application.appliedByOffice", "new_application_by_office", model);
    }

    @Test
    public void sendCancelledByOfficeNotification() {

        final Person person = new Person();

        final Application application = new Application();
        application.setPerson(person);

        final ApplicationComment comment = new ApplicationComment(person);

        final Map<String, Object> model = new HashMap<>();
        model.put("application", application);
        model.put("comment", comment);

        sut.sendCancelledByOfficeNotification(application, comment);

        verify(mailService).sendMailTo(person, "subject.application.cancelled.user", "cancelled_by_office", model);
    }


    @Test
    public void sendNewApplicationNotification() {

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

        final ApplicationComment comment = new ApplicationComment(person);

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

        verify(mailService).sendMailToEach(recipients, "subject.application.applied.boss", "new_applications", model, "Lord Helmchen");
    }


    @Test
    public void sendTemporaryAllowedNotification() {

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

        final ApplicationComment comment = new ApplicationComment(person);

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

        verify(mailService).sendMailTo(person, "subject.application.temporaryAllowed.user", "temporary_allowed_user", model);
        verify(mailService).sendMailToEach(recipients, "subject.application.temporaryAllowed.secondStage", "temporary_allowed_second_stage_authority", modelSecondStage);
    }
}
