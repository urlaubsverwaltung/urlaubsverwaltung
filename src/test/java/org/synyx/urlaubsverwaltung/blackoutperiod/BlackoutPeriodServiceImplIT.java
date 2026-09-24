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
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentEntity;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class BlackoutPeriodServiceImplIT extends SingleTenantTestContainersBase {

    private static final LocalDate FROM = LocalDate.of(2026, JANUARY, 1);
    private static final LocalDate TO = LocalDate.of(2026, DECEMBER, 31);

    @Autowired
    private BlackoutPeriodService sut;
    @Autowired
    private PersonService personService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private VacationTypeService vacationTypeService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void findBlackoutPeriodsForPersonsDoesNotQueryPerPerson() {

        final List<Person> persons = createPersons("user", 5);
        final Department department = createDepartment("Vertrieb", persons);
        createCompanyWideAndScopedBlackoutPeriods(department);

        final long statementsForOnePerson = countStatements(() -> sut.findBlackoutPeriodsForPersons(persons.subList(0, 1), FROM, TO));
        final long statementsForFivePersons = countStatements(() -> sut.findBlackoutPeriodsForPersons(persons, FROM, TO));

        // looking up four more persons must not cost any extra statement
        assertThat(statementsForFivePersons).isEqualTo(statementsForOnePerson);

        final Map<PersonId, List<BlackoutPeriod>> result = sut.findBlackoutPeriodsForPersons(persons, FROM, TO);
        assertThat(result).hasSize(5);
        assertThat(result.values()).allSatisfy(blackoutPeriods ->
            assertThat(blackoutPeriods).extracting(BlackoutPeriod::getTitle).containsExactly("Vertriebssperre", "Jahresabschluss"));
    }

    @Test
    void findBlackoutPeriodsForPersonsDoesNotQueryPerStoredBlackoutPeriod() {

        final List<Person> persons = createPersons("user", 5);
        final Department department = createDepartment("Vertrieb", persons);
        createCompanyWideAndScopedBlackoutPeriods(department);

        createPastBlackoutPeriod(department, 2020);
        final long statementsWithOnePastBlackoutPeriod = countStatements(() -> sut.findBlackoutPeriodsForPersons(persons, FROM, TO));

        IntStream.rangeClosed(2021, 2024).forEach(year -> createPastBlackoutPeriod(department, year));
        final long statementsWithFivePastBlackoutPeriods = countStatements(() -> sut.findBlackoutPeriodsForPersons(persons, FROM, TO));

        // past blackout periods must neither be loaded nor cost any extra statement
        assertThat(statementsWithFivePastBlackoutPeriods).isEqualTo(statementsWithOnePastBlackoutPeriod);
    }

    @Test
    void findBlackoutPeriodsForPersonsDoesNotQueryPerDepartmentMember() {

        final List<Person> persons = createPersons("user", 5);
        final Department department = createDepartment("Vertrieb", persons);
        createCompanyWideAndScopedBlackoutPeriods(department);

        final long statementsBefore = countStatements(() -> sut.findBlackoutPeriodsForPersons(persons, FROM, TO));

        createDepartment("Marketing", createPersons("marketing", 5));
        final long statementsWithAnotherDepartment = countStatements(() -> sut.findBlackoutPeriodsForPersons(persons, FROM, TO));

        // departments and their members must not be loaded for matching
        assertThat(statementsWithAnotherDepartment).isEqualTo(statementsBefore);
        assertThat(countDepartmentLoads(() -> sut.findBlackoutPeriodsForPersons(persons, FROM, TO))).isZero();
    }

    @Test
    void findBlockingBlackoutPeriodDoesNotQueryPerStoredBlackoutPeriod() {

        final List<Person> persons = createPersons("user", 1);
        final Person person = persons.getFirst();
        final Department department = createDepartment("Vertrieb", persons);
        createCompanyWideAndScopedBlackoutPeriods(department);

        final VacationType<?> vacationType = vacationTypeService.getAllVacationTypes().getFirst();
        final LocalDate startDate = LocalDate.of(2026, JUNE, 2);
        final LocalDate endDate = LocalDate.of(2026, JUNE, 3);

        createPastBlackoutPeriod(department, 2020);
        final long statementsWithOnePastBlackoutPeriod = countStatements(() -> sut.findBlockingBlackoutPeriod(person, startDate, endDate, vacationType));

        IntStream.rangeClosed(2021, 2024).forEach(year -> createPastBlackoutPeriod(department, year));
        final long statementsWithFivePastBlackoutPeriods = countStatements(() -> sut.findBlockingBlackoutPeriod(person, startDate, endDate, vacationType));

        assertThat(statementsWithFivePastBlackoutPeriods).isEqualTo(statementsWithOnePastBlackoutPeriod);

        final Optional<BlackoutPeriod> blocking = sut.findBlockingBlackoutPeriod(person, startDate, endDate, vacationType);
        assertThat(blocking).map(BlackoutPeriod::getTitle).hasValue("Vertriebssperre");
    }

    private List<Person> createPersons(String prefix, int count) {
        return IntStream.rangeClosed(1, count)
            .mapToObj(i -> personService.create(prefix + i, "First" + i, "Last" + i, prefix + i + "@example.org", List.of(), List.of(USER)))
            .toList();
    }

    private Department createDepartment(String name, List<Person> members) {
        final Department department = new Department();
        department.setName(name);
        department.setMembers(members);
        return departmentService.create(department);
    }

    private void createCompanyWideAndScopedBlackoutPeriods(Department department) {

        final BlackoutPeriod companyWide = new BlackoutPeriod();
        companyWide.setTitle("Jahresabschluss");
        companyWide.setStartDate(LocalDate.of(2026, DECEMBER, 20));
        companyWide.setEndDate(LocalDate.of(2027, JANUARY, 5));
        companyWide.setCompanyWide(true);
        companyWide.setAllVacationTypes(true);
        sut.create(companyWide);

        final BlackoutPeriod scoped = new BlackoutPeriod();
        scoped.setTitle("Vertriebssperre");
        scoped.setStartDate(LocalDate.of(2026, JUNE, 1));
        scoped.setEndDate(LocalDate.of(2026, JUNE, 10));
        scoped.setDepartments(List.of(department));
        scoped.setAllVacationTypes(true);
        sut.create(scoped);
    }

    private void createPastBlackoutPeriod(Department department, int year) {
        final BlackoutPeriod past = new BlackoutPeriod();
        past.setTitle("Sperre " + year);
        past.setStartDate(LocalDate.of(year, MARCH, 1));
        past.setEndDate(LocalDate.of(year, MARCH, 10));
        past.setDepartments(List.of(department));
        past.setVacationTypes(List.of(vacationTypeService.getAllVacationTypes().getFirst()));
        sut.create(past);
    }

    private long countStatements(Supplier<?> query) {
        return measure(query).getPrepareStatementCount();
    }

    private long countDepartmentLoads(Supplier<?> query) {
        return measure(query).getEntityStatistics(DepartmentEntity.class.getName()).getLoadCount();
    }

    private Statistics measure(Supplier<?> query) {

        // force queries to hit the database instead of returning managed entities from the session cache
        entityManager.flush();
        entityManager.clear();

        final Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        query.get();

        return statistics;
    }
}
