package org.synyx.urlaubsverwaltung.absence.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

@ExtendWith(MockitoExtension.class)
class AbsenceStatisticsViewControllerTest {

    private static final StaticMessageSource MESSAGE_SOURCE = new StaticMessageSource();

    static {
        MESSAGE_SOURCE.addMessage("application.data.vacationType.holiday", Locale.ENGLISH, "Holiday");
    }

    private AbsenceStatisticsViewController sut;

    @Mock
    private AbsenceStatisticsService absenceStatisticsService;
    @Mock
    private PersonService personService;
    @Mock
    private PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    @Mock
    private PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-31T10:15:00Z"), UTC);

    private static List<BigDecimal> zeroMonths() {
        return List.of(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }

    private static VacationType<?> vacationType(long id) {
        return TestDataCreator.createVacationType(id, HOLIDAY, MESSAGE_SOURCE);
    }

    @BeforeEach
    void setUp() {
        sut = new AbsenceStatisticsViewController(absenceStatisticsService, personService,
            defaultPersonSuggestionUrlStrategy, personSearchUiFragmentSupplier, clock);
    }

    @Nested
    class PersonSearch {

        @Test
        void personSearchUiFragmentSupplier() {
            assertThat(sut.personSearchUiFragmentSupplier()).isSameAs(personSearchUiFragmentSupplier);
        }

        @Test
        void returnsInjectedStrategy() {
            assertThat(sut.personSuggestionUrlStrategy()).isSameAs(defaultPersonSuggestionUrlStrategy);
        }
    }

    @Test
    void absenceStatisticsWithYear() throws Exception {

        final Year year = Year.now(clock);

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final VacationDaysTakenResult vacationDaysTaken = new VacationDaysTakenResult(ZERO, ZERO, ZERO, ZERO);
        final AbsenceStatistics selectedYearStatistics = new AbsenceStatistics(year, Map.of(), vacationDaysTaken);
        when(absenceStatisticsService.createStatistics(year, person)).thenReturn(selectedYearStatistics);

        perform(get("/web/absence/statistics")
            .param("year", String.valueOf(year.getValue()))
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYearStatistics", selectedYearStatistics))
            .andExpect(model().attribute("currentYear", year.getValue()))
            .andExpect(view().name("absences/absence_statistics"));
    }

    @Test
    void absenceStatisticsWithoutYear() throws Exception {

        final Year year = Year.now(clock);

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final VacationDaysTakenResult vacationDaysTaken = new VacationDaysTakenResult(ZERO, ZERO, ZERO, ZERO);
        final AbsenceStatistics selectedYearStatistics = new AbsenceStatistics(year, Map.of(), vacationDaysTaken);
        when(absenceStatisticsService.createStatistics(year, person)).thenReturn(selectedYearStatistics);

        perform(get("/web/absence/statistics"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYearStatistics", selectedYearStatistics))
            .andExpect(model().attribute("currentYear", year.getValue()))
            .andExpect(view().name("absences/absence_statistics"));
    }

    @Test
    void ensureAsOfDateIsTodayForTheCurrentYear() throws Exception {

        final Year year = Year.now(clock);

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final VacationDaysTakenResult vacationDaysTaken = new VacationDaysTakenResult(ZERO, ZERO, ZERO, ZERO);
        when(absenceStatisticsService.createStatistics(year, person))
            .thenReturn(new AbsenceStatistics(year, Map.of(), vacationDaysTaken));

        perform(get("/web/absence/statistics"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("asOfDate", LocalDate.of(2026, 7, 31)));
    }

    @Test
    void ensureAsOfDateIsTodayForAPastYear() throws Exception {

        // the date says when the figures were calculated, not which period they cover, therefore it stays today
        // even when an already finished year is shown

        final Year year = Year.of(2024);

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final VacationDaysTakenResult vacationDaysTaken = new VacationDaysTakenResult(ZERO, ZERO, ZERO, ZERO);
        when(absenceStatisticsService.createStatistics(year, person))
            .thenReturn(new AbsenceStatistics(year, Map.of(), vacationDaysTaken));

        perform(get("/web/absence/statistics").param("year", "2024"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("asOfDate", LocalDate.of(2026, 7, 31)));
    }

    @Test
    void graphDtoContainsTypesSortedDescendingByYearSumAndTheRingPercentage() throws Exception {

        final Year year = Year.now(clock);

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final VacationType<?> smallType = vacationType(1000L);
        final VacationType<?> bigType = vacationType(2000L);

        final MonthlyAbsenceDaysByType smallTypeDays = new MonthlyAbsenceDaysByType(zeroMonths(), new BigDecimal("5"));
        final MonthlyAbsenceDaysByType bigTypeDays = new MonthlyAbsenceDaysByType(zeroMonths(), new BigDecimal("20"));

        // insertion order deliberately the opposite of the expected (sorted) order
        final Map<VacationType<?>, MonthlyAbsenceDaysByType> monthlyAbsenceDaysByType =
            Map.of(smallType, smallTypeDays, bigType, bigTypeDays);

        final VacationDaysTakenResult vacationDaysTaken = new VacationDaysTakenResult(ZERO, ZERO, new BigDecimal("67"), ZERO);
        final AbsenceStatistics selectedYearStatistics = new AbsenceStatistics(year, monthlyAbsenceDaysByType, vacationDaysTaken);
        when(absenceStatisticsService.createStatistics(year, person)).thenReturn(selectedYearStatistics);

        final MvcResult result = perform(get("/web/absence/statistics")).andExpect(status().isOk()).andReturn();

        final AbsenceStatisticsViewController.GraphDto graphDto =
            (AbsenceStatisticsViewController.GraphDto) result.getModelAndView().getModel().get("absenceGraphStatistic");

        assertThat(graphDto.vacationDaysTakenPercentage()).isEqualByComparingTo("67");
        assertThat(graphDto.types()).hasSize(2);
        assertThat(graphDto.types().get(0).yearSum()).isEqualByComparingTo("20");
        assertThat(graphDto.types().get(1).yearSum()).isEqualByComparingTo("5");
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
