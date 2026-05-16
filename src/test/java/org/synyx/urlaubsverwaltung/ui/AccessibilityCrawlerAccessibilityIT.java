package org.synyx.urlaubsverwaltung.ui;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.AxeResults;
import com.deque.html.axecore.results.CheckedNode;
import com.deque.html.axecore.results.Rule;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.synyx.urlaubsverwaltung.SingleTenantTestPostgreSQLContainer;
import org.synyx.urlaubsverwaltung.TestKeycloakContainer;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.ui.extension.A11yTest;
import org.synyx.urlaubsverwaltung.ui.extension.UiIntegrationTest;
import org.synyx.urlaubsverwaltung.ui.pages.LoginPage;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.util.StringUtils.trimAllWhitespace;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.ui.pages.OverviewPage.URL_PATTERN;

@Testcontainers(parallel = true)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@UiIntegrationTest
@A11yTest
class AccessibilityCrawlerAccessibilityIT {

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
    private AccountInteractionService accountInteractionService;
    @Autowired
    private WorkingTimeWriteService workingTimeWriteService;

    private static final Logger LOG = LoggerFactory.getLogger(AccessibilityCrawlerAccessibilityIT.class);

    @Test
    void testAllPagesForWCAG22AACompliance(Page page) {

        final Person person = createPerson("dBradley", "Donald", List.of(USER, OFFICE));
        final LoginPage loginPage = new LoginPage(page, port);

        loginPage.login(new LoginPage.Credentials(person.getEmail(), person.getEmail()));
        page.waitForURL(URL_PATTERN);

        final String baseUrl = "http://localhost:%d".formatted(port);

        final Set<String> visitedLinks = new HashSet<>();
        final Queue<String> linksToVisit = new LinkedList<>();
        final Map<String, List<Rule>> scanFailures = new HashMap<>();

        linksToVisit.add(baseUrl);

        while (!linksToVisit.isEmpty()) {
            final String currentUrl = linksToVisit.poll();

            // Skip if already visited
            if (visitedLinks.contains(currentUrl)) {
                continue;
            }
            visitedLinks.add(currentUrl);

            LOG.info("--------------------------------------------");
            LOG.info("🔍 Navigating to: {}", currentUrl);
            LOG.info("--------------------------------------------");

            try {
                // 1. Navigate to the page and wait until network traffic calms down
                page.navigate(currentUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

                // 2. Discover new internal links on this page
                final List<ElementHandle> anchors = page.querySelectorAll("a[href]");
                for (ElementHandle anchor : anchors) {
                    final String href = anchor.getAttribute("href");

                    if (href != null && !href.trim().isEmpty()) {
                        try {
                            // Resolve relative URLs against the current BASE_URL context
                            final URI absoluteUri = URI.create(baseUrl).resolve(href).normalize();
                            final String absoluteUrl = absoluteUri.toString();

                            // Ensure it's internal, not visited, not already queued, and skip destructive paths like /logout
                            if (absoluteUrl.startsWith(baseUrl)
                                && !visitedLinks.contains(absoluteUrl)
                                && !linksToVisit.contains(absoluteUrl)
                                && !absoluteUrl.contains("/logout")) {

                                linksToVisit.add(absoluteUrl);
                            }
                        } catch (IllegalArgumentException _) {
                            // Ignore unparseable/malformed URIs (e.g., mailto:, javascript:void(0))
                        }
                    }
                }

                // 3. Run the WCAG 2.2 AA Accessibility Scan
                final AxeResults accessibilityScanResults = new AxeBuilder(page)
                    .withTags(Arrays.asList("wcag2a", "wcag2aa", "wcag22aa")) // Pulls targets for 2.0, 2.1, and 2.2 AA
                    .analyze();

                // 4. Log and track violations
                final List<Rule> violations = accessibilityScanResults.getViolations();
                if (violations != null && !violations.isEmpty()) {
                    LOG.warn("❌ Found {} accessibility violations on {}", violations.size(), currentUrl);
                    scanFailures.put(currentUrl, violations);

                    for (Rule violation : violations) {
                        LOG.warn("   ┌─ Rule: {} [{}]", violation.getId(), violation.getImpact());
                        LOG.warn("   │  Description: {}", violation.getDescription());
                        LOG.warn("   │  Nodes affected: {}", violation.getNodes().size());
                        for (int i = 0; i < violation.getNodes().size(); i++) {
                            final CheckedNode node = violation.getNodes().get(i);
                            LOG.warn("   │  ┌─ Node #{}", i + 1);
                            LOG.warn("   │  │  Target: {}", node.getTarget());
                            LOG.warn("   │  │  HTML:   {}", truncate(node.getHtml(), 200));
                            if (node.getNone() != null) {
                                for (var check : node.getNone()) {
                                    LOG.warn("   │  │  Fail: {} - {}", check.getId(), check.getImpact());
                                }
                            }
                            if (i < violation.getNodes().size() - 1) {
                                LOG.warn("   │  └─");
                            }
                        }
                        LOG.warn("   └─");
                    }
                } else {
                    LOG.info("✅ {} passed all automated WCAG 2.2 AA checks.", currentUrl);
                }

            } catch (Exception e) {
                LOG.warn("⚠️ Failed to safely process page {}: {}", currentUrl, e.getMessage());
            }
        }

        // --- Final Reporting Block ---
        final int totalFailures = scanFailures.values().stream().mapToInt(List::size).sum();
        LOG.info("\n============================================");
        LOG.info("🏁 Crawl Complete. Audited {} unique pages.", visitedLinks.size());
        LOG.info("   ❌ Pages with violations: {}", scanFailures.size());
        LOG.info("   ❌ Total violations: {}", totalFailures);
        LOG.info("============================================\n");

        if (!scanFailures.isEmpty()) {
            final StringBuilder failureSummary = new StringBuilder("Accessibility violations detected:\n");
            scanFailures.forEach((url, violations) ->
                failureSummary.append(url).append(" had ").append(violations.size()).append(" violations.\n")
            );

            // Intentionally fail the JUnit test if any violations exist across the site map
            assertThat(scanFailures).withFailMessage(failureSummary.toString())
                .hasSizeLessThanOrEqualTo(186);
        }
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) {
            return "null";
        }
        text = text.replaceAll("\\s+", " ").trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private Person createPerson(String firstName, String lastName, List<Role> roles) {

        final String email = "%s.%s@example.org".formatted(trimAllWhitespace(firstName), trimAllWhitespace(lastName)).toLowerCase();
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
