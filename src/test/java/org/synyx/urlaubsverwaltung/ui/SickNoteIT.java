package org.synyx.urlaubsverwaltung.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.synyx.urlaubsverwaltung.TestPostgreContainer;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNoteDetailPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNoteOverviewPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNotePage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
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
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.util.Locale.GERMAN;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.ui.PageConditions.pageIsVisible;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.main.allow-bean-definition-overriding=true"})
@ContextConfiguration(initializers = UITestInitializer.class)
class SickNoteIT {

    @TestConfiguration
    static class Configuration {
        @Bean
        @Primary
        public Clock clock() {
            // use a fixed clock to avoid weekends or public holidays while creating sick notes
            return Clock.fixed(Instant.parse("2022-02-01T00:00:00.00Z"), ZoneId.systemDefault());
        }
    }

    @LocalServerPort
    private int port;

    @Container
    private final BrowserWebDriverContainer<?> browserContainer = new BrowserWebDriverContainer<>()
        .withRecordingMode(RECORD_FAILING, new File("target"))
        .withCapabilities(chromeOptions());

    static final TestPostgreContainer postgre = new TestPostgreContainer();

    @DynamicPropertySource
    static void postgreProperties(DynamicPropertyRegistry registry) {
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
    void ensureSickNote() {
        final Person person = createPerson();

        final RemoteWebDriver webDriver = browserContainer.getWebDriver();
        final WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(20));

        final LoginPage loginPage = new LoginPage(webDriver, messageSource, GERMAN);
        final NavigationPage navigationPage = new NavigationPage(webDriver);

        webDriver.get("http://host.testcontainers.internal:" + port);

        wait.until(pageIsVisible(loginPage));
        loginPage.login(new LoginPage.Credentials(person.getUsername(), "secret"));

        wait.until(pageIsVisible(navigationPage));
        assertThat(navigationPage.quickAdd.hasPopup()).isTrue();

        sickNote(webDriver, person);
        sickNoteWithIncapacityCertificate(webDriver, person);
        childSickNote(webDriver, person);
        childSickNoteWithIncapacityCertificate(webDriver, person);

        sickNoteStatisticListView(webDriver, person);

        navigationPage.logout();
        wait.until(pageIsVisible(loginPage));
    }

    private void sickNote(RemoteWebDriver webDriver, Person person) {
        final WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(20));

        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final SickNotePage sickNotePage = new SickNotePage(webDriver);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(webDriver, messageSource, GERMAN);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        wait.until(pageIsVisible(sickNotePage));

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        sickNotePage.startDate(LocalDate.of(2022, FEBRUARY, 23));
        assertThat(sickNotePage.showsToDate(LocalDate.of(2022, FEBRUARY, 23))).isTrue();

        sickNotePage.submit();

        wait.until(pageIsVisible(sickNoteDetailPage));
        assertThat(sickNoteDetailPage.showsSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2022, FEBRUARY, 23))).isTrue();
        assertThat(sickNoteDetailPage.showsNoIncapacityCertificate()).isTrue();
    }

    private void sickNoteWithIncapacityCertificate(RemoteWebDriver webDriver, Person person) {
        final WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(20));

        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final SickNotePage sickNotePage = new SickNotePage(webDriver);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(webDriver, messageSource, GERMAN);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        wait.until(pageIsVisible(sickNotePage));

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        sickNotePage.startDate(LocalDate.of(2022, MARCH, 10));
        sickNotePage.toDate(LocalDate.of(2022, MARCH, 11));

        sickNotePage.aubStartDate(LocalDate.of(2022, MARCH, 11));
        assertThat(sickNotePage.showsAubToDate(LocalDate.of(2022, MARCH, 11))).isTrue();

        sickNotePage.submit();

        wait.until(pageIsVisible(sickNoteDetailPage));
        assertThat(sickNoteDetailPage.showsSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2022, MARCH, 10))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateTo(LocalDate.of(2022, MARCH, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteAubDateFrom(LocalDate.of(2022, MARCH, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteAubDateTo(LocalDate.of(2022, MARCH, 11))).isTrue();
    }

    private void childSickNote(RemoteWebDriver webDriver, Person person) {
        final WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(20));

        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final SickNotePage sickNotePage = new SickNotePage(webDriver);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(webDriver, messageSource, GERMAN);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        wait.until(pageIsVisible(sickNotePage));

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        sickNotePage.selectTypeChildSickNote();
        sickNotePage.startDate(LocalDate.of(2022, APRIL, 11));
        sickNotePage.toDate(LocalDate.of(2022, APRIL, 12));

        sickNotePage.submit();

        wait.until(pageIsVisible(sickNoteDetailPage));
        assertThat(sickNoteDetailPage.showsChildSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2022, APRIL, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateTo(LocalDate.of(2022, APRIL, 12))).isTrue();
        assertThat(sickNoteDetailPage.showsNoIncapacityCertificate()).isTrue();
    }

    private void childSickNoteWithIncapacityCertificate(RemoteWebDriver webDriver, Person person) {
        final WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(20));

        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final SickNotePage sickNotePage = new SickNotePage(webDriver);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(webDriver, messageSource, GERMAN);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        wait.until(pageIsVisible(sickNotePage));

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        sickNotePage.selectTypeChildSickNote();
        sickNotePage.startDate(LocalDate.of(2022, MAY, 10));
        sickNotePage.toDate(LocalDate.of(2022, MAY, 11));

        sickNotePage.aubStartDate(LocalDate.of(2022, MAY, 11));
        assertThat(sickNotePage.showsAubToDate(LocalDate.of(2022, MAY, 11))).isTrue();

        sickNotePage.submit();

        wait.until(pageIsVisible(sickNoteDetailPage));
        assertThat(sickNoteDetailPage.showsChildSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2022, MAY, 10))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateTo(LocalDate.of(2022, MAY, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteAubDateFrom(LocalDate.of(2022, MAY, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteAubDateTo(LocalDate.of(2022, MAY, 11))).isTrue();
    }

    private void sickNoteStatisticListView(RemoteWebDriver webDriver, Person person) {
        final WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(20));

        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final SickNoteOverviewPage sickNoteOverviewPage = new SickNoteOverviewPage(webDriver, messageSource, GERMAN);

        navigationPage.clickSickNotes();
        wait.until(pageIsVisible(sickNoteOverviewPage));

        assertThat(sickNoteOverviewPage.showsSickNoteStatistic(person.getFirstName(), person.getLastName(), 3, 1)).isTrue();
        assertThat(sickNoteOverviewPage.showsChildSickNoteStatistic(person.getFirstName(), person.getLastName(), 4, 1)).isTrue();
    }

    private Person createPerson() {
        final Person savedPerson = personService.create("pennyworth", "Alfred", "Pennyworth", "alfred.pennyworth@example.org", List.of(), List.of(USER, OFFICE));

        final LocalDate validFrom = LocalDate.of(2022, 1, 1);
        final List<Integer> workingDays = Stream.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY).map(DayOfWeek::getValue).collect(toList());
        workingTimeWriteService.touch(workingDays, validFrom, savedPerson);

        final LocalDate firstDayOfYear = LocalDate.of(2022, JANUARY, 1);
        final LocalDate lastDayOfYear = LocalDate.of(2022, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2022, APRIL, 1);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear, lastDayOfYear, true, expiryDate, TEN, TEN, TEN, ZERO, null);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear.plusYears(1), lastDayOfYear.plusYears(1), true, expiryDate.plusYears(1), TEN, TEN, TEN, ZERO, null);


        return savedPerson;
    }

    private ChromeOptions chromeOptions() {
        final ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", Map.of("intl.accept_languages", "de-DE"));
        options.addArguments("--disable-dev-shm-usage");
        return options;
    }
}
