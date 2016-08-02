package org.synyx.urlaubsverwaltung.core.mail;

import org.apache.velocity.app.VelocityEngine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

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

import static org.mockito.Mockito.times;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class MailServiceImplTest {

    private MailServiceImpl mailService;

    private JavaMailSenderImpl mailSender;
    private PersonService personService;
    private DepartmentService departmentService;

    private Application application;
    private Settings settings;

    @Before
    public void setUp() throws Exception {

        VelocityEngine velocityEngine = Mockito.mock(VelocityEngine.class);
        mailSender = Mockito.mock(JavaMailSenderImpl.class);
        personService = Mockito.mock(PersonService.class);
        departmentService = Mockito.mock(DepartmentService.class);

        SettingsService settingsService = Mockito.mock(SettingsService.class);

        mailService = new MailServiceImpl(mailSender, velocityEngine, personService, departmentService,
                settingsService);

        Person person = TestDataCreator.createPerson();

        application = createApplication(person);

        settings = new Settings();
        settings.getMailSettings().setActive(true);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);
    }

    private Application createApplication(Person person) {

        Application application = new Application();
        application.setPerson(person);
        application.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        application.setDayLength(DayLength.FULL);

        return application;
    }


    @Test
    public void ensureNoMailSentIfSendingMailsIsDeactivated() {

        settings.getMailSettings().setActive(false);

        Person person = TestDataCreator.createPerson();

        mailService.sendEmail(settings.getMailSettings(), Collections.singletonList(person), "subject", "text");

        Mockito.verifyZeroInteractions(mailSender);
    }


    @Test
    public void ensureMailSenderAttributesAreUpdatedWhenSendingMails() {

        MailSettings mailSettings = settings.getMailSettings();

        Person person = TestDataCreator.createPerson();

        mailService.sendEmail(mailSettings, Collections.singletonList(person), "subject", "text");

        Mockito.verify(mailSender).setHost(mailSettings.getHost());
        Mockito.verify(mailSender).setPort(mailSettings.getPort());
        Mockito.verify(mailSender).setUsername(mailSettings.getUsername());
        Mockito.verify(mailSender).setPassword(mailSettings.getPassword());
    }


    @Test
    public void ensureMailIsSentToAllRecipientsThatHaveAnEmailAddress() {

        Person person = TestDataCreator.createPerson("muster", "Max", "Mustermann", "max@muster.de");
        Person anotherPerson = TestDataCreator.createPerson("mmuster", "Marlene", "Muster", "max@muster.de");
        Person personWithoutMailAddress = TestDataCreator.createPerson("nomail", "No", "Mail", null);

        ArgumentCaptor<SimpleMailMessage> mailMessageArgumentCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        String subject = "subject.application.applied.boss";
        String body = "Mail Body";
        mailService.sendEmail(settings.getMailSettings(),
            Arrays.asList(person, anotherPerson, personWithoutMailAddress), subject, body);

        Mockito.verify(mailSender).send(mailMessageArgumentCaptor.capture());

        SimpleMailMessage mailMessage = mailMessageArgumentCaptor.getValue();

        Assert.assertNotNull("There must be recipients", mailMessage.getTo());
        Assert.assertEquals("Wrong number of recipients", 2, mailMessage.getTo().length);
        Assert.assertEquals("Wrong subject", "Neuer Urlaubsantrag", mailMessage.getSubject());
        Assert.assertEquals("Wrong body", body, mailMessage.getText());
    }


    @Test
    public void ensureNoMailIsSentIfTheRecipientsHaveNoMailAddress() {

        Person person = TestDataCreator.createPerson();
        person.setEmail(null);

        mailService.sendEmail(settings.getMailSettings(), Collections.singletonList(person), "Mail Subject",
            "Mail Body");

        Mockito.verifyZeroInteractions(mailSender);
    }


    @Test
    public void ensureSendsNewApplicationNotificationToBosses() {

        mailService.sendNewApplicationNotification(application, null);

        Mockito.verify(personService).getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);
    }


    @Test
    public void ensureSendsNewApplicationNotificationToDepartmentHeads() {

        Person boss = TestDataCreator.createPerson("boss");
        Person departmentHead = TestDataCreator.createPerson("departmentHead");

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS))
            .thenReturn(Collections.singletonList(boss));

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD))
            .thenReturn(Collections.singletonList(departmentHead));

        mailService.sendNewApplicationNotification(application, null);

        Mockito.verify(personService).getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD);
        Mockito.verify(departmentService)
            .isDepartmentHeadOfPerson(Mockito.eq(departmentHead), Mockito.eq(application.getPerson()));
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

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS))
            .thenReturn(Collections.singletonList(boss));

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD))
            .thenReturn(Collections.singletonList(departmentHead));

        mailService.sendRemindBossNotification(application);

        Mockito.verify(personService).getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD);
        Mockito.verify(departmentService)
            .isDepartmentHeadOfPerson(Mockito.eq(departmentHead), Mockito.eq(application.getPerson()));
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

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS))
                .thenReturn(Collections.singletonList(boss));

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD))
                .thenReturn(Arrays.asList(departmentHeadAC, departmentHeadB));

        Mockito.when(departmentService.isDepartmentHeadOfPerson(departmentHeadAC, personDepartmentA)).thenReturn(true);
        Mockito.when(departmentService.isDepartmentHeadOfPerson(departmentHeadB, personDepartmentB)).thenReturn(true);
        Mockito.when(departmentService.isDepartmentHeadOfPerson(departmentHeadAC, personDepartmentC)).thenReturn(true);

        mailService.sendRemindForWaitingApplicationsReminderNotification(Arrays.asList(applicationA, applicationB, applicationC));

        Mockito.verify(personService, times(3)).getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD);
        Mockito.verify(personService, times(3)).getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);
        Mockito.verify(departmentService)
                .isDepartmentHeadOfPerson(Mockito.eq(departmentHeadAC), Mockito.eq(applicationA.getPerson()));
        Mockito.verify(departmentService)
                .isDepartmentHeadOfPerson(Mockito.eq(departmentHeadB), Mockito.eq(applicationB.getPerson()));
        Mockito.verify(departmentService)
                .isDepartmentHeadOfPerson(Mockito.eq(departmentHeadAC), Mockito.eq(applicationC.getPerson()));
    }

    @Test
    public void ensureSendsAllowedNotificationToOffice() {

        mailService.sendAllowedNotification(application, null);

        Mockito.verify(personService).getPersonsWithNotificationType(MailNotification.NOTIFICATION_OFFICE);
    }
}
