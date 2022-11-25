package org.synyx.urlaubsverwaltung.application.statistics;

import com.opencsv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveStatisticsCsvExportServiceTest {

    private ApplicationForLeaveStatisticsCsvExportService sut;

    @Mock
    private CSVWriter csvWriter;
    @Mock
    private MessageSource messageSource;
    @Mock
    private VacationTypeService vacationTypeService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveStatisticsCsvExportService(messageSource, vacationTypeService, new DateFormatAware());
    }

    @Test
    void writeStatisticsForOnePersonFor2018() {
        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final List<ApplicationForLeaveStatistics> statistics = new ArrayList<>();
        final Person person = new Person();
        person.setFirstName("personOneFirstName");
        person.setLastName("personOneLastName");
        final PersonBasedata basedata = new PersonBasedata(new PersonId(-1), "42", "OneInformation");

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "holiday", true, YELLOW, false);

        final ApplicationForLeaveStatistics applicationForLeaveStatistics = new ApplicationForLeaveStatistics(person);
        applicationForLeaveStatistics.setPersonBasedata(basedata);
        applicationForLeaveStatistics.setLeftVacationDaysForPeriod(BigDecimal.valueOf(10));
        applicationForLeaveStatistics.setLeftOvertimeForPeriod(Duration.ofHours(2));
        applicationForLeaveStatistics.setLeftVacationDaysForYear(BigDecimal.valueOf(20));
        applicationForLeaveStatistics.setLeftOvertimeForYear(Duration.ofHours(4));
        applicationForLeaveStatistics.addWaitingVacationDays(vacationType, ONE);
        statistics.add(applicationForLeaveStatistics);

        addMessageSource("absence.period");
        addMessageSource("person.data.firstName");
        addMessageSource("person.data.lastName");
        addMessageSource("applications.statistics.allowed");
        addMessageSource("applications.statistics.waiting");
        addMessageSource("applications.statistics.left");
        addMessageSource("duration.vacationDays");
        addMessageSource("duration.overtime");
        addMessageSource("applications.statistics.total");
        addMessageSource("person.account.basedata.personnelNumber");
        addMessageSource("person.account.basedata.additionalInformation");
        addMessageSource(vacationType.getMessageKey());

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        sut.write(period, statistics, csvWriter);
        verify(csvWriter).writeNext(new String[]{"{absence.period}: 01.01.2018 - 31.12.2018"});
        verify(csvWriter).writeNext(new String[]{"{person.account.basedata.personnelNumber}", "{person.data.firstName}", "{person.data.lastName}", "", "{applications.statistics.allowed}", "{applications.statistics.waiting}", "{applications.statistics.left}", "", "{applications.statistics.left} (2018)", "", "{person.account.basedata.additionalInformation}"});
        verify(csvWriter).writeNext(new String[]{"", "", "", "", "", "", "{duration.vacationDays}", "{duration.overtime}", "{duration.vacationDays}", "{duration.overtime}"});
        verify(csvWriter).writeNext(new String[]{null, null, null, "{holiday}", "0", "1", null, null, null, null, null});
        verify(csvWriter).writeNext(new String[]{"42", "personOneFirstName", "personOneLastName", "{applications.statistics.total}", "0", "1", "10", "2", "20", "4", "OneInformation"});
    }

    @Test
    void writeStatisticsForTwoPersonsFor2019() {
        final LocalDate startDate = LocalDate.parse("2019-01-02");
        final LocalDate endDate = LocalDate.parse("2019-12-24");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final List<ApplicationForLeaveStatistics> statistics = new ArrayList<>();
        final Person personOne = new Person();
        personOne.setFirstName("personOneFirstName");
        personOne.setLastName("personOneLastName");
        final PersonBasedata basedataOne = new PersonBasedata(new PersonId(-1), "42", "OneInformation");

        final Person personTwo = new Person();
        personTwo.setFirstName("personTwoFirstName");
        personTwo.setLastName("personTwoLastName");
        final PersonBasedata basedataTwo = new PersonBasedata(new PersonId(-1), "42", "SecondInformation");

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "holiday", true, YELLOW, false);

        final ApplicationForLeaveStatistics personOneStatistics = new ApplicationForLeaveStatistics(personOne);
        personOneStatistics.setPersonBasedata(basedataOne);
        personOneStatistics.addWaitingVacationDays(vacationType, ONE);
        personOneStatistics.setLeftVacationDaysForPeriod(BigDecimal.valueOf(10));
        personOneStatistics.setLeftOvertimeForPeriod(Duration.ofHours(2));
        personOneStatistics.setLeftVacationDaysForYear(BigDecimal.valueOf(20));
        personOneStatistics.setLeftOvertimeForYear(Duration.ofHours(4));
        statistics.add(personOneStatistics);

        final ApplicationForLeaveStatistics personTwoStatistics = new ApplicationForLeaveStatistics(personTwo);
        personTwoStatistics.setPersonBasedata(basedataTwo);
        personTwoStatistics.addAllowedVacationDays(vacationType, TEN);
        statistics.add(personTwoStatistics);

        addMessageSource("absence.period");
        addMessageSource("person.data.firstName");
        addMessageSource("person.data.lastName");
        addMessageSource("applications.statistics.allowed");
        addMessageSource("applications.statistics.waiting");
        addMessageSource("applications.statistics.left");
        addMessageSource("duration.vacationDays");
        addMessageSource("duration.overtime");
        addMessageSource("applications.statistics.total");
        addMessageSource("person.account.basedata.personnelNumber");
        addMessageSource("person.account.basedata.additionalInformation");
        addMessageSource(vacationType.getMessageKey());

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        sut.write(period, statistics, csvWriter);
        verify(csvWriter).writeNext(new String[]{"{absence.period}: 02.01.2019 - 24.12.2019"});
        verify(csvWriter).writeNext(new String[]{"{person.account.basedata.personnelNumber}", "{person.data.firstName}",
            "{person.data.lastName}", "", "{applications.statistics.allowed}", "{applications.statistics.waiting}",
            "{applications.statistics.left}", "", "{applications.statistics.left} (2019)", "",
            "{person.account.basedata.additionalInformation}"});
        verify(csvWriter).writeNext(new String[]{"", "", "", "", "", "", "{duration.vacationDays}", "{duration.overtime}", "{duration.vacationDays}", "{duration.overtime}"});
        verify(csvWriter).writeNext(new String[]{"42", "personOneFirstName", "personOneLastName", "{applications.statistics.total}", "0", "1", "10", "2", "20", "4", "OneInformation"});
        verify(csvWriter).writeNext(new String[]{null, null, null, "{holiday}", "0", "1", null, null, null, null, null});
        verify(csvWriter).writeNext(new String[]{"42", "personTwoFirstName", "personTwoLastName", "{applications.statistics.total}", "10", "0", "0", "0", "0", "0", "SecondInformation"});
        verify(csvWriter).writeNext(new String[]{null, null, null, "{holiday}", "10", "0", null, null, null, null, null});
    }

    @Test
    void getFileNameForComplete2018() {
        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage("applications.statistics", new String[]{}, GERMAN)).thenReturn("test");

        final String fileName = sut.fileName(period);
        assertThat(fileName).isEqualTo("test_01012018_31122018.csv");
    }

    @Test
    void getFileNameForComplete2019() {
        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage(eq("applications.statistics"), any(), any())).thenReturn("test");

        String fileName = sut.fileName(period);
        assertThat(fileName).isEqualTo("test_01012019_31122019.csv");
    }

    private void addMessageSource(String key) {
        when(messageSource.getMessage(eq(key), any(), any())).thenReturn(String.format("{%s}", key));
    }
}
