package org.synyx.urlaubsverwaltung.mail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;

@RunWith(MockitoJUnitRunner.class)
public class RecipientServiceTest {

    private RecipientService sut;

    @Mock
    private PersonService personService;

    @Before
    public void setUp() {

        sut = new RecipientService(personService);
    }


    @Test
    public void getRecipientsWithNotificationType() {

        final Person person = new Person();
        final List<Person> persons = singletonList(person);

        final MailNotification notification = NOTIFICATION_BOSS_ALL;
        when(personService.getPersonsWithNotificationType(notification)).thenReturn(persons);

        final List<Person> recipients = sut.getRecipientsWithNotificationType(notification);

        assertThat(recipients).isEqualTo(persons);
    }

    @Test
    public void ensureFiltersOutPersonsWithoutMailAddress() {

        Person person = createPerson("muster", "Max", "Mustermann", "max@firma.test");
        Person anotherPerson = createPerson("mmuster", "Marlene", "Muster", "marlene@firma.test");
        Person personWithoutMailAddress = createPerson("nomail", "No", "Mail", null);

        List<String> recipients = sut.getMailAddresses(person, anotherPerson, personWithoutMailAddress);

        assertThat(recipients)
            .hasSize(2)
            .contains("max@firma.test", "marlene@firma.test");
    }
}
