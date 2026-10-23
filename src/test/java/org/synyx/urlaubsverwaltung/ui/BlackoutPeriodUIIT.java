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
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.ui.extension.UiIntegrationTest;
import org.synyx.urlaubsverwaltung.ui.extension.UiTest;
import org.synyx.urlaubsverwaltung.ui.pages.BlackoutPeriodFormPage;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.OverviewPage;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.time.Month.DECEMBER;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Testcontainers(parallel = true)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@UiIntegrationTest
@UiTest
class BlackoutPeriodUIIT {

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
    private ApplicationService applicationService;
    @Autowired
    private VacationTypeService vacationTypeService;

    @TestConfiguration
    static class TestSessionConfig {
        @Bean
        public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> disableCleanupCustomizer() {
            // Force-set the cleanup cron to the official disabled macro programmatically
            return sessionRepository -> sessionRepository.setCleanupCron(Scheduled.CRON_DISABLED);
        }
    }

    @Test
    void ensureImpactListsTheAffectedPersonsOfTheChosenDepartments(Page page) {

        final String email = "blackout.office@example.org";
        final String userId = keycloak.createUser(email, "Olga", "Office", email, email);
        final Person office = personService.create(userId, "Olga", "Office", email, List.of(), List.of(USER, OFFICE));

        final Person ada = personService.create("ada", "Ada", "Sperrfeld", "ada.sperrfeld@example.org", List.of(), List.of(USER));
        final Person ben = personService.create("ben", "Ben", "Sperrfeld", "ben.sperrfeld@example.org", List.of(), List.of(USER));
        final Person cleo = personService.create("cleo", "Cleo", "Sperrfeld", "cleo.sperrfeld@example.org", List.of(), List.of(USER));
        createDepartment("Sperrfeld Entwicklung", List.of(ada, ben));
        createDepartment("Sperrfeld Support", List.of(ben, cleo));
        createWaitingHoliday(cleo, LocalDate.of(2030, DECEMBER, 23));

        final LoginPage loginPage = new LoginPage(page, port);
        final BlackoutPeriodFormPage formPage = new BlackoutPeriodFormPage(page);

        loginPage.login(new LoginPage.Credentials(office.getEmail(), office.getEmail()));
        page.waitForURL(OverviewPage.URL_PATTERN);
        formPage.open(port);
        assertThat(page).hasTitle(Pattern.compile("^Neue Urlaubssperre erstellen"));

        // company-wide by default
        assertThat(formPage.departmentSelection()).isHidden();
        assertThat(formPage.impactSentence()).containsText("im Unternehmen");
        assertThat(formPage.impactSentence()).containsText("Abwesenheiten jeder Art");

        formPage.chooseSelectedDepartments();
        assertThat(formPage.departmentSelection()).isVisible();
        assertThat(formPage.impactSentence()).hasText("Wähle mindestens eine Abteilung und eine Abwesenheitsart, um die Auswirkung zu sehen.");

        formPage.checkDepartment("Sperrfeld Entwicklung");
        formPage.searchDepartment("support");
        formPage.checkDepartment("Sperrfeld Support");

        // Ben is a member of both departments and is listed once
        assertThat(formPage.departmentChips()).hasCount(2);
        assertThat(formPage.affectedPersons()).containsText(new String[]{"Ada Sperrfeld", "Ben Sperrfeld", "Cleo Sperrfeld"});
        assertThat(formPage.affectedPersonsHeading()).hasText("Betroffene Personen (3)");
        assertThat(formPage.impactSentence()).containsText("3 Personen aus Sperrfeld Entwicklung und Sperrfeld Support können");

        formPage.removeDepartmentChip("Sperrfeld Entwicklung");
        assertThat(formPage.affectedPersons()).containsText(new String[]{"Ben Sperrfeld", "Cleo Sperrfeld"});

        formPage.chooseSelectedVacationTypes();
        formPage.checkVacationType("Erholungsurlaub");
        assertThat(formPage.impactSentence()).hasText(
            "2 Personen aus Sperrfeld Support können im gewählten Zeitraum „Erholungsurlaub“ nicht selbst beantragen.");

        // the colliding application of Cleo is shown as soon as the period is complete
        formPage.startDate("20.12.2030");
        formPage.endDate("31.12.2030");
        assertThat(formPage.impactSentence()).containsText("vom 20.12.2030 bis 31.12.2030");
        assertThat(formPage.conflicts()).containsText("Cleo Sperrfeld: 23.12.30");

        page.navigate("http://localhost:%d/web/blackoutperiod".formatted(port));
        assertThat(page).hasTitle(Pattern.compile("^Urlaubssperren"));
    }

    private void createWaitingHoliday(Person person, LocalDate date) {
        final Application application = new Application();
        application.setPerson(person);
        application.setApplier(person);
        application.setStartDate(date);
        application.setEndDate(date);
        application.setDayLength(FULL);
        application.setStatus(WAITING);
        application.setApplicationDate(date.minusMonths(1));
        application.setVacationType(vacationTypeService.getActiveVacationTypes().stream()
            .filter(vacationType -> vacationType.getCategory() == HOLIDAY)
            .findFirst()
            .orElseThrow());
        applicationService.save(application);
    }

    private void createDepartment(String name, List<Person> members) {
        final Department department = new Department();
        department.setName(name);
        department.setMembers(members);
        departmentService.create(department);
    }
}
