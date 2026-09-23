package org.synyx.urlaubsverwaltung.blackoutperiod;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class BlackoutPeriodServiceImplIT extends SingleTenantTestContainersBase {

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO = LocalDate.of(2026, 12, 31);

    @Autowired
    private BlackoutPeriodService sut;
    @Autowired
    private PersonService personService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void findBlackoutPeriodsForPersonsDoesNotQueryPerPerson() {

        final List<Person> persons = IntStream.rangeClosed(1, 5)
            .mapToObj(i -> personService.create("user" + i, "First" + i, "Last" + i, "user" + i + "@example.org", List.of(), List.of(USER)))
            .toList();

        final Department department = new Department();
        department.setName("Vertrieb");
        department.setMembers(persons);
        final Department savedDepartment = departmentService.create(department);

        final BlackoutPeriod companyWide = new BlackoutPeriod();
        companyWide.setTitle("Jahresabschluss");
        companyWide.setStartDate(LocalDate.of(2026, 12, 20));
        companyWide.setEndDate(LocalDate.of(2027, 1, 5));
        companyWide.setCompanyWide(true);
        companyWide.setAllVacationTypes(true);
        sut.create(companyWide);

        final BlackoutPeriod scoped = new BlackoutPeriod();
        scoped.setTitle("Vertriebssperre");
        scoped.setStartDate(LocalDate.of(2026, 6, 1));
        scoped.setEndDate(LocalDate.of(2026, 6, 10));
        scoped.setDepartments(List.of(savedDepartment));
        scoped.setAllVacationTypes(true);
        sut.create(scoped);

        final long statementsForOnePerson = countStatements(() -> sut.findBlackoutPeriodsForPersons(persons.subList(0, 1), FROM, TO));
        final long statementsForFivePersons = countStatements(() -> sut.findBlackoutPeriodsForPersons(persons, FROM, TO));

        // looking up four more persons must not cost any extra statement
        assertThat(statementsForFivePersons).isEqualTo(statementsForOnePerson);

        final Map<PersonId, List<BlackoutPeriod>> result = sut.findBlackoutPeriodsForPersons(persons, FROM, TO);
        assertThat(result).hasSize(5);
        assertThat(result.values()).allSatisfy(blackoutPeriods ->
            assertThat(blackoutPeriods).extracting(BlackoutPeriod::getTitle).containsExactly("Vertriebssperre", "Jahresabschluss"));
    }

    private long countStatements(Supplier<?> query) {

        // force queries to hit the database instead of returning managed entities from the session cache
        entityManager.flush();
        entityManager.clear();

        final Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        query.get();

        return statistics.getPrepareStatementCount();
    }
}
