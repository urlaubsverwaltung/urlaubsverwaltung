package org.synyx.urlaubsverwaltung.absence.statistics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

/**
 * Verifies that {@code @PreAuthorize} on {@link AbsenceStatisticsViewController} actually blocks the request
 * before it reaches the controller - {@code standaloneSetup}-based {@link AbsenceStatisticsViewControllerTest}
 * cannot prove this, since it never wires in Spring Security.
 */
@SpringBootTest
class AbsenceStatisticsViewControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private AbsenceStatisticsService absenceStatisticsService;

    @Test
    void ensureYearWithDataRendersSuccessfully() throws Exception {

        final Person person = new Person("user", "Reichenbach", "Marie", "person@example.org");
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);

        final StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("application.data.vacationType.holiday", Locale.ENGLISH, "Testurlaub");
        final VacationType<?> vacationType = TestDataCreator.createVacationType(1000L, HOLIDAY, messageSource);

        final List<BigDecimal> daysByMonth = List.of(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, TEN);
        final MonthlyAbsenceDaysByType monthlyAbsenceDaysByType = new MonthlyAbsenceDaysByType(daysByMonth, TEN);
        final VacationDaysTakenResult vacationDaysTaken =
            new VacationDaysTakenResult(TEN, BigDecimal.valueOf(40), BigDecimal.valueOf(25), ZERO);

        final AbsenceStatistics statistics = new AbsenceStatistics(Year.of(2024), Map.of(vacationType, monthlyAbsenceDaysByType), vacationDaysTaken);
        when(absenceStatisticsService.createStatistics(any(), eq(person))).thenReturn(statistics);

        perform(get("/web/absence/statistics").param("year", "2024")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("OFFICE")))
        )
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Testurlaub")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "INACTIVE"})
    void ensureNoAccessForRolesWithoutPermission(final String role) throws Exception {

        perform(get("/web/absence/statistics")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority(role)))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(personService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "BOSS", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void ensureAccessForPermittedRoles(final String role) throws Exception {

        final Person person = new Person("user", "Reichenbach", "Marie", "person@example.org");
        person.setId(1L);
        when(personService.getSignedInUser()).thenReturn(person);

        final VacationDaysTakenResult emptyVacationDaysTaken = new VacationDaysTakenResult(ZERO, ZERO, ZERO, ZERO);
        when(absenceStatisticsService.createStatistics(any(), eq(person)))
            .thenReturn(new AbsenceStatistics(Year.now(), Map.of(), emptyVacationDaysTaken));

        perform(get("/web/absence/statistics")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isOk())
            // proves the page is actually rendered, including the year selector fragment and the mount
            // points of the three charts
            .andExpect(content().string(containsString("/web/absence/statistics?year=")))
            .andExpect(content().string(containsString("id=\"monthly-chart\"")))
            .andExpect(content().string(containsString("id=\"distribution-chart\"")))
            .andExpect(content().string(containsString("id=\"vacation-ring\"")));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
