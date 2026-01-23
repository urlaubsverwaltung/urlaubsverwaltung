package org.synyx.urlaubsverwaltung.sicknote.sickdays;


import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptgroupDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlOptionDto;
import org.synyx.urlaubsverwaltung.web.html.HtmlSelectDto;
import org.synyx.urlaubsverwaltung.web.html.PaginationDto;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToSunday;

@ExtendWith(MockitoExtension.class)
class SickDaysOverviewViewControllerTest {

    private SickDaysOverviewViewController sut;

    @Mock
    private SickDaysStatisticsService sickDaysStatisticsService;
    @Mock
    private PersonService personService;
    @Mock
    private DateFormatAware dateFormatAware;
    @Mock
    private DataWebProperties dataWebProperties;

    final DataWebProperties.Pageable pageableProperties = new DataWebProperties.Pageable();

    private static final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        pageableProperties.setOneIndexedParameters(true);
        pageableProperties.setPageParameter("page");
        lenient().when(dataWebProperties.getPageable()).thenReturn(pageableProperties);

        sut = new SickDaysOverviewViewController(sickDaysStatisticsService, personService, dateFormatAware, dataWebProperties, clock);
    }

    private static Stream<Arguments> dateInputAndIsoDateTuple() {
        final int year = clockYear();
        return Stream.of(
            Arguments.of(String.format("25.03.%s", year), LocalDate.of(year, 3, 25)),
            Arguments.of(String.format("25.03.%s", year - 2000), LocalDate.of(year, 3, 25)),
            Arguments.of(String.format("25.3.%s", year), LocalDate.of(year, 3, 25)),
            Arguments.of(String.format("25.3.%s", year - 2000), LocalDate.of(year, 3, 25)),
            Arguments.of(String.format("1.4.%s", year - 2000), LocalDate.of(year, 4, 1))
        );
    }

    @Test
    void ensureErrorHandlingWhenStartDateIsAfterToDate() {

        final Locale locale = Locale.GERMAN;
        final int year = clockYear();

        final String fromDateIsoString = year + "-03-01";
        final LocalDate fromDate = LocalDate.parse(fromDateIsoString);

        final String toDateIsoString = year + "-01-01";
        final LocalDate toDate = LocalDate.parse(toDateIsoString);

        when(dateFormatAware.parse(fromDateIsoString, locale)).thenReturn(Optional.of(fromDate));
        when(dateFormatAware.parse(toDateIsoString, locale)).thenReturn(Optional.of(toDate));

        final MvcTestResult result = mockmvc().get()
            .uri("/web/sickdays")
            .locale(locale)
            .param("from", fromDateIsoString)
            .param("to", toDateIsoString)
            .exchange();

        final PageImpl<SickDaysOverviewDto> page = new PageImpl<>(List.of(), defaultSickDaysPageable(), 0);
        final String filterQuery = "?from=%s&to=%s&query=&sort=person.firstName,ASC&size=20".formatted(fromDateIsoString, toDateIsoString);
        final PaginationDto<SickDaysOverviewDto> pagination = new PaginationDto<>(page, filterQuery, pageableProperties);

        assertThat(result)
            .hasStatusOk()
            .hasViewName("sicknote/sick_days")
            .model()
            .containsEntry("today", LocalDate.now(clock))
            .containsEntry("from", fromDate)
            .containsEntry("to", toDate)
            .containsEntry("sortSelect", expectedPersonSortSelect())
            .containsEntry("period", new FilterPeriod(fromDate, toDate))
            .containsEntry("errorEndDateBeforeStartDate", "sicknotes.daysOverview.sickDays.error.endDateBeforeStartDate")
            .containsEntry("statisticsPagination", pagination);
    }

    @ParameterizedTest
    @MethodSource("dateInputAndIsoDateTuple")
    void correctlyMapsCustomFormatOfFromDate(String givenDateString, LocalDate givenDate) {

        final Locale locale = Locale.GERMAN;
        final int year = clockYear();

        when(sickDaysStatisticsService.getAll(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(dateFormatAware.parse(givenDateString, locale)).thenReturn(Optional.of(givenDate));

        final MvcTestResult result = mockmvc().get()
            .uri("/web/sickdays")
            .locale(locale)
            .param("from", givenDateString)
            .exchange();

        assertThat(result)
            .hasStatusOk()
            .hasViewName("sicknote/sick_days")
            .model()
            .containsEntry("today", LocalDate.now(clock))
            .containsEntry("from", givenDate)
            .containsEntry("to", LocalDate.of(year, 12, 31))
            .containsEntry("sortSelect", expectedPersonSortSelect());
    }

    @ParameterizedTest
    @MethodSource("dateInputAndIsoDateTuple")
    void correctlyMapsCustomFormatOfToDate(String givenDateString, LocalDate givenDate) {

        final Locale locale = Locale.GERMAN;

        final int year = clockYear();
        final LocalDate fromDate = LocalDate.of(year, 1, 1);

        when(sickDaysStatisticsService.getAll(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

        when(dateFormatAware.parse(any(), eq(locale))).thenReturn(Optional.of(fromDate));
        when(dateFormatAware.parse(givenDateString, locale)).thenReturn(Optional.of(givenDate));

        final MvcTestResult result = mockmvc().get()
            .uri("/web/sickdays")
            .locale(locale)
            .param("to", givenDateString)
            .exchange();

        assertThat(result)
            .hasStatusOk()
            .hasViewName("sicknote/sick_days")
            .model()
            .containsEntry("today", LocalDate.now(clock))
            .containsEntry("from", fromDate)
            .containsEntry("to", givenDate)
            .containsEntry("sortSelect", expectedPersonSortSelect());
    }

    @Test
    void correctlySetsDefaultFilterInViewModel() {

        final int year = Year.now(clock).getValue();

        when(sickDaysStatisticsService.getAll(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

        final MvcTestResult result = mockmvc().get()
            .uri("/web/sickdays")
            .exchange();

        assertThat(result)
            .hasStatusOk()
            .hasViewName("sicknote/sick_days")
            .model()
            .containsEntry("today", LocalDate.now(clock))
            .containsEntry("from", LocalDate.of(year, 1, 1))
            .containsEntry("to", LocalDate.of(year, 12, 31))
            .containsEntry("sortSelect", expectedPersonSortSelect());
    }

    @Test
    void periodsSickNotesWithDateRangeWithRole() {

        final Locale locale = Locale.GERMAN;

        final Person office = new Person();
        office.setId(1L);
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);

        final Person person = new Person();
        person.setId(1L);
        person.setFirstName("FirstName one");
        person.setLastName("LastName one");
        person.setPermissions(List.of(USER));

        final Person person2 = new Person();
        person2.setId(2L);
        person2.setFirstName("FirstName two");
        person2.setLastName("LastName two");
        person2.setPermissions(List.of(USER));

        final Person person3 = new Person();
        person3.setId(3L);
        person3.setFirstName("FirstName three");
        person3.setLastName("LastName three");
        person3.setPermissions(List.of(USER));

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(
            LocalDate.of(2019, 1, 1),
            LocalDate.of(2019, 12, 31)
        );

        final SickNoteType childSickType = new SickNoteType();
        childSickType.setCategory(SICK_NOTE_CHILD);

        final SickNote childSickNote = SickNote.builder()
            .startDate(LocalDate.of(2019, 2, 1))
            .endDate(LocalDate.of(2019, 3, 1))
            .dayLength(FULL)
            .status(ACTIVE)
            .sickNoteType(childSickType)
            .person(person)
            .aubStartDate(LocalDate.of(2019, 2, 10))
            .aubEndDate(LocalDate.of(2019, 2, 15))
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final SickNoteType sickType = new SickNoteType();
        sickType.setCategory(SICK_NOTE);

        final SickNote sickNote = SickNote.builder()
            .startDate(LocalDate.of(2019, 4, 1))
            .endDate(LocalDate.of(2019, 5, 1))
            .dayLength(FULL)
            .status(ACTIVE)
            .sickNoteType(sickType)
            .person(person2)
            .aubStartDate(LocalDate.of(2019, 4, 10))
            .aubEndDate(LocalDate.of(2019, 4, 20))
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final LocalDate requestStartDate = LocalDate.of(2019, 2, 11);
        final LocalDate requestEndDate = LocalDate.of(2019, 4, 15);

        final PageableSearchQuery pageableSearchQuery =
            new PageableSearchQuery(PageRequest.of(2, 50, Sort.by(ASC, "person.firstName")), "");

        final SickDaysDetailedStatistics statisticsPersonOne = new SickDaysDetailedStatistics("0000001337", person, List.of(sickNote), List.of());
        final SickDaysDetailedStatistics statisticsPersonTwo = new SickDaysDetailedStatistics("0000000042", person2, List.of(childSickNote), List.of());
        final SickDaysDetailedStatistics statisticsPersonThree = new SickDaysDetailedStatistics("0000000021", person3, List.of(), List.of());

        when(sickDaysStatisticsService.getAll(office, requestStartDate, requestEndDate, pageableSearchQuery))
            .thenReturn(new PageImpl<>(List.of(statisticsPersonOne, statisticsPersonTwo, statisticsPersonThree)));

        when(dateFormatAware.parse(requestStartDate.toString(), locale)).thenReturn(Optional.of(requestStartDate));
        when(dateFormatAware.parse(requestEndDate.toString(), locale)).thenReturn(Optional.of(requestEndDate));

        final MvcTestResult result = mockmvc().get()
            .uri("/web/sickdays")
            .locale(locale)
            .param("from", requestStartDate.toString())
            .param("to", requestEndDate.toString())
            .param("page", "2")
            .param("size", "50")
            .exchange();

        assertThat(result)
            .hasStatusOk()
            .hasViewName("sicknote/sick_days")
            .model()
            .containsEntry("showPersonnelNumberColumn", true)
            .containsEntry("today", LocalDate.now(clock))
            .containsEntry("from", requestStartDate)
            .containsEntry("to", requestEndDate)
            .containsEntry("sortSelect", expectedPersonSortSelect())
            .containsEntry("period", new FilterPeriod(requestStartDate, requestEndDate))
            .extractingByKey("statisticsPagination")
            .extracting("page")
            .extracting("content", as(InstanceOfAssertFactories.LIST))
            .contains(new SickDaysOverviewDto(1L, "", "0000001337", "FirstName one", "LastName one", "FirstName one LastName one", "FO", BigDecimal.valueOf(15), BigDecimal.valueOf(6), ZERO, ZERO))
            .contains(new SickDaysOverviewDto(2L, "", "0000000042", "FirstName two", "LastName two", "FirstName two LastName two", "FT", ZERO, ZERO, BigDecimal.valueOf(19), BigDecimal.valueOf(5)))
            .contains(new SickDaysOverviewDto(3L, "", "0000000021", "FirstName three", "LastName three", "FirstName three LastName three", "FT", ZERO, ZERO, ZERO, ZERO));
    }

    @Test
    void ensureSickNotesAreFetchedWithCorrectFilterQuery() {

        final Person office = new Person();
        office.setId(1L);
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);

        final int year = Year.now(clock).getValue();
        final LocalDate startDate = ZonedDateTime.now(clock).withYear(year).with(firstDayOfYear()).toLocalDate();
        final LocalDate endDate = ZonedDateTime.now(clock).withYear(year).with(lastDayOfYear()).toLocalDate();

        final PageableSearchQuery pageableSearchQuery =
            new PageableSearchQuery(PageRequest.of(1, 50, Sort.by(ASC, "person.firstName")), "");

        when(sickDaysStatisticsService.getAll(office, startDate, endDate, pageableSearchQuery))
            .thenReturn(new PageImpl<>(List.of()));

        final MvcTestResult result = mockmvc().get()
            .uri("/web/sickdays")
            .param("from", "01.01." + year)
            .param("to", "31.12." + year)
            .param("page", "1")
            .param("size", "50")
            .exchange();

        assertThat(result)
            .hasStatusOk()
            .hasViewName("sicknote/sick_days")
            .model()
            .containsEntry("from", startDate)
            .containsEntry("to", endDate)
            .containsEntry("period", new FilterPeriod(startDate, endDate))
            .containsEntry("sortSelect", expectedPersonSortSelect());
    }

    @Test
    void sickNotesWithoutPersonnelNumberColumn() {

        final Locale locale = Locale.GERMAN;

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate requestStartDate = LocalDate.of(2019, 2, 11);
        final LocalDate requestEndDate = LocalDate.of(2019, 4, 15);

        final PageableSearchQuery pageableSearchQuery =
            new PageableSearchQuery(PageRequest.of(2, 50, Sort.by(ASC, "person.firstName")), "");

        final SickDaysDetailedStatistics statistics = new SickDaysDetailedStatistics("", signedInUser, List.of(), List.of());
        when(sickDaysStatisticsService.getAll(signedInUser, requestStartDate, requestEndDate, pageableSearchQuery))
            .thenReturn(new PageImpl<>(List.of(statistics)));

        when(dateFormatAware.parse(requestStartDate.toString(), locale)).thenReturn(Optional.of(requestStartDate));
        when(dateFormatAware.parse(requestEndDate.toString(), locale)).thenReturn(Optional.of(requestEndDate));

        final MvcTestResult result = mockmvc().get()
            .uri("/web/sickdays")
            .locale(locale)
            .param("from", requestStartDate.toString())
            .param("to", requestEndDate.toString())
            .param("page", "2")
            .param("size", "50")
            .exchange();

        assertThat(result)
            .hasStatusOk()
            .hasViewName("sicknote/sick_days")
            .model()
            .containsEntry("showPersonnelNumberColumn", false);
    }

    private Pageable defaultSickDaysPageable() {
        return PageRequest.of(0, 20, Sort.by(ASC, "person.firstName"));
    }

    private HtmlSelectDto expectedPersonSortSelect() {
        return new HtmlSelectDto(List.of(
            new HtmlOptgroupDto("sicknotes.sort.optgroup.person.label", List.of(
                new HtmlOptionDto("sicknotes.statistics.sort.firstName.asc", "person.firstName,asc", true),
                new HtmlOptionDto("sicknotes.statistics.sort.firstName.desc", "person.firstName,desc", false),
                new HtmlOptionDto("sicknotes.statistics.sort.lastName.asc", "person.lastName,asc", false),
                new HtmlOptionDto("sicknotes.statistics.sort.lastName.desc", "person.lastName,desc", false)
            ))
        ));
    }

    private static int clockYear() {
        return Year.now(clock).getValue();
    }

    private MockMvcTester mockmvc() {
        return MockMvcTester.of(List.of(sut),
            mockMvcBuilder ->
                mockMvcBuilder.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                    .build()
        );
    }
}
