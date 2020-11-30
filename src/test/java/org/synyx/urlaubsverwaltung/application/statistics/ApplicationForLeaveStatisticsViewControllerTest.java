package org.synyx.urlaubsverwaltung.application.statistics;

import liquibase.util.csv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveStatisticsViewControllerTest {

    private ApplicationForLeaveStatisticsViewController sut;

    @Mock
    private ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;
    @Mock
    private ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService;
    @Mock
    private VacationTypeService vacationTypeService;

    @BeforeEach
    void setUp() {

        final DateFormatAware dateFormatAware = new DateFormatAware();

        sut = new ApplicationForLeaveStatisticsViewController(applicationForLeaveStatisticsService,
            applicationForLeaveStatisticsCsvExportService, vacationTypeService, dateFormatAware);
    }

    @Test
    void applicationForLeaveStatisticsRedirectsToStatistics() throws Exception {

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        perform(post("/web/application/statistics")
            .flashAttr("period", filterPeriod))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/statistics?from=2019-01-01&to=2019-08-01"));
    }

    @Test
    void applicationForLeaveStatisticsRedirectsToStatisticsWithNullDates() throws Exception {

        final int year = Year.now(Clock.systemUTC()).getValue();

        final FilterPeriod filterPeriod = new FilterPeriod(null, null);

        perform(post("/web/application/statistics")
            .flashAttr("period", filterPeriod))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/statistics?from=" + year + "-01-01&to=" + year + "-12-31"));
    }

    @Test
    void applicationForLeaveStatisticsAddsErrorToModelAndShowsFormIfPeriodNotTheSameYear() throws Exception {

        perform(get("/web/application/statistics")
            .param("from", "01.01.2019")
            .param("to", "01.08.2020"))
            .andExpect(model().attribute("errors", "INVALID_PERIOD"))
            .andExpect(view().name("application/app_statistics"));
    }

    @Test
    void applicationForLeaveStatisticsSetsModelAndView() throws Exception {

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final List<ApplicationForLeaveStatistics> statistics = emptyList();
        when(applicationForLeaveStatisticsService.getStatistics(filterPeriod)).thenReturn(statistics);

        final List<VacationType> vacationType = Collections.singletonList(new VacationType());
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationType);

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"));

        resultActions
            .andExpect(model().attribute("from", startDate))
            .andExpect(model().attribute("to", endDate))
            .andExpect(model().attribute("statistics", statistics))
            .andExpect(model().attribute("period", filterPeriod))
            .andExpect(model().attribute("vacationTypes", vacationType))
            .andExpect(view().name("application/app_statistics"));
    }

    @Test
    void downloadCSVAddsErrorToModelAndShowsFormIfPeriodNotTheSameYear() throws Exception {

        perform(get("/web/application/statistics/download")
            .param("from", "01.01.2000")
            .param("to", "01.01.2019"))
            .andExpect(model().attribute("errors", "INVALID_PERIOD"))
            .andExpect(view().name("application/app_statistics"));
    }

    @Test
    void downloadCSVSetsDownloadHeaders() throws Exception {

        final String expectedFilename = "filename.csv";
        when(applicationForLeaveStatisticsCsvExportService.getFileName(any(FilterPeriod.class))).thenReturn(expectedFilename);

        perform(get("/web/application/statistics/download")
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"))
            .andExpect(header().string("Content-disposition", "attachment;filename=" + expectedFilename));
    }

    @Test
    void downloadCSVWritesCSV() throws Exception {

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final List<ApplicationForLeaveStatistics> statistics = emptyList();
        when(applicationForLeaveStatisticsService.getStatistics(any(FilterPeriod.class))).thenReturn(statistics);

        perform(get("/web/application/statistics/download")
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"));

        verify(applicationForLeaveStatisticsCsvExportService)
            .writeStatistics(refEq(filterPeriod), eq(statistics), any(CSVWriter.class));
    }

    @Test
    void downloadCSVSetsModelAndView() throws Exception {

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        when(applicationForLeaveStatisticsService.getStatistics(filterPeriod)).thenReturn(emptyList());

        perform(get("/web/application/statistics/download")
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"))
            .andExpect(model().attribute("period", filterPeriod))
            .andExpect(view().name("application/app_statistics"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
