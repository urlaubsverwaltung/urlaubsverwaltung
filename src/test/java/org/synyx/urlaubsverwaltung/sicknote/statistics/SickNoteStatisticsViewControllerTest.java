package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToSunday;

@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsViewControllerTest {

    private SickNoteStatisticsViewController sut;

    @Mock
    private SickNoteStatisticsService statisticsService;
    @Mock
    private PersonService personService;
    @Mock
    private PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    @Mock
    private PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SickNoteStatisticsViewController(statisticsService, personService, defaultPersonSuggestionUrlStrategy,
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
    void sickNoteStatistics() throws Exception {

        final Year year = Year.now(clock);

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final SickNoteStatistics selectedYearStatistics = new SickNoteStatistics(year, LocalDate.now(clock), List.of(), List.of());
        when(statisticsService.createStatisticsForPerson(year, person)).thenReturn(selectedYearStatistics);

        final Year previousSelectedYear = year.minusYears(1);
        final SickNoteStatistics previousSelectedYearStatistics = new SickNoteStatistics(previousSelectedYear, LocalDate.now(clock), List.of(), List.of());
        when(statisticsService.createStatisticsForPerson(previousSelectedYear, person)).thenReturn(previousSelectedYearStatistics);

        perform(get("/web/sicknote/statistics")
            .param("year", String.valueOf(year.getValue()))
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYearStatistics", selectedYearStatistics))
            .andExpect(model().attribute("previousSelectedYearStatistics", previousSelectedYearStatistics))
            .andExpect(model().attribute("currentYear", year.getValue()))
            .andExpect(view().name("sicknote/sick_notes_statistics"));
    }

    @Test
    void sickNoteStatisticsWithoutYear() throws Exception {

        final Year year = Year.now(clock);

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);
        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(year, LocalDate.now(clock), List.of(), List.of());
        when(statisticsService.createStatisticsForPerson(year, person)).thenReturn(sickNoteStatistics);

        final Year previousSelectedYear = year.minusYears(1);
        final SickNoteStatistics previousSelectedYearStatistics = new SickNoteStatistics(previousSelectedYear, LocalDate.now(clock), List.of(), List.of());
        when(statisticsService.createStatisticsForPerson(previousSelectedYear, person)).thenReturn(previousSelectedYearStatistics);

        perform(get("/web/sicknote/statistics"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYearStatistics", sickNoteStatistics))
            .andExpect(model().attribute("previousSelectedYearStatistics", previousSelectedYearStatistics))
            .andExpect(model().attribute("currentYear", year.getValue()))
            .andExpect(view().name("sicknote/sick_notes_statistics"));
    }

    @Test
    void sickNoteStatisticsProvidesSickRateDataSeries() throws Exception {

        final Year year = Year.now(clock);

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final LocalDate from = year.atDay(1);
        final LocalDate to = year.atMonth(12).atEndOfMonth();
        final LocalDate sickNoteStart = year.atMonth(1).atDay(10);
        final LocalDate sickNoteEnd = year.atMonth(1).atDay(11);

        // only the two sick note dates are target work days --> sick rate for January is 100%
        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(from, to,
            date -> date.equals(sickNoteStart) || date.equals(sickNoteEnd));

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .startDate(sickNoteStart)
            .endDate(sickNoteEnd)
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final SickNoteStatistics selectedYearStatistics =
            new SickNoteStatistics(year, LocalDate.now(clock), List.of(sickNote), List.of(person), Map.of(person, workingTimeCalendar));
        when(statisticsService.createStatisticsForPerson(year, person)).thenReturn(selectedYearStatistics);

        final Year previousSelectedYear = year.minusYears(1);
        final SickNoteStatistics previousSelectedYearStatistics = new SickNoteStatistics(previousSelectedYear, LocalDate.now(clock), List.of(), List.of());
        when(statisticsService.createStatisticsForPerson(previousSelectedYear, person)).thenReturn(previousSelectedYearStatistics);

        final MvcResult result = perform(get("/web/sicknote/statistics")
            .param("year", String.valueOf(year.getValue()))
        )
            .andExpect(status().isOk())
            .andReturn();

        final SickNoteStatisticsViewController.GraphDto graphDto =
            (SickNoteStatisticsViewController.GraphDto) result.getModelAndView().getModel().get("sickNoteGraphStatistic");

        assertThat(graphDto.sickRateDataSeries()).hasSize(2);

        final SickNoteStatisticsViewController.DataSeries currentYearRate = graphDto.sickRateDataSeries().get(0);
        assertThat(currentYearRate.year()).isEqualTo(year.getValue());
        assertThat(currentYearRate.data().get(0)).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(currentYearRate.data().subList(1, 12)).allMatch(rate -> rate.compareTo(BigDecimal.ZERO) == 0);

        final SickNoteStatisticsViewController.DataSeries previousYearRate = graphDto.sickRateDataSeries().get(1);
        assertThat(previousYearRate.year()).isEqualTo(previousSelectedYear.getValue());
        assertThat(previousYearRate.data()).allMatch(rate -> rate.compareTo(BigDecimal.ZERO) == 0);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
