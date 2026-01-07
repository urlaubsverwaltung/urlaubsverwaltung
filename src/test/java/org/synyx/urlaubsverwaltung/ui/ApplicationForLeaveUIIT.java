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
import org.synyx.urlaubsverwaltung.SingleTenantTestPostgreSQLContainer;
import org.synyx.urlaubsverwaltung.TestKeycloakContainer;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.ui.extension.UiTest;
import org.synyx.urlaubsverwaltung.ui.pages.ApplicationDetailPage;
import org.synyx.urlaubsverwaltung.ui.pages.ApplicationFormPage;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.OverviewPage;
import org.synyx.urlaubsverwaltung.ui.pages.settings.SettingsAbsencesPage;
import org.synyx.urlaubsverwaltung.ui.pages.settings.SettingsWorkingTimePage;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
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
import static java.time.LocalDate.now;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.util.Locale.GERMAN;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.util.StringUtils.trimAllWhitespace;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Testcontainers(parallel = true)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@UiTest
class ApplicationForLeaveUIIT {

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

        final LoginPage loginPage = new LoginPage(page, port);
        final NavigationPage navigationPage = new NavigationPage(page);
        final OverviewPage overviewPage = new OverviewPage(page, messageSource, GERMAN);
        final SettingsAbsencesPage settingsPage = new SettingsAbsencesPage(page);
        final SettingsWorkingTimePage settingsWorkingTimePage = new SettingsWorkingTimePage(page);
        final ApplicationFormPage applicationFormPage = new ApplicationFormPage(page);

        // log in as office user and disable the overtime feature
        loginPage.login(new LoginPage.Credentials(officePerson.getEmail(), officePerson.getEmail()));

        navigationPage.clickSettings();

        settingsPage.navigation().clickOvertime();
        settingsWorkingTimePage.disableOvertime();
        settingsWorkingTimePage.submit();
        page.waitForURL(url -> url.endsWith(SettingsWorkingTimePage.URL));

        navigationPage.logout();

        // now the quick-add button should link directly to application-for-leave page
        // for the user logged in with role=USER
        loginPage.login(new LoginPage.Credentials(userPerson.getEmail(), userPerson.getEmail()));

        overviewPage.waitForVisibleWithPerson(userPerson.getNiceName(), LocalDate.now().getYear());

        navigationPage.quickAdd.clickCreateNewApplication();
        applicationFormPage.waitForVisible();

        navigationPage.logout();
    }

    @Test
    @DisplayName("when USER is logged in and overtime feature is enabled then quick-add opens a popupmenu")
    void ensureQuickAddOpensPopupMenu(Page page) {
        final Person officePerson = createPerson("Alfred", "Pennyworth the second", List.of(USER, OFFICE));
        final Person userPerson = createPerson("The", "Joker the second", List.of(USER));

        final LoginPage loginPage = new LoginPage(page, port);
        final NavigationPage navigationPage = new NavigationPage(page);
        final OverviewPage overviewPage = new OverviewPage(page, messageSource, GERMAN);
        final SettingsAbsencesPage settingsPage = new SettingsAbsencesPage(page);
        final SettingsWorkingTimePage settingsWorkingTimePage = new SettingsWorkingTimePage(page);
        final ApplicationFormPage applicationFormPage = new ApplicationFormPage(page);

        loginPage.login(new LoginPage.Credentials(officePerson.getEmail(), officePerson.getEmail()));

        navigationPage.clickSettings();

        settingsPage.navigation().clickOvertime();
        settingsWorkingTimePage.enableOvertime();
        settingsWorkingTimePage.submit();
        page.waitForURL(url -> url.endsWith(SettingsWorkingTimePage.URL));

        navigationPage.logout();

        loginPage.login(new LoginPage.Credentials(userPerson.getEmail(), userPerson.getEmail()));

        overviewPage.waitForVisibleWithPerson(userPerson.getNiceName(), LocalDate.now().getYear());

        navigationPage.quickAdd.togglePopover();
        navigationPage.quickAdd.clickPopoverNewApplication();

        applicationFormPage.waitForVisible();

        navigationPage.logout();
    }

    @Test
    void checkIfItIsPossibleToRequestAnApplicationForLeave(Page page) {
        final Person officePerson = createPerson("Alfred", "Pennyworth the third", List.of(USER, OFFICE));
        final Person batman = createPerson("Bruce", "Wayne the third", List.of(USER));
        final Person joker = createPerson("Arthur", "Fleck the third", List.of(USER));

        final LoginPage loginPage = new LoginPage(page, port);
        final NavigationPage navigationPage = new NavigationPage(page);
        final OverviewPage overviewPage = new OverviewPage(page, messageSource, GERMAN);
        final ApplicationFormPage applicationFormPage = new ApplicationFormPage(page);
        final ApplicationDetailPage applicationDetailPage = new ApplicationDetailPage(page, messageSource, GERMAN);

        loginPage.login(new LoginPage.Credentials(officePerson.getEmail(), officePerson.getEmail()));

        overviewPage.waitForVisibleWithPerson(officePerson.getNiceName(), LocalDate.now().getYear());

        navigationPage.quickAdd.togglePopover();
        navigationPage.quickAdd.clickPopoverNewApplication();

        applicationFormPage.waitForVisible();
        applicationFormPage.from(getNextWorkday());

        applicationFormPage.selectReplacement(batman);
        applicationFormPage.waitForReplacementAtPosition(batman, 1);

        applicationFormPage.selectReplacement(joker);
        applicationFormPage.waitForReplacementAtPosition(joker, 1);
        applicationFormPage.waitForReplacementAtPosition(batman, 2);

        applicationFormPage.setCommentForReplacement(batman, "please be gentle!");

        applicationFormPage.submit();

        applicationDetailPage.waitForVisible();
        page.waitForCondition(applicationDetailPage::showsApplicationCreatedInfo);
        page.waitForCondition(() -> applicationDetailPage.isVisibleForPerson(officePerson.getNiceName()));

        // application created info vanishes sometime
        page.waitForCondition(() -> !applicationDetailPage.showsApplicationCreatedInfo());

        assertThat(applicationDetailPage.replacementLocator(batman)).isVisible();
        assertThat(applicationDetailPage.replacementLocator(joker)).isVisible();

        // ensure given information has been persisted successfully
        // currently the detail page hides some information like comments for replacements, therefore go to edit mode first
        applicationDetailPage.clickEdit();
        applicationFormPage.waitForReplacementAtPosition(joker, 1);
        applicationFormPage.waitForReplacementAtPosition(batman, 2, "please be gentle!");

        navigationPage.logout();
    }

    @Test
    void ensureCreatingApplicationForLeaveOfTypeSpecialLeave(Page page) {
        final Person officePerson = createPerson("Alfred", "Pennyworth the fifth", List.of(USER, OFFICE));

        final LoginPage loginPage = new LoginPage(page, port);
        final NavigationPage navigationPage = new NavigationPage(page);
        final OverviewPage overviewPage = new OverviewPage(page, messageSource, GERMAN);
        final ApplicationFormPage applicationFormPage = new ApplicationFormPage(page);
        final ApplicationDetailPage applicationDetailPage = new ApplicationDetailPage(page, messageSource, GERMAN);

        loginPage.login(new LoginPage.Credentials(officePerson.getEmail(), officePerson.getEmail()));

        overviewPage.waitForVisibleWithPerson(officePerson.getNiceName(), LocalDate.now().getYear());

        navigationPage.quickAdd.togglePopover();
        navigationPage.quickAdd.clickPopoverNewApplication();

        applicationFormPage.waitForVisible();
        assertThat(applicationFormPage.reasonLocator()).not().isVisible();

        applicationFormPage.selectVacationTypeOfName(msg("application.data.vacationType.specialleave", GERMAN));
        assertThat(applicationFormPage.reasonLocator()).isVisible();
        assertThat(applicationFormPage.overtimeHoursLocator()).not().isVisible();

        applicationFormPage.submit();
        assertThat(applicationFormPage.fromErrorLocator()).isVisible();
        assertThat(applicationFormPage.toErrorLocator()).isVisible();
        assertThat(applicationFormPage.reasonErrorLocator()).isVisible();

        applicationFormPage.from(getNextWorkday());
        applicationFormPage.reason("some reason text.");
        applicationFormPage.submit();

        page.waitForCondition(applicationDetailPage::showsApplicationCreatedInfo);
        // application created info vanishes sometime
        page.waitForCondition(() -> !applicationDetailPage.showsApplicationCreatedInfo());

        navigationPage.logout();
    }

    @Test
    void ensureCreatingApplicationForLeaveOfTypeOvertime(Page page) {
        final Person officePerson = createPerson("Alfred", "Pennyworth the sixth", List.of(USER, OFFICE));

        final LoginPage loginPage = new LoginPage(page, port);
        final NavigationPage navigationPage = new NavigationPage(page);
        final SettingsAbsencesPage settingsPage = new SettingsAbsencesPage(page);
        final SettingsWorkingTimePage settingsWorkingTimePage = new SettingsWorkingTimePage(page);
        final OverviewPage overviewPage = new OverviewPage(page, messageSource, GERMAN);
        final ApplicationFormPage applicationFormPage = new ApplicationFormPage(page);
        final ApplicationDetailPage applicationDetailPage = new ApplicationDetailPage(page, messageSource, GERMAN);

        loginPage.login(new LoginPage.Credentials(officePerson.getEmail(), officePerson.getEmail()));

        overviewPage.waitForVisibleWithPerson(officePerson.getNiceName(), LocalDate.now().getYear());

        // ensure overtime feature is enabled
        navigationPage.clickSettings();
        settingsPage.navigation().clickOvertime();
        settingsWorkingTimePage.enableOvertime();
        settingsWorkingTimePage.submit();
        page.waitForURL(url -> url.endsWith(SettingsWorkingTimePage.URL));

        navigationPage.quickAdd.togglePopover();
        navigationPage.quickAdd.clickPopoverNewApplication();

        applicationFormPage.waitForVisible();
        assertThat(applicationFormPage.overtimeHoursLocator()).not().isVisible();

        applicationFormPage.selectVacationTypeOfName(msg("application.data.vacationType.overtime", GERMAN));
        assertThat(applicationFormPage.overtimeHoursLocator()).isVisible();
        assertThat(applicationFormPage.reasonLocator()).not().isVisible();

        applicationFormPage.submit();
        applicationFormPage.waitForVisible();

        assertThat(applicationFormPage.fromErrorLocator()).isVisible();
        assertThat(applicationFormPage.toErrorLocator()).isVisible();
        assertThat(applicationFormPage.overtimeHoursErrorLocator()).isVisible();

        applicationFormPage.from(getNextWorkday());
        applicationFormPage.setOvertimeReductionHours(1);
        applicationFormPage.setOvertimeReductionMinutes(30);
        applicationFormPage.submit();

        page.context().waitForCondition(applicationDetailPage::showsApplicationCreatedInfo);
        // application created info vanishes sometime
        page.context().waitForCondition(() -> !applicationDetailPage.showsApplicationCreatedInfo());

        navigationPage.logout();
    }

    @Test
    @DisplayName("when USER is logged in and halfDay is disabled then application-for-leave can be created only for full days.")
    void ensureApplicationForLeaveWithDisabledHalfDayOption(Page page) {
        final Person officePerson = createPerson("Alfred", "Pennyworth the fourth", List.of(USER, OFFICE));
        final Person batman = createPerson("Bruce", "Wayne the fourth", List.of(USER));
        final Person joker = createPerson("Arthur", "Fleck the fourth", List.of(USER));

        final LoginPage loginPage = new LoginPage(page, port);
        final NavigationPage navigationPage = new NavigationPage(page);
        final SettingsAbsencesPage settingsPage = new SettingsAbsencesPage(page);
        final ApplicationFormPage applicationFormPage = new ApplicationFormPage(page);
        final ApplicationDetailPage applicationDetailPage = new ApplicationDetailPage(page, messageSource, GERMAN);

        // log in as office user and disable halfDay
        loginPage.login(new LoginPage.Credentials(officePerson.getEmail(), officePerson.getEmail()));

        navigationPage.quickAdd.togglePopover();
        navigationPage.quickAdd.clickPopoverNewApplication();

        // default is: half days enabled
        applicationFormPage.waitForVisible();
        assertThat(applicationFormPage.dayLengthFullLocator()).isVisible();
        assertThat(applicationFormPage.dayLengthMorningLocator()).isVisible();
        assertThat(applicationFormPage.dayLengthMorningLocator()).isVisible();

        navigationPage.clickSettings();

        settingsPage.waitForVisible();
        settingsPage.clickDisableHalfDayAbsence();
        settingsPage.submitAndWaitForPageRefresh();

        navigationPage.quickAdd.togglePopover();
        navigationPage.quickAdd.clickPopoverNewApplication();

        applicationFormPage.waitForVisible();
        // we just disabled half days in the settings: no dayLength inputs should be visible
        assertThat(applicationFormPage.dayLengthFullLocator()).not().isVisible();
        assertThat(applicationFormPage.dayLengthMorningLocator()).not().isVisible();
        assertThat(applicationFormPage.dayLengthMorningLocator()).not().isVisible();

        applicationFormPage.from(getNextWorkday());

        applicationFormPage.selectReplacement(batman);
        applicationFormPage.waitForReplacementAtPosition(batman, 1);

        applicationFormPage.selectReplacement(joker);
        applicationFormPage.waitForReplacementAtPosition(joker, 1);
        applicationFormPage.waitForReplacementAtPosition(batman, 2);

        applicationFormPage.setCommentForReplacement(batman, "please be gentle!");

        applicationFormPage.submit();
        page.waitForCondition(applicationDetailPage::showsApplicationCreatedInfo);
        page.waitForCondition(() -> applicationDetailPage.isVisibleForPerson(officePerson.getNiceName()));

        // application created info vanishes sometime
        page.waitForCondition(() -> !applicationDetailPage.showsApplicationCreatedInfo());

        assertThat(applicationDetailPage.replacementLocator(batman)).isVisible();
        assertThat(applicationDetailPage.replacementLocator(joker)).isVisible();

        // ensure given information has been persisted successfully
        // currently the detail page hides some information like comments for replacements, therefore go to edit mode first
        applicationDetailPage.clickEdit();

        applicationFormPage.waitForVisible();
        applicationFormPage.waitForReplacementAtPosition(joker, 1);
        applicationFormPage.waitForReplacementAtPosition(batman, 2, "please be gentle!");

        navigationPage.logout();
    }

    private Person createPerson(String firstName, String lastName, List<Role> roles) {

        final String email = format("%s.%s@example.org", trimAllWhitespace(firstName), trimAllWhitespace(lastName)).toLowerCase();
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

    private LocalDate getNextWorkday() {

        final FederalState federalState = settingsService.getSettings().getPublicHolidaysSettings().getFederalState();

        LocalDate nextWorkDay = now();
        while (publicHolidaysService.getPublicHoliday(nextWorkDay, federalState).isPresent()) {
            nextWorkDay = nextWorkDay.plusDays(1);
        }
        return nextWorkDay;
    }

    private String msg(String code, Locale locale) {
        return messageSource.getMessage(code, new Object[]{}, locale);
    }
}
