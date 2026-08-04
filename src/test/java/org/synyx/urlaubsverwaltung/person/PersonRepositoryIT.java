package org.synyx.urlaubsverwaltung.person;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.account.AccountService;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class PersonRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private PersonRepository sut;

    @Autowired
    private PersonService personService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountInteractionService accountInteractionService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void countPersonByPermissionsIsNot() {

        personService.create("marlene", "Marlene", "Muster", "muster@example.org", List.of(), List.of(USER, INACTIVE));
        personService.create("peter", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER, OFFICE));
        personService.create("bettina", "bettina", "Muster", "bettina@example.org", List.of(), List.of(USER));

        final int countOfActivePersons = sut.countByPermissionsNotContaining(INACTIVE);
        assertThat(countOfActivePersons).isEqualTo(2);
    }

    @Test
    void ensureToFindPersonsWithRoleWithoutTheId() {

        personService.create("marlene", "Marlene", "Muster", "muster@example.org", List.of(), List.of(USER, INACTIVE));
        personService.create("peter", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER, OFFICE));
        personService.create("simone", "Peter", "Muster", "simone@example.org", List.of(), List.of(USER, OFFICE));
        final Person savedBettina = personService.create("bettina", "bettina", "Muster", "bettina@example.org", List.of(), List.of(USER, OFFICE));

        final Long id = savedBettina.getId();
        final int countOfActivePersons = sut.countByPermissionsContainingAndIdNotIn(OFFICE, List.of(id));
        assertThat(countOfActivePersons).isEqualTo(2);
    }

    @Test
    void findByPersonByPermissionsNotContaining() {

        personService.create("marlene", "Marlene", "Muster", "muster@example.org", List.of(), List.of(USER, INACTIVE));
        final Person peter = personService.create("peter", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER, OFFICE));
        final Person bettina = personService.create("bettina", "bettina", "Muster", "bettina@example.org", List.of(), List.of(USER));

        final List<Person> notInactivePersons = sut.findByPermissionsNotContainingOrderByFirstNameAscLastNameAsc(INACTIVE);
        assertThat(notInactivePersons).containsExactly(bettina, peter);
    }

    @Test
    void ensureFindByPersonByPermissionsNotContainingOrderingIsCorrect() {

        final Person xenia = personService.create("xenia", "xenia", "Basta", "xenia@example.org", List.of(), List.of(USER));
        final Person peter = personService.create("peter", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER, OFFICE));
        final Person bettina = personService.create("bettina", "bettina", "Muster", "bettina@example.org", List.of(), List.of(USER));

        final List<Person> notInactivePersons = sut.findByPermissionsNotContainingOrderByFirstNameAscLastNameAsc(INACTIVE);
        assertThat(notInactivePersons).containsExactly(bettina, peter, xenia);
    }

    @Test
    void findByPersonByPermissionsContaining() {

        personService.create("marlene", "Marlene", "Muster", "muster@example.org", List.of(), List.of(USER, INACTIVE));
        personService.create("bettina", "bettina", "Muster", "bettina@example.org", List.of(), List.of(USER));
        final Person peter = personService.create("peter", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER, OFFICE));

        final List<Person> personsWithOfficeRole = sut.findByPermissionsContainingOrderByFirstNameAscLastNameAsc(OFFICE);
        assertThat(personsWithOfficeRole).containsExactly(peter);
    }

    @Test
    void ensureFindByPersonByPermissionsContainingOrderingIsCorrect() {

        final Person xenia = personService.create("xenia", "xenia", "Basta", "xenia@example.org", List.of(), List.of(USER));
        final Person peter = personService.create("peter", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER));
        final Person bettina = personService.create("bettina", "bettina", "Muster", "bettina@example.org", List.of(), List.of(USER));

        final List<Person> personsWithUserRole = sut.findByPermissionsContainingOrderByFirstNameAscLastNameAsc(USER);
        assertThat(personsWithUserRole).containsExactly(bettina, peter, xenia);
    }

    @Test
    void ensureFindByPersonByPermissionsContainingAndNotContaining() {

        personService.create("marlene", "Marlene", "Muster", "muster@example.org", List.of(), List.of(USER, OFFICE, INACTIVE));
        personService.create("bettina", "bettina", "Muster", "bettina@example.org", List.of(), List.of(USER));
        final Person peter = personService.create("peter", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER, OFFICE));

        final List<Person> personsWithOfficeRole = sut.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(OFFICE, INACTIVE);
        assertThat(personsWithOfficeRole).containsExactly(peter);
    }

    @Test
    void ensureFindByPersonByPermissionsContainingAndNotContainingOrderingIsCorrect() {

        final Person xenia = personService.create("xenia", "xenia", "Basta", "xenia@example.org", List.of(), List.of(USER));
        final Person peter = personService.create("peter", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER));
        final Person bettina = personService.create("bettina", "bettina", "Muster", "bettina@example.org", List.of(), List.of(USER));

        final List<Person> personsWithUserRole = sut.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(USER, INACTIVE);
        assertThat(personsWithUserRole).containsExactly(bettina, peter, xenia);
    }

    @Test
    void ensureFindByPersonByPermissionsNotContainingAndContainingNotification() {

        personService.create("marlene", "Marlene", "Muster", "muster@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED), List.of(USER, OFFICE, INACTIVE));
        personService.create("bettina", "bettina", "Muster", "bettina@example.org", List.of(), List.of(USER));
        final Person peter = personService.create("peter", "Peter", "Muster", "peter@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED), List.of(USER, OFFICE));

        final List<Person> personsWithOfficeRole = sut.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(INACTIVE, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        assertThat(personsWithOfficeRole).containsExactly(peter);
    }

    @Test
    void ensureFindByPersonByPermissionsContainingAndContainingNotificationsOrderingIsCorrect() {

        final Person xenia = personService.create("xenia", "xenia", "Basta", "xenia@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED), List.of(USER));
        final Person peter = personService.create("peter", "Peter", "Muster", "peter@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED), List.of(USER));
        final Person bettina = personService.create("bettina", "bettina", "Muster", "bettina@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED), List.of(USER));

        final List<Person> personsWithUserRole = sut.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(INACTIVE, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED);
        assertThat(personsWithUserRole).containsExactly(bettina, peter, xenia);
    }

    @Test
    void ensureFindByPermissionsNotContainingAndByNiceNameContainingIgnoreCase() {

        personService.create("username_1", "xenia", "Basta", "xenia@example.org", List.of(), List.of(USER));
        personService.create("username_3", "Mustafa", "Tunichtgut", "mustafa@example.org", List.of(), List.of(INACTIVE));
        final Person peter = personService.create("username_2", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER));
        final Person rosamund = personService.create("username_4", "Rosamund", "Hatgoldimmund", "rosamund@example.org", List.of(), List.of(USER));

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final Page<Person> actual = sut.findByPermissionsNotContainingAndByNiceNameContainingIgnoreCase(INACTIVE, "mu", pageRequest);
        assertThat(actual.getContent()).containsExactly(peter, rosamund);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Peter mu", "Peter Muster"})
    void ensureFindByPermissionsNotContainingAndByNiceNameContainingIgnoreCaseWithFirstAndLastName(final String query) {
        final Person peter = personService.create("username_2", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER));
        final Page<Person> actual = sut.findByPermissionsNotContainingAndByNiceNameContainingIgnoreCase(INACTIVE, query, PageRequest.of(0, 10));
        assertThat(actual.getContent()).containsExactly(peter);
    }

    @Test
    void ensureFindByPermissionsContainingAndNiceNameContainingIgnoreCase() {
        personService.create("username_1", "xenia", "Basta", "xenia@example.org", List.of(), List.of(USER));
        personService.create("username_2", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER));
        personService.create("username_4", "Rosamund", "Hatgoldimmund", "rosamund@example.org", List.of(), List.of(USER));
        final Person mustafa = personService.create("username_3", "Mustafa", "Tunichtgut", "mustafa@example.org", List.of(), List.of(INACTIVE));

        final Page<Person> actual = sut.findByPermissionsContainingAndNiceNameContainingIgnoreCase(INACTIVE, "mu", PageRequest.of(0, 10));
        assertThat(actual.getContent()).containsExactly(mustafa);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Peter mu", "Peter Muster"})
    void ensureFindByPermissionsContainingAndNiceNameContainingIgnoreCaseWithFirstAndLastName(final String query) {
        final Person peter = personService.create("username_2", "Peter", "Muster", "peter@example.org", List.of(), List.of(INACTIVE));
        final Page<Person> actual = sut.findByPermissionsContainingAndNiceNameContainingIgnoreCase(INACTIVE, query, PageRequest.of(0, 10));
        assertThat(actual.getContent()).containsExactly(peter);
    }

    @Test
    void ensureFindAllByIdOrderByFirstNameAscLastNameAsc() {

        final Person person1 = personService.create("username_1", "xenia", "Basta", "xenia@example.org", List.of(), List.of(USER));
        final Person person2 = personService.create("username_2", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER));
        final Person person3 = personService.create("username_3", "Mustafa", "Tunichtgut", "mustafa@example.org", List.of(), List.of(INACTIVE));
        personService.create("username_4", "Rosamund", "Hatgoldimmund", "rosamund@example.org", List.of(), List.of(USER));

        final List<Person> actual = sut.findAllByIdIsInOrderByFirstNameAscLastNameAsc(List.of(person1.getId(), person2.getId(), person3.getId()));

        assertThat(actual).satisfiesExactly(
            person -> assertThat(person).isEqualTo(person3),
            person -> assertThat(person).isEqualTo(person2),
            person -> assertThat(person).isEqualTo(person1)
        );
    }

    @Test
    void ensurePermissionsAndNotificationsAreFetchedWithoutOneQueryPerPerson() {

        // every person gets the same permissions and notifications, so the only thing that differs between the two
        // measurements below is the number of persons - and therefore the number of collections to initialise.
        final List<Person> persons = List.of(
            personService.create("username_1", "Xenia", "Basta", "xenia@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED), List.of(USER, OFFICE)),
            personService.create("username_2", "Peter", "Muster", "peter@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED), List.of(USER, OFFICE)),
            personService.create("username_3", "Mustafa", "Tunichtgut", "mustafa@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED), List.of(USER, OFFICE))
        );

        final List<Long> personIds = persons.stream().map(Person::getId).toList();

        final long statementsForOnePerson = fetchAndTouchPermissionsAndNotifications(personIds.subList(0, 1));
        final long statementsForThreePersons = fetchAndTouchPermissionsAndNotifications(personIds);

        // fetching two more persons with their permissions and notifications must not cost any extra queries
        assertThat(statementsForThreePersons).isEqualTo(statementsForOnePerson);
    }

    private long fetchAndTouchPermissionsAndNotifications(List<Long> personIds) {

        // force the next query to actually hit the database instead of returning managed entities from the session cache
        entityManager.flush();
        entityManager.clear();

        final Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        final List<Person> persons = sut.findAllByIdIsInOrderByFirstNameAscLastNameAsc(personIds);
        persons.forEach(person -> {
            person.getPermissions().size();
            person.getNotifications().size();
        });

        assertThat(persons).hasSize(personIds.size());
        return statistics.getPrepareStatementCount();
    }

    @Test
    void findAllWithAccountByYear() {

        final Year currentYear = Year.now();

        final Person person = personService.create("marlene", "Marlene", "Muster", "muster@example.org", List.of(), List.of(USER));
        personService.create("peter", "Peter", "Muster", "peter@example.org", List.of(), List.of(USER, OFFICE));
        personService.create("bettina", "bettina", "Muster", "bettina@example.org", List.of(), List.of(USER));

        final Account previousYearAccount = new Account();
        previousYearAccount.setPerson(person);
        previousYearAccount.setValidFrom(LocalDate.of(currentYear.minusYears(1).getValue(), JANUARY, 1));
        previousYearAccount.setValidTo(LocalDate.of(currentYear.minusYears(1).getValue(), DECEMBER, 31));
        accountService.save(previousYearAccount);

        assertThat(sut.findAllWithAccountByYear(currentYear.getValue())).hasSize(3);
        assertThat(sut.findAllWithAccountByYear(currentYear.minusYears(1).getValue())).hasSize(1);
    }
}



