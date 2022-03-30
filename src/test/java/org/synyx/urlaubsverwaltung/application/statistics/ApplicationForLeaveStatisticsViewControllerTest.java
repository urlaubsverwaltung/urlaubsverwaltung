package org.synyx.urlaubsverwaltung.application.statistics;

import liquibase.util.csv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
import static org.synyx.urlaubsverwaltung.application.statistics.ApplicationForLeaveStatisticsViewController.UTF8_BOM;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

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
        sut = new ApplicationForLeaveStatisticsViewController(applicationForLeaveStatisticsService,
            applicationForLeaveStatisticsCsvExportService, vacationTypeService, new DateFormatAware());
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

    private static Stream<Arguments> dateInputAndIsoDateTuple() {
        return Stream.of(
            Arguments.of("25.03.2022", "2022-03-25"),
            Arguments.of("25.03.22", "2022-03-25"),
            Arguments.of("25.3.2022", "2022-03-25"),
            Arguments.of("25.3.22", "2022-03-25"),
            Arguments.of("1.4.22", "2022-04-01")
        );
    }

    @ParameterizedTest
    @MethodSource("dateInputAndIsoDateTuple")
    void applicationForLeaveStatisticsRedirectsToStatisticsAfterIncorrectPeriodForStartDate(String givenDate, String givenIsoDate) throws Exception {

        perform(post("/web/application/statistics")
            .param("startDate", givenDate))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/statistics?from=" + givenIsoDate + "&to=2022-12-31"));
    }

    @ParameterizedTest
    @MethodSource("dateInputAndIsoDateTuple")
    void applicationForLeaveStatisticsRedirectsToStatisticsAfterIncorrectPeriodForEndDate(String givenDate, String givenIsoDate) throws Exception {

        perform(post("/web/application/statistics")
            .param("endDate", givenDate))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/application/statistics?from=2022-01-01&to=" + givenIsoDate));
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
    void applicationForLeaveStatisticsSetsModelAndViewWithoutStatistics() throws Exception {

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final List<ApplicationForLeaveStatistics> statistics = emptyList();
        when(applicationForLeaveStatisticsService.getStatistics(filterPeriod)).thenReturn(statistics);

        final List<VacationType> vacationType = List.of(new VacationType(1, true, HOLIDAY, "message_key", true));
        when(vacationTypeService.getAllVacationTypes()).thenReturn(vacationType);

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"));

        resultActions
            .andExpect(model().attribute("from", startDate))
            .andExpect(model().attribute("to", endDate))
            .andExpect(model().attribute("statistics", statistics))
            .andExpect(model().attribute("showPersonnelNumberColumn", false))
            .andExpect(model().attribute("period", filterPeriod))
            .andExpect(model().attribute("vacationTypes", vacationType))
            .andExpect(view().name("application/app_statistics"));
    }

    @Test
    void applicationForLeaveStatisticsSetsModelAndViewWithStatistics() throws Exception {

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person person = new Person();
        person.setId(1);

        final ApplicationForLeaveStatistics applicationForLeaveStatistics = new ApplicationForLeaveStatistics(person);
        applicationForLeaveStatistics.setPersonBasedata(new PersonBasedata(1, "42", null));

        final List<ApplicationForLeaveStatistics> statistics = List.of(applicationForLeaveStatistics);
        when(applicationForLeaveStatisticsService.getStatistics(filterPeriod)).thenReturn(statistics);

        final List<VacationType> vacationType = List.of(new VacationType(1, true, HOLIDAY, "message_key", true));
        when(vacationTypeService.getAllVacationTypes()).thenReturn(vacationType);

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"));

        resultActions
            .andExpect(model().attribute("from", startDate))
            .andExpect(model().attribute("to", endDate))
            .andExpect(model().attribute("statistics", hasSize(1)))
            .andExpect(model().attribute("statistics", contains(hasProperty("personnelNumber", is("42")))))
            .andExpect(model().attribute("showPersonnelNumberColumn", true))
            .andExpect(model().attribute("period", filterPeriod))
            .andExpect(model().attribute("vacationTypes", vacationType))
            .andExpect(view().name("application/app_statistics"));
    }

    @Test
    void downloadCSVReturnsBadRequestIfPeriodNotTheSameYear() throws Exception {

        perform(get("/web/application/statistics/download")
            .param("from", "01.01.2000")
            .param("to", "01.01.2019"))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"25.03.2022", "25.03.22", "25.3.2022", "25.3.22", "1.4.22"})
    void downloadCSVSetsDownloadHeaders(String givenDate) throws Exception {

        final String expectedFilename = "filename.csv";
        when(applicationForLeaveStatisticsCsvExportService.getFileName(any(FilterPeriod.class))).thenReturn(expectedFilename);

        perform(get("/web/application/statistics/download")
            .param("from", givenDate)
            .param("to", givenDate))
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
    void downloadCSVContainsUTF8BOM() throws Exception {

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        when(applicationForLeaveStatisticsService.getStatistics(filterPeriod)).thenReturn(emptyList());

        byte[] response = perform(get("/web/application/statistics/download")
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

        assertThat(response).contains(UTF8_BOM);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
