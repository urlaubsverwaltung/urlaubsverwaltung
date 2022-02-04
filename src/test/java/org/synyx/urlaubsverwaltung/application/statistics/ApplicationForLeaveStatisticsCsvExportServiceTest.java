package org.synyx.urlaubsverwaltung.application.statistics;

import liquibase.util.csv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.person.BasedataType.ADDITIONAL_INFORMATION;
import static org.synyx.urlaubsverwaltung.person.BasedataType.PERSONNEL_NUMBER;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveStatisticsCsvExportServiceTest {

    private ApplicationForLeaveStatisticsCsvExportService sut;

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
        person.setBasedata(Map.of(ADDITIONAL_INFORMATION, "OneInformation"));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);

        final ApplicationForLeaveStatistics applicationForLeaveStatistics = new ApplicationForLeaveStatistics(person);
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
        addMessageSource("person.account.basedata.PERSONNEL_NUMBER");
        addMessageSource("person.account.basedata.ADDITIONAL_INFORMATION");

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final CSVWriter csvWriter = mock(CSVWriter.class);
        sut.writeStatistics(period, statistics, csvWriter);
        verify(csvWriter).writeNext(new String[]{"{absence.period}: 01.01.2018 - 31.12.2018"});
        verify(csvWriter).writeNext(new String[]{"{person.account.basedata.PERSONNEL_NUMBER}","{person.data.firstName}", "{person.data.lastName}", "{person.account.basedata.ADDITIONAL_INFORMATION}", "", "{applications.statistics.allowed}", "{applications.statistics.waiting}", "{applications.statistics.left} (2018)", ""});
        verify(csvWriter).writeNext(new String[]{"", "", "", "", "", "", "", "{duration.vacationDays}", "{duration.overtime}"});
        verify(csvWriter).writeNext(new String[]{null, null, null, null, null, "0", "1", null, null});
        verify(csvWriter).writeNext(new String[]{"", "personOneFirstName", "personOneLastName", "OneInformation", "{applications.statistics.total}", "0", "1", "0", "0"});
    }

    @Test
    void writeStatisticsForTwoPersonsFor2019() {
        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final List<ApplicationForLeaveStatistics> statistics = new ArrayList<>();
        final Person personOne = new Person();
        personOne.setFirstName("personOneFirstName");
        personOne.setLastName("personOneLastName");
        personOne.setBasedata(Map.of(ADDITIONAL_INFORMATION, "OneInformation"));

        final Person personTwo = new Person();
        personTwo.setFirstName("personTwoFirstName");
        personTwo.setLastName("personTwoLastName");
        personTwo.setBasedata(Map.of(PERSONNEL_NUMBER, "42"));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);

        final ApplicationForLeaveStatistics personOneStatistics = new ApplicationForLeaveStatistics(personOne);
        personOneStatistics.addWaitingVacationDays(vacationType, ONE);
        statistics.add(personOneStatistics);

        final ApplicationForLeaveStatistics personTwoStatistics = new ApplicationForLeaveStatistics(personTwo);
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
        addMessageSource("person.account.basedata.PERSONNEL_NUMBER");
        addMessageSource("person.account.basedata.ADDITIONAL_INFORMATION");

        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final CSVWriter csvWriter = mock(CSVWriter.class);
        sut.writeStatistics(period, statistics, csvWriter);

        verify(csvWriter).writeNext(new String[]{"{absence.period}: 01.01.2019 - 31.12.2019"});
        verify(csvWriter).writeNext(new String[]{"{person.account.basedata.PERSONNEL_NUMBER}","{person.data.firstName}", "{person.data.lastName}", "{person.account.basedata.ADDITIONAL_INFORMATION}", "", "{applications.statistics.allowed}", "{applications.statistics.waiting}", "{applications.statistics.left} (2019)", ""});
        verify(csvWriter).writeNext(new String[]{"", "", "", "", "", "", "", "{duration.vacationDays}", "{duration.overtime}"});
        verify(csvWriter).writeNext(new String[]{null, null, null, null, null, "0", "1", null, null});
        verify(csvWriter).writeNext(new String[]{"", "personOneFirstName", "personOneLastName", "OneInformation", "{applications.statistics.total}", "0", "1", "0", "0"});
        verify(csvWriter).writeNext(new String[]{null, null, null, null, null, "10", "0", null, null});
        verify(csvWriter).writeNext(new String[]{"42", "personTwoFirstName", "personTwoLastName", "", "{applications.statistics.total}", "10", "0", "0", "0"});
    }

    @Test
    void getFileNameForComplete2018() {
        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage("applications.statistics", new String[]{}, GERMAN)).thenReturn("test");

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

    private void addMessageSource(String key) {
        when(messageSource.getMessage(eq(key), any(), any())).thenReturn(String.format("{%s}", key));
    }
}
