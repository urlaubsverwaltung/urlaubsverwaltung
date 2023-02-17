package org.synyx.urlaubsverwaltung.application.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationForLeave;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
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

    private static final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveExportViewController(personService, applicationForLeaveExportService,
            applicationForLeaveCsvExportService, new DateFormatAware(), clock);
    }

    @Test
    void ensuresToDownloadCSVIfNotTheSameYearIsABadRequest() throws Exception {
        perform(get("/web/application/export")
            .param("from", "01.01.2022")
            .param("to", "01.01.2023"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ensuresToExportAbsences() throws Exception {

        final Locale locale = JAPANESE;

        final Person signedInUser = new Person();
        signedInUser.setId(1);
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-08-01");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final VacationTypeEntity vacationTypeEntity = new VacationTypeEntity();
        vacationTypeEntity.setId(1);
        vacationTypeEntity.setVisibleToEveryone(true);
        vacationTypeEntity.setCategory(HOLIDAY);
        vacationTypeEntity.setMessageKey("messagekey.holiday");

        final Application application = new Application();
        application.setId(42);
        application.setPerson(signedInUser);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(DayLength.FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(vacationTypeEntity);

        when(workDaysCountService.getWorkDaysCount(application.getDayLength(), application.getStartDate(), application.getEndDate(), application.getPerson())).thenReturn(TEN);
        final ApplicationForLeave applicationForLeave = new ApplicationForLeave(application, workDaysCountService);

        final ApplicationForLeaveExport applicationForLeaveExport = new ApplicationForLeaveExport("1", signedInUser.getFirstName(), signedInUser.getLastName(), List.of(applicationForLeave), List.of("departmentA"));
        when(applicationForLeaveExportService.getAll(signedInUser, startDate, endDate, defaultPersonSearchQuery())).thenReturn(new PageImpl<>(List.of(applicationForLeaveExport)));

        final CSVFile csvFile = new CSVFile("csv-file-name", new ByteArrayResource("csv-resource".getBytes()));
        when(applicationForLeaveCsvExportService.generateCSV(filterPeriod, locale, List.of(applicationForLeaveExport))).thenReturn(csvFile);

        perform(get("/web/application/export")
            .locale(locale)
            .param("from", "01.01.2019")
            .param("to", "01.08.2019"))
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
