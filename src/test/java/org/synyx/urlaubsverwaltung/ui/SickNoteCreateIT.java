package org.synyx.urlaubsverwaltung.ui;

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
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNoteDetailPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNotePage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

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
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.ui.PageConditions.pageIsVisible;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING;
import static org.testcontainers.containers.MariaDBContainer.NAME;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = SickNoteCreateIT.Initializer.class)
class SickNoteCreateIT {

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
    private WorkingTimeWriteService workingTimeWriteService;

    @Test
    void ensureSickNoteCreation() {
        final Person person = createPerson();

        final RemoteWebDriver webDriver = browserContainer.getWebDriver();
        final WebDriverWait wait = new WebDriverWait(webDriver, 20);

        final LoginPage loginPage = new LoginPage(webDriver);
        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final SickNotePage sickNotePage = new SickNotePage(webDriver);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(webDriver);

        webDriver.get("http://host.testcontainers.internal:" + port);

        wait.until(pageIsVisible(loginPage));
        loginPage.login(new LoginPage.Credentials(person.getUsername(), "secret"));

        wait.until(pageIsVisible(navigationPage));

        assertThat(navigationPage.quickAdd.hasPopup()).isTrue();
        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        wait.until(pageIsVisible(sickNotePage));

        assertThat(sickNotePage.personSelected("Alfred Pennyworth")).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        final int currentYear = LocalDate.now().getYear();

        sickNotePage.startDate(LocalDate.of(currentYear, FEBRUARY, 23));
        assertThat(sickNotePage.showsFromDate(LocalDate.of(currentYear, FEBRUARY, 23))).isTrue();

        sickNotePage.submit();

        wait.until(pageIsVisible(sickNoteDetailPage));
        assertThat(sickNoteDetailPage.showsSickNoteForPerson("Alfred Pennyworth")).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2021, FEBRUARY, 23))).isTrue();

        navigationPage.logout();
        wait.until(pageIsVisible(loginPage));
    }

    private Person createPerson() {
        final Person person = new Person("pennyworth", "Pennyworth", "Alfred", "alfred.pennyworth@example.org");
        person.setPassword("2f09520efd37e0add52eb78b19195ff9a07c07acbcfc9b61349be76da7a1bccfc60c9b80218d31ec");
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

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            applicationContext.addApplicationListener((ApplicationListener<WebServerInitializedEvent>) event ->
                org.testcontainers.Testcontainers.exposeHostPorts(event.getWebServer().getPort()));
        }
    }
}
