package org.synyx.urlaubsverwaltung.ui;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.ColorScheme;
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
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.ui.extension.UiIntegrationTest;
import org.synyx.urlaubsverwaltung.ui.extension.UiTest;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.ThemeColors;
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
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Testcontainers(parallel = true)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@UiIntegrationTest
@UiTest
class NavigationUIIT {

    // narrower than the `desktop` breakpoint (1280px) so the mobile hamburger menu / drawer is used
    private static final int MOBILE_VIEWPORT_WIDTH = 390;
    private static final int MOBILE_VIEWPORT_HEIGHT = 844;
    // wide enough for the `desktop` breakpoint (1280px), where the navigation gets its resize handle
    private static final int DESKTOP_VIEWPORT_WIDTH = 1440;
    private static final int DESKTOP_VIEWPORT_HEIGHT = 900;

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

    @TestConfiguration
    static class TestSessionConfig {
        @Bean
        public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> disableCleanupCustomizer() {
            // Force-set the cleanup cron to the official disabled macro programmatically
            return sessionRepository -> sessionRepository.setCleanupCron(Scheduled.CRON_DISABLED);
        }
    }

    @Test
    void ensureMobileNavigationDrawerOpensFullyAndShowsLinks(Page page) {

        final Person person = createPerson("mNav", "Marlene", List.of(USER));

        page.setViewportSize(MOBILE_VIEWPORT_WIDTH, MOBILE_VIEWPORT_HEIGHT);

        final LoginPage loginPage = new LoginPage(page, port);
        final NavigationPage navigationPage = new NavigationPage(page);

        loginPage.login(new LoginPage.Credentials(person.getEmail(), person.getEmail()));

        navigationPage.openMobileMenu();

        assertThat(navigationPage.mobileMenu()).isVisible();
        assertThat(navigationPage.overviewLink()).isVisible();

        // regression guard: the drawer previously collapsed to a small anchor-sized box (a few dozen px)
        // instead of covering the viewport, on browsers where the anchor-positioning-based sizing broke.
        final BoundingBox box = navigationPage.mobileMenu().boundingBox();
        Assertions.assertThat(box).isNotNull();
        Assertions.assertThat(box.width).isGreaterThan(MOBILE_VIEWPORT_WIDTH * 0.9);
        Assertions.assertThat(box.height).isGreaterThan(MOBILE_VIEWPORT_HEIGHT * 0.5);
    }

    @Test
    void ensureResizeHandleUsesTheDarkLineColorInDarkMode(Page page) {

        final Person person = createPerson("dNav", "Dagmar", List.of(USER));

        page.setViewportSize(DESKTOP_VIEWPORT_WIDTH, DESKTOP_VIEWPORT_HEIGHT);
        page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(ColorScheme.DARK));

        final LoginPage loginPage = new LoginPage(page, port);
        final NavigationPage navigationPage = new NavigationPage(page);

        loginPage.login(new LoginPage.Credentials(person.getEmail(), person.getEmail()));

        assertThat(page.locator("html")).hasClass(Pattern.compile("\\btheme-dark\\b"));
        assertThat(navigationPage.resizeHandle()).isAttached();
        // the light line colour (blue-300) glares on the dark navigation
        Assertions.assertThat(navigationPage.resizeHandleLineColor()).isEqualTo(ThemeColors.colorOf(page, "--color-sky-600"));
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
        final List<Integer> workingDays = Stream.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY).map(DayOfWeek::getValue).toList();
        workingTimeWriteService.touch(workingDays, firstDayOfYear, savedPerson);

        final LocalDate lastDayOfYear = firstDayOfYear.withMonth(DECEMBER.getValue()).withDayOfMonth(31);
        final LocalDate expiryDate = LocalDate.of(currentYear.getValue(), APRIL, 1);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear, lastDayOfYear, true, expiryDate, TEN, TEN, TEN, ZERO, null);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear.plusYears(1), lastDayOfYear.plusYears(1), true, expiryDate.plusYears(1), TEN, TEN, TEN, ZERO, null);

        return savedPerson;
    }
}
