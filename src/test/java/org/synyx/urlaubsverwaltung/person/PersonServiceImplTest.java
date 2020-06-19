package org.synyx.urlaubsverwaltung.person;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.synyx.urlaubsverwaltung.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;

@RunWith(MockitoJUnitRunner.class)
public class PersonServiceImplTest {

    private PersonService sut;

    @Mock
    private PersonDAO personDAO;
    @Mock
    private AccountInteractionService accountInteractionService;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private final ArgumentCaptor<PersonDisabledEvent> personDisabledEventArgumentCaptor = ArgumentCaptor.forClass(PersonDisabledEvent.class);

    @Before
    public void setUp() {

        sut = new PersonServiceImpl(personDAO, accountInteractionService, workingTimeService, applicationEventPublisher);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void ensureDefaultAccountAndWorkingTimeCreation() {
        when(personDAO.save(any(Person.class))).thenReturn(new Person());

        sut.create("rick", "Grimes", "Rick", "rick@grimes.de", emptyList(), emptyList());
        verify(accountInteractionService).createDefaultAccount(any(Person.class));
        verify(workingTimeService).createDefaultWorkingTime(any(Person.class));
    }

    @Test
    public void ensureCreatedPersonHasCorrectAttributes() {

        final Person person = new Person("rick", "Grimes", "Rick", "rick@grimes.de");
        person.setPermissions(asList(USER, BOSS));
        person.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        when(personDAO.save(person)).thenReturn(person);

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
    public void ensureCreatedPersonIsPersisted() {

        final Person person = createPerson();
        when(personDAO.save(person)).thenReturn(person);

        final Person savedPerson = sut.create(person);
        assertThat(savedPerson).isEqualTo(person);
    }

    @Test
    public void ensureUpdatedPersonHasCorrectAttributes() {

        Person person = createPerson();
        when(personDAO.findById(anyInt())).thenReturn(Optional.of(person));
        when(personDAO.save(person)).thenReturn(person);

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
    public void ensureUpdatedPersonIsPersisted() {

        final Person person = createPerson();
        person.setId(1);
        when(personDAO.save(person)).thenReturn(person);

        sut.update(person);

        verify(personDAO).save(person);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfPersonToBeUpdatedHasNoID() {

        Person person = createPerson();
        person.setId(null);

        sut.update(person);
    }


    @Test
    public void ensureSaveCallsCorrectDaoMethod() {

        final Person person = createPerson();
        when(personDAO.save(person)).thenReturn(person);

        final Person savedPerson = sut.save(person);
        assertThat(savedPerson).isEqualTo(person);
    }


    @Test
    public void ensureGetPersonByIDCallsCorrectDaoMethod() {

        sut.getPersonByID(123);
        verify(personDAO).findById(123);
    }


    @Test
    public void ensureGetPersonByLoginCallsCorrectDaoMethod() {
        String username = "foo";
        sut.getPersonByUsername(username);

        verify(personDAO).findByUsername(username);
    }


    @Test
    public void ensureGetActivePersonsReturnsOnlyPersonsThatHaveNotInactiveRole() {

        Person inactive = createPerson("inactive");
        inactive.setPermissions(singletonList(Role.INACTIVE));

        Person user = createPerson("user");
        user.setPermissions(singletonList(USER));

        Person boss = createPerson("boss");
        boss.setPermissions(asList(USER, BOSS));

        Person office = createPerson("office");
        office.setPermissions(asList(USER, BOSS, OFFICE));

        List<Person> allPersons = asList(inactive, user, boss, office);

        when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> activePersons = sut.getActivePersons();

        Assert.assertEquals("Wrong number of persons", 3, activePersons.size());

        Assert.assertTrue("Missing person", activePersons.contains(user));
        Assert.assertTrue("Missing person", activePersons.contains(boss));
        Assert.assertTrue("Missing person", activePersons.contains(office));
    }


    @Test
    public void ensureGetInactivePersonsReturnsOnlyPersonsThatHaveInactiveRole() {

        Person inactive = createPerson("inactive");
        inactive.setPermissions(singletonList(Role.INACTIVE));

        Person user = createPerson("user");
        user.setPermissions(singletonList(USER));

        Person boss = createPerson("boss");
        boss.setPermissions(asList(USER, BOSS));

        Person office = createPerson("office");
        office.setPermissions(asList(USER, BOSS, OFFICE));

        List<Person> allPersons = asList(inactive, user, boss, office);

        when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> inactivePersons = sut.getInactivePersons();

        Assert.assertEquals("Wrong number of persons", 1, inactivePersons.size());

        Assert.assertTrue("Missing person", inactivePersons.contains(inactive));
    }


    @Test
    public void ensureGetPersonsByRoleReturnsOnlyPersonsWithTheGivenRole() {

        Person user = createPerson("user");
        user.setPermissions(singletonList(USER));

        Person boss = createPerson("boss");
        boss.setPermissions(asList(USER, BOSS));

        Person office = createPerson("office");
        office.setPermissions(asList(USER, BOSS, OFFICE));

        List<Person> allPersons = asList(user, boss, office);

        when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> filteredList = sut.getActivePersonsByRole(BOSS);

        Assert.assertEquals("Wrong number of persons", 2, filteredList.size());

        Assert.assertTrue("Missing person", filteredList.contains(boss));
        Assert.assertTrue("Missing person", filteredList.contains(office));
    }


    @Test
    public void ensureGetPersonsByNotificationTypeReturnsOnlyPersonsWithTheGivenNotificationType() {

        Person user = createPerson("user");
        user.setPermissions(singletonList(USER));
        user.setNotifications(singletonList(NOTIFICATION_USER));

        Person boss = createPerson("boss");
        boss.setPermissions(asList(USER, BOSS));
        boss.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL));

        Person office = createPerson("office");
        office.setPermissions(asList(USER, BOSS, OFFICE));
        office.setNotifications(asList(NOTIFICATION_USER, NOTIFICATION_BOSS_ALL,
            MailNotification.NOTIFICATION_OFFICE));

        List<Person> allPersons = asList(user, boss, office);

        when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> filteredList = sut.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL);

        Assert.assertEquals("Wrong number of persons", 2, filteredList.size());

        Assert.assertTrue("Missing person", filteredList.contains(boss));
        Assert.assertTrue("Missing person", filteredList.contains(office));
    }


    @Test
    public void ensureGetActivePersonsReturnSortedList() {

        Person shane = createPerson("shane");
        Person carl = createPerson("carl");
        Person rick = createPerson("rick");

        List<Person> unsortedPersons = asList(shane, carl, rick);

        when(personDAO.findAll()).thenReturn(unsortedPersons);

        List<Person> sortedList = sut.getActivePersons();

        Assert.assertEquals("Wrong number of persons", 3, sortedList.size());
        Assert.assertEquals("Wrong first person", carl, sortedList.get(0));
        Assert.assertEquals("Wrong second person", rick, sortedList.get(1));
        Assert.assertEquals("Wrong third person", shane, sortedList.get(2));
    }


    @Test
    public void ensureGetInactivePersonsReturnSortedList() {

        Person shane = createPerson("shane");
        Person carl = createPerson("carl");
        Person rick = createPerson("rick");

        List<Person> unsortedPersons = asList(shane, carl, rick);
        unsortedPersons.forEach(person -> person.setPermissions(singletonList(Role.INACTIVE)));

        when(personDAO.findAll()).thenReturn(unsortedPersons);

        List<Person> sortedList = sut.getInactivePersons();

        Assert.assertEquals("Wrong number of persons", 3, sortedList.size());
        Assert.assertEquals("Wrong first person", carl, sortedList.get(0));
        Assert.assertEquals("Wrong second person", rick, sortedList.get(1));
        Assert.assertEquals("Wrong third person", shane, sortedList.get(2));
    }


    @Test
    public void ensureGetPersonsByRoleReturnSortedList() {

        Person shane = createPerson("shane");
        Person carl = createPerson("carl");
        Person rick = createPerson("rick");

        List<Person> unsortedPersons = asList(shane, carl, rick);
        unsortedPersons.forEach(person -> person.setPermissions(singletonList(USER)));

        when(personDAO.findAll()).thenReturn(unsortedPersons);

        List<Person> sortedList = sut.getActivePersonsByRole(USER);

        Assert.assertEquals("Wrong number of persons", 3, sortedList.size());
        Assert.assertEquals("Wrong first person", carl, sortedList.get(0));
        Assert.assertEquals("Wrong second person", rick, sortedList.get(1));
        Assert.assertEquals("Wrong third person", shane, sortedList.get(2));
    }


    @Test
    public void ensureGetPersonsByNotificationTypeReturnSortedList() {

        Person shane = createPerson("shane");
        Person carl = createPerson("carl");
        Person rick = createPerson("rick");

        List<Person> unsortedPersons = asList(shane, carl, rick);
        unsortedPersons.forEach(person -> person.setNotifications(singletonList(NOTIFICATION_USER)));

        when(personDAO.findAll()).thenReturn(unsortedPersons);

        List<Person> sortedList = sut.getPersonsWithNotificationType(NOTIFICATION_USER);

        Assert.assertEquals("Wrong number of persons", 3, sortedList.size());
        Assert.assertEquals("Wrong first person", carl, sortedList.get(0));
        Assert.assertEquals("Wrong second person", rick, sortedList.get(1));
        Assert.assertEquals("Wrong third person", shane, sortedList.get(2));
    }

    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfNoPersonCanBeFoundForTheCurrentlySignedInUser() {

        sut.getSignedInUser();
    }


    @Test
    public void ensureReturnsPersonForCurrentlySignedInUser() {

        Person person = createPerson();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(person.getNiceName());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(personDAO.findByUsername(anyString())).thenReturn(person);

        Person signedInUser = sut.getSignedInUser();

        Assert.assertEquals("Wrong person", person, signedInUser);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIllegalOnNullAuthentication() {

        sut.getSignedInUser();
    }

    @Test
    public void ensureCanAppointPersonAsOfficeUser() {

        when(personDAO.findAll()).thenReturn(emptyList());
        when(personDAO.save(any())).then(returnsFirstArg());

        final Person person = createPerson();
        person.setPermissions(singletonList(USER));
        assertThat(person.getPermissions()).containsOnly(USER);

        final Person personWithOfficeRole = sut.appointAsOfficeUserIfNoOfficeUserPresent(person);

        final Collection<Role> permissions = personWithOfficeRole.getPermissions();
        assertThat(permissions)
            .hasSize(2)
            .contains(USER, OFFICE);
    }

    @Test
    public void ensureCanNotAppointPersonAsOfficeUser() {

        Person officePerson = new Person();
        officePerson.setPermissions(singletonList(OFFICE));
        when(personDAO.findAll()).thenReturn(singletonList(officePerson));

        final Person person = createPerson();
        person.setPermissions(singletonList(USER));
        assertThat(person.getPermissions()).containsOnly(USER);

        final Person personWithOfficeRole = sut.appointAsOfficeUserIfNoOfficeUserPresent(person);

        final Collection<Role> permissions = personWithOfficeRole.getPermissions();
        assertThat(permissions)
            .hasSize(1)
            .containsOnly(USER);
    }

    @Test
    public void ensurePersonDisabledEventIsFiredAfterPersonSave() {

        final Person inactivePerson = createPerson("inactive person", INACTIVE);
        inactivePerson.setId(1);

        when(personDAO.save(inactivePerson)).thenReturn(inactivePerson);

        final Person savedInactivePerson = sut.save(inactivePerson);

        verify(applicationEventPublisher).publishEvent(personDisabledEventArgumentCaptor.capture());

        final PersonDisabledEvent actualPersonDisabledEvent = personDisabledEventArgumentCaptor.getValue();
        assertThat(actualPersonDisabledEvent.getPersonId()).isEqualTo(savedInactivePerson.getId());
    }

    @Test
    public void ensurePersonDisabledEventIsNotFiredAfterPersonSave() {

        final Person activePerson = createPerson("active person", USER);
        activePerson.setId(1);

        when(personDAO.save(activePerson)).thenReturn(activePerson);

        sut.save(activePerson);

        verifyZeroInteractions(applicationEventPublisher);
    }
}
