package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {

    private PersonServiceImpl sut;

    @Mock
    private PersonRepository personRepository;
    @Mock
    private AccountInteractionService accountInteractionService;
    @Mock
    private WorkingTimeWriteService workingTimeWriteService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Captor
    private ArgumentCaptor<PersonCreatedEvent> personCreatedEventArgumentCaptor;
    @Captor
    private ArgumentCaptor<PersonDeletedEvent> personDeletedEventArgumentCaptor;

    @BeforeEach
    void setUp() {
        sut = new PersonServiceImpl(personRepository, accountInteractionService, workingTimeWriteService, applicationEventPublisher);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ensureDefaultAccountAndWorkingTimeCreation() {
        when(personRepository.save(any(Person.class))).thenReturn(new Person());

        sut.create("rick", "Rick", "Grimes", "rick@grimes.de", emptyList(), emptyList());
        verify(accountInteractionService).createDefaultAccount(any(Person.class));
        verify(workingTimeWriteService).createDefaultWorkingTime(any(Person.class));
    }

    @Test
    void ensurePersonCreatedEventIsFired() {

        when(personRepository.save(any(Person.class))).thenAnswer(returnsFirstArg());
        final Person person = sut.create("rick", "Rick", "Grimes", "rick@grimes.de");

        verify(applicationEventPublisher).publishEvent(personCreatedEventArgumentCaptor.capture());
        assertThat(personCreatedEventArgumentCaptor.getValue().getUsername()).isEqualTo(person.getUsername());
    }

    @Test
    void ensureCreatedPersonHasStrippedUsername() {

        when(personRepository.save(any(Person.class))).thenAnswer(returnsFirstArg());

        final Person createdPerson = sut.create("  rick ", "", "", "r", List.of(), List.of());
        assertThat(createdPerson.getUsername()).isEqualTo("rick");
    }

    @Test
    void ensureCreatedPersonHasStrippedFirstName() {

        when(personRepository.save(any(Person.class))).thenAnswer(returnsFirstArg());

        final Person createdPerson = sut.create("", " Rick  ", "", "r", List.of(), List.of());
        assertThat(createdPerson.getFirstName()).isEqualTo("Rick");
    }

    @Test
    void ensureCreatedPersonHasStrippedLastName() {

        when(personRepository.save(any(Person.class))).thenAnswer(returnsFirstArg());

        final Person createdPerson = sut.create("", "", " Grimes  ", "r", List.of(), List.of());
        assertThat(createdPerson.getLastName()).isEqualTo("Grimes");
    }

    @Test
    void ensureCreatedPersonHasStrippedEmail() {

        when(personRepository.save(any(Person.class))).thenAnswer(returnsFirstArg());

        final Person createdPerson = sut.create("", "", "", " rick@grimes.de  ", List.of(), List.of());
        assertThat(createdPerson.getEmail()).isEqualTo("rick@grimes.de");
    }

    @Test
    void ensureCreatedPersonHasCorrectAttributes() {

        when(personRepository.save(any(Person.class))).thenAnswer(returnsFirstArg());

        final Person createdPerson = sut.create("rick", "Rick", "Grimes", "rick@grimes.de", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED), List.of(USER, BOSS));
        assertThat(createdPerson.getUsername()).isEqualTo("rick");
        assertThat(createdPerson.getFirstName()).isEqualTo("Rick");
        assertThat(createdPerson.getLastName()).isEqualTo("Grimes");
        assertThat(createdPerson.getEmail()).isEqualTo("rick@grimes.de");

        assertThat(createdPerson.getNotifications())
            .hasSize(1)
            .contains(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);

        assertThat(createdPerson.getPermissions())
            .hasSize(2)
            .contains(USER, BOSS);

        verify(accountInteractionService).createDefaultAccount(any());
        verify(workingTimeWriteService).createDefaultWorkingTime(any());
    }

    @Test
    void ensureCreatedPersonIsPersisted() {

        when(personRepository.save(any(Person.class))).thenAnswer(returnsFirstArg());

        final Person savedPerson = sut.create("muster", "Marlene", "Muster", "muster@example.org");
        assertThat(savedPerson.getUsername()).isEqualTo("muster");
        assertThat(savedPerson.getLastName()).isEqualTo("Muster");
        assertThat(savedPerson.getFirstName()).isEqualTo("Marlene");
        assertThat(savedPerson.getEmail()).isEqualTo("muster@example.org");
    }

    @Test
    void ensureNotificationIsSendForCreatedPerson() {

        when(personRepository.save(any(Person.class))).thenAnswer(returnsFirstArg());

        final Person createdPerson = sut.create("muster", "Marlene", "Muster", "muster@example.org");

        verify(applicationEventPublisher).publishEvent(personCreatedEventArgumentCaptor.capture());

        final PersonCreatedEvent personCreatedEvent = personCreatedEventArgumentCaptor.getValue();
        assertThat(personCreatedEvent.getSource()).isEqualTo(sut);
        assertThat(personCreatedEvent.getPersonId()).isEqualTo(createdPerson.getId());
        assertThat(personCreatedEvent.getPersonNiceName()).isEqualTo(createdPerson.getNiceName());
    }

    @Test
    void ensureUpdatedPersonIsPersisted() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personRepository.save(person)).thenReturn(person);

        sut.update(person);
        verify(personRepository).save(person);
    }

    @Test
    void ensureUpdatedPersonHasStrippedUsername() {

        final Person person = new Person(" muster  ", "", "", "");
        person.setId(1L);
        when(personRepository.save(person)).thenAnswer(returnsFirstArg());

        final Person updatedPerson = sut.update(person);
        assertThat(updatedPerson.getUsername()).isEqualTo("muster");
    }

    @Test
    void ensureUpdatedPersonHasStrippedFirstName() {

        final Person person = new Person("", "", " Marlene  ", "");
        person.setId(1L);
        when(personRepository.save(person)).thenAnswer(returnsFirstArg());

        final Person updatedPerson = sut.update(person);
        assertThat(updatedPerson.getFirstName()).isEqualTo("Marlene");
    }

    @Test
    void ensureUpdatedPersonHasStrippedLastName() {

        final Person person = new Person("", " Muster  ", "", "");
        person.setId(1L);
        when(personRepository.save(person)).thenAnswer(returnsFirstArg());

        final Person updatedPerson = sut.update(person);
        assertThat(updatedPerson.getLastName()).isEqualTo("Muster");
    }

    @Test
    void ensureUpdatedPersonHasStrippedEmail() {

        final Person person = new Person("", "", "", " muster@example.org  ");
        person.setId(1L);
        when(personRepository.save(person)).thenAnswer(returnsFirstArg());

        final Person updatedPerson = sut.update(person);
        assertThat(updatedPerson.getEmail()).isEqualTo("muster@example.org");
    }

    @Test
    void ensureThrowsIfPersonToBeUpdatedHasNoID() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(null);
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.update(person));
    }

    @Test
    void ensureSaveCallsCorrectDaoMethod() {

        when(personRepository.save(any())).thenAnswer(returnsFirstArg());

        final Person savedPerson = sut.create("muster", "Marlene", "Muster", "muster@example.org");
        assertThat(savedPerson.getUsername()).isEqualTo("muster");
        assertThat(savedPerson.getLastName()).isEqualTo("Muster");
        assertThat(savedPerson.getFirstName()).isEqualTo("Marlene");
        assertThat(savedPerson.getEmail()).isEqualTo("muster@example.org");
    }

    @Test
    void ensureGetPersonByIDCallsCorrectDaoMethod() {

        sut.getPersonByID(123L);
        verify(personRepository).findById(123L);
    }

    @Test
    void ensureGetPersonByLoginCallsCorrectDaoMethod() {
        final String username = "foo";
        sut.getPersonByUsername(username);

        verify(personRepository).findByUsernameIgnoreCase(username);
    }

    @Test
    void ensureGetPersonByMailAddressDelegatesToRepository() {
        final String mailAddress = "foo@bar.test";
        sut.getPersonByMailAddress(mailAddress);

        verify(personRepository).findByEmailIgnoreCase(mailAddress);
    }

    @Test
    void ensureGetActivePersonsReturnsOnlyPersonsThatHaveNotInactiveRole() {

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setPermissions(List.of(USER));

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(asList(USER, BOSS));

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(asList(USER, BOSS, OFFICE));

        when(personRepository.findByPermissionsNotContainingOrderByFirstNameAscLastNameAsc(INACTIVE)).thenReturn(List.of(user, boss, office));

        final List<Person> activePersons = sut.getActivePersons();
        assertThat(activePersons)
            .hasSize(3)
            .contains(user)
            .contains(boss)
            .contains(office);
    }

    @Test
    void ensureGetActivePersonsPage() {

        final Page<Person> expected = Page.empty();
        final PageRequest pageRequest = PageRequest.of(1, 100);
        final PageableSearchQuery personPageableSearchQuery = new PageableSearchQuery(pageRequest, "name-query");

        when(personRepository.findByPermissionsNotContainingAndByNiceNameContainingIgnoreCase(INACTIVE, "name-query", pageRequest)).thenReturn(expected);

        final Page<Person> actual = sut.getActivePersons(personPageableSearchQuery);
        assertThat(actual).isSameAs(expected);
    }

    @Test
    void ensureGetInactivePersonsReturnsOnlyPersonsThatHaveInactiveRole() {

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setPermissions(List.of(INACTIVE));

        when(personRepository.findByPermissionsContainingOrderByFirstNameAscLastNameAsc(INACTIVE)).thenReturn(List.of(user));

        final List<Person> activePersons = sut.getInactivePersons();
        assertThat(activePersons)
            .containsExactly(user);
    }

    @Test
    void ensureGetInactivePersonsPage() {

        final Page<Person> expected = Page.empty();
        final PageRequest pageRequest = PageRequest.of(1, 100, Sort.by(Sort.Direction.ASC, "firstName"));
        final PageableSearchQuery personPageableSearchQuery = new PageableSearchQuery(pageRequest, "name-query");

        // currently a hard coded pageRequest is used in implementation
        final PageRequest pageRequestInternal = PageRequest.of(1, 100, Sort.Direction.ASC, "firstName", "lastName");
        when(personRepository.findByPermissionsContainingAndNiceNameContainingIgnoreCase(INACTIVE, "name-query", pageRequestInternal)).thenReturn(expected);

        final Page<Person> actual = sut.getInactivePersons(personPageableSearchQuery);
        assertThat(actual).isSameAs(expected);
    }

    @Test
    void ensureGetPersonsByRoleReturnsOnlyPersonsWithTheGivenRole() {

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(asList(USER, BOSS));

        final Person bossOffice = new Person("muster", "Muster", "Marlene", "muster@example.org");
        bossOffice.setPermissions(asList(USER, BOSS, OFFICE));

        when(personRepository.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(BOSS, INACTIVE)).thenReturn(asList(boss, bossOffice));

        final List<Person> filteredList = sut.getActivePersonsByRole(BOSS);
        assertThat(filteredList)
            .hasSize(2)
            .contains(boss)
            .contains(bossOffice);
    }

    @Test
    void ensureGetPersonsByNotificationTypeReturnsOnlyPersonsWithTheGivenNotificationType() {

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(asList(USER, BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(asList(USER, BOSS, OFFICE));
        office.setNotifications(asList(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        when(personRepository.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(INACTIVE, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(List.of(boss, office));

        final List<Person> filteredList = sut.getActivePersonsWithNotificationType(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        assertThat(filteredList)
            .hasSize(2)
            .contains(boss)
            .contains(office);
    }

    @Test
    void ensureThrowsIfNoPersonCanBeFoundForTheCurrentlySignedInUser() {
        assertThatIllegalStateException()
            .isThrownBy(() -> sut.getSignedInUser());
    }

    @Test
    void ensureReturnsPersonForCurrentlySignedInUser() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personRepository.findByUsernameIgnoreCase("muster")).thenReturn(Optional.of(person));

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(person.getUsername());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        final Person signedInUser = sut.getSignedInUser();
        assertThat(signedInUser).isEqualTo(person);
    }

    @Test
    void ensureThrowsIllegalOnNullAuthentication() {
        assertThatIllegalStateException()
            .isThrownBy(() -> sut.getSignedInUser());
    }

    @Test
    void ensureCanAppointPersonAsOfficeUser() {

        when(personRepository.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(OFFICE, INACTIVE)).thenReturn(emptyList());
        when(personRepository.save(any())).then(returnsFirstArg());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER));
        assertThat(person.getPermissions()).containsOnly(USER);

        final Person personWithOfficeRole = sut.appointAsOfficeUserIfNoOfficeUserPresent(person);
        assertThat(personWithOfficeRole.getPermissions())
            .hasSize(2)
            .contains(USER, OFFICE);
    }

    @Test
    void ensureCanNotAppointPersonAsOfficeUser() {

        final Person officePerson = new Person();
        officePerson.setPermissions(List.of(OFFICE));
        when(personRepository.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(OFFICE, INACTIVE)).thenReturn(List.of(officePerson));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER));
        assertThat(person.getPermissions()).containsOnly(USER);

        final Person personWithOfficeRole = sut.appointAsOfficeUserIfNoOfficeUserPresent(person);
        assertThat(personWithOfficeRole.getPermissions())
            .containsOnly(USER);
    }

    @Test
    void ensurePersonUpdatedEventIsFiredAfterUpdate() {

        final Person activePerson = createPerson("active person", USER);
        activePerson.setId(1L);
        when(personRepository.save(activePerson)).thenReturn(activePerson);

        sut.update(activePerson);
        verify(applicationEventPublisher).publishEvent(any(PersonUpdatedEvent.class));
    }

    @Test
    void ensurePersonDisabledEventIsFiredAfterPersonUpdate() {

        final Person inactivePerson = createPerson("inactive person", INACTIVE);
        inactivePerson.setId(1L);
        when(personRepository.save(inactivePerson)).thenReturn(inactivePerson);

        sut.update(inactivePerson);
        verify(applicationEventPublisher).publishEvent(any(PersonDisabledEvent.class));
    }

    @Test
    void ensurePersonDisabledEventIsNotFiredAfterPersonUpdateAndRoleNotInactive() {

        final Person inactivePerson = createPerson("inactive person", USER);
        inactivePerson.setId(1L);
        when(personRepository.save(inactivePerson)).thenReturn(inactivePerson);

        sut.update(inactivePerson);
        verify(applicationEventPublisher, never()).publishEvent(any(PersonDisabledEvent.class));
    }

    @Test
    void numberOfActivePersons() {

        when(personRepository.countByPermissionsNotContaining(INACTIVE)).thenReturn(2);

        final int numberOfActivePersons = sut.numberOfActivePersons();
        assertThat(numberOfActivePersons).isEqualTo(2);
    }

    @Test
    void deletesExistingPersonDelegatesAndSendsEvent() {

        final Person signedInUser = new Person("signedInUser", "signed", "in", "user@example.org");

        final Person person = new Person();
        final long personId = 42;
        person.setId(personId);
        when(personRepository.existsById(personId)).thenReturn(true);

        sut.delete(person, signedInUser);

        final InOrder inOrder = inOrder(applicationEventPublisher, accountInteractionService, workingTimeWriteService, personRepository);

        inOrder.verify(personRepository).existsById(42L);
        inOrder.verify(applicationEventPublisher).publishEvent(personDeletedEventArgumentCaptor.capture());
        assertThat(personDeletedEventArgumentCaptor.getValue().person())
            .isEqualTo(person);

        inOrder.verify(accountInteractionService).deleteAllByPerson(person);
        inOrder.verify(workingTimeWriteService).deleteAllByPerson(person);
        inOrder.verify(personRepository).delete(person);
    }

    @Test
    void deletingNotExistingPersonThrowsException() {
        final Person signedInUser = new Person("signedInUser", "signed", "in", "user@example.org");

        final Person person = new Person();
        person.setId(1L);
        assertThatThrownBy(() -> sut.delete(person, signedInUser)).isInstanceOf(IllegalArgumentException.class);

        verify(personRepository).existsById(1L);
        verifyNoMoreInteractions(applicationEventPublisher, workingTimeWriteService, accountInteractionService, personRepository);
    }

    @Test
    void numberOfPersonsWithRoleWithoutId() {

        when(personRepository.countByPermissionsContainingAndIdNotIn(OFFICE, List.of(1L))).thenReturn(2);

        final int numberOfOfficeExceptId = sut.numberOfPersonsWithOfficeRoleExcludingPerson(1);
        assertThat(numberOfOfficeExceptId).isEqualTo(2);
    }
}
