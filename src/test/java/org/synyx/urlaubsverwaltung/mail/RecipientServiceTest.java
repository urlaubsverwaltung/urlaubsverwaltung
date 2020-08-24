package org.synyx.urlaubsverwaltung.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;

@ExtendWith(MockitoExtension.class)
class RecipientServiceTest {

    private RecipientService sut;

    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {

        sut = new RecipientService(personService);
    }


    @Test
    void getRecipientsWithNotificationType() {

        final Person person = new Person();
        final List<Person> persons = singletonList(person);

        final MailNotification notification = NOTIFICATION_BOSS_ALL;
        when(personService.getPersonsWithNotificationType(notification)).thenReturn(persons);

        final List<Person> recipients = sut.getRecipientsWithNotificationType(notification);

        assertThat(recipients).isEqualTo(persons);
    }

    @Test
    void ensureFiltersOutPersonsWithoutMailAddress() {

        Person person = createPerson("muster", "Max", "Mustermann", "max@firma.test");
        Person anotherPerson = createPerson("mmuster", "Marlene", "Muster", "marlene@firma.test");
        Person personWithoutMailAddress = createPerson("nomail", "No", "Mail", null);

        List<String> recipients = sut.getMailAddresses(person, anotherPerson, personWithoutMailAddress);

        assertThat(recipients)
            .hasSize(2)
            .contains("max@firma.test", "marlene@firma.test");
    }
}
