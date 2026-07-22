package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class OvertimeStatisticsViewControllerTest {

    private OvertimeStatisticsViewController sut;

    @Mock
    private OvertimeStatisticsService overtimeStatisticsService;
    @Mock
    private PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    @Mock
    private PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new OvertimeStatisticsViewController(overtimeStatisticsService, defaultPersonSuggestionUrlStrategy,
            personSearchUiFragmentSupplier, clock);
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
    void overtimeStatistics() throws Exception {

        final Year year = Year.now(clock);

        final OvertimeStatistics statistics = new OvertimeStatistics(year, LocalDate.now(clock), List.of(), 0);
        when(overtimeStatisticsService.createStatistics(year)).thenReturn(statistics);

        perform(get("/web/overtime/statistics")
            .param("year", String.valueOf(year.getValue()))
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("statistics", statistics))
            .andExpect(model().attribute("currentYear", year.getValue()))
            .andExpect(view().name("overtime/overtime_statistics"));
    }

    @Test
    void overtimeStatisticsWithoutYear() throws Exception {

        final Year year = Year.now(clock);

        final OvertimeStatistics statistics = new OvertimeStatistics(year, LocalDate.now(clock), List.of(), 0);
        when(overtimeStatisticsService.createStatistics(year)).thenReturn(statistics);

        perform(get("/web/overtime/statistics"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("statistics", statistics))
            .andExpect(model().attribute("currentYear", year.getValue()))
            .andExpect(view().name("overtime/overtime_statistics"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
