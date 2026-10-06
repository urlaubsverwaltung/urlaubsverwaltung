package org.synyx.urlaubsverwaltung.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.synyx.urlaubsverwaltung.SingleTenantTestPostgreSQLContainer;
import org.synyx.urlaubsverwaltung.TestKeycloakContainer;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.ui.extension.UiIntegrationTest;
import org.synyx.urlaubsverwaltung.ui.extension.UiTest;
import org.synyx.urlaubsverwaltung.ui.pages.AbsenceOverviewPage;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.util.StringUtils.trimAllWhitespace;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Testcontainers(parallel = true)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@UiIntegrationTest
@UiTest
class AbsenceOverviewUIIT {

    @LocalServerPort
    private int port;

    @Container
    @ServiceConnection
    private static final SingleTenantTestPostgreSQLContainer postgre = new SingleTenantTestPostgreSQLContainer();
    @Container
    private static final TestKeycloakContainer keycloak = new TestKeycloakContainer();

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        keycloak.configureSpringDataSource(registry);
    }

    @Autowired
    private PersonService personService;
    @Autowired
    private AccountInteractionService accountInteractionService;
    @Autowired
    private WorkingTimeWriteService workingTimeWriteService;
    @Autowired
    private DepartmentService departmentService;

    @TestConfiguration
    static class TestSessionConfig {
        @Bean
        public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> disableCleanupCustomizer() {
            // Force-set the cleanup cron to the official disabled macro programmatically
            return sessionRepository -> sessionRepository.setCleanupCron(Scheduled.CRON_DISABLED);
        }
    }

    @Test
    void ensureDepartmentPickerFiltersTheOverview(Page page) {

        final Person olga = createPerson("Olga", "Office", List.of(USER, OFFICE));
        final Person alice = createPerson("Alice", "Accounting", List.of(USER));
        final Person bob = createPerson("Bob", "Sales", List.of(USER));
        createPerson("Zoe", "Ohneabteilung", List.of(USER));
        createDepartment("Buchhaltung", List.of(alice));
        createDepartment("Vertrieb", List.of(olga, bob));

        login(page, olga);

        final AbsenceOverviewPage overview = new AbsenceOverviewPage(page);
        overview.navigate(port);

        // without a selection the own department is shown
        assertThat(overview.getDepartmentPickerButtonLocator()).containsText("Vertrieb");
        overview.showsPersons("Bob", "Olga");

        overview.openDepartmentPicker();
        overview.checkDepartment("Buchhaltung");
        overview.confirmDepartmentPicker();

        assertThat(page).hasURL(Pattern.compile("department=Buchhaltung"));
        assertThat(page).hasURL(Pattern.compile("department=Vertrieb"));
        assertThat(overview.getDepartmentPickerButtonLocator()).containsText("Buchhaltung, Vertrieb");
        overview.showsPersons("Alice", "Bob", "Olga");

        overview.openDepartmentPicker();
        overview.checkAllPersons();
        overview.confirmDepartmentPicker();

        // "Alle Personen" is every active person for office - including Zoe without department and the persons of
        // the other tests of this class, which share the database
        final String[] everyActivePerson = personService.getActivePersons().stream()
            .map(Person::getFirstName)
            .sorted()
            .toArray(String[]::new);

        assertThat(page).hasURL(Pattern.compile("allPersons=true"));
        assertThat(overview.getDepartmentPickerButtonLocator()).containsText("Alle Personen");
        overview.showsPersons(everyActivePerson);

        // changing the year keeps "Alle Personen"
        overview.selectYear(String.valueOf(Year.now().getValue() - 1));

        assertThat(page).hasURL(Pattern.compile("allPersons=true"));
        overview.showsPersons(everyActivePerson);
    }

    @Test
    void ensureClosingTheDepartmentPickerWithoutConfirmingKeepsTheSelection(Page page) {

        // the test class shares one database - own persons and departments keep this test independent of the other
        final Person paula = createPerson("Paula", "Personal", List.of(USER, OFFICE));
        final Person erik = createPerson("Erik", "Einkauf", List.of(USER));
        createDepartment("Einkauf", List.of(erik));
        createDepartment("Personal", List.of(paula));

        login(page, paula);

        final AbsenceOverviewPage overview = new AbsenceOverviewPage(page);
        overview.navigate(port);
        overview.showsPersons("Paula");

        overview.openDepartmentPicker();
        overview.checkDepartment("Einkauf");
        page.keyboard().press("Escape");

        assertThat(overview.getDepartmentPickerPopoverLocator()).isHidden();
        assertThat(page).not().hasURL(Pattern.compile("department="));
        overview.showsPersons("Paula");

        overview.openDepartmentPicker();
        assertThat(overview.getDepartmentPickerPopoverLocator()
            .getByRole(AriaRole.CHECKBOX, new Locator.GetByRoleOptions().setName("Einkauf"))).not().isChecked();
    }

    private void login(Page page, Person person) {
        final LoginPage loginPage = new LoginPage(page, port);
        loginPage.login(new LoginPage.Credentials(person.getEmail(), person.getEmail()));
    }

    private Department createDepartment(String name, List<Person> members) {

        final Optional<Department> existingDepartment = departmentService.getAllDepartments().stream()
            .filter(department -> department.getName().equals(name))
            .findFirst();
        if (existingDepartment.isPresent()) {
            return existingDepartment.get();
        }

        final Department department = new Department();
        department.setName(name);
        department.setMembers(members);
        return departmentService.create(department);
    }

    private Person createPerson(String firstName, String lastName, List<Role> roles) {

        final String email = "%s.%s@example.org".formatted(trimAllWhitespace(firstName), trimAllWhitespace(lastName)).toLowerCase();
        final Optional<Person> personByMailAddress = personService.getPersonByMailAddress(email);
        if (personByMailAddress.isPresent()) {
            return personByMailAddress.get();
        }

        final String userId = keycloak.createUser(email, firstName, lastName, email, email);
        final Person savedPerson = personService.create(userId, firstName, lastName, email, List.of(), roles);

        final LocalDate validFrom = LocalDate.of(2022, JANUARY, 1);
        final List<Integer> workingDays = Stream.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY).map(DayOfWeek::getValue).toList();
        workingTimeWriteService.touch(workingDays, validFrom, savedPerson);

        final LocalDate firstDayOfYear = LocalDate.of(2022, JANUARY, 1);
        final LocalDate lastDayOfYear = LocalDate.of(2022, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2022, APRIL, 1);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear, lastDayOfYear, true, expiryDate, TEN, TEN, TEN, ZERO, null);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear.plusYears(1), lastDayOfYear.plusYears(1), true, expiryDate.plusYears(1), TEN, TEN, TEN, ZERO, null);

        return savedPerson;
    }
}
