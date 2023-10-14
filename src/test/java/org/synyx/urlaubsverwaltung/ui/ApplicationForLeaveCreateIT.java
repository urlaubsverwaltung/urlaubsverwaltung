package org.synyx.urlaubsverwaltung.ui;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.synyx.urlaubsverwaltung.TestPostgreContainer;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.ui.extension.UiTest;
import org.synyx.urlaubsverwaltung.ui.pages.ApplicationDetailPage;
import org.synyx.urlaubsverwaltung.ui.pages.ApplicationPage;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.OverviewPage;
import org.synyx.urlaubsverwaltung.ui.pages.SettingsPage;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;
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
import static java.time.LocalDate.now;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.util.Locale.GERMAN;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
@UiTest
class ApplicationForLeaveCreateIT {

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
    private PublicHolidaysService publicHolidaysService;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private MessageSource messageSource;

    @Test
    @DisplayName("when USER is logged in and overtime feature is disabled then quick-add directly links to application-for-leave")
    void ensureQuickAddDirectlyLinksToApplicationForLeave(Page page) {
        final Person userPerson = createPerson("The", "Joker", List.of(USER));
        final Person officePerson = createPerson("Alfred", "Pennyworth", List.of(USER, OFFICE));

        final LoginPage loginPage = new LoginPage(page, messageSource, GERMAN);
        final NavigationPage navigationPage = new NavigationPage(page);
        final OverviewPage overviewPage = new OverviewPage(page, messageSource, GERMAN);
        final SettingsPage settingsPage = new SettingsPage(page);
        final ApplicationPage applicationPage = new ApplicationPage(page);

        page.navigate("http://localhost:" + port);

        // log in as office user
        // and disable the overtime feature

        page.context().waitForCondition(loginPage::isVisible);
        loginPage.login(new LoginPage.Credentials(officePerson.getUsername(), "secret"));

        page.waitForURL(url -> url.endsWith("/web/person/%s/overview".formatted(officePerson.getId())));
        page.context().waitForCondition(navigationPage::isVisible);
        page.context().waitForCondition(overviewPage::isVisible);

        navigationPage.clickSettings();
        page.context().waitForCondition(settingsPage::isVisible);

        settingsPage.clickWorkingTimeTab();
        settingsPage.disableOvertime();
        settingsPage.saveSettings();

        navigationPage.logout();
        page.waitForURL(url -> url.endsWith("/login"));
        page.context().waitForCondition(loginPage::isVisible);

        // now the quick-add button should link directly to application-for-leave page
        // for the user logged in with role=USER

        loginPage.login(new LoginPage.Credentials(userPerson.getUsername(), "secret"));

        page.waitForURL(url -> url.endsWith("/web/person/%s/overview".formatted(userPerson.getId())));
        page.context().waitForCondition(navigationPage::isVisible);
        page.context().waitForCondition(overviewPage::isVisible);

        assertThat(navigationPage.quickAdd.hasNoPopup()).isTrue();

        navigationPage.quickAdd.click();
        page.context().waitForCondition(applicationPage::isVisible);
    }

    @Test
    @DisplayName("when USER is logged in and overtime feature is enabled then quick-add opens a popupmenu")
    void ensureQuickAddOpensPopupMenu(Page page) {
        final Person officePerson = createPerson("Alfred", "Pennyworth", List.of(USER, OFFICE));
        final Person userPerson = createPerson("The", "Joker", List.of(USER));

        final LoginPage loginPage = new LoginPage(page, messageSource, GERMAN);
        final NavigationPage navigationPage = new NavigationPage(page);
        final OverviewPage overviewPage = new OverviewPage(page, messageSource, GERMAN);
        final SettingsPage settingsPage = new SettingsPage(page);
        final ApplicationPage applicationPage = new ApplicationPage(page);

        page.navigate("http://localhost:" + port);

        page.context().waitForCondition(loginPage::isVisible);
        loginPage.login(new LoginPage.Credentials(officePerson.getUsername(), "secret"));

        page.waitForURL(url -> url.endsWith("/web/person/%s/overview".formatted(officePerson.getId())));
        page.context().waitForCondition(navigationPage::isVisible);

        navigationPage.clickSettings();
        page.context().waitForCondition(settingsPage::isVisible);

        settingsPage.clickWorkingTimeTab();
        settingsPage.enableOvertime();
        settingsPage.saveSettings();

        navigationPage.logout();
        page.context().waitForCondition(loginPage::isVisible);

        loginPage.login(new LoginPage.Credentials(userPerson.getUsername(), "secret"));

        page.context().waitForCondition(overviewPage::isVisible);
        assertThat(overviewPage.isVisibleForPerson(userPerson.getNiceName(), LocalDate.now().getYear())).isTrue();

        assertThat(navigationPage.quickAdd.hasPopup()).isTrue();

        // clicking the element should open the popup menu
        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newApplication();

        page.context().waitForCondition(applicationPage::isVisible);

        navigationPage.logout();
        page.context().waitForCondition(loginPage::isVisible);
    }

    @Test
    void checkIfItIsPossibleToRequestAnApplicationForLeave(Page page) {
        final Person officePerson = createPerson("Alfred", "Pennyworth", List.of(USER, OFFICE));
        final Person batman = createPerson("Bruce", "Wayne", List.of(USER));
        final Person joker = createPerson("Arthur", "Fleck", List.of(USER));

        final LoginPage loginPage = new LoginPage(page, messageSource, GERMAN);
        final NavigationPage navigationPage = new NavigationPage(page);
        final OverviewPage overviewPage = new OverviewPage(page, messageSource, GERMAN);
        final ApplicationPage applicationPage = new ApplicationPage(page);
        final ApplicationDetailPage applicationDetailPage = new ApplicationDetailPage(page, messageSource, GERMAN);

        page.navigate("http://localhost:" + port);

        page.context().waitForCondition(loginPage::isVisible);
        loginPage.login(new LoginPage.Credentials(officePerson.getUsername(), "secret"));

        page.waitForURL(url -> url.endsWith("/web/person/%s/overview".formatted(officePerson.getId())));
        page.context().waitForCondition(navigationPage::isVisible);
        page.context().waitForCondition(overviewPage::isVisible);
        assertThat(overviewPage.isVisibleForPerson(officePerson.getNiceName(), LocalDate.now().getYear())).isTrue();

        assertThat(navigationPage.quickAdd.hasPopup()).isTrue();
        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newApplication();
        page.context().waitForCondition(applicationPage::isVisible);

        applicationPage.from(getNextWorkday());

        applicationPage.selectReplacement(batman);
        page.context().waitForCondition(() -> applicationPage.showsAddedReplacementAtPosition(batman, 1));

        applicationPage.selectReplacement(joker);
        page.context().waitForCondition(() -> applicationPage.showsAddedReplacementAtPosition(joker, 1));
        page.context().waitForCondition(() -> applicationPage.showsAddedReplacementAtPosition(batman, 2));

        applicationPage.setCommentForReplacement(batman, "please be gentle!");

        applicationPage.submit();

        page.context().waitForCondition(applicationDetailPage::isVisible);
        page.context().waitForCondition(applicationDetailPage::showsApplicationCreatedInfo);
        assertThat(applicationDetailPage.isVisibleForPerson(officePerson.getNiceName())).isTrue();

        // application created info vanishes sometime
        page.context().waitForCondition(() -> !applicationDetailPage.showsApplicationCreatedInfo());

        assertThat(applicationDetailPage.showsReplacement(batman)).isTrue();
        assertThat(applicationDetailPage.showsReplacement(joker)).isTrue();

        // ensure given information has been persisted successfully
        // (currently the detail page hides some information like comments for replacements)
        applicationDetailPage.selectEdit();
        page.context().waitForCondition(applicationPage::isVisible);

        assertThat(applicationPage.showsAddedReplacementAtPosition(joker, 1)).isTrue();
        assertThat(applicationPage.showsAddedReplacementAtPosition(batman, 2, "please be gentle!")).isTrue();

        navigationPage.logout();
        page.context().waitForCondition(loginPage::isVisible);
    }

    @Test
    void ensureCreatingApplicationForLeaveOfTypeSpecialLeave(Page page) {
        final Person officePerson = createPerson("Alfred", "Pennyworth", List.of(USER, OFFICE));

        final LoginPage loginPage = new LoginPage(page, messageSource, GERMAN);
        final NavigationPage navigationPage = new NavigationPage(page);
        final OverviewPage overviewPage = new OverviewPage(page, messageSource, GERMAN);
        final ApplicationPage applicationPage = new ApplicationPage(page);
        final ApplicationDetailPage applicationDetailPage = new ApplicationDetailPage(page, messageSource, GERMAN);

        page.navigate("http://localhost:" + port);

        page.context().waitForCondition(loginPage::isVisible);
        loginPage.login(new LoginPage.Credentials(officePerson.getUsername(), "secret"));

        page.waitForURL(url -> url.endsWith("/web/person/%s/overview".formatted(officePerson.getId())));
        page.context().waitForCondition(navigationPage::isVisible);
        page.context().waitForCondition(overviewPage::isVisible);
        assertThat(overviewPage.isVisibleForPerson(officePerson.getNiceName(), LocalDate.now().getYear())).isTrue();

        assertThat(navigationPage.quickAdd.hasPopup()).isTrue();
        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newApplication();
        page.context().waitForCondition(applicationPage::isVisible);

        assertThat(applicationPage.showsReason()).isFalse();

        applicationPage.selectVacationTypeOfName("Sonderurlaub");
        assertThat(applicationPage.showsReason()).isTrue();

        applicationPage.submit();

        assertThat(applicationPage.showsFromError()).isTrue();
        assertThat(applicationPage.showsToError()).isTrue();
        assertThat(applicationPage.showsReasonError()).isTrue();

        applicationPage.from(getNextWorkday());
        applicationPage.reason("some reason text.");
        applicationPage.submit();

        page.context().waitForCondition(applicationDetailPage::isVisible);
        page.context().waitForCondition(applicationDetailPage::showsApplicationCreatedInfo);
        // application created info vanishes sometime
        page.context().waitForCondition(() -> !applicationDetailPage.showsApplicationCreatedInfo());

        navigationPage.logout();
        page.context().waitForCondition(loginPage::isVisible);
    }

    @Test
    @DisplayName("when USER is logged in and halfDay is disabled then application-for-leave can be created only for full days.")
    void ensureApplicationForLeaveWithDisabledHalfDayOption(Page page) {
        final Person officePerson = createPerson("Alfred", "Pennyworth", List.of(USER, OFFICE));
        final Person batman = createPerson("Bruce", "Wayne", List.of(USER));
        final Person joker = createPerson("Arthur", "Fleck", List.of(USER));

        final LoginPage loginPage = new LoginPage(page, messageSource, GERMAN);
        final NavigationPage navigationPage = new NavigationPage(page);
        final OverviewPage overviewPage = new OverviewPage(page, messageSource, GERMAN);
        final SettingsPage settingsPage = new SettingsPage(page);
        final ApplicationPage applicationPage = new ApplicationPage(page);
        final ApplicationDetailPage applicationDetailPage = new ApplicationDetailPage(page, messageSource, GERMAN);

        page.navigate("http://localhost:" + port);

        // log in as office user
        // and disable halfDay

        page.context().waitForCondition(loginPage::isVisible);
        loginPage.login(new LoginPage.Credentials(officePerson.getUsername(), "secret"));

        page.context().waitForCondition(navigationPage::isVisible);
        page.context().waitForCondition(overviewPage::isVisible);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newApplication();
        page.context().waitForCondition(applicationPage::isVisible);

        // default is: half days enabled
        assertThat(applicationPage.showsDayLengthInputs()).isTrue();

        navigationPage.clickSettings();
        page.context().waitForCondition(settingsPage::isVisible);

        settingsPage.clickDisableHalfDayAbsence();
        settingsPage.saveSettings();
        page.context().waitForCondition(navigationPage::isVisible);

        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newApplication();
        page.context().waitForCondition(applicationPage::isVisible);

        // we just disabled half days in the settings
        assertThat(applicationPage.showsDayLengthInputs()).isFalse();

        applicationPage.from(getNextWorkday());

        applicationPage.selectReplacement(batman);
        page.context().waitForCondition(() -> applicationPage.showsAddedReplacementAtPosition(batman, 1));

        applicationPage.selectReplacement(joker);
        page.context().waitForCondition(() -> applicationPage.showsAddedReplacementAtPosition(joker, 1));
        page.context().waitForCondition(() -> applicationPage.showsAddedReplacementAtPosition(batman, 2));

        applicationPage.setCommentForReplacement(batman, "please be gentle!");

        applicationPage.submit();
        page.context().waitForCondition(applicationDetailPage::isVisible);
        page.context().waitForCondition(applicationDetailPage::showsApplicationCreatedInfo);
        assertThat(applicationDetailPage.isVisibleForPerson(officePerson.getNiceName())).isTrue();

        // application created info vanishes sometime
        page.context().waitForCondition(() -> !applicationDetailPage.showsApplicationCreatedInfo());

        assertThat(applicationDetailPage.showsReplacement(batman)).isTrue();
        assertThat(applicationDetailPage.showsReplacement(joker)).isTrue();

        // ensure given information has been persisted successfully
        // (currently the detail page hides some information like comments for replacements)
        applicationDetailPage.selectEdit();
        page.context().waitForCondition(applicationPage::isVisible);

        assertThat(applicationPage.showsAddedReplacementAtPosition(joker, 1)).isTrue();
        assertThat(applicationPage.showsAddedReplacementAtPosition(batman, 2, "please be gentle!")).isTrue();

        navigationPage.logout();
        page.context().waitForCondition(loginPage::isVisible);
    }

    private Person createPerson(String firstName, String lastName, List<Role> roles) {
        final String username = lastName + UUID.randomUUID().getLeastSignificantBits();
        final String email = String.format("%s.%s@example.org", firstName, lastName);
        final Person savedPerson = personService.create(username, firstName, lastName, email, List.of(), roles);

        final Year currentYear = Year.now();
        final LocalDate firstDayOfYear = currentYear.atDay(1);
        final List<Integer> workingDays = Stream.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY).map(DayOfWeek::getValue).collect(toList());
        workingTimeWriteService.touch(workingDays, firstDayOfYear, savedPerson);

        final LocalDate lastDayOfYear = firstDayOfYear.withMonth(DECEMBER.getValue()).withDayOfMonth(31);
        final LocalDate expiryDate = LocalDate.of(currentYear.getValue(), APRIL, 1);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear, lastDayOfYear, true, expiryDate, TEN, TEN, TEN, ZERO, null);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear.plusYears(1), lastDayOfYear.plusYears(1), true, expiryDate.plusYears(1), TEN, TEN, TEN, ZERO, null);

        return savedPerson;
    }

    private LocalDate getNextWorkday() {

        final FederalState federalState = settingsService.getSettings().getWorkingTimeSettings().getFederalState();

        LocalDate nextWorkDay = now();
        while (publicHolidaysService.getPublicHoliday(nextWorkDay, federalState).isPresent()) {
            nextWorkDay = nextWorkDay.plusDays(1);
        }
        return nextWorkDay;
    }
}
