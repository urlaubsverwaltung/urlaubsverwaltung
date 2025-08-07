package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsViewControllerTest {

    private SickNoteStatisticsViewController sut;

    @Mock
    private SickNoteStatisticsService statisticsService;
    @Mock
    private PersonService personService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SickNoteStatisticsViewController(statisticsService, personService, clock);
    }

    @Test
    void sickNoteStatistics() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final Year currentYear = Year.now(clock);
        final SickNoteStatistics selectedYearStatistics = new SickNoteStatistics(currentYear, LocalDate.now(clock), List.of(), List.of());
        when(statisticsService.createStatisticsForPerson(person, currentYear)).thenReturn(selectedYearStatistics);

        final Year previousSelectedYear = currentYear.minusYears(1);
        final SickNoteStatistics previousSelectedYearStatistics = new SickNoteStatistics(previousSelectedYear, LocalDate.now(clock), List.of(), List.of());
        when(statisticsService.createStatisticsForPerson(person, previousSelectedYear)).thenReturn(previousSelectedYearStatistics);

        final int currentYearValue = currentYear.getValue();
        final ResultActions resultActions = perform(get("/web/sicknote/statistics")
            .param("year", String.valueOf(currentYearValue)));
        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYearStatistics", selectedYearStatistics))
            .andExpect(model().attribute("previousSelectedYearStatistics", previousSelectedYearStatistics))
            .andExpect(model().attribute("currentYear", currentYearValue))
            .andExpect(view().name("sicknote/sick_notes_statistics"));
    }

    @Test
    void sickNoteStatisticsWithoutYear() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final Year currentYear = Year.now(clock);
        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(currentYear, LocalDate.now(clock), List.of(), List.of());
        when(statisticsService.createStatisticsForPerson(person, currentYear)).thenReturn(sickNoteStatistics);

        final Year previousSelectedYear = currentYear.minusYears(1);
        final SickNoteStatistics previousSelectedYearStatistics = new SickNoteStatistics(previousSelectedYear, LocalDate.now(clock), List.of(), List.of());
        when(statisticsService.createStatisticsForPerson(person, previousSelectedYear)).thenReturn(previousSelectedYearStatistics);

        final int currentYearValue = Year.now(clock).getValue();
        final ResultActions resultActions = perform(get("/web/sicknote/statistics"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYearStatistics", sickNoteStatistics))
            .andExpect(model().attribute("currentYear", currentYearValue))
            .andExpect(view().name("sicknote/sick_notes_statistics"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
