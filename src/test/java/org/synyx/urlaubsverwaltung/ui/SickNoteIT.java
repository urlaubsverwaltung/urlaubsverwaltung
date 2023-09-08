package org.synyx.urlaubsverwaltung.ui;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.synyx.urlaubsverwaltung.TestPostgreContainer;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.ui.extension.UiTest;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNoteDetailPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNoteOverviewPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNotePage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.main.allow-bean-definition-overriding=true"})
@UiTest
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
    void ensureSickNote(Page page) {
        final Person person = createPerson();

        final LoginPage loginPage = new LoginPage(page, messageSource, GERMAN);
        final NavigationPage navigationPage = new NavigationPage(page);

        page.navigate("http://localhost:" + port);

        loginPage.login(new LoginPage.Credentials(person.getUsername(), "secret"));

        page.context().waitForCondition(navigationPage::isVisible);
        assertThat(navigationPage.quickAdd.hasPopup()).isTrue();

        sickNote(page, person);
        sickNoteWithIncapacityCertificate(page, person);
        childSickNote(page, person);
        childSickNoteWithIncapacityCertificate(page, person);
        sickNoteStatisticListView(page, person);

        navigationPage.logout();
        page.context().waitForCondition(loginPage::isVisible);
    }

    private void sickNote(Page page, Person person) {
        final NavigationPage navigationPage = new NavigationPage(page);
        final SickNotePage sickNotePage = new SickNotePage(page);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(page, messageSource, GERMAN);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        page.context().waitForCondition(sickNotePage::isVisible);

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        sickNotePage.startDate(LocalDate.of(2022, FEBRUARY, 23));
        assertThat(sickNotePage.showsToDate(LocalDate.of(2022, FEBRUARY, 23))).isTrue();

        sickNotePage.submit();
        page.context().waitForCondition(sickNoteDetailPage::isVisible);

        assertThat(sickNoteDetailPage.showsSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2022, FEBRUARY, 23))).isTrue();
        assertThat(sickNoteDetailPage.showsNoIncapacityCertificate()).isTrue();
    }

    private void sickNoteWithIncapacityCertificate(Page page, Person person) {
        final NavigationPage navigationPage = new NavigationPage(page);
        final SickNotePage sickNotePage = new SickNotePage(page);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(page, messageSource, GERMAN);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        page.context().waitForCondition(sickNotePage::isVisible);

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        sickNotePage.startDate(LocalDate.of(2022, MARCH, 10));
        sickNotePage.toDate(LocalDate.of(2022, MARCH, 11));

        sickNotePage.aubStartDate(LocalDate.of(2022, MARCH, 11));
        assertThat(sickNotePage.showsAubToDate(LocalDate.of(2022, MARCH, 11))).isTrue();

        sickNotePage.submit();
        page.context().waitForCondition(sickNoteDetailPage::isVisible);

        assertThat(sickNoteDetailPage.showsSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2022, MARCH, 10))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateTo(LocalDate.of(2022, MARCH, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteAubDateFrom(LocalDate.of(2022, MARCH, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteAubDateTo(LocalDate.of(2022, MARCH, 11))).isTrue();
    }

    private void childSickNote(Page page, Person person) {
        final NavigationPage navigationPage = new NavigationPage(page);
        final SickNotePage sickNotePage = new SickNotePage(page);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(page, messageSource, GERMAN);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        page.context().waitForCondition(sickNotePage::isVisible);

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        sickNotePage.selectTypeChildSickNote();
        sickNotePage.startDate(LocalDate.of(2022, APRIL, 11));
        sickNotePage.toDate(LocalDate.of(2022, APRIL, 12));

        sickNotePage.submit();
        page.context().waitForCondition(sickNoteDetailPage::isVisible);

        assertThat(sickNoteDetailPage.showsChildSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2022, APRIL, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateTo(LocalDate.of(2022, APRIL, 12))).isTrue();
        assertThat(sickNoteDetailPage.showsNoIncapacityCertificate()).isTrue();
    }

    private void childSickNoteWithIncapacityCertificate(Page page, Person person) {
        final NavigationPage navigationPage = new NavigationPage(page);
        final SickNotePage sickNotePage = new SickNotePage(page);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(page, messageSource, GERMAN);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();
        page.context().waitForCondition(sickNotePage::isVisible);

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        sickNotePage.selectTypeChildSickNote();
        sickNotePage.startDate(LocalDate.of(2022, MAY, 10));
        sickNotePage.toDate(LocalDate.of(2022, MAY, 11));

        sickNotePage.aubStartDate(LocalDate.of(2022, MAY, 11));
        assertThat(sickNotePage.showsAubToDate(LocalDate.of(2022, MAY, 11))).isTrue();

        sickNotePage.submit();
        page.context().waitForCondition(sickNoteDetailPage::isVisible);

        assertThat(sickNoteDetailPage.showsChildSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(LocalDate.of(2022, MAY, 10))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateTo(LocalDate.of(2022, MAY, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteAubDateFrom(LocalDate.of(2022, MAY, 11))).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteAubDateTo(LocalDate.of(2022, MAY, 11))).isTrue();
    }

    private void sickNoteStatisticListView(Page page, Person person) {

        final NavigationPage navigationPage = new NavigationPage(page);
        final SickNoteOverviewPage sickNoteOverviewPage = new SickNoteOverviewPage(page, messageSource, GERMAN);

        navigationPage.clickSickNotes();
        page.context().waitForCondition(sickNoteOverviewPage::isVisible);

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
}
