package org.synyx.urlaubsverwaltung.core.mail;

import org.apache.velocity.app.VelocityEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.util.Arrays;

/**
 * @author Aljona Murygina - murygina@synyx.de
 */
public class MailServiceTest {

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

        mailService = new MailServiceImpl(mailSender, velocityEngine, personService);

        person = new Person();
        application = new Application();
        application.setPerson(person);
        application.setVacationType(VacationType.HOLIDAY);
        application.setHowLong(DayLength.FULL);

    }

    @Test
    public void ensureMailIsSentToAllRecipients() {

        Person person = new Person("muster", "Muster", "Max", "max@muster.de");

        mailService.sendEmail(Arrays.asList(person), "Mail Subject", "Mail Body");

        Mockito.verify(mailSender).send(Mockito.any(MimeMessagePreparator.class));

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
