package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
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
import org.synyx.urlaubsverwaltung.person.PersonPageRequest;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptgroupDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptionDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlSelectDto;
import org.synyx.urlaubsverwaltung.web.html.PaginationDto;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.Locale.JAPANESE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
    @Mock
    private DataWebProperties dataWebProperties;

    final DataWebProperties.Pageable pageableProperties = new DataWebProperties.Pageable();

    private static final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        pageableProperties.setOneIndexedParameters(true);
        pageableProperties.setPageParameter("page");
        lenient().when(dataWebProperties.getPageable()).thenReturn(pageableProperties);

        sut = new ApplicationForLeaveStatisticsViewController(personService, applicationForLeaveStatisticsService,
            applicationForLeaveStatisticsCsvExportService, vacationTypeService, dateFormatAware, messageSource, dataWebProperties, clock);
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
            .andExpect(model().attribute("errors", List.of("applications.statistics.error.differentYear")))
            .andExpect(model().attributeExists("sortSelect"))
            .andExpect(model().attributeExists("statisticsPagination"))
            .andExpect(view().name("application/application-statistics"));
    }

    @Test
    void applicationForLeaveStatisticsAddsErrorToModelAndShowsFormIfPeriodInWrongOrder() throws Exception {

        final Locale locale = Locale.GERMAN;

        when(dateFormatAware.parse("01.12.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 12, 1)));
        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));

        perform(
            get("/web/application/statistics")
                .locale(locale)
                .param("from", "01.12.2019")
                .param("to", "01.01.2019")
        )
            .andExpect(model().attribute("errors", List.of("applications.statistics.error.endDateBeforeStartDate")))
            .andExpect(model().attributeExists("sortSelect"))
            .andExpect(model().attributeExists("statisticsPagination"))
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

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by("firstName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(signedInUser, filterPeriod, pageRequest, ""))
            .thenReturn(new PageImpl<>(List.of()));

        final List<VacationType<?>> vacationType = List.of(ProvidedVacationType.builder(messageSource).messageKey("vacation-type-label-message-key").id(1L).build());
        when(vacationTypeService.getAllVacationTypes()).thenReturn(vacationType);

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 8, 1)));

        final ResultActions resultActions = perform(
            get("/web/application/statistics")
                .locale(locale)
                .param("from", "01.01.2019")
                .param("to", "01.08.2019")
        );

        resultActions
            .andExpect(model().attribute("from", startDate))
            .andExpect(model().attribute("to", endDate))
            .andExpect(model().attribute("showPersonnelNumberColumn", false))
            .andExpect(model().attribute("period", filterPeriod))
            .andExpect(model().attribute("vacationTypes", List.of(new ApplicationForLeaveStatisticsVacationTypeDto("vacation type label", 1L))))
            .andExpect(view().name("application/application-statistics"));

        final PaginationDto<ApplicationForLeaveStatisticsDto> statisticsPagination = (PaginationDto<ApplicationForLeaveStatisticsDto>) resultActions.andReturn().getModelAndView().getModel().get("statisticsPagination");
        assertThat(statisticsPagination.getPage().getContent()).isEmpty();
    }

    @Test
    void applicationForLeaveStatisticsSetsModelAndViewWithStatistics() throws Exception {

        final Locale locale = JAPANESE;
        when(messageSource.getMessage("vacation-type-label-message-key", new Object[]{}, locale)).thenReturn("vacation type label");
        when(messageSource.getMessage("hours.abbr", new Object[]{}, locale)).thenReturn("Std.");

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource).messageKey("vacation-type-label-message-key").id(2L).build();
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

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by("firstName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(signedInUser, filterPeriod, pageRequest, ""))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 8, 1)));

        final ResultActions resultActions = perform(
            get("/web/application/statistics")
                .locale(locale)
                .param("from", "01.01.2019")
                .param("to", "01.08.2019")
        );

        resultActions
            .andExpect(model().attribute("from", startDate))
            .andExpect(model().attribute("to", endDate))
            .andExpect(model().attribute("showPersonnelNumberColumn", true))
            .andExpect(model().attribute("period", filterPeriod))
            .andExpect(model().attribute("vacationTypes", List.of(new ApplicationForLeaveStatisticsVacationTypeDto("vacation type label", 2L))))
            .andExpect(view().name("application/application-statistics"));

        @SuppressWarnings("unchecked") final List<ApplicationForLeaveStatisticsDto> statistics = (List<ApplicationForLeaveStatisticsDto>) resultActions.andReturn().getModelAndView().getModel().get("statistics");

        assertThat(statistics).hasSize(1);

        final ApplicationForLeaveStatisticsDto dto = statistics.get(0);
        assertThat(dto).isInstanceOf(ApplicationForLeaveStatisticsDto.class);
        assertThat(dto.getFirstName()).isEqualTo("Firstname");
        assertThat(dto.getLastName()).isEqualTo("Lastname");
        assertThat(dto.getNiceName()).isEqualTo("Firstname Lastname");
        assertThat(dto.getGravatarURL()).isEqualTo("https://gravatar.com/avatar/fb48a07b1c7315ffd490dc41292e56a4");
        assertThat(dto.getPersonnelNumber()).isEqualTo("42");
        assertThat(dto.getTotalAllowedVacationDays()).isEqualTo(BigDecimal.valueOf(4));
        assertThat(dto.getAllowedVacationDays()).hasSize(1);
        assertThat(dto.getTotalWaitingVacationDays()).isEqualTo(BigDecimal.valueOf(3));
        assertThat(dto.getWaitingVacationDays()).hasSize(1);
        assertThat(dto.getLeftVacationDays()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(dto.getLeftOvertime()).isEqualTo("10 Std.");
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

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by("firstName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("max")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("query", "max"));

        @SuppressWarnings("unchecked") final List<ApplicationForLeaveStatisticsDto> statistics = (List<ApplicationForLeaveStatisticsDto>) resultActions.andReturn().getModelAndView().getModel().get("statistics");

        assertThat(statistics).hasSize(1);
        assertThat(statistics.get(0)).isInstanceOf(ApplicationForLeaveStatisticsDto.class);
        assertThat(statistics.get(0).getFirstName()).isEqualTo("Max");
    }

    @ParameterizedTest
    @CsvSource(value = {
        "person.firstName,ASC:firstName",
        "person.lastName,ASC:lastName",
    }, delimiter = ':')
    void applicationForLeaveStatisticsSetsModelAndViewWithStatisticsSortedByPersonAscendingBy(String sortQuery, String expectedSortProperty) throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by(expectedSortProperty));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", sortQuery));

        @SuppressWarnings("unchecked") final List<ApplicationForLeaveStatisticsDto> statistics = (List<ApplicationForLeaveStatisticsDto>) resultActions.andReturn().getModelAndView().getModel().get("statistics");

        assertThat(statistics).hasSize(1);
        assertThat(statistics.get(0)).isInstanceOf(ApplicationForLeaveStatisticsDto.class);
        assertThat(statistics.get(0).getFirstName()).isEqualTo("John");
    }

    @ParameterizedTest
    @CsvSource(value = {
        "totalAllowedVacationDays,ASC:totalAllowedVacationDays",
        "totalWaitingVacationDays,ASC:totalWaitingVacationDays",
        "leftVacationDaysForPeriod,ASC:leftVacationDaysForPeriod",
        "leftVacationDaysForYear,ASC:leftVacationDaysForYear"
    }, delimiter = ':')
    void applicationForLeaveStatisticsSetsModelAndViewWithStatisticsSortedByStatisticsAscendingBy(String sortQuery, String expectedSortProperty) throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final ApplicationForLeaveStatisticsPageRequest pageRequest = ApplicationForLeaveStatisticsPageRequest.of(0, 20, Sort.by(expectedSortProperty));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByStatistics(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", sortQuery));

        @SuppressWarnings("unchecked") final List<ApplicationForLeaveStatisticsDto> statistics = (List<ApplicationForLeaveStatisticsDto>) resultActions.andReturn().getModelAndView().getModel().get("statistics");

        assertThat(statistics).hasSize(1);
        assertThat(statistics.get(0)).isInstanceOf(ApplicationForLeaveStatisticsDto.class);
        assertThat(statistics.get(0).getFirstName()).isEqualTo("John");
    }

    @ParameterizedTest
    @CsvSource(value = {
        "person.firstName,DESC:firstName",
        "person.lastName,DESC:lastName",
    }, delimiter = ':')
    void applicationForLeaveStatisticsSetsModelAndViewWithStatisticsSortedByPersonDescendingBy(String sortQuery, String expectedSortProperty) throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, expectedSortProperty));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", sortQuery));

        @SuppressWarnings("unchecked") final List<ApplicationForLeaveStatisticsDto> statistics = (List<ApplicationForLeaveStatisticsDto>) resultActions.andReturn().getModelAndView().getModel().get("statistics");

        assertThat(statistics).hasSize(1);
        assertThat(statistics.get(0)).isInstanceOf(ApplicationForLeaveStatisticsDto.class);
        assertThat(statistics.get(0).getFirstName()).isEqualTo("John");
    }

    @ParameterizedTest
    @CsvSource(value = {
        "totalAllowedVacationDays,DESC:totalAllowedVacationDays",
        "totalWaitingVacationDays,DESC:totalWaitingVacationDays",
        "leftVacationDaysForPeriod,DESC:leftVacationDaysForPeriod",
        "leftVacationDaysForYear,DESC:leftVacationDaysForYear"
    }, delimiter = ':')
    void applicationForLeaveStatisticsSetsModelAndViewWithStatisticsSortedByStatisticsDescendingBy(String sortQuery, String expectedSortProperty) throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final ApplicationForLeaveStatisticsPageRequest pageRequest = ApplicationForLeaveStatisticsPageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, expectedSortProperty));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByStatistics(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", sortQuery));

        @SuppressWarnings("unchecked") final List<ApplicationForLeaveStatisticsDto> statistics = (List<ApplicationForLeaveStatisticsDto>) resultActions.andReturn().getModelAndView().getModel().get("statistics");

        assertThat(statistics).hasSize(1);
        assertThat(statistics.get(0)).isInstanceOf(ApplicationForLeaveStatisticsDto.class);
        assertThat(statistics.get(0).getFirstName()).isEqualTo("John");
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

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "lastName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", "person.lastName,DESC"));

        assertThat(resultActions.andReturn().getModelAndView().getModel()).containsEntry("sortQuery", "person.lastName,DESC");
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

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by("firstName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of()));

        perform(get("/web/application/statistics/download")
            .locale(locale)
            .param("from", givenDate)
            .param("to", givenDate))
            .andExpect(header().string("Content-disposition", "attachment; filename=\"filename.csv\"; filename*=UTF-8''filename.csv"))
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

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by("firstName"));
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(signedInUser, List.of(vacationType));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(signedInUser, filterPeriod, pageRequest, ""))
            .thenReturn(new PageImpl<>(List.of(statistics)));

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
        final PersonPageRequest pageRequest = PersonPageRequest.of(2, 50, Sort.by("firstName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(signedInUser, filterPeriod, pageRequest, ""))
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
        final PersonPageRequest pageRequest = PersonPageRequest.of(0, Integer.MAX_VALUE, Sort.by("firstName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(signedInUser, filterPeriod, pageRequest, ""))
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
        final PersonPageRequest pageRequest = PersonPageRequest.of(0, Integer.MAX_VALUE, Sort.by("firstName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(signedInUser, filterPeriod, pageRequest, ""))
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

    @Test
    void applicationForLeaveStatisticsSortSelectHasCorrectOptgroups() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by("firstName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics"));

        final HtmlSelectDto sortSelect = (HtmlSelectDto) resultActions.andReturn().getModelAndView().getModel().get("sortSelect");
        assertThat(sortSelect).isInstanceOf(HtmlSelectDto.class);

        final List<HtmlOptgroupDto> optgroups = sortSelect.optgroups();
        assertThat(optgroups).hasSize(2);
        assertThat(optgroups.get(0).labelMessageKey()).isEqualTo("applications.sort.optgroup.person.label");
        assertThat(optgroups.get(1).labelMessageKey()).isEqualTo("applications.sort.optgroup.statistics.label");
    }

    @Test
    void applicationForLeaveStatisticsSortSelectPersonOptgroupHasCorrectOptions() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by("firstName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics"));

        final HtmlSelectDto sortSelect = (HtmlSelectDto) resultActions.andReturn().getModelAndView().getModel().get("sortSelect");
        final HtmlOptgroupDto personOptgroup = sortSelect.optgroups().get(0);

        assertThat(personOptgroup.labelMessageKey()).isEqualTo("applications.sort.optgroup.person.label");

        final List<HtmlOptionDto> personOptions = personOptgroup.options();
        assertThat(personOptions).hasSize(4);

        assertThat(personOptions.stream().filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.firstName.asc"))
            .findFirst().orElseThrow()).hasFieldOrPropertyWithValue("value", "person.firstName,asc")
            .hasFieldOrPropertyWithValue("selected", true);

        assertThat(personOptions.stream().filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.firstName.desc"))
            .findFirst().orElseThrow()).hasFieldOrPropertyWithValue("value", "person.firstName,desc")
            .hasFieldOrPropertyWithValue("selected", false);

        assertThat(personOptions.stream().filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.lastName.asc"))
            .findFirst().orElseThrow()).hasFieldOrPropertyWithValue("value", "person.lastName,asc")
            .hasFieldOrPropertyWithValue("selected", false);

        assertThat(personOptions.stream().filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.lastName.desc"))
            .findFirst().orElseThrow()).hasFieldOrPropertyWithValue("value", "person.lastName,desc")
            .hasFieldOrPropertyWithValue("selected", false);
    }

    @Test
    void applicationForLeaveStatisticsSortSelectStatisticsOptgroupHasCorrectOptions() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by("firstName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics"));

        final HtmlSelectDto sortSelect = (HtmlSelectDto) resultActions.andReturn().getModelAndView().getModel().get("sortSelect");
        final HtmlOptgroupDto statisticsOptgroup = sortSelect.optgroups().get(1);

        assertThat(statisticsOptgroup.labelMessageKey()).isEqualTo("applications.sort.optgroup.statistics.label");

        final List<HtmlOptionDto> statisticsOptions = statisticsOptgroup.options();
        assertThat(statisticsOptions).hasSize(8);

        assertThat(statisticsOptions.stream().filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.totalAllowedVacationDays.asc"))
            .findFirst().orElseThrow()).hasFieldOrPropertyWithValue("value", "totalAllowedVacationDays,asc")
            .hasFieldOrPropertyWithValue("selected", false);

        assertThat(statisticsOptions.stream().filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.totalAllowedVacationDays.desc"))
            .findFirst().orElseThrow()).hasFieldOrPropertyWithValue("value", "totalAllowedVacationDays,desc")
            .hasFieldOrPropertyWithValue("selected", false);

        assertThat(statisticsOptions.stream().filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.totalWaitingVacationDays.asc"))
            .findFirst().orElseThrow()).hasFieldOrPropertyWithValue("value", "totalWaitingVacationDays,asc")
            .hasFieldOrPropertyWithValue("selected", false);

        assertThat(statisticsOptions.stream().filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.totalWaitingVacationDays.desc"))
            .findFirst().orElseThrow()).hasFieldOrPropertyWithValue("value", "totalWaitingVacationDays,desc")
            .hasFieldOrPropertyWithValue("selected", false);

        assertThat(statisticsOptions.stream().filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.leftVacationDaysForPeriod.asc"))
            .findFirst().orElseThrow()).hasFieldOrPropertyWithValue("value", "leftVacationDaysForPeriod,asc")
            .hasFieldOrPropertyWithValue("selected", false);

        assertThat(statisticsOptions.stream().filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.leftVacationDaysForPeriod.desc"))
            .findFirst().orElseThrow()).hasFieldOrPropertyWithValue("value", "leftVacationDaysForPeriod,desc")
            .hasFieldOrPropertyWithValue("selected", false);

        assertThat(statisticsOptions.stream().filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.leftVacationDaysForYear.asc"))
            .findFirst().orElseThrow()).hasFieldOrPropertyWithValue("value", "leftVacationDaysForYear,asc")
            .hasFieldOrPropertyWithValue("selected", false);

        assertThat(statisticsOptions.stream().filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.leftVacationDaysForYear.desc"))
            .findFirst().orElseThrow()).hasFieldOrPropertyWithValue("value", "leftVacationDaysForYear,desc")
            .hasFieldOrPropertyWithValue("selected", false);
    }

    @Test
    void applicationForLeaveStatisticsSortSelectPersonFirstNameAscIsSelectedWhenSortedByPersonFirstNameAsc() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by("firstName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", "person.firstName,ASC"));

        final HtmlSelectDto sortSelect = (HtmlSelectDto) resultActions.andReturn().getModelAndView().getModel().get("sortSelect");
        final HtmlOptgroupDto personOptgroup = sortSelect.optgroups().get(0);

        final HtmlOptionDto selectedOption = personOptgroup.options().stream()
            .filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.firstName.asc"))
            .findFirst()
            .orElseThrow();

        assertThat(selectedOption.value()).isEqualTo("person.firstName,asc");
        assertThat(selectedOption.selected()).isTrue();
    }

    @Test
    void applicationForLeaveStatisticsSortSelectPersonLastNameDescIsSelectedWhenSortedByPersonLastNameDesc() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final PersonPageRequest pageRequest = PersonPageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "lastName"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByPerson(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", "person.lastName,DESC"));

        final HtmlSelectDto sortSelect = (HtmlSelectDto) resultActions.andReturn().getModelAndView().getModel().get("sortSelect");
        final HtmlOptgroupDto personOptgroup = sortSelect.optgroups().get(0);

        final HtmlOptionDto selectedOption = personOptgroup.options().stream()
            .filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.lastName.desc"))
            .findFirst()
            .orElseThrow();

        assertThat(selectedOption.value()).isEqualTo("person.lastName,desc");
        assertThat(selectedOption.selected()).isTrue();
    }

    @Test
    void applicationForLeaveStatisticsSortSelectStatisticsTotalAllowedVacationDaysAscIsSelectedWhenSortedByStatisticsTotalAllowedVacationDaysAsc() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final ApplicationForLeaveStatisticsPageRequest pageRequest = ApplicationForLeaveStatisticsPageRequest.of(0, 20, Sort.by("totalAllowedVacationDays"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByStatistics(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", "totalAllowedVacationDays,ASC"));

        final HtmlSelectDto sortSelect = (HtmlSelectDto) resultActions.andReturn().getModelAndView().getModel().get("sortSelect");
        final HtmlOptgroupDto statisticsOptgroup = sortSelect.optgroups().get(1);

        final HtmlOptionDto selectedOption = statisticsOptgroup.options().stream()
            .filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.totalAllowedVacationDays.asc"))
            .findFirst()
            .orElseThrow();

        assertThat(selectedOption.value()).isEqualTo("totalAllowedVacationDays,asc");
        assertThat(selectedOption.selected()).isTrue();
    }

    @Test
    void applicationForLeaveStatisticsSortSelectStatisticsLeftVacationDaysForYearDescIsSelectedWhenSortedByStatisticsLeftVacationDaysForYearDesc() throws Exception {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person();
        person.setId(2L);
        person.setFirstName("John");
        final ApplicationForLeaveStatistics statistic = new ApplicationForLeaveStatistics(person, List.of());

        final ApplicationForLeaveStatisticsPageRequest pageRequest = ApplicationForLeaveStatisticsPageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "leftVacationDaysForYear"));
        when(applicationForLeaveStatisticsService.getStatisticsSortedByStatistics(eq(signedInUser), any(FilterPeriod.class), eq(pageRequest), eq("")))
            .thenReturn(new PageImpl<>(List.of(statistic)));

        final ResultActions resultActions = perform(get("/web/application/statistics")
            .param("sort", "leftVacationDaysForYear,DESC"));

        final HtmlSelectDto sortSelect = (HtmlSelectDto) resultActions.andReturn().getModelAndView().getModel().get("sortSelect");
        final HtmlOptgroupDto statisticsOptgroup = sortSelect.optgroups().get(1);

        final HtmlOptionDto selectedOption = statisticsOptgroup.options().stream()
            .filter(opt -> opt.textMessageKey().equals("applications.statistics.sort.leftVacationDaysForYear.desc"))
            .findFirst()
            .orElseThrow();

        assertThat(selectedOption.value()).isEqualTo("leftVacationDaysForYear,desc");
        assertThat(selectedOption.selected()).isTrue();
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build()
            .perform(builder);
    }
}
