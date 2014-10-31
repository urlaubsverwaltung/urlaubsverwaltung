package org.synyx.urlaubsverwaltung.core.mail;

import org.apache.velocity.app.VelocityEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
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

    @Before
    public void setUp() throws Exception {

        velocityEngine = Mockito.mock(VelocityEngine.class);
        mailSender = Mockito.mock(JavaMailSender.class);
        personService = Mockito.mock(PersonService.class);

        mailService = new MailServiceImpl(mailSender, velocityEngine, personService);

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

}
