package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.csv.CSVFile;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsViewControllerTest {

    private static final byte[] UTF8_BOM = new byte[]{(byte) 239, (byte) 187, (byte) 191};

    private SickNoteStatisticsViewController sut;

    @Mock
    private SickNoteStatisticsService statisticsService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private PersonService personService;
    @Mock
    private SickNoteDetailedStatisticsCsvExportService sickNoteDetailedStatisticsCsvExportService;
    @Mock
    private DateFormatAware dateFormatAware;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SickNoteStatisticsViewController(statisticsService, sickNoteDetailedStatisticsCsvExportService,
            personService, dateFormatAware, clock);
    }

    @Test
    void sickNoteStatistics() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(clock, List.of(), workDaysCountService);
        when(statisticsService.createStatisticsForPerson(eq(person), any(Clock.class))).thenReturn(sickNoteStatistics);

        final int currentYear = Year.now(clock).getValue();
        final ResultActions resultActions = perform(get("/web/sicknote/statistics")
            .param("year", String.valueOf(currentYear)));
        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("statistics", sickNoteStatistics))
            .andExpect(model().attribute("currentYear", currentYear))
            .andExpect(view().name("thymeleaf/sicknote/sick_notes_statistics"));
    }

    @Test
    void sickNoteStatisticsWithoutYear() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(clock, List.of(), workDaysCountService);
        when(statisticsService.createStatisticsForPerson(eq(person), any(Clock.class))).thenReturn(sickNoteStatistics);

        final int currentYear = Year.now(clock).getValue();

        final ResultActions resultActions = perform(get("/web/sicknote/statistics"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("statistics", sickNoteStatistics))
            .andExpect(model().attribute("currentYear", currentYear))
            .andExpect(view().name("thymeleaf/sicknote/sick_notes_statistics"));
    }

    @Test
    void downloadCSVReturnsBadRequestIfPeriodNotTheSameYear() throws Exception {

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2018-08-01");

        final String fromString = "01.01.2019";
        when(dateFormatAware.parse(fromString)).thenReturn(Optional.of(startDate));
        final String endString = "01.08.2018";
        when(dateFormatAware.parse(endString)).thenReturn(Optional.of(endDate));

        perform(get("/web/sicknote/statistics/download")
            .param("from", fromString)
            .param("to", endString))
            .andExpect(status().isBadRequest());
    }

    @Test
    void downloadCSVSetsDownloadHeaders() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final String dateString = "2022-05-10";
        final LocalDate date = LocalDate.parse(dateString);
        final FilterPeriod filterPeriod = new FilterPeriod(date, date);

        when(dateFormatAware.parse(dateString)).thenReturn(Optional.of(date));
        when(dateFormatAware.parse(dateString)).thenReturn(Optional.of(date));

        final List<SickNoteDetailedStatistics> statistics = emptyList();
        when(statisticsService.getAllSickNotes(signedInUser, date, date)).thenReturn(statistics);
        when(sickNoteDetailedStatisticsCsvExportService.generateCSV(filterPeriod, statistics)).thenReturn(new CSVFile("filename.csv", new ByteArrayResource(new byte[]{})));

        perform(get("/web/sicknote/statistics/download")
            .param("from", dateString)
            .param("to", dateString))
            .andExpect(header().string("Content-disposition", "attachment; filename=\"filename.csv\""))
            .andExpect(header().string("Content-Type", "text/csv"));
    }

    @Test
    void downloadCSVWritesCSV() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final String fromString = "01.01.2019";
        when(dateFormatAware.parse(fromString)).thenReturn(Optional.of(startDate));
        final String endString = "01.08.2019";
        when(dateFormatAware.parse(endString)).thenReturn(Optional.of(endDate));

        final List<SickNoteDetailedStatistics> statistics = emptyList();
        when(statisticsService.getAllSickNotes(signedInUser, startDate, endDate)).thenReturn(statistics);
        when(sickNoteDetailedStatisticsCsvExportService.generateCSV(filterPeriod, statistics)).thenReturn(new CSVFile("filename.csv", new ByteArrayResource(new byte[]{})));

        perform(get("/web/sicknote/statistics/download")
            .param("from", fromString)
            .param("to", endString)).andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
