package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.csv.CSVFile;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveStatisticsViewControllerTest {

    private ApplicationForLeaveStatisticsViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private ApplicationForLeaveStatisticsService applicationForLeaveStatisticsService;
    @Mock
    private ApplicationForLeaveStatisticsCsvExportService applicationForLeaveStatisticsCsvExportService;
    @Mock
    private VacationTypeService vacationTypeService;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveStatisticsViewController(personService, applicationForLeaveStatisticsService,
            applicationForLeaveStatisticsCsvExportService, vacationTypeService, new DateFormatAware(), messageSource);
    }

    @Test
    void applicationForLeaveStatisticsAddsErrorToModelAndShowsFormIfPeriodNotTheSameYear() throws Exception {

        perform(get("/web/application/statistics")
            .param("from", "01.01.2019")
            .param("to", "01.08.2020"))
            .andExpect(model().attribute("errors", "INVALID_PERIOD"))
            .andExpect(view().name("thymeleaf/application/application-statistics"));
    }

    @Test
    void applicationForLeaveStatisticsSetsModelAndViewWithoutStatistics() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        when(applicationForLeaveStatisticsService.getStatistics(eq(signedInUser), eq(filterPeriod), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        final List<VacationType> vacationType = List.of(new VacationType(1, true, HOLIDAY, "message_key", true, YELLOW, false));
        when(vacationTypeService.getAllVacationTypes()).thenReturn(vacationType);

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"));

        resultActions
            .andExpect(model().attribute("from", startDate))
            .andExpect(model().attribute("to", endDate))
            .andExpect(model().attribute("statisticsPagination", hasProperty("page", hasProperty("content", is(List.of())))))
            .andExpect(model().attribute("showPersonnelNumberColumn", false))
            .andExpect(model().attribute("period", filterPeriod))
            .andExpect(model().attribute("vacationTypes", vacationType))
            .andExpect(view().name("thymeleaf/application/application-statistics"));
    }

    @Test
    void applicationForLeaveStatisticsSetsModelAndViewWithStatistics() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        when(messageSource.getMessage("hours.abbr", new Object[]{}, ENGLISH)).thenReturn("Std.");

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key_holiday", true, YELLOW, false);
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person person = new Person();
        person.setId(2);
        person.setFirstName("Firstname");
        person.setLastName("Lastname");
        person.setEmail("firstname.lastname@example.org");

        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of(vacationType));
        statistic.setPersonBasedata(new PersonBasedata(new PersonId(1), "42", "some additional information"));
        statistic.setLeftOvertimeForYear(Duration.ofHours(10));
        statistic.setLeftVacationDaysForYear(BigDecimal.valueOf(2));
        statistic.addWaitingVacationDays(vacationType, BigDecimal.valueOf(3));
        statistic.addAllowedVacationDays(vacationType, BigDecimal.valueOf(4));

        when(applicationForLeaveStatisticsService.getStatistics(eq(signedInUser), eq(filterPeriod), eq(defaultPageRequest())))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"));

        resultActions
            .andExpect(model().attribute("from", startDate))
            .andExpect(model().attribute("to", endDate))
            .andExpect(model().attribute("statistics", hasSize(1)))
            .andExpect(model().attribute("statistics", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveStatisticsDto.class),
                    hasProperty("firstName", is("Firstname")),
                    hasProperty("lastName", is("Lastname")),
                    hasProperty("niceName", is("Firstname Lastname")),
                    hasProperty("gravatarURL", is("https://gravatar.com/avatar/fb48a07b1c7315ffd490dc41292e56a4")),
                    hasProperty("personnelNumber", is("42")),
                    hasProperty("totalAllowedVacationDays", is(BigDecimal.valueOf(4))),
                    hasProperty("allowedVacationDays", aMapWithSize(1)),
                    hasProperty("totalWaitingVacationDays", is(BigDecimal.valueOf(3))),
                    hasProperty("waitingVacationDays", aMapWithSize(1)),
                    hasProperty("leftVacationDays", is(BigDecimal.valueOf(2))),
                    hasProperty("leftOvertime", is("10 Std."))
                )
            )))
            .andExpect(model().attribute("showPersonnelNumberColumn", true))
            .andExpect(model().attribute("period", filterPeriod))
            .andExpect(model().attribute("vacationTypes", List.of(vacationType)))
            .andExpect(view().name("thymeleaf/application/application-statistics"));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "person.firstName,ASC:person.firstName",
        "person.lastName,ASC:person.lastName",
        "totalAllowedVacationDays,ASC:totalAllowedVacationDays",
        "totalWaitingVacationDays,ASC:totalWaitingVacationDays",
        "leftVacationDaysForPeriod,ASC:leftVacationDaysForPeriod",
        "leftVacationDaysForYear,ASC:leftVacationDaysForYear"
    }, delimiter = ':')
    void applicationForLeaveStatisticsSetsModelAndViewWithStatisticsSortedAscendingBy(String sortQuery, String expectedSortProperty) throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, expectedSortProperty));
        when(applicationForLeaveStatisticsService.getStatistics(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest)))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", sortQuery));

        resultActions
            .andExpect(model().attribute("statistics", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveStatisticsDto.class),
                    hasProperty("firstName", is("John"))
                )
            )));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "person.firstName,DESC:person.firstName",
        "person.lastName,DESC:person.lastName",
        "totalAllowedVacationDays,DESC:totalAllowedVacationDays",
        "totalWaitingVacationDays,DESC:totalWaitingVacationDays",
        "leftVacationDaysForPeriod,DESC:leftVacationDaysForPeriod",
        "leftVacationDaysForYear,DESC:leftVacationDaysForYear"
    }, delimiter = ':')
    void applicationForLeaveStatisticsSetsModelAndViewWithStatisticsSortedDescendingBy(String sortQuery, String expectedSortProperty) throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, expectedSortProperty));
        when(applicationForLeaveStatisticsService.getStatistics(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest)))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", sortQuery));

        resultActions
            .andExpect(model().attribute("statistics", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveStatisticsDto.class),
                    hasProperty("firstName", is("John"))
                )
            )));
    }

    @Test
    void applicationForLeaveStatisticsSetsModelAndViewSortQuery() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "person.lastName"));
        when(applicationForLeaveStatisticsService.getStatistics(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest)))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", "person.lastName,DESC"));

        resultActions
            .andExpect(model().attribute("sortQuery", is("person.lastName,DESC")));
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
        when(applicationForLeaveStatisticsCsvExportService.generateCSV(any(FilterPeriod.class), any())).thenReturn(new CSVFile("filename.csv", new ByteArrayResource(new byte[]{})));

        final Person signedInUser = new Person();
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        when(applicationForLeaveStatisticsService.getStatistics(eq(signedInUser), any(FilterPeriod.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        perform(get("/web/application/statistics/download")
            .param("from", givenDate)
            .param("to", givenDate))
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

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key_holiday", true, YELLOW, false);

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(signedInUser, List.of(vacationType));
        when(applicationForLeaveStatisticsService.getStatistics(eq(signedInUser), any(FilterPeriod.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(statistics)));

        final CSVFile csvFile = new CSVFile("csv-file-name", new ByteArrayResource("csv-resource".getBytes()));
        when(applicationForLeaveStatisticsCsvExportService.generateCSV(filterPeriod, List.of(statistics))).thenReturn(csvFile);

        perform(get("/web/application/statistics/download")
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));
    }

    private static Pageable defaultPageRequest() {
        return PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "person.firstName"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build()
            .perform(builder);
    }
}
