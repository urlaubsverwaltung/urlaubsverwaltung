package org.synyx.urlaubsverwaltung.ui;

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
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.NavigationPage;
import org.synyx.urlaubsverwaltung.ui.pages.PaginationPage;
import org.synyx.urlaubsverwaltung.ui.pages.PersonsPage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
import static java.time.Month.JANUARY;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.util.StringUtils.trimAllWhitespace;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Testcontainers(parallel = true)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@UiTest
class PersonsUIIT {

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
    void ensurePersonPagination(Page page) {

        final Person anne = createPerson("Anne", "Roth", List.of(USER, OFFICE));
        createPerson("Brigitte", "Haendel", List.of(USER));
        createPerson("Elena", "Schneider", List.of(USER));
        createPerson("Franziska", "Baier", List.of(USER));
        createPerson("Hans", "Dampf", List.of(USER));

        login(page, anne);

        final NavigationPage navigationPage = new NavigationPage(page);
        navigationPage.clickPersons();

        final PersonsPage personsPage = new PersonsPage(page);
        final PaginationPage personsPagination = personsPage.getPersonsPagination();

        // the select gives us 10 as the lowest value, however, we would have to create too many persons.
        // therefore load the first persons page with size of 2 via URL
        page.navigate(page.url() + "?size=2");

        personsPagination.showsCurrentPage(0);
        assertThat(personsPagination.getPageSizeSelectLocator()).hasValue("2");
        assertThat(personsPagination.getTotalElementsLocator()).containsText("5");
        assertThat(personsPage.getPersonRowLocator(0, "Anne Roth")).isVisible();
        assertThat(personsPage.getPersonRowLocator(1, "Brigitte Haendel")).isVisible();
        personsPage.showsNthPersons(2);

        personsPagination.getPageButtonLocator(1).click();

        personsPagination.showsCurrentPage(1);
        personsPage.showsPersonRow(0, "Elena Schneider");
        personsPage.showsPersonRow(1, "Franziska Baier");
        personsPage.showsNthPersons(2);

        personsPagination.getPageButtonLocator(2).click();

        personsPagination.showsCurrentPage(2);
        personsPage.showsPersonRow(0, "Hans Dampf");
        personsPage.showsNthPersons(1);
    }

    private void login(Page page, Person person) {
        final LoginPage loginPage = new LoginPage(page, port);
        loginPage.login(new LoginPage.Credentials(person.getEmail(), person.getEmail()));
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
