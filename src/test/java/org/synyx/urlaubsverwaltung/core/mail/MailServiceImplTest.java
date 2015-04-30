package org.synyx.urlaubsverwaltung.core.mail;

import org.apache.velocity.app.VelocityEngine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.util.Arrays;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class MailServiceImplTest {

    private MailServiceImpl mailService;
    private JavaMailSender mailSender;
    private VelocityEngine velocityEngine;
    private PersonService personService;

    private Person person;
    private Application application;

    @Before
    public void setUp() throws Exception {

        velocityEngine = Mockito.mock(VelocityEngine.class);
        mailSender = Mockito.mock(JavaMailSender.class);
        personService = Mockito.mock(PersonService.class);

        mailService = new MailServiceImpl(mailSender, velocityEngine, personService, "", "", "");

        person = new Person();
        application = new Application();
        application.setPerson(person);
        application.setVacationType(VacationType.HOLIDAY);
        application.setHowLong(DayLength.FULL);
    }


    @Test
    public void ensureMailIsSentToAllRecipientsThatHaveAnEmailAddress() {

        Person person = new Person("muster", "Muster", "Max", "max@muster.de");
        Person anotherPerson = new Person("mmuster", "Muster", "Marlene", "marlene@muster.de");
        Person personWithoutMailAddress = new Person("nomail", "Mail", "No", null);

        ArgumentCaptor<SimpleMailMessage> mailMessageArgumentCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        String subject = "subject.new";
        String body = "Mail Body";
        mailService.sendEmail(Arrays.asList(person, anotherPerson, personWithoutMailAddress), subject, body);

        Mockito.verify(mailSender).send(mailMessageArgumentCaptor.capture());

        SimpleMailMessage mailMessage = mailMessageArgumentCaptor.getValue();

        Assert.assertNotNull("There must be recipients", mailMessage.getTo());
        Assert.assertEquals("Wrong number of recipients", 2, mailMessage.getTo().length);
        Assert.assertEquals("Wrong subject", "Neuer Urlaubsantrag", mailMessage.getSubject());
        Assert.assertEquals("Wrong body", body, mailMessage.getText());
    }


    @Test
    public void ensureNoMailIsSentIfTheRecipientsHaveNoMailAddress() {

        Person person = new Person("muster", "Muster", "Max", null);

        mailService.sendEmail(Arrays.asList(person), "Mail Subject", "Mail Body");

        Mockito.verifyZeroInteractions(mailSender);
    }


    @Test
    public void ensureSendsNewApplicationNotificationToBosses() {

        mailService.sendNewApplicationNotification(application);

        Mockito.verify(personService).getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);
    }


    @Test
    public void ensureSendsRemindNotificationToBosses() {

        mailService.sendRemindBossNotification(application);

        Mockito.verify(personService).getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);
    }


    @Test
    public void ensureSendsAllowedNotificationToOffice() {

        mailService.sendAllowedNotification(application, null);

        Mockito.verify(personService).getPersonsWithNotificationType(MailNotification.NOTIFICATION_OFFICE);
    }
}
