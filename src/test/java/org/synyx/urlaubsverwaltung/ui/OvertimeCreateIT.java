package org.synyx.urlaubsverwaltung.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.synyx.urlaubsverwaltung.TestPostgreContainer;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.OvertimeDetailPage;
import org.synyx.urlaubsverwaltung.ui.pages.OvertimePage;
import org.synyx.urlaubsverwaltung.ui.pages.SettingsPage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.util.Locale.GERMAN;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = UITestInitializer.class)
class OvertimeCreateIT {

    @LocalServerPort
    private int port;

    static final TestPostgreContainer postgre = new TestPostgreContainer();

    @DynamicPropertySource
    static void postgreDBProperties(DynamicPropertyRegistry registry) {
        postgre.start();
        postgre.configureSpringDataSource(registry);
    }

    @Autowired
    private PersonService personService;
    @Autowired
    private AccountInteractionService accountInteractionService;
    @Autowired
    private WorkingTimeWriteService workingTimeWriteService;
    @Autowired
    private MessageSource messageSource;

    @Test
    void ensureOvertimeCreation() {
        final Person person = createPerson();

        withPage(page -> {

            final LoginPage loginPage = new LoginPage(page, messageSource, GERMAN);
            final NavigationPage navigationPage = new NavigationPage(page);
            final SettingsPage settingsPage = new SettingsPage(page);
            final OvertimePage overtimePage = new OvertimePage(page);
            final OvertimeDetailPage overtimeDetailPage = new OvertimeDetailPage(page);

            page.navigate("http://localhost:" + port);
            page.waitForURL(url -> url.endsWith("/login"));

            loginPage.login(new LoginPage.Credentials(person.getUsername(), "secret"));

            page.waitForURL(url -> url.endsWith("/web/person/%s/overview".formatted(person.getId())));

            navigationPage.clickSettings();

            settingsPage.clickWorkingTimeTab();
            assertThat(settingsPage.overtimeEnabled()).isFalse();

            settingsPage.enableOvertime();
            settingsPage.saveSettings();

            assertThat(navigationPage.quickAdd.hasPopup()).isTrue();
            navigationPage.quickAdd.click();
            navigationPage.quickAdd.newOvertime();

            final int currentYear = LocalDate.now().getYear();

            overtimePage.startDate(LocalDate.of(currentYear, FEBRUARY, 23));
            page.context().waitForCondition(() -> overtimePage.showsEndDate(LocalDate.of(currentYear, FEBRUARY, 23)));

            overtimePage.hours(1);
            overtimePage.minutes(90);

            overtimePage.submit();

            page.context().waitForCondition(overtimeDetailPage::isVisible);
            page.context().waitForCondition(overtimeDetailPage::showsOvertimeCreatedInfo);

            // overtime created info vanishes sometime
            page.context().waitForCondition(() -> !overtimeDetailPage.showsOvertimeCreatedInfo());

            assertThat(overtimeDetailPage.isVisibleForPerson(person.getNiceName())).isTrue();
            assertThat(overtimeDetailPage.showsHours(2)).isTrue();
            assertThat(overtimeDetailPage.showsMinutes(30)).isTrue();

            navigationPage.logout();
        });


    }

    private Person createPerson() {
        final Person savedPerson = personService.create("dBradley", "Donald", "Bradley", "Donald.Bradley@example.org", List.of(), List.of(USER, OFFICE));

        final int currentYear = LocalDate.now().getYear();
        final LocalDate validFrom = LocalDate.of(currentYear - 1, 1, 1);
        final List<Integer> workingDays = Stream.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY).map(DayOfWeek::getValue).collect(toList());
        workingTimeWriteService.touch(workingDays, validFrom, savedPerson);

        final LocalDate firstDayOfYear = LocalDate.of(currentYear, JANUARY, 1);
        final LocalDate lastDayOfYear = LocalDate.of(currentYear, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(currentYear, APRIL, 1);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear, lastDayOfYear, true, expiryDate, TEN, TEN, TEN, ZERO, null);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear.plusYears(1), lastDayOfYear.plusYears(1), true, expiryDate.plusDays(1), TEN, TEN, TEN, ZERO, null);

        return savedPerson;
    }

    // TODO use junit extension
    private void withPage(Consumer<Page> consumer) {

        try (Playwright playwright = Playwright.create(new Playwright.CreateOptions())) {
            try (Browser browser = playwright.chromium().launch()) {
                try (BrowserContext browserContext = browser.newContext(browserContextOptions())) {

                    final Page page = browserContext.newPage();

                    consumer.accept(page);
                }
            }
        }
    }

    private static Browser.NewContextOptions browserContextOptions() {
        return new Browser.NewContextOptions()
            .setRecordVideoDir(Paths.get("target"))
            .setLocale("de-DE")
            .setScreenSize(1500, 500)
            .setViewportSize(1920, 1080);
    }
}
