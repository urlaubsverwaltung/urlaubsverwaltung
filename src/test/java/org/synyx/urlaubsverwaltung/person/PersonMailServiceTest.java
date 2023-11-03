package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto;

import java.util.List;
import java.util.Map;

import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.SECOND_STAGE_AUTHORITY;

@ExtendWith(MockitoExtension.class)
class PersonMailServiceTest {

    private PersonMailService sut;

    @Mock
    private MailService mailService;
    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new PersonMailService(mailService, personService);
    }

    @Test
    void ensureSendsPersonCreationNotification() {

        final Person personWithNotification = new Person("peter", "Mahler", "Peter", "mahler@example.org");
        personWithNotification.setNotifications(List.of(MailNotification.NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL));
        when(personService.getActivePersonsWithNotificationType(NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL)).thenReturn(List.of(personWithNotification));

        final Person createdPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        createdPerson.setId(1L);
        final PersonCreatedEvent event = new PersonCreatedEvent(this, createdPerson.getId(), createdPerson.getNiceName(), createdPerson.getUsername(), createdPerson.getEmail(), createdPerson.isActive());

        final Map<String, Object> model = Map.of(
            "personId", event.getPersonId(),
            "personNiceName", event.getPersonNiceName()
        );

        sut.sendPersonCreationNotification(event);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(personWithNotification));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.person.created");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("person_created_office");
        assertThat(mails.get(0).getTemplateModel(GERMAN)).isEqualTo(model);
    }

    @Test
    void ensureSendsPersonGainedPermissionsNotification() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final List<PersonPermissionsRoleDto> addedPermissions = List.of(SECOND_STAGE_AUTHORITY);
        final Map<String, Object> model = Map.of(
            "person", person,
            "addedPermissions", addedPermissions
        );

        sut.sendPersonGainedMorePermissionsNotification(person, addedPermissions);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final List<Mail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.person.gained-permissions");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("person_gained_permissions");
        assertThat(mails.get(0).getTemplateModel(GERMAN)).isEqualTo(model);
    }
}
