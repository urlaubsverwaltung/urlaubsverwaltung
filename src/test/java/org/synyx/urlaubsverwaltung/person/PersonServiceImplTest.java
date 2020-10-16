package org.synyx.urlaubsverwaltung.person;

import org.junit.Assert;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {

    private PersonService sut;

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

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personRepository.findById(anyInt())).thenReturn(Optional.of(person));
        when(personRepository.save(person)).thenReturn(person);

        Person updatedPerson = sut.update(42, "rick", "Grimes", "Rick", "rick@grimes.de",
            asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL),
            asList(USER, BOSS));

        Assert.assertEquals("Wrong username", "rick", updatedPerson.getUsername());
        Assert.assertEquals("Wrong first name", "Rick", updatedPerson.getFirstName());
        Assert.assertEquals("Wrong last name", "Grimes", updatedPerson.getLastName());
        Assert.assertEquals("Wrong email", "rick@grimes.de", updatedPerson.getEmail());

        Assert.assertEquals("Wrong number of notifications", 2, updatedPerson.getNotifications().size());
        Assert.assertTrue("Missing notification", updatedPerson.getNotifications().contains(NOTIFICATION_USER));
        Assert.assertTrue("Missing notification", updatedPerson.getNotifications().contains(NOTIFICATION_BOSS_ALL));

        Assert.assertEquals("Wrong number of permissions", 2, updatedPerson.getPermissions().size());
        Assert.assertTrue("Missing permission", updatedPerson.getPermissions().contains(USER));
        Assert.assertTrue("Missing permission", updatedPerson.getPermissions().contains(BOSS));

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

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(null);

        assertThatIllegalArgumentException().isThrownBy(() -> sut.update(person));
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
        String username = "foo";
        sut.getPersonByUsername(username);

        verify(personRepository).findByUsername(username);
    }


    @Test
    void ensureGetActivePersonsReturnsOnlyPersonsThatHaveNotInactiveRole() {

        Person inactive = new Person("muster", "Muster", "Marlene", "muster@example.org");
        inactive.setPermissions(singletonList(Role.INACTIVE));

        Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setPermissions(singletonList(USER));

        Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(asList(USER, BOSS));

        Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(asList(USER, BOSS, OFFICE));

        List<Person> allPersons = asList(inactive, user, boss, office);

        when(personRepository.findAll()).thenReturn(allPersons);

        List<Person> activePersons = sut.getActivePersons();

        Assert.assertEquals("Wrong number of persons", 3, activePersons.size());

        Assert.assertTrue("Missing person", activePersons.contains(user));
        Assert.assertTrue("Missing person", activePersons.contains(boss));
        Assert.assertTrue("Missing person", activePersons.contains(office));
    }


    @Test
    void ensureGetInactivePersonsReturnsOnlyPersonsThatHaveInactiveRole() {

        Person inactive = new Person("muster", "Muster", "Marlene", "muster@example.org");
        inactive.setPermissions(singletonList(Role.INACTIVE));

        Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setPermissions(singletonList(USER));

        Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(asList(USER, BOSS));

        Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(asList(USER, BOSS, OFFICE));

        List<Person> allPersons = asList(inactive, user, boss, office);

        when(personRepository.findAll()).thenReturn(allPersons);

        List<Person> inactivePersons = sut.getInactivePersons();

        Assert.assertEquals("Wrong number of persons", 1, inactivePersons.size());

        Assert.assertTrue("Missing person", inactivePersons.contains(inactive));
    }


    @Test
    void ensureGetPersonsByRoleReturnsOnlyPersonsWithTheGivenRole() {

        Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setPermissions(singletonList(USER));

        Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(asList(USER, BOSS));

        Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(asList(USER, BOSS, OFFICE));

        List<Person> allPersons = asList(user, boss, office);

        when(personRepository.findAll()).thenReturn(allPersons);

        List<Person> filteredList = sut.getActivePersonsByRole(BOSS);

        Assert.assertEquals("Wrong number of persons", 2, filteredList.size());

        Assert.assertTrue("Missing person", filteredList.contains(boss));
        Assert.assertTrue("Missing person", filteredList.contains(office));
    }


    @Test
    void ensureGetPersonsByNotificationTypeReturnsOnlyPersonsWithTheGivenNotificationType() {

        Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setPermissions(singletonList(USER));
        user.setNotifications(singletonList(NOTIFICATION_USER));

        Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");
        boss.setPermissions(asList(USER, BOSS));
        boss.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(asList(USER, BOSS, OFFICE));
        office.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL,
            MailNotification.NOTIFICATION_OFFICE));

        List<Person> allPersons = asList(user, boss, office);

        when(personRepository.findAll()).thenReturn(allPersons);

        List<Person> filteredList = sut.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL);

        Assert.assertEquals("Wrong number of persons", 2, filteredList.size());

        Assert.assertTrue("Missing person", filteredList.contains(boss));
        Assert.assertTrue("Missing person", filteredList.contains(office));
    }


    @Test
    void ensureGetActivePersonsReturnSortedList() {

        Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        Person carl = new Person("carl", "carl", "carl", "carl@example.org");
        Person rick = new Person("rick", "rick", "rick", "rick@example.org");

        List<Person> unsortedPersons = asList(shane, carl, rick);

        when(personRepository.findAll()).thenReturn(unsortedPersons);

        List<Person> sortedList = sut.getActivePersons();

        Assert.assertEquals("Wrong number of persons", 3, sortedList.size());
        Assert.assertEquals("Wrong first person", carl, sortedList.get(0));
        Assert.assertEquals("Wrong second person", rick, sortedList.get(1));
        Assert.assertEquals("Wrong third person", shane, sortedList.get(2));
    }


    @Test
    void ensureGetInactivePersonsReturnSortedList() {

        Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        Person carl = new Person("carl", "carl", "carl", "carl@example.org");
        Person rick = new Person("rick", "rick", "rick", "rick@example.org");

        List<Person> unsortedPersons = asList(shane, carl, rick);
        unsortedPersons.forEach(person -> person.setPermissions(singletonList(Role.INACTIVE)));

        when(personRepository.findAll()).thenReturn(unsortedPersons);

        List<Person> sortedList = sut.getInactivePersons();

        Assert.assertEquals("Wrong number of persons", 3, sortedList.size());
        Assert.assertEquals("Wrong first person", carl, sortedList.get(0));
        Assert.assertEquals("Wrong second person", rick, sortedList.get(1));
        Assert.assertEquals("Wrong third person", shane, sortedList.get(2));
    }


    @Test
    void ensureGetPersonsByRoleReturnSortedList() {

        Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        Person carl = new Person("carl", "carl", "carl", "carl@example.org");
        Person rick = new Person("rick", "rick", "rick", "rick@example.org");

        List<Person> unsortedPersons = asList(shane, carl, rick);
        unsortedPersons.forEach(person -> person.setPermissions(singletonList(USER)));

        when(personRepository.findAll()).thenReturn(unsortedPersons);

        List<Person> sortedList = sut.getActivePersonsByRole(USER);

        Assert.assertEquals("Wrong number of persons", 3, sortedList.size());
        Assert.assertEquals("Wrong first person", carl, sortedList.get(0));
        Assert.assertEquals("Wrong second person", rick, sortedList.get(1));
        Assert.assertEquals("Wrong third person", shane, sortedList.get(2));
    }


    @Test
    void ensureGetPersonsByNotificationTypeReturnSortedList() {

        Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        Person carl = new Person("carl", "carl", "carl", "carl@example.org");
        Person rick = new Person("rick", "rick", "rick", "rick@example.org");

        List<Person> unsortedPersons = asList(shane, carl, rick);
        unsortedPersons.forEach(person -> person.setNotifications(singletonList(NOTIFICATION_USER)));

        when(personRepository.findAll()).thenReturn(unsortedPersons);

        List<Person> sortedList = sut.getPersonsWithNotificationType(NOTIFICATION_USER);

        Assert.assertEquals("Wrong number of persons", 3, sortedList.size());
        Assert.assertEquals("Wrong first person", carl, sortedList.get(0));
        Assert.assertEquals("Wrong second person", rick, sortedList.get(1));
        Assert.assertEquals("Wrong third person", shane, sortedList.get(2));
    }

    @Test
    void ensureThrowsIfNoPersonCanBeFoundForTheCurrentlySignedInUser() {
        assertThatIllegalStateException().isThrownBy(() -> sut.getSignedInUser());
    }

    @Test
    void ensureReturnsPersonForCurrentlySignedInUser() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(person.getNiceName());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(personRepository.findByUsername(anyString())).thenReturn(person);

        Person signedInUser = sut.getSignedInUser();

        Assert.assertEquals("Wrong person", person, signedInUser);
    }

    @Test
    void ensureThrowsIllegalOnNullAuthentication() {
        assertThatIllegalStateException().isThrownBy(() -> sut.getSignedInUser());
    }

    @Test
    void ensureCanAppointPersonAsOfficeUser() {

        when(personRepository.findAll()).thenReturn(emptyList());
        when(personRepository.save(any())).then(returnsFirstArg());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(singletonList(USER));
        assertThat(person.getPermissions()).containsOnly(USER);

        final Person personWithOfficeRole = sut.appointAsOfficeUserIfNoOfficeUserPresent(person);

        final Collection<Role> permissions = personWithOfficeRole.getPermissions();
        assertThat(permissions)
            .hasSize(2)
            .contains(USER, OFFICE);
    }

    @Test
    void ensureCanNotAppointPersonAsOfficeUser() {

        Person officePerson = new Person();
        officePerson.setPermissions(singletonList(OFFICE));
        when(personRepository.findAll()).thenReturn(singletonList(officePerson));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(singletonList(USER));
        assertThat(person.getPermissions()).containsOnly(USER);

        final Person personWithOfficeRole = sut.appointAsOfficeUserIfNoOfficeUserPresent(person);

        final Collection<Role> permissions = personWithOfficeRole.getPermissions();
        assertThat(permissions)
            .hasSize(1)
            .containsOnly(USER);
    }

    @Test
    void ensurePersonDisabledEventIsFiredAfterPersonSave() {

        final Person inactivePerson = createPerson("inactive person", INACTIVE);
        inactivePerson.setId(1);

        when(personRepository.save(inactivePerson)).thenReturn(inactivePerson);

        final Person savedInactivePerson = sut.save(inactivePerson);

        verify(applicationEventPublisher).publishEvent(personDisabledEventArgumentCaptor.capture());

        final PersonDisabledEvent actualPersonDisabledEvent = personDisabledEventArgumentCaptor.getValue();
        assertThat(actualPersonDisabledEvent.getPersonId()).isEqualTo(savedInactivePerson.getId());
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
