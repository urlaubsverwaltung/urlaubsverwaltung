package org.synyx.urlaubsverwaltung.application.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.math.BigDecimal.TEN;
import static java.util.Locale.JAPANESE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveExportViewControllerTest {

    private ApplicationForLeaveExportViewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private ApplicationForLeaveExportService applicationForLeaveExportService;
    @Mock
    private ApplicationForLeaveCsvExportService applicationForLeaveCsvExportService;
    @Mock
    private WorkDaysCountService workDaysCountService;
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

        when(dateFormatAware.parse("01.01.2022", locale)).thenReturn(Optional.of(LocalDate.of(2022, 1, 1)));
        when(dateFormatAware.parse("01.01.2023", locale)).thenReturn(Optional.of(LocalDate.of(2023, 1, 1)));

        perform(
            get("/web/application/export")
                .locale(locale)
                .param("from", "01.01.2022")
                .param("to", "01.01.2023")
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensuresToExportAbsencesForSelectionWithDefaultValues() throws Exception {

        final Locale locale = JAPANESE;

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(HOLIDAY)
            .visibleToEveryone(true)
            .messageKey("messagekey.holiday")
            .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(signedInUser);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(DayLength.FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(vacationType);

        when(workDaysCountService.getWorkDaysCount(application.getDayLength(), application.getStartDate(), application.getEndDate(), application.getPerson())).thenReturn(TEN);
        final ApplicationForLeave applicationForLeave = new ApplicationForLeave(application, workDaysCountService);

        final ApplicationForLeaveExport applicationForLeaveExport = new ApplicationForLeaveExport("1", signedInUser.getFirstName(), signedInUser.getLastName(), List.of(applicationForLeave), List.of("departmentA"));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "person.firstName")), "");
        when(applicationForLeaveExportService.getAll(signedInUser, startDate, endDate, pageableSearchQuery)).thenReturn(new PageImpl<>(List.of(applicationForLeaveExport)));

        final CSVFile csvFile = new CSVFile("csv-file-name", new ByteArrayResource("csv-resource".getBytes()));
        when(applicationForLeaveCsvExportService.generateCSV(filterPeriod, locale, List.of(applicationForLeaveExport))).thenReturn(csvFile);

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 8, 1)));

        perform(get("/web/application/export")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));
    }

    @Test
    void ensuresToExportAbsencesForSelectionWithDifferentPageAndSize() throws Exception {

        final Locale locale = JAPANESE;

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(HOLIDAY)
            .visibleToEveryone(true)
            .messageKey("messagekey.holiday")
            .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(signedInUser);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(DayLength.FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(vacationType);

        when(workDaysCountService.getWorkDaysCount(application.getDayLength(), application.getStartDate(), application.getEndDate(), application.getPerson())).thenReturn(TEN);
        final ApplicationForLeave applicationForLeave = new ApplicationForLeave(application, workDaysCountService);

        final ApplicationForLeaveExport applicationForLeaveExport = new ApplicationForLeaveExport("1", signedInUser.getFirstName(), signedInUser.getLastName(), List.of(applicationForLeave), List.of("departmentA"));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(2, 50, Sort.by(Sort.Direction.ASC, "person.firstName")), "");
        when(applicationForLeaveExportService.getAll(signedInUser, startDate, endDate, pageableSearchQuery)).thenReturn(new PageImpl<>(List.of(applicationForLeaveExport)));

        final CSVFile csvFile = new CSVFile("csv-file-name", new ByteArrayResource("csv-resource".getBytes()));
        when(applicationForLeaveCsvExportService.generateCSV(filterPeriod, locale, List.of(applicationForLeaveExport))).thenReturn(csvFile);

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 8, 1)));

        perform(get("/web/application/export")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019")
            .param("page", "2")
            .param("size", "50"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));
    }

    @Test
    void ensuresToExportAbsencesForAll() throws Exception {

        final Locale locale = JAPANESE;

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 8, 1)));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(HOLIDAY)
            .visibleToEveryone(true)
            .messageKey("messagekey.holiday")
            .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(signedInUser);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(DayLength.FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(vacationType);

        when(workDaysCountService.getWorkDaysCount(application.getDayLength(), application.getStartDate(), application.getEndDate(), application.getPerson())).thenReturn(TEN);
        final ApplicationForLeave applicationForLeave = new ApplicationForLeave(application, workDaysCountService);

        final ApplicationForLeaveExport applicationForLeaveExport = new ApplicationForLeaveExport("1", signedInUser.getFirstName(), signedInUser.getLastName(), List.of(applicationForLeave), List.of("departmentA"));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "person.firstName")), "");
        when(applicationForLeaveExportService.getAll(signedInUser, startDate, endDate, pageableSearchQuery)).thenReturn(new PageImpl<>(List.of(applicationForLeaveExport)));

        final CSVFile csvFile = new CSVFile("csv-file-name", new ByteArrayResource("csv-resource".getBytes()));
        when(applicationForLeaveCsvExportService.generateCSV(filterPeriod, locale, List.of(applicationForLeaveExport))).thenReturn(csvFile);

        perform(get("/web/application/export")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019")
            .param("allElements", "true"))
            .andExpect(status().isOk())
            .andExpect(content().string("csv-resource"));
    }

    @Test
    void ensuresToExportAbsencesForAllWithSelectionParametersAndAllElementsShouldWin() throws Exception {

        final Locale locale = JAPANESE;

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(HOLIDAY)
            .visibleToEveryone(true)
            .messageKey("messagekey.holiday")
            .build();

        final Application application = new Application();
        application.setId(42L);
        application.setPerson(signedInUser);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(DayLength.FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(vacationType);

        when(workDaysCountService.getWorkDaysCount(application.getDayLength(), application.getStartDate(), application.getEndDate(), application.getPerson())).thenReturn(TEN);
        final ApplicationForLeave applicationForLeave = new ApplicationForLeave(application, workDaysCountService);

        final ApplicationForLeaveExport applicationForLeaveExport = new ApplicationForLeaveExport("1", signedInUser.getFirstName(), signedInUser.getLastName(), List.of(applicationForLeave), List.of("departmentA"));
        final PageableSearchQuery pageableSearchQuery = new PageableSearchQuery(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "person.firstName")), "");
        when(applicationForLeaveExportService.getAll(signedInUser, startDate, endDate, pageableSearchQuery)).thenReturn(new PageImpl<>(List.of(applicationForLeaveExport)));

        final CSVFile csvFile = new CSVFile("csv-file-name", new ByteArrayResource("csv-resource".getBytes()));
        when(applicationForLeaveCsvExportService.generateCSV(filterPeriod, locale, List.of(applicationForLeaveExport))).thenReturn(csvFile);

        when(dateFormatAware.parse("01.01.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 1, 1)));
        when(dateFormatAware.parse("01.08.2019", locale)).thenReturn(Optional.of(LocalDate.of(2019, 8, 1)));

        perform(get("/web/application/export")
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

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build()
            .perform(builder);
    }
}
