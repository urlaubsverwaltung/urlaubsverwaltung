package org.synyx.urlaubsverwaltung.sicknote.statistics;

import liquibase.util.csv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsViewControllerTest {

    protected static final byte[] UTF8_BOM = new byte[]{(byte) 239, (byte) 187, (byte) 191};

    private SickNoteStatisticsViewController sut;

    @Mock
    private SickNoteStatisticsService sickNoteStatisticsService;
    @Mock
    private SickNoteDetailedStatisticsService sickNoteDetailedStatisticsService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private SickNoteDetailedStatisticsCsvExportService sickNoteDetailedStatisticsCsvExportService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SickNoteStatisticsViewController(sickNoteStatisticsService, sickNoteDetailedStatisticsService, sickNoteDetailedStatisticsCsvExportService, new DateFormatAware(), clock);
    }

    @Test
    void sickNoteStatistics() throws Exception {

        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(clock, sickNoteService, workDaysCountService);
        when(sickNoteStatisticsService.createStatistics(any(Clock.class))).thenReturn(sickNoteStatistics);

        final ResultActions resultActions = perform(get("/web/sicknote/statistics")
            .param("year", String.valueOf(Year.now(clock).getValue())));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("statistics", sickNoteStatistics));
        resultActions.andExpect(model().attribute("selectedYear", LocalDate.now(clock).getYear()));
        resultActions.andExpect(view().name("thymeleaf/sicknote/sick_notes_statistics"));
    }

    @Test
    void sickNoteStatisticsWithoutYear() throws Exception {

        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(clock, sickNoteService, workDaysCountService);
        when(sickNoteStatisticsService.createStatistics(any(Clock.class))).thenReturn(sickNoteStatistics);

        final ResultActions resultActions = perform(get("/web/sicknote/statistics"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("statistics", sickNoteStatistics));
        resultActions.andExpect(model().attribute("selectedYear", nullValue()));
        resultActions.andExpect(view().name("thymeleaf/sicknote/sick_notes_statistics"));
    }

    @Test
    void downloadCSVReturnsBadRequestIfPeriodNotTheSameYear() throws Exception {

        perform(get("/web/sicknote/statistics/download")
            .param("from", "01.01.2000")
            .param("to", "01.01.2019"))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"25.03.2022", "25.03.22", "25.3.2022", "25.3.22", "1.4.22"})
    void downloadCSVSetsDownloadHeaders(String givenDate) throws Exception {

        final String expectedFilename = "filename.csv";
        when(sickNoteDetailedStatisticsCsvExportService.getFileName(any(FilterPeriod.class))).thenReturn(expectedFilename);

        perform(get("/web/sicknote/statistics/download")
            .param("from", givenDate)
            .param("to", givenDate))
            .andExpect(header().string("Content-disposition", "attachment;filename=" + expectedFilename));
    }

    @Test
    void downloadCSVWritesCSV() throws Exception {

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final List<SickNoteDetailedStatistics> statistics = emptyList();
        when(sickNoteDetailedStatisticsService.getAllSicknotes(any(FilterPeriod.class))).thenReturn(statistics);

        perform(get("/web/sicknote/statistics/download")
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"));

        verify(sickNoteDetailedStatisticsCsvExportService)
            .writeStatistics(refEq(filterPeriod), eq(statistics), any(CSVWriter.class));
    }

    @Test
    void downloadCSVContainsUTF8BOM() throws Exception {

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        when(sickNoteDetailedStatisticsService.getAllSicknotes(filterPeriod)).thenReturn(emptyList());

        byte[] response = perform(get("/web/sicknote/statistics/download")
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        assertThat(response).contains(UTF8_BOM);
    }


    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
