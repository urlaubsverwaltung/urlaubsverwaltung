package org.synyx.urlaubsverwaltung.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.ui.pages.ApplicationDetailPage;
import org.synyx.urlaubsverwaltung.ui.pages.ApplicationPage;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.OverviewPage;
import org.synyx.urlaubsverwaltung.ui.pages.SettingsPage;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.now;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.synyx.urlaubsverwaltung.period.WeekDay.FRIDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.MONDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.SATURDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.SUNDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.THURSDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.TUESDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.WEDNESDAY;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.ui.PageConditions.isTrue;
import static org.synyx.urlaubsverwaltung.ui.PageConditions.pageIsVisible;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING;
import static org.testcontainers.containers.MariaDBContainer.NAME;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = ApplicationForLeaveCreateIT.Initializer.class)
class ApplicationForLeaveCreateIT {

    @LocalServerPort
    private int port;

    @Container
    private final BrowserWebDriverContainer<?> browserContainer = new BrowserWebDriverContainer<>()
        .withRecordingMode(RECORD_FAILING, new File("target"))
        .withCapabilities(new FirefoxOptions());

    static final MariaDBContainer<?> mariaDB = new MariaDBContainer<>(NAME + ":10.5");

    @DynamicPropertySource
    static void mariaDBProperties(DynamicPropertyRegistry registry) {
        mariaDB.start();
        registry.add("spring.datasource.url", mariaDB::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDB::getUsername);
        registry.add("spring.datasource.password", mariaDB::getPassword);
    }

    @Autowired
    private PersonService personService;
    @Autowired
    private AccountInteractionService accountInteractionService;
    @Autowired
    private WorkingTimeService workingTimeService;
    @Autowired
    private PublicHolidaysService publicHolidaysService;
    @Autowired
    private SettingsService settingsService;

    @Test
    @DisplayName("when USER is logged in and overtime feature is disabled then quick-add directly links to application-for-leave")
    void ensureQuickAddDirectlyLinksToApplicationForLeave() {
        final Person officePerson = createPerson("Alfred", "Pennyworth", List.of(USER, OFFICE));
        final Person userPerson = createPerson("The", "Joker", List.of(USER));

        final RemoteWebDriver webDriver = browserContainer.getWebDriver();
        final WebDriverWait wait = new WebDriverWait(webDriver, 20);

        final LoginPage loginPage = new LoginPage(webDriver);
        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final OverviewPage overviewPage = new OverviewPage(webDriver);
        final SettingsPage settingsPage = new SettingsPage(webDriver);
        final ApplicationPage applicationPage = new ApplicationPage(webDriver);

        webDriver.get("http://host.testcontainers.internal:" + port);

        // log in as office user
        // and disable the overtime feature

        wait.until(pageIsVisible(loginPage));
        loginPage.login(new LoginPage.Credentials(officePerson.getUsername(), "secret"));

        wait.until(pageIsVisible(navigationPage));
        wait.until(pageIsVisible(overviewPage));

        navigationPage.clickSettings();
        wait.until(pageIsVisible(settingsPage));

        settingsPage.clickWorkingTimeTab();
        assertThat(settingsPage.overtimeEnabled()).isFalse();

        navigationPage.logout();
        wait.until(pageIsVisible(loginPage));

        // now the quick-add button should link directly to application-for-leave page
        // for the user logged in with role=USER

        loginPage.login(new LoginPage.Credentials(userPerson.getUsername(), "secret"));

        wait.until(pageIsVisible(overviewPage));

        assertThat(navigationPage.quickAdd.hasPopup()).isFalse();

        navigationPage.quickAdd.click();
        wait.until(pageIsVisible(applicationPage));
    }

    @Test
    @DisplayName("when USER is logged in and overtime feature is enabled then quick-add opens a popupmenu")
    void ensureQuickAddOpensPopupMenu() {
        final Person officePerson = createPerson("Alfred", "Pennyworth", List.of(USER, OFFICE));
        final Person userPerson = createPerson("The", "Joker", List.of(USER));

        final RemoteWebDriver webDriver = browserContainer.getWebDriver();
        final WebDriverWait wait = new WebDriverWait(webDriver, 20);

        final LoginPage loginPage = new LoginPage(webDriver);
        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final OverviewPage overviewPage = new OverviewPage(webDriver);
        final SettingsPage settingsPage = new SettingsPage(webDriver);
        final ApplicationPage applicationPage = new ApplicationPage(webDriver);

        webDriver.get("http://host.testcontainers.internal:" + port);

        wait.until(pageIsVisible(loginPage));
        loginPage.login(new LoginPage.Credentials(officePerson.getUsername(), "secret"));

        wait.until(pageIsVisible(navigationPage));

        navigationPage.clickSettings();
        wait.until(pageIsVisible(settingsPage));

        settingsPage.clickWorkingTimeTab();
        settingsPage.enableOvertime();
        settingsPage.saveSettings();

        navigationPage.logout();
        wait.until(pageIsVisible(loginPage));

        loginPage.login(new LoginPage.Credentials(userPerson.getUsername(), "secret"));

        wait.until(pageIsVisible(overviewPage));
        wait.until(isTrue(navigationPage.quickAdd::hasPopup));

        // clicking the element should open the popup menu
        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newApplication();

        wait.until(pageIsVisible(applicationPage));

        navigationPage.logout();
        wait.until(pageIsVisible(loginPage));
    }

    @Test
    void checkIfItIsPossibleToRequestAnApplicationForLeave() {
        final Person officePerson = createPerson("Alfred", "Pennyworth", List.of(USER, OFFICE));

        final RemoteWebDriver webDriver = browserContainer.getWebDriver();
        final WebDriverWait wait = new WebDriverWait(webDriver, 20);

        final LoginPage loginPage = new LoginPage(webDriver);
        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final OverviewPage overviewPage = new OverviewPage(webDriver);
        final ApplicationPage applicationPage = new ApplicationPage(webDriver);
        final ApplicationDetailPage applicationDetailPage = new ApplicationDetailPage(webDriver);

        webDriver.get("http://host.testcontainers.internal:" + port);

        wait.until(pageIsVisible(loginPage));
        loginPage.login(new LoginPage.Credentials(officePerson.getUsername(), "secret"));

        wait.until(pageIsVisible(navigationPage));
        wait.until(pageIsVisible(overviewPage));
        assertThat(overviewPage.isVisibleForPerson("Alfred Pennyworth")).isTrue();

        assertThat(navigationPage.quickAdd.hasPopup()).isTrue();
        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newApplication();
        wait.until(pageIsVisible(applicationPage));

        applicationPage.from(getNextWorkday());
        applicationPage.submit();

        wait.until(pageIsVisible(applicationDetailPage));
        wait.until(isTrue(applicationDetailPage::showsApplicationCreatedInfo));
        assertThat(applicationDetailPage.isVisibleForPerson("Alfred Pennyworth")).isTrue();

        // application created info vanishes sometime
        wait.until(not(isTrue(applicationDetailPage::showsApplicationCreatedInfo)));

        navigationPage.logout();
        wait.until(pageIsVisible(loginPage));
    }

    private Person createPerson(String firstName, String lastName, List<Role> roles) {
        final String username = lastName + UUID.randomUUID().getLeastSignificantBits();
        final String email = String.format("%s.%s@example.org", firstName, lastName);
        final Person person = new Person(username, lastName, firstName, email);
        person.setPassword("2f09520efd37e0add52eb78b19195ff9a07c07acbcfc9b61349be76da7a1bccfc60c9b80218d31ec");
        person.setPermissions(roles);
        final Person savedPerson = personService.save(person);

        final int currentYear = LocalDate.now().getYear();
        final LocalDate validFrom = LocalDate.of(currentYear - 1, 1, 1);
        final List<Integer> workingDays = List.of(MONDAY.getDayOfWeek(), TUESDAY.getDayOfWeek(), WEDNESDAY.getDayOfWeek(), THURSDAY.getDayOfWeek(), FRIDAY.getDayOfWeek(), SATURDAY.getDayOfWeek(), SUNDAY.getDayOfWeek());
        workingTimeService.touch(workingDays, empty(), validFrom, savedPerson);

        final LocalDate firstDayOfYear = LocalDate.of(currentYear, JANUARY, 1);
        final LocalDate lastDayOfYear = LocalDate.of(currentYear, DECEMBER, 31);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear, lastDayOfYear, TEN, TEN, TEN, ZERO, null);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear.plusYears(1), lastDayOfYear.plusYears(1), TEN, TEN, TEN, ZERO, null);


        return savedPerson;
    }

    private LocalDate getNextWorkday() {

        final FederalState federalState = settingsService.getSettings().getWorkingTimeSettings().getFederalState();

        LocalDate nextWorkDay = now();
        while (DayLength.ZERO.compareTo(publicHolidaysService.getAbsenceTypeOfDate(nextWorkDay, federalState)) != 0) {
            nextWorkDay = nextWorkDay.plusDays(1);
        }
        return nextWorkDay;
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            applicationContext.addApplicationListener((ApplicationListener<WebServerInitializedEvent>) event ->
                org.testcontainers.Testcontainers.exposeHostPorts(event.getWebServer().getPort()));
        }
    }
}
