package org.synyx.urlaubsverwaltung.ui;

import com.microsoft.playwright.Page;
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
import org.synyx.urlaubsverwaltung.ui.pages.ApplicationStatisticsPage;
import org.synyx.urlaubsverwaltung.ui.pages.DepartmentPickerPage;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Month.APRIL;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Testcontainers(parallel = true)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@UiIntegrationTest
@UiTest
class ApplicationStatisticsUIIT {

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
    private DepartmentService departmentService;
    @Autowired
    private AccountInteractionService accountInteractionService;
    @Autowired
    private WorkingTimeWriteService workingTimeWriteService;

    @TestConfiguration
    static class TestSessionConfig {
        @Bean
        public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> disableCleanupCustomizer() {
            return sessionRepository -> sessionRepository.setCleanupCron(Scheduled.CRON_DISABLED);
        }
    }

    @Test
    void ensureDepartmentPickerFiltersTheStatisticsOfTheDepartmentHead(Page page) {

        final Person dora = createPerson("Dora", "Head", List.of(USER, DEPARTMENT_HEAD));
        final Person emma = createPerson("Emma", "Einkauf", List.of(USER));
        final Person erik = createPerson("Erik", "Einkauf", List.of(USER));
        final Person lars = createPerson("Lars", "Lager", List.of(USER));
        final Person vera = createPerson("Vera", "Vertrieb", List.of(USER));
        createDepartment("Einkauf", List.of(emma, erik), List.of(dora));
        createDepartment("Lager", List.of(lars), List.of(dora));
        // dora is a plain member here and not responsible for vera
        createDepartment("Vertrieb", List.of(dora, vera), List.of());

        login(page, dora);

        final ApplicationStatisticsPage statistics = new ApplicationStatisticsPage(page);
        statistics.navigate(port);

        final DepartmentPickerPage departmentPicker = statistics.getDepartmentPicker();

        // without a selection everyone the department head is responsible for is shown
        assertThat(departmentPicker.getButtonLocator()).containsText("Alle Personen");
        statistics.showsPersons("Emma Einkauf", "Erik Einkauf", "Lars Lager");

        departmentPicker.open();
        assertThat(departmentPicker.getDepartmentCheckboxLocator("Vertrieb")).hasCount(0);
        departmentPicker.checkDepartment("Einkauf");
        departmentPicker.confirm();

        assertThat(page).hasURL(Pattern.compile("department=Einkauf"));
        assertThat(departmentPicker.getButtonLocator()).containsText("Einkauf");
        statistics.showsPersons("Emma Einkauf", "Erik Einkauf");
        assertThat(page.locator("#statistics-csv-download-link-all")).hasAttribute("href", Pattern.compile("department=Einkauf"));

        // the search keeps the selected department
        statistics.search("Erik");
        statistics.showsPersons("Erik Einkauf");
        assertThat(page).hasURL(Pattern.compile("department=Einkauf"));
    }

    private void login(Page page, Person person) {
        final LoginPage loginPage = new LoginPage(page, port);
        loginPage.login(new LoginPage.Credentials(person.getEmail(), person.getEmail()));
    }

    private void createDepartment(String name, List<Person> members, List<Person> departmentHeads) {
        final Department department = new Department();
        department.setName(name);
        department.setMembers(members);
        department.setDepartmentHeads(departmentHeads);
        departmentService.create(department);
    }

    private Person createPerson(String firstName, String lastName, List<Role> roles) {
        final String email = "%s.%s@example.org".formatted(firstName, lastName).toLowerCase();
        final String userId = keycloak.createUser(email, firstName, lastName, email, email);
        final Person person = personService.create(userId, firstName, lastName, email, List.of(), roles);

        // persons without a holiday account of the year get no statistics
        final Year year = Year.now();
        workingTimeWriteService.touch(List.of(DayOfWeek.MONDAY.getValue()), year.atDay(1), person);
        final LocalDate lastDayOfYear = year.atMonth(12).atEndOfMonth();
        accountInteractionService.updateOrCreateHolidaysAccount(person, year.atDay(1), lastDayOfYear, true, year.atMonth(APRIL).atDay(1), TEN, TEN, TEN, ZERO, null);

        return person;
    }
}
