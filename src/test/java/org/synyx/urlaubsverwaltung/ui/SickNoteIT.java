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
import org.synyx.urlaubsverwaltung.ui.pages.SickNoteDetailPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNoteOverviewPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNotePage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.containers.BrowserWebDriverContainer;
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
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.ui.PageConditions.pageIsVisible;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = UITestInitializer.class)
class SickNoteIT {

    @LocalServerPort
    private int port;

    @Container
    private final BrowserWebDriverContainer<?> browserContainer = new BrowserWebDriverContainer<>()
        .withRecordingMode(RECORD_FAILING, new File("target"))
        .withCapabilities(new ChromeOptions());

    static final TestMariaDBContainer mariaDB = new TestMariaDBContainer();

    @DynamicPropertySource
    static void mariaDBProperties(DynamicPropertyRegistry registry) {
        mariaDB.start();
        mariaDB.configureSpringDataSource(registry);
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
        final WebDriverWait wait = new WebDriverWait(webDriver, 20);

        final LoginPage loginPage = new LoginPage(webDriver, messageSource, ENGLISH);
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
        final WebDriverWait wait = new WebDriverWait(webDriver, 20);

        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final SickNotePage sickNotePage = new SickNotePage(webDriver);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(webDriver, messageSource, ENGLISH);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        wait.until(pageIsVisible(sickNotePage));

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        final int currentYear = LocalDate.now().getYear();

        sickNotePage.startDate(LocalDate.of(currentYear, FEBRUARY, 23));
        assertThat(sickNotePage.showsToDate(LocalDate.of(currentYear, FEBRUARY, 23))).isTrue();

        sickNotePage.submit();

        wait.until(pageIsVisible(sickNoteDetailPage));
        assertThat(sickNoteDetailPage.showsSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2021, FEBRUARY, 23))).isTrue();
        assertThat(sickNoteDetailPage.showsNoIncapacityCertificate()).isTrue();
    }

    private void sickNoteWithIncapacityCertificate(RemoteWebDriver webDriver, Person person) {
        final WebDriverWait wait = new WebDriverWait(webDriver, 20);

        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final SickNotePage sickNotePage = new SickNotePage(webDriver);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(webDriver, messageSource, ENGLISH);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        wait.until(pageIsVisible(sickNotePage));

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        final int currentYear = LocalDate.now().getYear();

        sickNotePage.startDate(LocalDate.of(currentYear, MARCH, 10));
        sickNotePage.toDate(LocalDate.of(currentYear, MARCH, 11));

        sickNotePage.aubStartDate(LocalDate.of(currentYear, MARCH, 11));
        assertThat(sickNotePage.showsAubToDate(LocalDate.of(currentYear, MARCH, 11))).isTrue();

        sickNotePage.submit();

        wait.until(pageIsVisible(sickNoteDetailPage));
        assertThat(sickNoteDetailPage.showsSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2021, MARCH, 10))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateTo(LocalDate.of(2021, MARCH, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteAubDateFrom(LocalDate.of(2021, MARCH, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteAubDateTo(LocalDate.of(2021, MARCH, 11))).isTrue();
    }

    private void childSickNote(RemoteWebDriver webDriver, Person person) {
        final WebDriverWait wait = new WebDriverWait(webDriver, 20);

        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final SickNotePage sickNotePage = new SickNotePage(webDriver);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(webDriver, messageSource, ENGLISH);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        wait.until(pageIsVisible(sickNotePage));

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        final int currentYear = LocalDate.now().getYear();

        sickNotePage.selectTypeChildSickNote();
        sickNotePage.startDate(LocalDate.of(currentYear, APRIL, 10));
        sickNotePage.toDate(LocalDate.of(currentYear, APRIL, 11));

        sickNotePage.submit();

        wait.until(pageIsVisible(sickNoteDetailPage));
        assertThat(sickNoteDetailPage.showsChildSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2021, APRIL, 10))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateTo(LocalDate.of(2021, APRIL, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsNoIncapacityCertificate()).isTrue();
    }

    private void childSickNoteWithIncapacityCertificate(RemoteWebDriver webDriver, Person person) {
        final WebDriverWait wait = new WebDriverWait(webDriver, 20);

        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final SickNotePage sickNotePage = new SickNotePage(webDriver);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(webDriver, messageSource, ENGLISH);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        wait.until(pageIsVisible(sickNotePage));

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        final int currentYear = LocalDate.now().getYear();

        sickNotePage.selectTypeChildSickNote();
        sickNotePage.startDate(LocalDate.of(currentYear, MAY, 10));
        sickNotePage.toDate(LocalDate.of(currentYear, MAY, 11));

        sickNotePage.aubStartDate(LocalDate.of(currentYear, MAY, 11));
        assertThat(sickNotePage.showsAubToDate(LocalDate.of(currentYear, MAY, 11))).isTrue();

        sickNotePage.submit();

        wait.until(pageIsVisible(sickNoteDetailPage));
        assertThat(sickNoteDetailPage.showsChildSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2021, MAY, 10))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateTo(LocalDate.of(2021, MAY, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteAubDateFrom(LocalDate.of(2021, MAY, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteAubDateTo(LocalDate.of(2021, MAY, 11))).isTrue();
    }

    private void sickNoteStatisticListView(RemoteWebDriver webDriver, Person person) {
        final WebDriverWait wait = new WebDriverWait(webDriver, 20);

        final NavigationPage navigationPage = new NavigationPage(webDriver);
        final SickNoteOverviewPage sickNoteOverviewPage = new SickNoteOverviewPage(webDriver, messageSource, ENGLISH);

        navigationPage.clickSickNotes();
        wait.until(pageIsVisible(sickNoteOverviewPage));

        assertThat(sickNoteOverviewPage.showsSickNoteStatistic(person.getFirstName(), person.getLastName(), 3, 1)).isTrue();
        assertThat(sickNoteOverviewPage.showsChildSickNoteStatistic(person.getFirstName(), person.getLastName(), 4, 1)).isTrue();
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
}
