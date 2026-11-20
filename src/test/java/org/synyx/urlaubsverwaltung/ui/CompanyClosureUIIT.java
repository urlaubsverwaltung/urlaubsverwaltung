package org.synyx.urlaubsverwaltung.ui;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.synyx.urlaubsverwaltung.SingleTenantTestPostgreSQLContainer;
import org.synyx.urlaubsverwaltung.TestKeycloakContainer;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.ui.extension.UiIntegrationTest;
import org.synyx.urlaubsverwaltung.ui.extension.UiTest;
import org.synyx.urlaubsverwaltung.ui.pages.CompanyClosureFormPage;
import org.synyx.urlaubsverwaltung.ui.pages.CompanyClosureOverviewPage;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.ui.pages.OverviewPage;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.time.Month.JUNE;
import static java.util.Locale.GERMAN;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.scheduling.annotation.Scheduled.CRON_DISABLED;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Testcontainers(parallel = true)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@UiIntegrationTest
@UiTest
class CompanyClosureUIIT {

    @LocalServerPort
    private int port;

    @Container
    @ServiceConnection
    private static final SingleTenantTestPostgreSQLContainer postgre = new SingleTenantTestPostgreSQLContainer();
    @Container
    private static final TestKeycloakContainer keycloak = new TestKeycloakContainer();

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        keycloak.configureSpringDataSource(registry);
    }

    @Autowired
    private PersonService personService;
    @Autowired
    private CompanyClosureService companyClosureService;

    @TestConfiguration
    static class TestSessionConfig {
        @Bean
        public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> disableCleanupCustomizer() {
            // Force-set the cleanup cron to the official disabled macro programmatically
            return sessionRepository -> sessionRepository.setCleanupCron(CRON_DISABLED);
        }
    }

    @Test
    void ensureCancelLeadsToTheOverviewAfterInvalidSubmits(Page page) {

        loginAsOffice(page, "closure.cancel@example.org");

        final CompanyClosureOverviewPage overviewPage = new CompanyClosureOverviewPage(page);
        final CompanyClosureFormPage formPage = new CompanyClosureFormPage(page);

        overviewPage.open(port);
        overviewPage.clickCreate();
        page.waitForURL(CompanyClosureFormPage.newUrl(port));
        formPage.markDocument();

        formPage.save();
        assertThat(formPage.dayError()).isVisible();
        assertThat(formPage.monthError()).isVisible();
        assertThat(page).hasURL(CompanyClosureFormPage.newUrl(port));

        formPage.day("31");
        formPage.month("4");
        formPage.save();
        assertThat(formPage.monthDayError()).isVisible();
        assertThat(formPage.dayError()).isHidden();
        assertThat(page).hasURL(CompanyClosureFormPage.newUrl(port));

        // the form has been rendered into the page instead of loading it again, with the datepicker still working
        assertThat(formPage.markedDocument()).hasJSProperty("sameDocument", true);
        assertThat(formPage.validFromDatepicker()).hasCount(1);
        assertThat(formPage.validFromFields()).hasCount(1);

        // one step back although the form has been submitted twice
        formPage.cancel();
        assertThat(page).hasURL(CompanyClosureOverviewPage.url(port));
        assertThat(page).hasTitle(Pattern.compile("^Betriebsruhe"));
    }

    @Test
    void ensureClosureDayIsCreatedAfterAnInvalidSubmit(Page page) {

        loginAsOffice(page, "closure.create@example.org");

        final CompanyClosureOverviewPage overviewPage = new CompanyClosureOverviewPage(page);
        final CompanyClosureFormPage formPage = new CompanyClosureFormPage(page);

        formPage.open(port);

        formPage.save();
        assertThat(formPage.dayError()).isVisible();

        formPage.day("15");
        formPage.month("5");
        formPage.germanName("Brückentag");
        formPage.save();

        assertThat(page).hasURL(CompanyClosureOverviewPage.url(port));
        assertThat(page).hasTitle(Pattern.compile("^Betriebsruhe"));
        assertThat(overviewPage.closureDays()).containsText("Brückentag");
    }

    @Test
    void ensureClosureDayIsEditedAndCancelLeadsToTheOverviewAfterInvalidSubmits(Page page) {

        final Person office = loginAsOffice(page, "closure.edit@example.org");
        companyClosureService.createCompanyClosure(MonthDay.of(JUNE, 16), Map.of(GERMAN, "Sommerfest"), FULL, LocalDate.now(), office);

        final CompanyClosureOverviewPage overviewPage = new CompanyClosureOverviewPage(page);
        final CompanyClosureFormPage formPage = new CompanyClosureFormPage(page);
        final String editUrl = CompanyClosureFormPage.editUrl(port, "06-16");

        overviewPage.open(port);
        overviewPage.clickEdit("Sommerfest");
        page.waitForURL(editUrl);
        formPage.markDocument();

        formPage.germanName("");
        formPage.save();
        assertThat(formPage.namesError()).isVisible();
        assertThat(page).hasURL(editUrl);

        formPage.save();
        assertThat(formPage.namesError()).isVisible();
        assertThat(page).hasURL(editUrl);

        // the form has been rendered into the page instead of loading it again, with the datepicker still working
        assertThat(formPage.markedDocument()).hasJSProperty("sameDocument", true);
        assertThat(formPage.validFromDatepicker()).hasCount(1);
        assertThat(formPage.validFromFields()).hasCount(1);

        // one step back although the form has been submitted twice
        formPage.cancel();
        assertThat(page).hasURL(CompanyClosureOverviewPage.url(port));
        assertThat(overviewPage.closureDays()).containsText("Sommerfest");

        overviewPage.clickEdit("Sommerfest");
        page.waitForURL(editUrl);

        formPage.germanName("");
        formPage.save();
        assertThat(formPage.namesError()).isVisible();

        formPage.germanName("Betriebsausflug");
        formPage.save();

        assertThat(page).hasURL(CompanyClosureOverviewPage.url(port));
        assertThat(overviewPage.closureDays()).containsText("Betriebsausflug");
        assertThat(overviewPage.closureDays()).not().containsText("Sommerfest");
    }

    private Person loginAsOffice(Page page, String email) {
        final String userId = keycloak.createUser(email, "Olga", "Office", email, email);
        final Person office = personService.create(userId, "Olga", "Office", email, List.of(), List.of(USER, OFFICE));

        new LoginPage(page, port).login(new LoginPage.Credentials(office.getEmail(), office.getEmail()));
        page.waitForURL(OverviewPage.URL_PATTERN);

        return office;
    }
}
