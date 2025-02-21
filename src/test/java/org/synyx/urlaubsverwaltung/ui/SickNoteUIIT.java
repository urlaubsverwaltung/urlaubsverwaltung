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
import org.synyx.urlaubsverwaltung.SingleTenantTestPostgreSQLContainer;
import org.synyx.urlaubsverwaltung.TestKeycloakContainer;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.ui.extension.UiTest;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNoteDetailPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNoteExtensionPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNoteOverviewPage;
import org.synyx.urlaubsverwaltung.ui.pages.SickNotePage;
import org.synyx.urlaubsverwaltung.ui.pages.settings.SettingsPage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.util.StringUtils.trimAllWhitespace;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Testcontainers(parallel = true)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.main.allow-bean-definition-overriding=true"})
@UiTest
class SickNoteUIIT {

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
    private static final SingleTenantTestPostgreSQLContainer postgre = new SingleTenantTestPostgreSQLContainer();
    @Container
    private static final TestKeycloakContainer keycloak = new TestKeycloakContainer();

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        postgre.configureSpringDataSource(registry);
        keycloak.configureSpringDataSource(registry);
    }

    @Autowired
    private PersonService personService;
    @Autowired
    private AccountInteractionService accountInteractionService;
    @Autowired
    private WorkingTimeWriteService workingTimeWriteService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private Clock clock;

    @Test
    void ensureSickNote(Page page) {

        final Person person = createPerson("Alfred", "Pennyworth", List.of(USER, OFFICE));
        login(page, person);

        final NavigationPage navigationPage = new NavigationPage(page);
        assertThat(navigationPage.quickAdd.hasPopup()).isTrue();

        sickNote(page, person, LocalDate.of(2022, FEBRUARY, 23));
        sickNoteWithIncapacityCertificate(page, person);
        childSickNote(page, person);
        childSickNoteWithIncapacityCertificate(page, person);
        sickNoteStatisticListView(page, person);
    }

    @Test
    void ensureUserCanExtendSickNote(Page page) {
        final NavigationPage navigationPage = new NavigationPage(page);

        final Person office = createPerson("Alfred-2", "Pennyworth-2", List.of(USER, OFFICE));
        login(page, office);
        enableUserSickNoteCreation(page, true);
        navigationPage.logout();

        final Person user = createPerson("Dick", "Grayson", List.of(USER));
        login(page, user);

        final LocalDate startDate = LocalDate.now(clock).minusDays(1);
        sickNote(page, user, startDate);
        sickNoteExtension(page, user, startDate, LocalDate.now(clock));
    }

    private void enableUserSickNoteCreation(Page page, boolean enable) {
        final NavigationPage navigationPage = new NavigationPage(page);
        final SettingsPage settingsPage = new SettingsPage(page);

        navigationPage.clickSettings();

        if (enable) {
            settingsPage.clickUserSubmitSickNotesAllowed();
        } else {
            settingsPage.clickUserToSubmitSickNotesForbidden();
        }

        settingsPage.saveSettings();
    }

    private void login(Page page, Person person) {
        final LoginPage loginPage = new LoginPage(page);
        page.navigate("http://localhost:" + port + "/oauth2/authorization/keycloak");
        loginPage.login(new LoginPage.Credentials(person.getEmail(), person.getEmail()));
    }

    private void sickNote(Page page, Person person, LocalDate startDate) {

        createSickNote(page, person, startDate);

        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(page, messageSource, GERMAN);
        assertThat(sickNoteDetailPage.showsSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(startDate)).isTrue();
        assertThat(sickNoteDetailPage.showsNoIncapacityCertificate()).isTrue();
    }

    private void createSickNote(Page page, Person person, LocalDate startDate) {
        final NavigationPage navigationPage = new NavigationPage(page);
        final SickNotePage sickNotePage = new SickNotePage(page);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();

        if (person.hasRole(OFFICE)) {
            assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        }
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        sickNotePage.startDate(startDate);
        assertThat(sickNotePage.showsToDate(startDate)).isTrue();

        sickNotePage.submit();
    }

    private void sickNoteExtension(Page page, Person person, LocalDate startDate, LocalDate nextEndDate) {
        final NavigationPage navigationPage = new NavigationPage(page);
        final SickNoteExtensionPage sickNoteExtensionPage = new SickNoteExtensionPage(page, messageSource, GERMAN);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(page, messageSource, GERMAN);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();

        assertThat(sickNoteExtensionPage.isVisible()).isTrue();
        sickNoteExtensionPage.setCustomNextEndDate(nextEndDate);

        assertThat(sickNoteExtensionPage.showsExtensionPreview(startDate, nextEndDate)).isTrue();
        sickNoteExtensionPage.submit();

        // no extension hint shown since sick note has not been accepted yet (sick note has been edited right away)
        assertThat(sickNoteDetailPage.showsSickNoteForPerson(person.getNiceName())).isTrue();
        assertThat(sickNoteDetailPage.showsSickNoteDateFrom(startDate)).isTrue();
    }

    private void sickNoteWithIncapacityCertificate(Page page, Person person) {
        final NavigationPage navigationPage = new NavigationPage(page);
        final SickNotePage sickNotePage = new SickNotePage(page);
        final SickNoteDetailPage sickNoteDetailPage = new SickNoteDetailPage(page, messageSource, GERMAN);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newSickNote();

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        sickNotePage.startDate(LocalDate.of(2022, MARCH, 10));
        sickNotePage.toDate(LocalDate.of(2022, MARCH, 11));

        sickNotePage.aubStartDate(LocalDate.of(2022, MARCH, 11));
        assertThat(sickNotePage.showsAubToDate(LocalDate.of(2022, MARCH, 11))).isTrue();

        sickNotePage.submit();

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

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        sickNotePage.selectTypeChildSickNote();
        sickNotePage.startDate(LocalDate.of(2022, APRIL, 11));
        sickNotePage.toDate(LocalDate.of(2022, APRIL, 12));

        sickNotePage.submit();

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

        assertThat(sickNotePage.personSelected(person.getNiceName())).isTrue();
        assertThat(sickNotePage.typeSickNoteSelected()).isTrue();
        assertThat(sickNotePage.dayTypeFullSelected()).isTrue();

        sickNotePage.selectTypeChildSickNote();
        sickNotePage.startDate(LocalDate.of(2022, MAY, 10));
        sickNotePage.toDate(LocalDate.of(2022, MAY, 11));

        sickNotePage.aubStartDate(LocalDate.of(2022, MAY, 11));
        assertThat(sickNotePage.showsAubToDate(LocalDate.of(2022, MAY, 11))).isTrue();

        sickNotePage.submit();

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

        assertThat(sickNoteOverviewPage.showsSickNoteStatistic(person.getFirstName(), person.getLastName(), 3, 1)).isTrue();
        assertThat(sickNoteOverviewPage.showsChildSickNoteStatistic(person.getFirstName(), person.getLastName(), 4, 1)).isTrue();
    }

    private Person createPerson(String firstName, String lastName, List<Role> roles) {

        final String email = format("%s.%s@example.org", trimAllWhitespace(firstName), trimAllWhitespace(lastName)).toLowerCase();
        final Optional<Person> personByMailAddress = personService.getPersonByMailAddress(email);
        if (personByMailAddress.isPresent()) {
            return personByMailAddress.get();
        }

        final String userId = keycloak.createUser(email, firstName, lastName, email, email);
        final Person savedPerson = personService.create(userId, firstName, lastName, email, List.of(), roles);

        final LocalDate validFrom = LocalDate.of(2022, 1, 1);
        final List<Integer> workingDays = Stream.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY).map(DayOfWeek::getValue).toList();
        workingTimeWriteService.touch(workingDays, validFrom, savedPerson);

        final LocalDate firstDayOfYear = LocalDate.of(2022, JANUARY, 1);
        final LocalDate lastDayOfYear = LocalDate.of(2022, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2022, APRIL, 1);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear, lastDayOfYear, true, expiryDate, TEN, TEN, TEN, ZERO, null);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear.plusYears(1), lastDayOfYear.plusYears(1), true, expiryDate.plusYears(1), TEN, TEN, TEN, ZERO, null);

        return savedPerson;
    }
}
