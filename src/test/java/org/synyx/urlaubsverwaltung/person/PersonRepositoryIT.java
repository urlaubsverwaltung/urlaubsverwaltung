package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class PersonRepositoryIT extends TestContainersBase {

    @Autowired
    private PersonRepository sut;

    @Autowired
    private PersonService personService;

    @Test
    void countPersonByPermissionsIsNot() {

        final Person marlene = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        marlene.setPermissions(List.of(USER, INACTIVE));
        personService.save(marlene);

        final Person peter = new Person("peter", "Muster", "Peter", "peter@example.org");
        peter.setPermissions(List.of(USER, OFFICE));
        personService.save(peter);

        final Person bettina = new Person("bettina", "Muster", "bettina", "bettina@example.org");
        bettina.setPermissions(List.of(USER));
        personService.save(bettina);

        final int countOfActivePersons = sut.countByPermissionsNotContaining(INACTIVE);
        assertThat(countOfActivePersons).isEqualTo(2);
    }

    @Test
    void findByPersonByPermissionsNotContaining() {

        final Person marlene = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        marlene.setPermissions(List.of(USER, INACTIVE));
        personService.save(marlene);

        final Person peter = new Person("peter", "Muster", "Peter", "peter@example.org");
        peter.setPermissions(List.of(USER, OFFICE));
        personService.save(peter);

        final Person bettina = new Person("bettina", "Muster", "bettina", "bettina@example.org");
        bettina.setPermissions(List.of(USER));
        personService.save(bettina);

        final List<Person> notInactivePersons = sut.findByPermissionsNotContainingOrderByFirstNameAscLastNameAsc(INACTIVE);
        assertThat(notInactivePersons).containsExactly(bettina, peter);
    }

    @Test
    void ensureFindByPersonByPermissionsNotContainingOrderingIsCorrect() {

        final Person xenia = new Person("xenia", "Basta", "xenia", "xenia@example.org");
        xenia.setPermissions(List.of(USER));
        personService.save(xenia);

        final Person peter = new Person("peter", "Muster", "Peter", "peter@example.org");
        peter.setPermissions(List.of(USER, OFFICE));
        personService.save(peter);

        final Person bettina = new Person("bettina", "Muster", "bettina", "bettina@example.org");
        bettina.setPermissions(List.of(USER));
        personService.save(bettina);

        final List<Person> notInactivePersons = sut.findByPermissionsNotContainingOrderByFirstNameAscLastNameAsc(INACTIVE);
        assertThat(notInactivePersons).containsExactly(bettina, peter, xenia);
    }

    @Test
    void findByPersonByPermissionsContaining() {

        final Person marlene = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        marlene.setPermissions(List.of(USER, INACTIVE));
        personService.save(marlene);

        final Person peter = new Person("peter", "Muster", "Peter", "peter@example.org");
        peter.setPermissions(List.of(USER, OFFICE));
        personService.save(peter);

        final Person bettina = new Person("bettina", "Muster", "bettina", "bettina@example.org");
        bettina.setPermissions(List.of(USER));
        personService.save(bettina);

        final List<Person> personsWithOfficeRole = sut.findByPermissionsContainingOrderByFirstNameAscLastNameAsc(OFFICE);
        assertThat(personsWithOfficeRole).containsExactly(peter);
    }

    @Test
    void ensureFindByPersonByPermissionsContainingOrderingIsCorrect() {

        final Person xenia = new Person("xenia", "Basta", "xenia", "xenia@example.org");
        xenia.setPermissions(List.of(USER));
        personService.save(xenia);

        final Person peter = new Person("peter", "Muster", "Peter", "peter@example.org");
        peter.setPermissions(List.of(USER));
        personService.save(peter);

        final Person bettina = new Person("bettina", "Muster", "bettina", "bettina@example.org");
        bettina.setPermissions(List.of(USER));
        personService.save(bettina);

        final List<Person> personsWithUserRole = sut.findByPermissionsContainingOrderByFirstNameAscLastNameAsc(USER);
        assertThat(personsWithUserRole).containsExactly(bettina, peter, xenia);
    }

    @Test
    void ensureFindByPersonByPermissionsContainingAndNotContaining() {

        final Person marlene = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        marlene.setPermissions(List.of(USER, OFFICE, INACTIVE));
        personService.save(marlene);

        final Person peter = new Person("peter", "Muster", "Peter", "peter@example.org");
        peter.setPermissions(List.of(USER, OFFICE));
        personService.save(peter);

        final Person bettina = new Person("bettina", "Muster", "bettina", "bettina@example.org");
        bettina.setPermissions(List.of(USER));
        personService.save(bettina);

        final List<Person> personsWithOfficeRole = sut.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(OFFICE, INACTIVE);
        assertThat(personsWithOfficeRole).containsExactly(peter);
    }

    @Test
    void ensureFindByPersonByPermissionsContainingAndNotContainingOrderingIsCorrect() {

        final Person xenia = new Person("xenia", "Basta", "xenia", "xenia@example.org");
        xenia.setPermissions(List.of(USER));
        personService.save(xenia);

        final Person peter = new Person("peter", "Muster", "Peter", "peter@example.org");
        peter.setPermissions(List.of(USER));
        personService.save(peter);

        final Person bettina = new Person("bettina", "Muster", "bettina", "bettina@example.org");
        bettina.setPermissions(List.of(USER));
        personService.save(bettina);

        final List<Person> personsWithUserRole = sut.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(USER, INACTIVE);
        assertThat(personsWithUserRole).containsExactly(bettina, peter, xenia);
    }

    @Test
    void ensureFindByPersonByPermissionsNotContainingAndContainingNotification() {

        final Person marlene = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        marlene.setPermissions(List.of(USER, OFFICE, INACTIVE));
        marlene.setNotifications(List.of(NOTIFICATION_OFFICE));
        personService.save(marlene);

        final Person peter = new Person("peter", "Muster", "Peter", "peter@example.org");
        peter.setPermissions(List.of(USER, OFFICE));
        peter.setNotifications(List.of(NOTIFICATION_OFFICE));
        personService.save(peter);

        final Person bettina = new Person("bettina", "Muster", "bettina", "bettina@example.org");
        bettina.setPermissions(List.of(USER));
        bettina.setNotifications(List.of(NOTIFICATION_USER));
        personService.save(bettina);

        final List<Person> personsWithOfficeRole = sut.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(INACTIVE, NOTIFICATION_OFFICE);
        assertThat(personsWithOfficeRole).containsExactly(peter);
    }

    @Test
    void ensureFindByPersonByPermissionsContainingAndContainingNotificationsOrderingIsCorrect() {

        final Person xenia = new Person("xenia", "Basta", "xenia", "xenia@example.org");
        xenia.setPermissions(List.of(USER));
        xenia.setNotifications(List.of(NOTIFICATION_OFFICE));
        personService.save(xenia);

        final Person peter = new Person("peter", "Muster", "Peter", "peter@example.org");
        peter.setPermissions(List.of(USER));
        peter.setNotifications(List.of(NOTIFICATION_OFFICE));
        personService.save(peter);

        final Person bettina = new Person("bettina", "Muster", "bettina", "bettina@example.org");
        bettina.setPermissions(List.of(USER));
        bettina.setNotifications(List.of(NOTIFICATION_OFFICE));
        personService.save(bettina);

        final List<Person> personsWithUserRole = sut.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(INACTIVE, NOTIFICATION_OFFICE);
        assertThat(personsWithUserRole).containsExactly(bettina, peter, xenia);
    }
}



