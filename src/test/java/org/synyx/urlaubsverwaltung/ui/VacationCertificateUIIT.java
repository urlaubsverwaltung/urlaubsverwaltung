package org.synyx.urlaubsverwaltung.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.assertj.core.api.Assertions;
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
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.ui.extension.UiIntegrationTest;
import org.synyx.urlaubsverwaltung.ui.extension.UiTest;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.OverviewPage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JUNE;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Testcontainers(parallel = true)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@UiIntegrationTest
@UiTest
class VacationCertificateUIIT {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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
    private ApplicationService applicationService;
    @Autowired
    private VacationTypeService vacationTypeService;

    @TestConfiguration
    static class TestSessionConfig {
        @Bean
        public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> disableCleanupCustomizer() {
            return sessionRepository -> sessionRepository.setCleanupCron(Scheduled.CRON_DISABLED);
        }
    }

    @Test
    void ensureOfficeCanCreateVacationCertificate(Page page) {

        final Year year = Year.now();
        final Person office = createPerson("vcOffice", "Olivia", List.of(USER, OFFICE));
        final Person employee = createPerson("vcEmployee", "Emil", List.of(USER));

        final LocalDate vacationStart = LocalDate.of(year.getValue(), FEBRUARY, 1).with(nextOrSame(MONDAY));
        final LocalDate vacationEnd = vacationStart.plusDays(2);
        createAllowedHoliday(employee, vacationStart, vacationEnd);

        final LoginPage loginPage = new LoginPage(page, port);
        final NavigationPage navigationPage = new NavigationPage(page);
        loginPage.login(new LoginPage.Credentials(office.getEmail(), office.getEmail()));
        page.waitForURL(OverviewPage.URL_PATTERN);

        page.navigate("http://localhost:%d/web/person/%d".formatted(port, employee.getId()));
        page.locator("[data-test-id=vacation-certificate-link]").click();

        final LocalDate employmentEnd = LocalDate.of(year.getValue(), JUNE, 30);
        page.locator("duet-date-picker #employmentTo").fill(DateTimeFormatter.ofPattern("d.M.yyyy").format(employmentEnd));
        page.locator("#compensatedDays").fill("2.5");
        page.locator("#employer").fill("<b>ACME GmbH</b>\nHauptstraße 1");
        page.locator("[data-test-id=vacation-certificate-submit]").click();

        final Locator employer = page.locator("[data-test-id=vacation-certificate-employer]");
        assertThat(employer).containsText("<b>ACME GmbH</b>");
        assertThat(employer.locator("b")).hasCount(0);
        Assertions.assertThat(employer.innerText()).isEqualTo("<b>ACME GmbH</b>\nHauptstraße 1");
        assertThat(page.locator("[data-test-id=vacation-certificate-employee]")).hasText(employee.getNiceName());
        assertThat(page.locator("[data-test-id=vacation-certificate-employment]"))
            .hasText("%s bis %s".formatted(DATE.format(year.atDay(1)), DATE.format(employmentEnd)));
        assertThat(page.locator("[data-test-id=vacation-certificate-entitlement]")).hasText("10 Arbeitstage bei einer 5-Tage-Woche");
        assertThat(page.locator("[data-test-id=vacation-certificate-granted-periods]")).containsText(DATE.format(vacationStart));
        assertThat(page.locator("[data-test-id=vacation-certificate-granted-total]")).hasText("Gewährt insgesamt: 3 Arbeitstage");
        assertThat(page.locator("[data-test-id=vacation-certificate-compensation]")).hasText("Für 2,5 Arbeitstage wurde Urlaubsabgeltung gezahlt.");

        navigationPage.logout();
    }

    private void createAllowedHoliday(Person person, LocalDate from, LocalDate to) {
        final VacationType<?> holiday = vacationTypeService.getActiveVacationTypes().stream()
            .filter(vacationType -> vacationType.getCategory() == HOLIDAY)
            .findFirst()
            .orElseThrow();

        final Application application = new Application();
        application.setPerson(person);
        application.setApplier(person);
        application.setVacationType(holiday);
        application.setStartDate(from);
        application.setEndDate(to);
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);
        application.setApplicationDate(from.minusWeeks(2));
        applicationService.save(application);
    }

    private Person createPerson(String firstName, String lastName, List<Role> roles) {

        final String email = "%s.%s@example.org".formatted(firstName, lastName).toLowerCase();
        final Optional<Person> personByMailAddress = personService.getPersonByMailAddress(email);
        if (personByMailAddress.isPresent()) {
            return personByMailAddress.get();
        }

        final String userId = keycloak.createUser(email, firstName, lastName, email, email);
        final Person savedPerson = personService.create(userId, firstName, lastName, email, List.of(), roles);

        final Year currentYear = Year.now();
        final LocalDate firstDayOfYear = currentYear.atDay(1);
        final List<Integer> workingDays = Stream.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY).map(DayOfWeek::getValue).toList();
        workingTimeWriteService.touch(workingDays, firstDayOfYear, savedPerson);

        final LocalDate lastDayOfYear = firstDayOfYear.withMonth(DECEMBER.getValue()).withDayOfMonth(31);
        final LocalDate expiryDate = LocalDate.of(currentYear.getValue(), APRIL, 1);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear, lastDayOfYear, true, expiryDate, TEN, TEN, ZERO, ZERO, null);

        return savedPerson;
    }
}
