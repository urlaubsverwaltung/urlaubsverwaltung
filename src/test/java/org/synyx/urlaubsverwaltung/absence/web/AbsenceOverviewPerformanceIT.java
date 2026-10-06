package org.synyx.urlaubsverwaltung.absence.web;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Guards the database round trips of the absence overview for many persons over a whole year and logs the time a
 * request takes. Only the statement count is asserted: locally the database runs on loopback, so wall time hides
 * round trips that cost seconds against a networked database.
 */
// the page is large - without buffering the whole render, the response is already committed
// when Thymeleaf asks for the session to write the CSRF token
@SpringBootTest(properties = "spring.thymeleaf.servlet.produce-partial-output-while-processing=false")
@AutoConfigureMockMvc
@Transactional
class AbsenceOverviewPerformanceIT extends SingleTenantTestContainersBase {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final int PERSONS = 100;
    // measured before the absence bars - the bars must not add round trips
    private static final long MAX_STATEMENTS_PER_REQUEST = 125;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PersonService personService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private VacationTypeService vacationTypeService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void absenceOverviewOfAWholeYearForManyPersons() throws Exception {

        final Person office = personService.create("perf-office", "Olivia", "Office", "perf-office@example.org", List.of(), List.of(USER, OFFICE));
        final VacationType<?> vacationType = vacationTypeService.getActiveVacationTypes().getFirst();

        for (int i = 0; i < PERSONS; i++) {
            final Person person = personService.create("perf-" + i, "Person" + i, "Perf", "perf-" + i + "@example.org", List.of(), List.of(USER));
            for (int month = 1; month <= 12; month++) {
                saveAllowedApplication(person, vacationType, LocalDate.of(2026, month, 5), LocalDate.of(2026, month, 9));
            }
        }
        entityManager.flush();

        final Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        final List<Long> statementCounts = new ArrayList<>();
        final List<Long> millis = new ArrayList<>();
        for (int run = 0; run < 10; run++) {
            entityManager.clear();
            statistics.clear();
            final long start = System.nanoTime();
            mockMvc.perform(get("/web/absences").param("year", "2026").param("month", "")
                    .with(csrf())
                    .with(oidcSubject(office, List.of(USER, OFFICE))))
                .andExpect(status().isOk());
            millis.add((System.nanoTime() - start) / 1_000_000);
            statementCounts.add(statistics.getPrepareStatementCount());
        }

        // the first three runs warm up caches and the JIT
        final List<Long> measured = millis.subList(3, millis.size()).stream().sorted().toList();
        LOG.info("absence overview, {} persons, whole year: {} statements, median {} ms",
            PERSONS + 1, statementCounts.getLast(), measured.get(measured.size() / 2));

        assertThat(statementCounts.getLast()).isLessThanOrEqualTo(MAX_STATEMENTS_PER_REQUEST);
    }

    private void saveAllowedApplication(Person person, VacationType<?> vacationType, LocalDate from, LocalDate to) {
        final Application application = new Application();
        application.setPerson(person);
        application.setApplier(person);
        application.setVacationType(vacationType);
        application.setStartDate(from);
        application.setEndDate(to);
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);
        applicationService.save(application);
    }

    private static OidcLoginRequestPostProcessor oidcSubject(Person person, List<Role> roles) {
        final OidcIdToken.Builder tokenBuilder = OidcIdToken.withTokenValue("not-empty-token-value")
            .claim("sub", person.getUsername());
        final List<SimpleGrantedAuthority> authorities = roles.stream().map(Role::name).map(SimpleGrantedAuthority::new).toList();
        return oidcLogin().oidcUser(new DefaultOidcUser(authorities, tokenBuilder.build()));
    }
}
