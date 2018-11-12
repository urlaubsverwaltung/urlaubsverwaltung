package org.synyx.urlaubsverwaltung.core.mail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.springframework.context.MessageSource;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.settings.MailSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.core.mail.MailServiceImpl.LOCALE;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class MailServiceImplTest {

    private MailServiceImpl mailService;

    private MessageSource messageSource;
    private MailBuilder mailBuilder;
    private MailSender mailSender;
    private PersonService personService;
    private DepartmentService departmentService;

    private Application application;
    private Settings settings;

    @Before
    public void setUp() throws Exception {

        messageSource = Mockito.mock(MessageSource.class);
        mailBuilder = Mockito.mock(MailBuilder.class);
        mailSender = Mockito.mock(MailSender.class);
        personService = Mockito.mock(PersonService.class);
        departmentService = Mockito.mock(DepartmentService.class);

        SettingsService settingsService = Mockito.mock(SettingsService.class);

        // TODO: Would be better to mock this service directly
        RecipientService recipientService = new RecipientService(personService, departmentService);

        mailService = new MailServiceImpl(messageSource, mailBuilder, mailSender, recipientService, departmentService,
                settingsService);

        Person person = TestDataCreator.createPerson();

        application = createApplication(person);

        settings = new Settings();
        settings.getMailSettings().setActive(true);

        when(settingsService.getSettings()).thenReturn(settings);
    }


    private Application createApplication(Person person) {

        Application application = new Application();
        application.setPerson(person);
        application.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        application.setDayLength(DayLength.FULL);

        return application;
    }


    @Test
    public void ensureMailIsSentToAllRecipientsThatHaveAnEmailAddress() {

        Person person = TestDataCreator.createPerson("muster", "Max", "Mustermann", "max@firma.test");
        Person anotherPerson = TestDataCreator.createPerson("mmuster", "Marlene", "Muster", "max@firma.test");
        Person personWithoutMailAddress = TestDataCreator.createPerson("nomail", "No", "Mail", null);

        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_OFFICE))
            .thenReturn(Arrays.asList(person, anotherPerson, personWithoutMailAddress));

        ArgumentCaptor<List> recipientsArgumentCaptor = ArgumentCaptor.forClass(List.class);

        mailService.sendCancellationRequest(application, null);

        Mockito.verify(mailSender)
            .sendEmail(eq(settings.getMailSettings()), recipientsArgumentCaptor.capture(), Mockito.anyString(),
                Mockito.anyString());

        List value = recipientsArgumentCaptor.getValue();
        Assert.assertEquals("Wrong number of recipients", 2, value.size());
    }


    @Test
    public void ensureSendsNewApplicationNotificationToBosses() {

        mailService.sendNewApplicationNotification(application, null);

        Mockito.verify(personService).getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);
    }

    @Test
    public void ensureSendsNewApplicationNotificationSubjectIncludesApplicationPersonName () {

        Person boss = TestDataCreator.createPerson("boss");
        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS))
                .thenReturn(Collections.singletonList(boss));
        when(messageSource.getMessage("subject.application.applied.boss", new String[]{"Marlene Muster"}, LOCALE)).thenReturn("Neuer Urlaubsantrag für Marlene Muster");

        mailService.sendNewApplicationNotification(application, null);

        verify(mailSender).sendEmail(any(MailSettings.class), any(List.class), eq("Neuer Urlaubsantrag für "+ application.getPerson().getNiceName()), anyString());
    }

    @Test
    public void ensureSendsNewApplicationNotificationToDepartmentHeads() {

        Person boss = TestDataCreator.createPerson("boss");
        Person departmentHead = TestDataCreator.createPerson("departmentHead");

        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS))
            .thenReturn(Collections.singletonList(boss));

        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD))
            .thenReturn(Collections.singletonList(departmentHead));

        mailService.sendNewApplicationNotification(application, null);

        Mockito.verify(personService).getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD);
        Mockito.verify(departmentService)
            .isDepartmentHeadOfPerson(eq(departmentHead), eq(application.getPerson()));
    }


    @Test
    public void ensureSendsRemindNotificationToBosses() {

        mailService.sendRemindBossNotification(application);

        Mockito.verify(personService).getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);
    }


    @Test
    public void ensureSendsRemindNotificationToDepartmentHeads() {

        Person boss = TestDataCreator.createPerson("boss");
        Person departmentHead = TestDataCreator.createPerson("departmentHead");

        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS))
            .thenReturn(Collections.singletonList(boss));

        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD))
            .thenReturn(Collections.singletonList(departmentHead));

        mailService.sendRemindBossNotification(application);

        Mockito.verify(personService).getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD);
        Mockito.verify(departmentService)
            .isDepartmentHeadOfPerson(eq(departmentHead), eq(application.getPerson()));
    }


    @Test
    public void ensureSendRemindForWaitingApplicationsReminderNotification() throws Exception {

        Person boss = TestDataCreator.createPerson("boss");
        Person departmentHeadAC = TestDataCreator.createPerson("departmentHeadAC");
        Person departmentHeadB = TestDataCreator.createPerson("departmentHeadB");

        Person personDepartmentA = TestDataCreator.createPerson("personDepartmentA");
        Person personDepartmentB = TestDataCreator.createPerson("personDepartmentB");
        Person personDepartmentC = TestDataCreator.createPerson("personDepartmentC");

        Application applicationA = createApplication(personDepartmentA);
        Application applicationB = createApplication(personDepartmentB);
        Application applicationC = createApplication(personDepartmentC);

        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS))
            .thenReturn(Collections.singletonList(boss));

        when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD))
            .thenReturn(Arrays.asList(departmentHeadAC, departmentHeadB));

        when(departmentService.isDepartmentHeadOfPerson(departmentHeadAC, personDepartmentA)).thenReturn(true);
        when(departmentService.isDepartmentHeadOfPerson(departmentHeadB, personDepartmentB)).thenReturn(true);
        when(departmentService.isDepartmentHeadOfPerson(departmentHeadAC, personDepartmentC)).thenReturn(true);

        mailService.sendRemindForWaitingApplicationsReminderNotification(Arrays.asList(applicationA, applicationB,
                applicationC));

        Mockito.verify(personService, times(3))
            .getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD);
        Mockito.verify(personService, times(3)).getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);
        Mockito.verify(departmentService)
            .isDepartmentHeadOfPerson(eq(departmentHeadAC), eq(applicationA.getPerson()));
        Mockito.verify(departmentService)
            .isDepartmentHeadOfPerson(eq(departmentHeadB), eq(applicationB.getPerson()));
        Mockito.verify(departmentService)
            .isDepartmentHeadOfPerson(eq(departmentHeadAC), eq(applicationC.getPerson()));
    }


    @Test
    public void ensureSendsAllowedNotificationToOffice() {

        mailService.sendAllowedNotification(application, null);

        Mockito.verify(personService).getPersonsWithNotificationType(MailNotification.NOTIFICATION_OFFICE);
    }
}
