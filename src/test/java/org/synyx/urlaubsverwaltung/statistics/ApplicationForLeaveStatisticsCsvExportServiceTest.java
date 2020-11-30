package org.synyx.urlaubsverwaltung.statistics;

import liquibase.util.csv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationForLeaveStatisticsCsvExportServiceTest {

    private ApplicationForLeaveStatisticsCsvExportService sut;

    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        messageSource = mock(MessageSource.class);
        VacationTypeService vacationTypeService = mock(VacationTypeService.class);

        final DateFormatAware dateFormatAware = new DateFormatAware();

        sut = new ApplicationForLeaveStatisticsCsvExportService(messageSource, vacationTypeService, dateFormatAware);
    }

    @Test
    void writeStatisticsForOnePersonFor2018() {
        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final List<ApplicationForLeaveStatistics> statistics = new ArrayList<>();
        final Person person = mock(Person.class);
        when(person.getFirstName()).thenReturn("personOneFirstName");
        when(person.getLastName()).thenReturn("personOneLastName");

        final VacationTypeService vts = mock(VacationTypeService.class);
        when(vts.getVacationTypes()).thenReturn(emptyList());

        statistics.add(new ApplicationForLeaveStatistics(person, vts));

        final CSVWriter csvWriter = mock(CSVWriter.class);

        mockMessageSource("absence.period");

        mockMessageSource("person.data.firstName");
        mockMessageSource("person.data.lastName");
        mockMessageSource("applications.statistics.allowed");
        mockMessageSource("applications.statistics.waiting");
        mockMessageSource("applications.statistics.left");

        mockMessageSource("duration.vacationDays");
        mockMessageSource("duration.overtime");

        mockMessageSource("applications.statistics.total");

        sut.writeStatistics(period, statistics, csvWriter);

        verify(csvWriter, times(1))
            .writeNext(new String[]{"{absence.period}: 01.01.2018 - 31.12.2018"});
        verify(csvWriter, times(1))
            .writeNext(new String[]{"{person.data.firstName}", "{person.data.lastName}", "", "{applications.statistics.allowed}", "{applications.statistics.waiting}", "{applications.statistics.left} (2018)", ""});
        verify(csvWriter, times(1))
            .writeNext(new String[]{"", "", "", "", "", "{duration.vacationDays}", "{duration.overtime}"});
        verify(csvWriter, times(1))
            .writeNext(new String[]{"personOneFirstName", "personOneLastName", "{applications.statistics.total}", "0", "0", "0", "0"});
    }

    @Test
    void writeStatisticsForTwoPersonsFor2019() {
        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final List<ApplicationForLeaveStatistics> statistics = new ArrayList<>();
        final Person personOne = mock(Person.class);
        when(personOne.getFirstName()).thenReturn("personOneFirstName");
        when(personOne.getLastName()).thenReturn("personOneLastName");

        final Person personTwo = mock(Person.class);
        when(personTwo.getFirstName()).thenReturn("personTwoFirstName");
        when(personTwo.getLastName()).thenReturn("personTwoLastName");

        VacationTypeService vts = mock(VacationTypeService.class);
        when(vts.getVacationTypes()).thenReturn(emptyList());

        statistics.add(new ApplicationForLeaveStatistics(personOne, vts));
        statistics.add(new ApplicationForLeaveStatistics(personTwo, vts));

        final CSVWriter csvWriter = mock(CSVWriter.class);

        mockMessageSource("absence.period");

        mockMessageSource("person.data.firstName");
        mockMessageSource("person.data.lastName");
        mockMessageSource("applications.statistics.allowed");
        mockMessageSource("applications.statistics.waiting");
        mockMessageSource("applications.statistics.left");

        mockMessageSource("duration.vacationDays");
        mockMessageSource("duration.overtime");

        mockMessageSource("applications.statistics.total");

        sut.writeStatistics(period, statistics, csvWriter);

        verify(csvWriter, times(1))
            .writeNext(new String[]{"{absence.period}: 01.01.2019 - 31.12.2019"});
        verify(csvWriter, times(1))
            .writeNext(new String[]{"{person.data.firstName}", "{person.data.lastName}", "", "{applications.statistics.allowed}", "{applications.statistics.waiting}", "{applications.statistics.left} (2019)", ""});
        verify(csvWriter, times(1))
            .writeNext(new String[]{"", "", "", "", "", "{duration.vacationDays}", "{duration.overtime}"});
        verify(csvWriter, times(1))
            .writeNext(new String[]{"personOneFirstName", "personOneLastName", "{applications.statistics.total}", "0", "0", "0", "0"});
        verify(csvWriter, times(1))
            .writeNext(new String[]{"personTwoFirstName", "personTwoLastName", "{applications.statistics.total}", "0", "0", "0", "0"});
    }

    @Test
    void getFileNameForComplete2018() {
        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage("applications.statistics", new String[]{"Statistik"}, GERMAN)).thenReturn("test");

        final String fileName = sut.getFileName(period);
        assertThat(fileName).isEqualTo("test_01012018_31122018.csv");
    }

    @Test
    void getFileNameForComplete2019() {
        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage(eq("applications.statistics"), any(), any())).thenReturn("test");

        String fileName = sut.getFileName(period);
        assertThat(fileName).isEqualTo("test_01012019_31122019.csv");
    }

    private void mockMessageSource(String key) {
        when(messageSource.getMessage(eq(key), any(), any())).thenReturn(String.format("{%s}", key));
    }
}
