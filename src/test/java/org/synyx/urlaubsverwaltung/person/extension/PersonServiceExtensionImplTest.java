package org.synyx.urlaubsverwaltung.person.extension;


import de.focus_shift.urlaubsverwaltung.extension.api.person.MailNotificationDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.PersonDTO;
import de.focus_shift.urlaubsverwaltung.extension.api.person.RoleDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceExtensionImplTest {

    @Mock
    private PersonService personService;

    @InjectMocks
    private PersonServiceExtensionImpl sut;

    private static Person anyPerson() {
        PersonDTO personDTO = anyPersonDTO(1L);
        final Person createdPerson = new Person(personDTO.getUsername(), personDTO.getLastName(), personDTO.getFirstName(), personDTO.getEmail());
        createdPerson.setId(personDTO.getId());
        createdPerson.setPermissions(Set.of(Role.USER));
        createdPerson.setNotifications(Set.of(MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        return createdPerson;
    }

    private static PersonDTO anyPersonDTO() {
        return anyPersonDTO(null);
    }

    private static PersonDTO anyPersonDTO(Long id) {
        return PersonDTO.builder()
            .id(id)
            .username("muster")
            .lastName("Muster")
            .firstName("Marlene")
            .email("muster@example.org")
            .enabled(true)
            .permissions(Set.of(RoleDTO.USER))
            .notifications(Set.of(MailNotificationDTO.NOTIFICATION_EMAIL_APPLICATION_ALLOWED))
            .build();
    }

    @Test
    void createHappyPath() {

        final Person createdPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        createdPerson.setId(1L);
        createdPerson.setPermissions(List.of(Role.USER));
        createdPerson.setNotifications(List.of(MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        when(personService.create(any(String.class), any(String.class), any(String.class), any(String.class))).thenReturn(createdPerson);

        final PersonDTO createdDTO = sut.create(anyPersonDTO());
        assertThat(createdDTO.getId()).isOne();
        assertThat(createdDTO.getUsername()).isEqualTo("muster");
        assertThat(createdDTO.getLastName()).isEqualTo("Muster");
        assertThat(createdDTO.getFirstName()).isEqualTo("Marlene");
        assertThat(createdDTO.getEmail()).isEqualTo("muster@example.org");
        assertThat(createdDTO.getPermissions()).containsOnly(RoleDTO.USER);
        assertThat(createdDTO.getNotifications()).containsOnly(MailNotificationDTO.NOTIFICATION_EMAIL_APPLICATION_ALLOWED);
        assertThat(createdDTO.isEnabled()).isTrue();
    }

    @Test
    void updateHappyPath() {
        final PersonDTO personDTO = anyPersonDTO(1L);
        final Person createdPerson = anyPerson();

        when(personService.update(any())).thenReturn(createdPerson);

        final PersonDTO updatedPersonDTO = sut.update(personDTO);

        assertThat(updatedPersonDTO).isNotNull();
        assertThat(updatedPersonDTO.getId()).isOne();
        assertThat(updatedPersonDTO.getUsername()).isEqualTo("muster");
        assertThat(updatedPersonDTO.getLastName()).isEqualTo("Muster");
        assertThat(updatedPersonDTO.getFirstName()).isEqualTo("Marlene");
        assertThat(updatedPersonDTO.getEmail()).isEqualTo("muster@example.org");
        assertThat(updatedPersonDTO.getPermissions()).containsOnly(RoleDTO.USER);
        assertThat(updatedPersonDTO.getNotifications()).containsOnly(MailNotificationDTO.NOTIFICATION_EMAIL_APPLICATION_ALLOWED);
        assertThat(updatedPersonDTO.isEnabled()).isTrue();


        ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personService).update(personArgumentCaptor.capture());

        final Person value = personArgumentCaptor.getValue();
        assertThat(value).isNotNull();
        assertThat(value.getId()).isOne();
        assertThat(value.getUsername()).isEqualTo("muster");
    }

    @Test
    void deleteHappyPath() {
        final Person signedInUserId = new Person("boss", "Scherer", "Theresa", "boss@example.org");
        when(personService.getPersonByID(any())).thenReturn(Optional.of(signedInUserId));

        sut.delete(anyPersonDTO(1L), 42L);

        ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);

        verify(personService).getPersonByID(42L);

        verify(personService).delete(personArgumentCaptor.capture(), eq(signedInUserId));

        final Person personToDelete = personArgumentCaptor.getValue();
        assertThat(personToDelete).isNotNull();
        assertThat(personToDelete.getId()).isOne();
        assertThat(personToDelete.getUsername()).isEqualTo("muster");
    }

    @Test
    void ensureDeleteHandlesSignedInUserUnknown() {

        sut.delete(anyPersonDTO(), 42L);

        verify(personService).getPersonByID(42L);
        verifyNoMoreInteractions(personService);
    }

    @Test
    void getPersonByIdHappyPath() {

        final PersonDTO personDTO = anyPersonDTO(1L);
        when(personService.getPersonByID(any())).thenReturn(Optional.of(anyPerson()));

        Optional<PersonDTO> personById = sut.getPersonById(personDTO.getId());

        verify(personService).getPersonByID(personDTO.getId());
        assertThat(personById).isPresent()
            .contains(personDTO);
    }

    @Test
    void ensureGetPersonByIdHandlesNotFound() {

        Optional<PersonDTO> personById = sut.getPersonById(1L);

        verify(personService).getPersonByID(1L);
        assertThat(personById).isEmpty();
    }

    @Test
    void getPersonByUsernameHappyPath() {

        final PersonDTO personDTO = anyPersonDTO(1L);
        when(personService.getPersonByUsername(any())).thenReturn(Optional.of(anyPerson()));

        Optional<PersonDTO> personById = sut.getPersonByUsername(personDTO.getUsername());

        verify(personService).getPersonByUsername(personDTO.getUsername());
        assertThat(personById).isPresent()
            .contains(personDTO);
    }

    @Test
    void ensureGetPersonByUsernameHandlesNotFound() {

        Optional<PersonDTO> personById = sut.getPersonByUsername("muster");

        verify(personService).getPersonByUsername("muster");
        assertThat(personById).isEmpty();
    }

    @Test
    void getPersonByMailAddressHappyPath() {

        final PersonDTO personDTO = anyPersonDTO(1L);
        when(personService.getPersonByMailAddress(any())).thenReturn(Optional.of(anyPerson()));

        Optional<PersonDTO> personById = sut.getPersonByMailAddress(personDTO.getEmail());

        verify(personService).getPersonByMailAddress(personDTO.getEmail());
        assertThat(personById).isPresent()
            .contains(personDTO);
    }

    @Test
    void ensureGetPersonByMailAddressHandlesNotFound() {

        Optional<PersonDTO> personById = sut.getPersonByUsername("muster");

        verify(personService).getPersonByUsername("muster");
        assertThat(personById).isEmpty();
    }

    @Test
    void getActivePersonsHappyPath() {

        PersonDTO anyPersonDTO = anyPersonDTO(1L);
        Person anyPerson = anyPerson();

        Page<Person> page = new PageImpl<>(List.of(anyPerson));
        when(personService.getActivePersons(any())).thenReturn(page);

        Stream<PersonDTO> result = sut.getActivePersons();

        ArgumentCaptor<PageableSearchQuery> argumentCaptor = ArgumentCaptor.forClass(PageableSearchQuery.class);

        verify(personService).getActivePersons(argumentCaptor.capture());

        PageableSearchQuery pageableSearchQueryCaptor = argumentCaptor.getValue();
        assertThat(pageableSearchQueryCaptor).isNotNull();
        assertThat(pageableSearchQueryCaptor.getQuery()).isEmpty();
        assertThat(pageableSearchQueryCaptor.getPageable().getSort()).isEqualTo(Sort.unsorted());
        assertThat(pageableSearchQueryCaptor.getPageable().getPageNumber()).isZero();
        assertThat(pageableSearchQueryCaptor.getPageable().getPageSize()).isEqualTo(25);

        assertThat(result).containsOnly(anyPersonDTO);
    }

    @Test
    void ensureGetActivePersonsHandlesEmptyPage() {

        when(personService.getActivePersons(any())).thenReturn(Page.empty());

        Stream<PersonDTO> result = sut.getActivePersons();

        assertThat(result).isEmpty();
    }

    @Test
    void getInactivePersonsHappyPath() {

        PersonDTO anyPersonDTO = anyPersonDTO(1L);
        Person anyPerson = anyPerson();

        Page<Person> page = new PageImpl<>(List.of(anyPerson));
        when(personService.getInactivePersons(any())).thenReturn(page);

        Stream<PersonDTO> result = sut.getInactivePersons();


        ArgumentCaptor<PageableSearchQuery> argumentCaptor = ArgumentCaptor.forClass(PageableSearchQuery.class);

        verify(personService).getInactivePersons(argumentCaptor.capture());

        PageableSearchQuery pageableSearchQueryCaptor = argumentCaptor.getValue();
        assertThat(pageableSearchQueryCaptor).isNotNull();
        assertThat(pageableSearchQueryCaptor.getQuery()).isEmpty();
        assertThat(pageableSearchQueryCaptor.getPageable().getSort()).isEqualTo(Sort.unsorted());
        assertThat(pageableSearchQueryCaptor.getPageable().getPageNumber()).isZero();
        assertThat(pageableSearchQueryCaptor.getPageable().getPageSize()).isEqualTo(25);

        assertThat(result).containsOnly(anyPersonDTO);
    }

    @Test
    void ensureGetInactivePersonsHandlesEmptyPage() {

        when(personService.getInactivePersons(any())).thenReturn(Page.empty());

        Stream<PersonDTO> result = sut.getInactivePersons();

        assertThat(result).isEmpty();
    }


    @Test
    void getSignedInUserHappyPath() {
        when(personService.getSignedInUser()).thenReturn(anyPerson());

        final PersonDTO signedInUser = sut.getSignedInUser();

        verify(personService).getSignedInUser();
        assertThat(signedInUser).isNotNull();
    }

    @Test
    void appointAsInitialUserIfNoInitialUserPresentHappyPath() {

        final PersonDTO personDTO = anyPersonDTO(1L);
        final Person person = anyPerson();
        when(personService.appointAsOfficeUserIfNoOfficeUserPresent(any())).thenReturn(person);

        final PersonDTO appointedPerson = sut.appointAsInitialUserIfNoInitialUserPresent(personDTO);

        assertThat(appointedPerson).isNotNull();
        assertThat(appointedPerson.getId()).isOne();
        assertThat(appointedPerson.getUsername()).isEqualTo("muster");
        assertThat(appointedPerson.getLastName()).isEqualTo("Muster");
        assertThat(appointedPerson.getFirstName()).isEqualTo("Marlene");
        assertThat(appointedPerson.getEmail()).isEqualTo("muster@example.org");
        assertThat(appointedPerson.getPermissions()).containsOnly(RoleDTO.USER);
        assertThat(appointedPerson.getNotifications()).containsOnly(MailNotificationDTO.NOTIFICATION_EMAIL_APPLICATION_ALLOWED);
        assertThat(appointedPerson.isEnabled()).isTrue();

        ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personService).appointAsOfficeUserIfNoOfficeUserPresent(personArgumentCaptor.capture());

        final Person personToBeAppointed = personArgumentCaptor.getValue();
        assertThat(personToBeAppointed).isNotNull();
        assertThat(personToBeAppointed.getId()).isOne();
        assertThat(personToBeAppointed.getUsername()).isEqualTo("muster");
    }
}
