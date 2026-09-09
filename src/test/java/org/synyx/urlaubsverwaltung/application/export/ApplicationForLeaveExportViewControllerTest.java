package org.synyx.urlaubsverwaltung.application.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationForLeave;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.csv.CSVFile;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.math.BigDecimal.TEN;
import static java.time.Month.AUGUST;
import static java.time.Month.JANUARY;
import static java.util.Locale.JAPANESE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveExportViewControllerTest {

    private static final LocalDate START_DATE = LocalDate.parse("2019-01-01");
    private static final LocalDate END_DATE = LocalDate.parse("2019-08-01");
    private static final FilterPeriod FILTER_PERIOD = new FilterPeriod(START_DATE, END_DATE);

    private ApplicationForLeaveExportViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private ApplicationForLeaveExportService applicationForLeaveExportService;
    @Mock
    private ApplicationForLeaveCsvExportService applicationForLeaveCsvExportService;
    @Mock
    private DateFormatAware dateFormatAware;

    private static final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveExportViewController(personService, applicationForLeaveExportService,
            applicationForLeaveCsvExportService, dateFormatAware, clock);
    }

    @Test
    void ensuresToDownloadCSVIfNotTheSameYearIsABadRequest() throws Exception {

        final Locale locale = Locale.GERMAN;

        when(dateFormatAware.parse("01.01.2022", locale)).thenReturn(Optional.of(LocalDate.of(2022, JANUARY, 1)));
        when(dateFormatAware.parse("01.01.2023", locale)).thenReturn(Optional.of(LocalDate.of(2023, JANUARY, 1)));

        perform(
            get("/web/application/export")
                .locale(locale)
                .param("from", "01.01.2022")
                .param("to", "01.01.2023")
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensuresToExportAbsencesOfTheGivenPersons() throws Exception {

        final Locale locale = JAPANESE;
        final Person signedInUser = signedInUser();

        final ApplicationForLeaveExport export = export(signedInUser);
        when(applicationForLeaveExportService.getAllForPersons(signedInUser, START_DATE, END_DATE, List.of(new PersonId(21L), new PersonId(42L))))
            .thenReturn(List.of(export));

        mockCsvFileFor(locale, List.of(export));
        mockDateParsing(locale);

        perform(get("/web/application/export")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019")
            .param("personIds", "21", "42"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));
    }

    @Test
    void ensuresStatisticsSortingAndPaginationDoNotInfluenceTheExportedPersons() throws Exception {

        final Locale locale = JAPANESE;
        final Person signedInUser = signedInUser();

        final ApplicationForLeaveExport export = export(signedInUser);
        when(applicationForLeaveExportService.getAllForPersons(signedInUser, START_DATE, END_DATE, List.of(new PersonId(21L))))
            .thenReturn(List.of(export));

        mockCsvFileFor(locale, List.of(export));
        mockDateParsing(locale);

        perform(get("/web/application/export")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019")
            .param("personIds", "21")
            // leftovers of the statistics page the export must not care about
            .param("page", "2")
            .param("size", "50")
            .param("sort", "statistics.leftVacationDaysForYear,asc")
            .param("query", "hans"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));
    }

    @Test
    void ensuresToExportAnEmptyAbsencesCsvWithoutPersonIds() throws Exception {

        final Locale locale = JAPANESE;
        final Person signedInUser = signedInUser();

        when(applicationForLeaveExportService.getAllForPersons(signedInUser, START_DATE, END_DATE, List.of()))
            .thenReturn(List.of());

        mockCsvFileFor(locale, List.of());
        mockDateParsing(locale);

        perform(get("/web/application/export")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));
    }

    @Test
    void ensuresToExportAbsencesForAll() throws Exception {

        final Locale locale = JAPANESE;
        final Person signedInUser = signedInUser();

        final ApplicationForLeaveExport export = export(signedInUser);
        when(applicationForLeaveExportService.getAll(signedInUser, START_DATE, END_DATE)).thenReturn(List.of(export));

        mockCsvFileFor(locale, List.of(export));
        mockDateParsing(locale);

        perform(get("/web/application/export")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019")
            .param("allElements", "true"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));
    }

    @Test
    void ensuresAllElementsWinsOverGivenPersonIds() throws Exception {

        final Locale locale = JAPANESE;
        final Person signedInUser = signedInUser();

        final ApplicationForLeaveExport export = export(signedInUser);
        when(applicationForLeaveExportService.getAll(signedInUser, START_DATE, END_DATE)).thenReturn(List.of(export));

        mockCsvFileFor(locale, List.of(export));
        mockDateParsing(locale);

        perform(get("/web/application/export")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019")
            .param("allElements", "true")
            .param("personIds", "21", "42"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));

        verify(applicationForLeaveExportService, never()).getAllForPersons(any(), any(), any(), any());
    }

    private Person signedInUser() {
        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        return signedInUser;
    }

    private void mockDateParsing(Locale locale) {
        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, JANUARY, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, AUGUST, 1)));
    }

    private void mockCsvFileFor(Locale locale, List<ApplicationForLeaveExport> exports) {
        final CSVFile csvFile = new CSVFile("csv-file-name", new ByteArrayResource("csv-resource".getBytes()));
        when(applicationForLeaveCsvExportService.generateCSV(FILTER_PERIOD, locale, exports)).thenReturn(csvFile);
    }

    private static ApplicationForLeaveExport export(Person person) {

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(HOLIDAY)
            .visibleToEveryone(true)
            .messageKey("messagekey.holiday")
            .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(person);
        application.setStartDate(START_DATE);
        application.setEndDate(END_DATE);
        application.setDayLength(DayLength.FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(vacationType);

        final ApplicationForLeave applicationForLeave = new ApplicationForLeave(application, workDaysByYear(START_DATE.getYear(), TEN));

        return new ApplicationForLeaveExport("1", person.getFirstName(), person.getLastName(), List.of(applicationForLeave), List.of("departmentA"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build()
            .perform(builder);
    }

    private static SortedMap<Integer, BigDecimal> workDaysByYear(int year, BigDecimal workDays) {
        final SortedMap<Integer, BigDecimal> workDaysByYear = new TreeMap<>();
        workDaysByYear.put(year, workDays);
        return workDaysByYear;
    }
}
