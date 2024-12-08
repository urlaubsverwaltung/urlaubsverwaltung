package org.synyx.urlaubsverwaltung.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.synyx.urlaubsverwaltung.SingleTenantTestPostgreSQLContainer;
import org.synyx.urlaubsverwaltung.TestKeycloakContainer;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.ui.extension.UiTest;
import org.synyx.urlaubsverwaltung.ui.pages.ApplicationPage;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.settings.SettingsAbsenceTypesPage;
import org.synyx.urlaubsverwaltung.ui.pages.settings.SettingsPage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
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
class CustomAbsenceTypeUIIT {

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

    @Test
    void ensureAddingAndEnablingCustomVacationType(Page page) {

        final Person person = createPerson("dBradley", "Donald", List.of(USER, OFFICE));

        final LoginPage loginPage = new LoginPage(page);
        final NavigationPage navigationPage = new NavigationPage(page);
        final SettingsPage settingsPage = new SettingsPage(page);
        final SettingsAbsenceTypesPage settingsAbsenceTypesPage = new SettingsAbsenceTypesPage(page);
        final ApplicationPage applicationPage = new ApplicationPage(page);

        page.navigate("http://localhost:" + port + "/oauth2/authorization/keycloak");
        page.context().waitForCondition(loginPage::isVisible);
        page.waitForLoadState();

        loginPage.login(new LoginPage.Credentials(person.getEmail(), person.getEmail()));

        navigationPage.clickSettings();
        settingsPage.navigation().goToAbsenceTypes();
        settingsAbsenceTypesPage.addNewVacationType();

        // ensure at least one translation required error hint
        settingsAbsenceTypesPage.submitCustomAbsenceTypes();
        assertThat(settingsAbsenceTypesPage.vacationTypeMissingTranslationError(settingsAbsenceTypesPage.lastVacationType())).isVisible();

        // ensure saving valid vacation type succeeds
        settingsAbsenceTypesPage.setVacationTypeLabel(settingsAbsenceTypesPage.lastVacationType(), GERMAN, "Biertag");
        settingsAbsenceTypesPage.submitCustomAbsenceTypes();
        assertThat(settingsAbsenceTypesPage.vacationTypeMissingTranslationError(settingsAbsenceTypesPage.lastVacationType())).not().isVisible();

        // enable vacation type to ensure selecting it later creating a new application for leave
        final Locator status = settingsAbsenceTypesPage.vacationTypeStatusCheckbox(settingsAbsenceTypesPage.lastVacationType());
        assertThat(status).not().isChecked();
        status.check();
        settingsAbsenceTypesPage.submitCustomAbsenceTypes();

        // ensure unique vacation type name error hint
        settingsAbsenceTypesPage.addNewVacationType();
        settingsAbsenceTypesPage.setVacationTypeLabel(settingsAbsenceTypesPage.lastVacationType(), GERMAN, "Biertag");
        settingsAbsenceTypesPage.submitCustomAbsenceTypes();
        assertThat(settingsAbsenceTypesPage.vacationTypeUniqueTranslationError(settingsAbsenceTypesPage.lastVacationType(), GERMAN)).isVisible();

        // ensure vacation type is selectable creating a new application for leave
        navigationPage.quickAdd.click();
        navigationPage.quickAdd.newApplication();
        page.context().waitForCondition(applicationPage::isVisible);

        // this throws when the name cannot be found
        applicationPage.selectVacationTypeOfName("Biertag");

        navigationPage.logout();
        page.context().waitForCondition(loginPage::isVisible);
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
}
