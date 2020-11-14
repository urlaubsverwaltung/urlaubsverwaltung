package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
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
    private WorkingTimeService workingTimeService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private final ArgumentCaptor<PersonDisabledEvent> personDisabledEventArgumentCaptor = ArgumentCaptor.forClass(PersonDisabledEvent.class);

    @BeforeEach
    void setUp() {
        sut = new PersonServiceImpl(personRepository, accountInteractionService, workingTimeService, applicationEventPublisher);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ensureDefaultAccountAndWorkingTimeCreation() {
        when(personRepository.save(any(Person.class))).thenReturn(new Person());

        sut.create("rick", "Grimes", "Rick", "rick@grimes.de", emptyList(), emptyList());
        verify(accountInteractionService).createDefaultAccount(any(Person.class));
        verify(workingTimeService).createDefaultWorkingTime(any(Person.class));
    }

    @Test
    void ensureCreatedPersonHasCorrectAttributes() {

        final Person person = new Person("rick", "Grimes", "Rick", "rick@grimes.de");
        person.setPermissions(asList(USER, BOSS));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        when(personRepository.save(person)).thenReturn(person);

        final Person createdPerson = sut.create(person);
        assertThat(createdPerson.getUsername()).isEqualTo("rick");
        assertThat(createdPerson.getFirstName()).isEqualTo("Rick");
        assertThat(createdPerson.getLastName()).isEqualTo("Grimes");
        assertThat(createdPerson.getEmail()).isEqualTo("rick@grimes.de");

        assertThat(createdPerson.getNotifications())
            .hasSize(2)
            .contains(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL);

        assertThat(createdPerson.getPermissions())
            .hasSize(2)
            .contains(USER, BOSS);

        verify(accountInteractionService).createDefaultAccount(person);
        verify(workingTimeService).createDefaultWorkingTime(person);
    }

    @Test
    void ensureCreatedPersonIsPersisted() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personRepository.save(person)).thenReturn(person);

        final Person savedPerson = sut.create(person);
        assertThat(savedPerson).isEqualTo(person);
    }

    @Test
    void ensureUpdatedPersonHasCorrectAttributes() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personRepository.findById(anyInt())).thenReturn(Optional.of(person));
        when(personRepository.save(person)).thenReturn(person);

        final Person updatedPerson = sut.update(42, "rick", "Grimes", "Rick", "rick@grimes.de",
            asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL),
            asList(USER, BOSS));
        assertThat(updatedPerson.getUsername()).isEqualTo("rick");
        assertThat(updatedPerson.getFirstName()).isEqualTo("Rick");
        assertThat(updatedPerson.getLastName()).isEqualTo("Grimes");
        assertThat(updatedPerson.getEmail()).isEqualTo("rick@grimes.de");

        assertThat(updatedPerson.getNotifications())
            .hasSize(2)
            .contains(NOTIFICATION_USER)
            .contains(NOTIFICATION_BOSS_ALL);

        assertThat(updatedPerson.getPermissions())
            .hasSize(2)
            .contains(USER)
            .contains(BOSS);
    }

    @Test
    void ensureUpdatedPersonIsPersisted() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personRepository.save(person)).thenReturn(person);

        sut.update(person);
        verify(personRepository).save(person);
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

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personRepository.save(person)).thenReturn(person);

        final Person savedPerson = sut.save(person);
        assertThat(savedPerson).isEqualTo(person);
    }

    @Test
    void ensureGetPersonByIDCallsCorrectDaoMethod() {

        sut.getPersonByID(123);
        verify(personRepository).findById(123);
    }

    @Test
    void ensureGetPersonByLoginCallsCorrectDaoMethod() {
        final String username = "foo";
        sut.getPersonByUsername(username);

        verify(personRepository).findByUsername(username);
    }

    @Test
    void ensureGetActivePersonsReturnsOnlyPersonsThatHaveNotInactiveRole() {

        final Person inactive = new Person("muster", "Muster", "Marlene", "muster@example.org");
        inactive.setPermissions(List.of(INACTIVE));

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setPermissions(List.of(USER));

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(asList(USER, BOSS));

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(asList(USER, BOSS, OFFICE));

        when(personRepository.findAll()).thenReturn(asList(inactive, user, boss, office));

        final List<Person> activePersons = sut.getActivePersons();
        assertThat(activePersons)
            .hasSize(3)
            .contains(user)
            .contains(boss)
            .contains(office);
    }

    @Test
    void ensureGetInactivePersonsReturnsOnlyPersonsThatHaveInactiveRole() {

        final Person inactive = new Person("muster", "Muster", "Marlene", "muster@example.org");
        inactive.setPermissions(List.of(INACTIVE));

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setPermissions(List.of(USER));

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(asList(USER, BOSS));

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(asList(USER, BOSS, OFFICE));

        when(personRepository.findAll()).thenReturn(asList(inactive, user, boss, office));

        List<Person> inactivePersons = sut.getInactivePersons();
        assertThat(inactivePersons)
            .hasSize(1)
            .contains(inactive);
    }

    @Test
    void ensureGetPersonsByRoleReturnsOnlyPersonsWithTheGivenRole() {

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setPermissions(List.of(USER));

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(asList(USER, BOSS));

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(asList(USER, BOSS, OFFICE));

        when(personRepository.findAll()).thenReturn(asList(user, boss, office));

        final List<Person> filteredList = sut.getActivePersonsByRole(BOSS);
        assertThat(filteredList)
            .hasSize(2)
            .contains(boss)
            .contains(office);
    }

    @Test
    void ensureGetPersonsByNotificationTypeReturnsOnlyPersonsWithTheGivenNotificationType() {

        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setPermissions(List.of(USER));
        user.setNotifications(List.of(NOTIFICATION_USER));

        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(asList(USER, BOSS));
        boss.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(asList(USER, BOSS, OFFICE));
        office.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL, NOTIFICATION_OFFICE));

        when(personRepository.findAll()).thenReturn(asList(user, boss, office));

        final List<Person> filteredList = sut.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL);
        assertThat(filteredList)
            .hasSize(2)
            .contains(boss)
            .contains(office);
    }

    @Test
    void ensureGetActivePersonsReturnSortedList() {

        final Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        final Person carl = new Person("carl", "carl", "carl", "carl@example.org");
        final Person rick = new Person("rick", "rick", "rick", "rick@example.org");

        when(personRepository.findAll()).thenReturn(asList(shane, carl, rick));

        final List<Person> sortedList = sut.getActivePersons();
        assertThat(sortedList)
            .containsExactly(carl, rick, shane);
    }


    @Test
    void ensureGetInactivePersonsReturnSortedList() {

        final Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        shane.setPermissions(List.of(INACTIVE));
        final Person carl = new Person("carl", "carl", "carl", "carl@example.org");
        carl.setPermissions(List.of(INACTIVE));
        final Person rick = new Person("rick", "rick", "rick", "rick@example.org");
        rick.setPermissions(List.of(INACTIVE));
        when(personRepository.findAll()).thenReturn(asList(shane, carl, rick));

        final List<Person> sortedList = sut.getInactivePersons();
        assertThat(sortedList)
            .containsExactly(carl, rick, shane);
    }

    @Test
    void ensureGetPersonsByRoleReturnSortedList() {

        final Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        shane.setPermissions(List.of(USER));
        final Person carl = new Person("carl", "carl", "carl", "carl@example.org");
        carl.setPermissions(List.of(USER));
        final Person rick = new Person("rick", "rick", "rick", "rick@example.org");
        rick.setPermissions(List.of(USER));
        when(personRepository.findAll()).thenReturn(asList(shane, carl, rick));

        final List<Person> sortedList = sut.getActivePersonsByRole(USER);
        assertThat(sortedList)
            .containsExactly(carl, rick, shane);
    }

    @Test
    void ensureGetPersonsByNotificationTypeReturnSortedList() {

        final Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        shane.setNotifications(List.of(NOTIFICATION_USER));
        final Person carl = new Person("carl", "carl", "carl", "carl@example.org");
        carl.setNotifications(List.of(NOTIFICATION_USER));
        final Person rick = new Person("rick", "rick", "rick", "rick@example.org");
        rick.setNotifications(List.of(NOTIFICATION_USER));
        when(personRepository.findAll()).thenReturn(asList(shane, carl, rick));

        final List<Person> sortedList = sut.getPersonsWithNotificationType(NOTIFICATION_USER);
        assertThat(sortedList).
            containsExactly(carl, rick, shane);
    }

    @Test
    void ensureThrowsIfNoPersonCanBeFoundForTheCurrentlySignedInUser() {
        assertThatIllegalStateException()
            .isThrownBy(() -> sut.getSignedInUser());
    }

    @Test
    void ensureReturnsPersonForCurrentlySignedInUser() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personRepository.findByUsername("muster")).thenReturn(person);

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

        when(personRepository.findAll()).thenReturn(emptyList());
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
        when(personRepository.findAll()).thenReturn(List.of(officePerson));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(USER));
        assertThat(person.getPermissions()).containsOnly(USER);

        final Person personWithOfficeRole = sut.appointAsOfficeUserIfNoOfficeUserPresent(person);
        assertThat(personWithOfficeRole.getPermissions())
            .containsOnly(USER);
    }

    @Test
    void ensurePersonDisabledEventIsFiredAfterPersonSave() {

        final Person inactivePerson = createPerson("inactive person", INACTIVE);
        inactivePerson.setId(1);
        when(personRepository.save(inactivePerson)).thenReturn(inactivePerson);

        final Person savedInactivePerson = sut.save(inactivePerson);
        verify(applicationEventPublisher).publishEvent(personDisabledEventArgumentCaptor.capture());
        assertThat(personDisabledEventArgumentCaptor.getValue().getPersonId())
            .isEqualTo(savedInactivePerson.getId());
    }

    @Test
    void ensurePersonDisabledEventIsNotFiredAfterPersonSave() {

        final Person activePerson = createPerson("active person", USER);
        activePerson.setId(1);
        when(personRepository.save(activePerson)).thenReturn(activePerson);

        sut.save(activePerson);
        verifyNoInteractions(applicationEventPublisher);
    }
}
