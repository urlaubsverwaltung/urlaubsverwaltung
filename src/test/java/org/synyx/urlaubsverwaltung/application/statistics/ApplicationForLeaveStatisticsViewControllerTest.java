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
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.csv.CSVFile;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.Locale.JAPANESE;
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
    private DateFormatAware dateFormatAware;
    @Mock
    private MessageSource messageSource;

    private static final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveStatisticsViewController(personService, applicationForLeaveStatisticsService,
            applicationForLeaveStatisticsCsvExportService, vacationTypeService, dateFormatAware, messageSource, clock);
    }

    @Test
    void applicationForLeaveStatisticsAddsErrorToModelAndShowsFormIfPeriodNotTheSameYear() throws Exception {

        final Locale locale = Locale.GERMAN;

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2020", locale)).thenReturn(Optional.of(LocalDate.of(2020, 8, 1)));

        perform(
            get("/web/application/statistics")
                .locale(locale)
                .param("from", "01.01.2019")
                .param("to", "01.08.2020")
        )
            .andExpect(model().attribute("errors", "INVALID_PERIOD"))
            .andExpect(model().attributeExists("sortSelect", "statisticsPagination"))
            .andExpect(view().name("application/application-statistics"));
    }

    @Test
    void applicationForLeaveStatisticsSetsModelAndViewWithoutStatistics() throws Exception {

        final Locale locale = JAPANESE;
        when(messageSource.getMessage("vacation-type-label-message-key", new Object[]{}, locale)).thenReturn("vacation type label");

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        when(applicationForLeaveStatisticsService.getStatistics(signedInUser, filterPeriod, defaultPersonSearchQuery()))
            .thenReturn(new PageImpl<>(List.of()));

        final List<VacationType<?>> vacationType = List.of(ProvidedVacationType.builder(messageSource).messageKey("vacation-type-label-message-key").build());
        when(vacationTypeService.getAllVacationTypes()).thenReturn(vacationType);

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 8, 1)));

        perform(
            get("/web/application/statistics")
                .locale(locale)
                .param("from", "01.01.2019")
                .param("to", "01.08.2019")
        )
            .andExpect(model().attribute("from", startDate))
            .andExpect(model().attribute("to", endDate))
            .andExpect(model().attribute("statisticsPagination", hasProperty("page", hasProperty("content", is(List.of())))))
            .andExpect(model().attribute("showPersonnelNumberColumn", false))
            .andExpect(model().attribute("period", filterPeriod))
            .andExpect(model().attribute("vacationTypes", List.of(new ApplicationForLeaveStatisticsVacationTypeDto("vacation type label"))))
            .andExpect(view().name("application/application-statistics"));
    }

    @Test
    void applicationForLeaveStatisticsSetsModelAndViewWithStatistics() throws Exception {

        final Locale locale = JAPANESE;
        when(messageSource.getMessage("vacation-type-label-message-key", new Object[]{}, locale)).thenReturn("vacation type label");
        when(messageSource.getMessage("hours.abbr", new Object[]{}, locale)).thenReturn("Std.");

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource).messageKey("vacation-type-label-message-key").build();
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("Firstname");
        person.setLastName("Lastname");
        person.setEmail("firstname.lastname@example.org");

        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of(vacationType));
        statistic.setPersonBasedata(new PersonBasedata(new PersonId(1L), "42", "some additional information"));
        statistic.setLeftOvertimeForYear(Duration.ofHours(10));
        statistic.setLeftVacationDaysForYear(BigDecimal.valueOf(2));
        statistic.addWaitingVacationDays(vacationType, BigDecimal.valueOf(3));
        statistic.addAllowedVacationDays(vacationType, BigDecimal.valueOf(4));

        when(applicationForLeaveStatisticsService.getStatistics(signedInUser, filterPeriod, defaultPersonSearchQuery()))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 8, 1)));

        perform(
            get("/web/application/statistics")
                .locale(locale)
                .param("from", "01.01.2019")
                .param("to", "01.08.2019")
        )
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
            .andExpect(model().attribute("vacationTypes", List.of(new ApplicationForLeaveStatisticsVacationTypeDto("vacation type label"))))
            .andExpect(view().name("application/application-statistics"));
    }

    @Test
    void applicationForLeaveStatisticsWithSearchQuery() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("Max");

        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "person.firstName"));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "max");
        when(applicationForLeaveStatisticsService.getStatistics(eq(signedInUser), any(FilterPeriod.class), eq(pageableSearchQuery)))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("query", "max"));

        resultActions
            .andExpect(model().attribute("statistics", hasItems(
                allOf(
                    instanceOf(ApplicationForLeaveStatisticsDto.class),
                    hasProperty("firstName", is("Max"))
                )
            )));
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
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, expectedSortProperty));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");
        when(applicationForLeaveStatisticsService.getStatistics(eq(signedInUser), any(FilterPeriod.class), eq(pageableSearchQuery)))
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
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, expectedSortProperty));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");
        when(applicationForLeaveStatisticsService.getStatistics(eq(signedInUser), any(FilterPeriod.class), eq(pageableSearchQuery)))
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
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "person.lastName"));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(pageRequest, "");
        when(applicationForLeaveStatisticsService.getStatistics(eq(signedInUser), any(FilterPeriod.class), eq(pageableSearchQuery)))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", "person.lastName,DESC"));

        resultActions
            .andExpect(model().attribute("sortQuery", is("person.lastName,DESC")));
    }

    @Test
    void downloadCSVReturnsBadRequestIfPeriodNotTheSameYear() throws Exception {

        final Locale locale = Locale.GERMAN;

        when(dateFormatAware.parse("01.01.2000", locale)).thenReturn(Optional.of(LocalDate.of(2000, 1, 1)));
        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));

        perform(
            get("/web/application/statistics/download")
                .locale(locale)
                .param("from", "01.01.2000")
                .param("to", "01.01.2019")
        )
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"25.03.2022", "25.03.22", "25.3.2022", "25.3.22", "1.4.22"})
    void downloadCSVSetsDownloadHeaders(String givenDate) throws Exception {

        final Locale locale = JAPANESE;

        when(applicationForLeaveStatisticsCsvExportService.generateCSV(any(FilterPeriod.class), eq(locale), any()))
            .thenReturn(new CSVFile("filename.csv", new ByteArrayResource(new byte[]{})));

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        when(applicationForLeaveStatisticsService.getStatistics(eq(signedInUser), any(FilterPeriod.class), eq(defaultPersonSearchQuery())))
            .thenReturn(new PageImpl<>(List.of()));

        perform(get("/web/application/statistics/download")
            .locale(locale)
            .param("from", givenDate)
            .param("to", givenDate))
            .andExpect(header().string("Content-disposition", "attachment; filename=\"=?UTF-8?Q?filename.csv?=\"; filename*=UTF-8''filename.csv"))
            .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"));
    }

    @Test
    void ensureToDownloadCSVStatisticsForSelectionWithDefaultValues() throws Exception {

        final Locale locale = JAPANESE;

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(signedInUser, List.of(vacationType));
        when(applicationForLeaveStatisticsService.getStatistics(signedInUser, filterPeriod, defaultPersonSearchQuery())).thenReturn(new PageImpl<>(List.of(statistics)));

        final CSVFile csvFile = new CSVFile("csv-file-name", new ByteArrayResource("csv-resource".getBytes()));
        when(applicationForLeaveStatisticsCsvExportService.generateCSV(filterPeriod, locale, List.of(statistics))).thenReturn(csvFile);

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 8, 1)));

        perform(get("/web/application/statistics/download")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));
    }

    @Test
    void ensureToDownloadCSVStatisticsForSelectionWithGivenValues() throws Exception {

        final Locale locale = JAPANESE;

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(signedInUser, List.of(vacationType));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(2, 50, Sort.by(Sort.Direction.ASC, "person.firstName")), "");
        when(applicationForLeaveStatisticsService.getStatistics(signedInUser, filterPeriod, pageableSearchQuery))
            .thenReturn(new PageImpl<>(List.of(statistics)));

        final CSVFile csvFile = new CSVFile("csv-file-name", new ByteArrayResource("csv-resource".getBytes()));
        when(applicationForLeaveStatisticsCsvExportService.generateCSV(filterPeriod, locale, List.of(statistics))).thenReturn(csvFile);

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 8, 1)));

        perform(get("/web/application/statistics/download")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019")
            .param("page", "2")
            .param("size", "50"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));
    }

    @Test
    void ensureToDownloadCSVStatisticsForAll() throws Exception {

        final Locale locale = JAPANESE;

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(signedInUser, List.of(vacationType));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "person.firstName")), "");
        when(applicationForLeaveStatisticsService.getStatistics(signedInUser, filterPeriod, pageableSearchQuery))
            .thenReturn(new PageImpl<>(List.of(statistics)));

        final CSVFile csvFile = new CSVFile("csv-file-name", new ByteArrayResource("csv-resource".getBytes()));
        when(applicationForLeaveStatisticsCsvExportService.generateCSV(filterPeriod, locale, List.of(statistics))).thenReturn(csvFile);

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 8, 1)));

        perform(get("/web/application/statistics/download")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019")
            .param("allElements", "true"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));
    }

    @Test
    void ensureToDownloadCSVStatisticsForAllWithSelectionParameterAndAllShouldWin() throws Exception {

        final Locale locale = JAPANESE;

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(signedInUser, List.of(vacationType));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "person.firstName")), "");
        when(applicationForLeaveStatisticsService.getStatistics(signedInUser, filterPeriod, pageableSearchQuery))
            .thenReturn(new PageImpl<>(List.of(statistics)));

        final CSVFile csvFile = new CSVFile("csv-file-name", new ByteArrayResource("csv-resource".getBytes()));
        when(applicationForLeaveStatisticsCsvExportService.generateCSV(filterPeriod, locale, List.of(statistics))).thenReturn(csvFile);

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 8, 1)));

        perform(get("/web/application/statistics/download")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019")
            .param("allElements", "true")
            .param("page", "2")
            .param("size", "50")
            .param("query", "hans"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));
    }

    private static PageableSearchQuery defaultPersonSearchQuery() {
        return new PageableSearchQuery(defaultPageRequest(), "");
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
