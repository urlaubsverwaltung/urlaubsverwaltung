package org.synyx.urlaubsverwaltung.statistics.web;

import liquibase.util.csv.CSVWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationForLeaveStatisticsViewControllerTest {

    private ApplicationForLeaveStatisticsViewController sut;

    @Mock
    private ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;

    @Mock
    private ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService;

    @Mock
    private VacationTypeService vacationTypeService;

    private FilterPeriod period;

    @Before
    public void setUp() {

        final String startDate = "01.01.2019";
        final String endDate = "01.08.2019";

        period = new FilterPeriod(startDate, endDate);

        sut = new ApplicationForLeaveStatisticsViewController(applicationForLeaveStatisticsService, applicationForLeaveStatisticsCsvExportService, vacationTypeService);
    }

    @Test
    public void applicationForLeaveStatisticsRedirectsToStatistics() throws Exception {

        final String expectedRedirect = "/web/application/statistics?from=" + period.getStartDateAsString() +
            "&to=" + period.getEndDateAsString();

        perform(post("/web/application/statistics")
            .flashAttr("period", period))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", expectedRedirect));
    }

    @Test
    public void applicationForLeaveStatisticsAddsErrorToModelAndShowsFormIfPeriodNotTheSameYear() throws Exception {

        final String startDate = "01.01.2019";
        final String endDate = "01.08.2020";

        FilterPeriod periodWithDifferentYear = new FilterPeriod(startDate, endDate);

        perform(get("/web/application/statistics")
            .param("from", periodWithDifferentYear.getStartDateAsString())
            .param("to", periodWithDifferentYear.getEndDateAsString()))
            .andExpect(model().attribute("errors", "INVALID_PERIOD"))
            .andExpect(view().name("application/app_statistics"));
    }

    @Test
    public void applicationForLeaveStatisticsSetsModelAndView() throws Exception {

        final List<ApplicationForLeaveStatistics> statistics = Collections.emptyList();
        when(applicationForLeaveStatisticsService.getStatistics(refEq(period))).thenReturn(statistics);

        final List<VacationType> vacationType = Collections.singletonList(new VacationType());
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationType);

        perform(get("/web/application/statistics")
            .param("from", period.getStartDateAsString())
            .param("to", period.getEndDateAsString()))
            .andExpect(model().attribute("from", period.getStartDate()))
            .andExpect(model().attribute("to", period.getEndDate()))
            .andExpect(model().attribute("statistics", statistics))
            .andExpect(model().attribute("period", samePropertyValuesAs(period)))
            .andExpect(model().attribute("vacationTypes", vacationType))
            .andExpect(view().name("application/app_statistics"));
    }

    @Test
    public void downloadCSVAddsErrorToModelAndShowsFormIfPeriodNotTheSameYear() throws Exception {

        final String startDate = "01.01.2019";
        final String endDate = "01.08.2020";

        FilterPeriod periodWithDifferentYear = new FilterPeriod(startDate, endDate);

        perform(get("/web/application/statistics/download")
            .param("from", periodWithDifferentYear.getStartDateAsString())
            .param("to", periodWithDifferentYear.getEndDateAsString()))
            .andExpect(model().attribute("errors", "INVALID_PERIOD"))
            .andExpect(view().name("application/app_statistics"));
    }

    @Test
    public void downloadCSVSetsDownloadHeaders() throws Exception {

        final String expectedFilename = "filename.csv";
        when(applicationForLeaveStatisticsCsvExportService.getFileName(any(FilterPeriod.class))).thenReturn(expectedFilename);

        perform(get("/web/application/statistics/download")
            .param("from", period.getStartDateAsString())
            .param("to", period.getEndDateAsString()))
            .andExpect(header().string("Content-disposition", "attachment;filename=" + expectedFilename));
    }

    @Test
    public void downloadCSVWritesCSV() throws Exception {

        final List<ApplicationForLeaveStatistics> statistics = Collections.emptyList();
        when(applicationForLeaveStatisticsService.getStatistics(refEq(period))).thenReturn(statistics);

        perform(get("/web/application/statistics/download")
            .param("from", period.getStartDateAsString())
            .param("to", period.getEndDateAsString()));

        verify(applicationForLeaveStatisticsCsvExportService).writeStatistics(refEq(period), eq(statistics), any(CSVWriter.class));
    }

    @Test
    public void downloadCSVSetsModelAndView() throws Exception {

        when(applicationForLeaveStatisticsService.getStatistics(refEq(period))).thenReturn(Collections.emptyList());

        perform(get("/web/application/statistics/download")
            .param("from", period.getStartDateAsString())
            .param("to", period.getEndDateAsString()))
            .andExpect(model().attribute("period", samePropertyValuesAs(period)))
            .andExpect(view().name("application/app_statistics"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {

        return standaloneSetup(sut).build().perform(builder);
    }

}
