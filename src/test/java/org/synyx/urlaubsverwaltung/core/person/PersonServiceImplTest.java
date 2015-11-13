package org.synyx.urlaubsverwaltung.core.person;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.keys.KeyPairService;
import org.synyx.urlaubsverwaltung.core.util.CryptoUtil;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
public class PersonServiceImplTest {

    private PersonService sut;

    private PersonDAO personDAO;
    private KeyPairService keyPairService;

    private KeyPair generatedKeyPair;

    @Before
    public void setUp() throws NoSuchAlgorithmException {

        personDAO = Mockito.mock(PersonDAO.class);
        keyPairService = Mockito.mock(KeyPairService.class);

        sut = new PersonServiceImpl(personDAO, keyPairService);

        generatedKeyPair = CryptoUtil.generateKeyPair();
        Mockito.when(keyPairService.generate(Mockito.anyString())).thenReturn(generatedKeyPair);
    }


    @Test
    public void ensureCreatedPersonHasCorrectAttributes() {

        Person person = sut.create("rick", "Grimes", "Rick", "rick@grimes.de",
                Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS),
                Arrays.asList(Role.USER, Role.BOSS));

        Assert.assertEquals("Wrong login name", "rick", person.getLoginName());
        Assert.assertEquals("Wrong first name", "Rick", person.getFirstName());
        Assert.assertEquals("Wrong last name", "Grimes", person.getLastName());
        Assert.assertEquals("Wrong email", "rick@grimes.de", person.getEmail());

        Assert.assertEquals("Wrong number of notifications", 2, person.getNotifications().size());
        Assert.assertTrue("Missing notification",
            person.getNotifications().contains(MailNotification.NOTIFICATION_USER));
        Assert.assertTrue("Missing notification",
            person.getNotifications().contains(MailNotification.NOTIFICATION_BOSS));

        Assert.assertEquals("Wrong number of permissions", 2, person.getPermissions().size());
        Assert.assertTrue("Missing permission", person.getPermissions().contains(Role.USER));
        Assert.assertTrue("Missing permission", person.getPermissions().contains(Role.BOSS));
    }


    @Test
    public void ensureCreatedPersonIsPersisted() {

        Person person = sut.create("rick", "Grimes", "Rick", "rick@grimes.de",
                Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS),
                Arrays.asList(Role.USER, Role.BOSS));

        Mockito.verify(personDAO).save(person);
    }


    @Test
    public void ensureGeneratesKeyPairOnCreationOfPerson() throws Exception {

        Person person = sut.create("rick", "Grimes", "Rick", "rick@grimes.de",
                Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS),
                Arrays.asList(Role.USER, Role.BOSS));

        Mockito.verify(keyPairService).generate("rick");

        Assert.assertNotNull("Should have private key", person.getPrivateKey());
        Assert.assertNotNull("Should have public key", person.getPublicKey());

        Assert.assertEquals("Wrong private key", generatedKeyPair.getPrivate(),
            CryptoUtil.getPrivateKeyByBytes(person.getPrivateKey()));
    }


    @Test
    public void ensureUpdatedPersonHasCorrectAttributes() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(personDAO.findOne(Mockito.anyInt())).thenReturn(person);

        Person updatedPerson = sut.update(42, "rick", "Grimes", "Rick", "rick@grimes.de",
                Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS),
                Arrays.asList(Role.USER, Role.BOSS));

        Assert.assertEquals("Wrong login name", "rick", updatedPerson.getLoginName());
        Assert.assertEquals("Wrong first name", "Rick", updatedPerson.getFirstName());
        Assert.assertEquals("Wrong last name", "Grimes", updatedPerson.getLastName());
        Assert.assertEquals("Wrong email", "rick@grimes.de", updatedPerson.getEmail());

        Assert.assertEquals("Wrong number of notifications", 2, updatedPerson.getNotifications().size());
        Assert.assertTrue("Missing notification",
            updatedPerson.getNotifications().contains(MailNotification.NOTIFICATION_USER));
        Assert.assertTrue("Missing notification",
            updatedPerson.getNotifications().contains(MailNotification.NOTIFICATION_BOSS));

        Assert.assertEquals("Wrong number of permissions", 2, updatedPerson.getPermissions().size());
        Assert.assertTrue("Missing permission", updatedPerson.getPermissions().contains(Role.USER));
        Assert.assertTrue("Missing permission", updatedPerson.getPermissions().contains(Role.BOSS));

        Assert.assertEquals("Private key should not have changed", person.getPrivateKey(),
            updatedPerson.getPrivateKey());
        Assert.assertEquals("Public key should not have changed", person.getPublicKey(), updatedPerson.getPublicKey());
    }


    @Test
    public void ensureUpdatedPersonIsPersisted() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(personDAO.findOne(Mockito.anyInt())).thenReturn(person);

        sut.update(42, "rick", "Grimes", "Rick", "rick@grimes.de",
            Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS),
            Arrays.asList(Role.USER, Role.BOSS));

        Mockito.verify(personDAO).save(person);
    }


    @Test
    public void ensureDoesNotGenerateKeyPairOnUpdateOfPerson() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(personDAO.findOne(Mockito.anyInt())).thenReturn(person);

        Person updatedPerson = sut.update(42, "rick", "Grimes", "Rick", "rick@grimes.de",
                Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS),
                Arrays.asList(Role.USER, Role.BOSS));

        Mockito.verifyZeroInteractions(keyPairService);

        Assert.assertEquals("Private key should not have changed", person.getPrivateKey(),
            updatedPerson.getPrivateKey());
        Assert.assertEquals("Public key should not have changed", person.getPublicKey(), updatedPerson.getPublicKey());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfNoPersonWithIDForPersonToBeUpdatedExists() {

        Mockito.when(personDAO.findOne(Mockito.anyInt())).thenReturn(null);

        sut.update(42, "rick", "Grimes", "Rick", "rick@grimes.de",
            Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS),
            Arrays.asList(Role.USER, Role.BOSS));
    }


    @Test
    public void ensureSaveCallsCorrectDaoMethod() {

        Person personToSave = TestDataCreator.createPerson();
        sut.save(personToSave);
        Mockito.verify(personDAO).save(personToSave);
    }


    @Test
    public void ensureGetPersonByIDCallsCorrectDaoMethod() {

        sut.getPersonByID(123);
        Mockito.verify(personDAO).findOne(123);
    }


    @Test
    public void ensureGetPersonByLoginCallsCorrectDaoMethod() {

        String login = "foo";

        sut.getPersonByLogin(login);

        Mockito.verify(personDAO).findByLoginName(login);
    }


    @Test
    public void ensureGetActivePersonsReturnsOnlyPersonsThatHaveNotInactiveRole() {

        Person inactive = TestDataCreator.createPerson("inactive");
        inactive.setPermissions(Collections.singletonList(Role.INACTIVE));

        Person user = TestDataCreator.createPerson("user");
        user.setPermissions(Collections.singletonList(Role.USER));

        Person boss = TestDataCreator.createPerson("boss");
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Person office = TestDataCreator.createPerson("office");
        office.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));

        List<Person> allPersons = Arrays.asList(inactive, user, boss, office);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> activePersons = sut.getActivePersons();

        Assert.assertEquals("Wrong number of persons", 3, activePersons.size());

        Assert.assertTrue("Missing person", activePersons.contains(user));
        Assert.assertTrue("Missing person", activePersons.contains(boss));
        Assert.assertTrue("Missing person", activePersons.contains(office));
    }


    @Test
    public void ensureGetInactivePersonsReturnsOnlyPersonsThatHaveInactiveRole() {

        Person inactive = TestDataCreator.createPerson("inactive");
        inactive.setPermissions(Collections.singletonList(Role.INACTIVE));

        Person user = TestDataCreator.createPerson("user");
        user.setPermissions(Collections.singletonList(Role.USER));

        Person boss = TestDataCreator.createPerson("boss");
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Person office = TestDataCreator.createPerson("office");
        office.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));

        List<Person> allPersons = Arrays.asList(inactive, user, boss, office);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> inactivePersons = sut.getInactivePersons();

        Assert.assertEquals("Wrong number of persons", 1, inactivePersons.size());

        Assert.assertTrue("Missing person", inactivePersons.contains(inactive));
    }


    @Test
    public void ensureGetPersonsByRoleReturnsOnlyPersonsWithTheGivenRole() {

        Person user = TestDataCreator.createPerson("user");
        user.setPermissions(Collections.singletonList(Role.USER));

        Person boss = TestDataCreator.createPerson("boss");
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Person office = TestDataCreator.createPerson("office");
        office.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));

        List<Person> allPersons = Arrays.asList(user, boss, office);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> filteredList = sut.getPersonsByRole(Role.BOSS);

        Assert.assertEquals("Wrong number of persons", 2, filteredList.size());

        Assert.assertTrue("Missing person", filteredList.contains(boss));
        Assert.assertTrue("Missing person", filteredList.contains(office));
    }


    @Test
    public void ensureGetPersonsByNotificationTypeReturnsOnlyPersonsWithTheGivenNotificationType() {

        Person user = TestDataCreator.createPerson("user");
        user.setPermissions(Collections.singletonList(Role.USER));
        user.setNotifications(Collections.singletonList(MailNotification.NOTIFICATION_USER));

        Person boss = TestDataCreator.createPerson("boss");
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));
        boss.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS));

        Person office = TestDataCreator.createPerson("office");
        office.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));
        office.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS,
                MailNotification.NOTIFICATION_OFFICE));

        List<Person> allPersons = Arrays.asList(user, boss, office);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> filteredList = sut.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);

        Assert.assertEquals("Wrong number of persons", 2, filteredList.size());

        Assert.assertTrue("Missing person", filteredList.contains(boss));
        Assert.assertTrue("Missing person", filteredList.contains(office));
    }
}
