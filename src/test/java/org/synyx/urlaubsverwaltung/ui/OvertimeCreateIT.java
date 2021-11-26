package org.synyx.urlaubsverwaltung.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.OvertimeDetailPage;
import org.synyx.urlaubsverwaltung.ui.pages.OvertimePage;
import org.synyx.urlaubsverwaltung.ui.pages.SettingsPage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.util.Locale.GERMAN;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.ui.PageConditions.isTrue;
import static org.synyx.urlaubsverwaltung.ui.PageConditions.pageIsVisible;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = UITestInitializer.class)
class OvertimeCreateIT {

    @LocalServerPort
    private int port;

    @Container
    private final BrowserWebDriverContainer<?> browserContainer = new BrowserWebDriverContainer<>()
        .withRecordingMode(RECORD_FAILING, new File("target"))
        .withCapabilities(chromeOptions());

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

        final RemoteWebDriver webDriver = browserContainer.getWebDriver();
        final WebDriverWait wait = new WebDriverWait(webDriver, 20);

        final LoginPage loginPage = new LoginPage(webDriver, messageSource, GERMAN);
        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final SettingsPage settingsPage = new SettingsPage(webDriver);
        final OvertimePage overtimePage = new OvertimePage(webDriver);
        final OvertimeDetailPage overtimeDetailPage = new OvertimeDetailPage(webDriver);

        webDriver.get("http://host.testcontainers.internal:" + port);

        wait.until(pageIsVisible(loginPage));
        loginPage.login(new LoginPage.Credentials(person.getUsername(), "secret"));

        wait.until(pageIsVisible(navigationPage));

        navigationPage.clickSettings();
        wait.until(pageIsVisible(settingsPage));

        settingsPage.clickWorkingTimeTab();
        assertThat(settingsPage.overtimeEnabled()).isFalse();

        settingsPage.enableOvertime();
        settingsPage.saveSettings();

        assertThat(navigationPage.quickAdd.hasPopup()).isTrue();
        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newOvertime();
        wait.until(pageIsVisible(overtimePage));

        final int currentYear = LocalDate.now().getYear();

        overtimePage.startDate(LocalDate.of(currentYear, FEBRUARY, 23));
        assertThat(overtimePage.showsEndDate(LocalDate.of(currentYear, FEBRUARY, 23))).isTrue();

        overtimePage.hours(1);
        overtimePage.minutes(90);

        overtimePage.submit();

        wait.until(pageIsVisible(overtimeDetailPage));
        wait.until(isTrue(overtimeDetailPage::showsOvertimeCreatedInfo));

        // overtime created info vanishes sometime
        wait.until(not(isTrue(overtimeDetailPage::showsOvertimeCreatedInfo)));

        assertThat(overtimeDetailPage.isVisibleForPerson(person.getNiceName())).isTrue();
        assertThat(overtimeDetailPage.showsHours(2)).isTrue();
        assertThat(overtimeDetailPage.showsMinutes(30)).isTrue();

        navigationPage.logout();
        wait.until(pageIsVisible(loginPage));
    }

    private Person createPerson() {
        final Person person = new Person("dBradley", "Bradley", "Donald", "Donald.Bradley@example.org");
        person.setPermissions(List.of(USER, OFFICE));
        final Person savedPerson = personService.save(person);

        final int currentYear = LocalDate.now().getYear();
        final LocalDate validFrom = LocalDate.of(currentYear - 1, 1, 1);
        final List<Integer> workingDays = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY).stream().map(DayOfWeek::getValue).collect(toList());
        workingTimeWriteService.touch(workingDays, validFrom, savedPerson);

        final LocalDate firstDayOfYear = LocalDate.of(currentYear, JANUARY, 1);
        final LocalDate lastDayOfYear = LocalDate.of(currentYear, DECEMBER, 31);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear, lastDayOfYear, TEN, TEN, TEN, ZERO, null);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear.plusYears(1), lastDayOfYear.plusYears(1), TEN, TEN, TEN, ZERO, null);

        return savedPerson;
    }

    private ChromeOptions chromeOptions() {
        final ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", Map.of("intl.accept_languages", "de-DE"));
        return options;
    }
}
