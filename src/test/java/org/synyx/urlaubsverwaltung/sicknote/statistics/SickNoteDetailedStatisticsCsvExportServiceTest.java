package org.synyx.urlaubsverwaltung.sicknote.statistics;

import liquibase.util.csv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.List;

import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SickNoteDetailedStatisticsCsvExportServiceTest {

    @Mock
    private MessageSource messageSource;
    @Mock
    private CSVWriter csvWriter;

    SickNoteDetailedStatisticsCsvExportService sut;

    @BeforeEach
    void setUp() {
        sut = new SickNoteDetailedStatisticsCsvExportService(messageSource, new DateFormatAware());
    }

    @Test
    void getFileNameForComplete2018() {
        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage("sicknote.statistics", new String[]{}, GERMAN)).thenReturn("test");

        final String fileName = sut.getFileName(period);
        assertThat(fileName).isEqualTo("test_01012018_31122018.csv");
    }

    @Test
    void getFileNameForComplete2019() {
        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage(eq("sicknote.statistics"), any(), any())).thenReturn("test");

        String fileName = sut.getFileName(period);
        assertThat(fileName).isEqualTo("test_01012019_31122019.csv");
    }

    @Test
    void writeStatisticsForOnePerson() {
        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final Person person = new Person();
        person.setFirstName("personOneFirstName");
        person.setLastName("personOneLastName");

        final SickNoteType sickNoteType = new SickNoteType();

        final SickNote sickNote = new SickNote();
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(startDate.plusDays(1));
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        sickNote.setSickNoteType(sickNoteType);

        final SickNote sickNoteWithAub = new SickNote();
        sickNoteWithAub.setStartDate(startDate.plusDays(3));
        sickNoteWithAub.setEndDate(startDate.plusDays(4));
        sickNoteWithAub.setAubStartDate(startDate.plusDays(3));
        sickNoteWithAub.setAubEndDate(startDate.plusDays(4));

        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE_CHILD);
        sickNoteWithAub.setSickNoteType(sickNoteType);

        List<SickNote> sicknotes = List.of(sickNote,sickNoteWithAub);
        List<String> departments = List.of("Here", "There");
        final SickNoteDetailedStatistics sickNoteDetailedStatistics = new
            SickNoteDetailedStatistics("42", person.getFirstName(), person.getLastName(), sicknotes, departments);

        final List<SickNoteDetailedStatistics> statistics = List.of(sickNoteDetailedStatistics);

        addMessageSource("absence.period");
        addMessageSource("person.account.basedata.personnelNumber");
        addMessageSource("person.data.firstName");
        addMessageSource("person.data.lastName");
        addMessageSource("sicknotes.statistics.departments");
        addMessageSource("sicknotes.statistics.from");
        addMessageSource("sicknotes.statistics.to");
        addMessageSource("sicknotes.statistics.type");
        addMessageSource("sicknotes.statistics.certificate");

        sut.writeStatistics(period, statistics, csvWriter);

        verify(csvWriter).writeNext(new String[]{"{absence.period}: 01.01.2022 - 31.12.2022"});
        verify(csvWriter).writeNext(new String[]{"{person.account.basedata.personnelNumber}", "{person.data.firstName}", "{person.data.lastName}", "{sicknotes.statistics.departments}", "{sicknotes.statistics.from}", "{sicknotes.statistics.to}", "{sicknotes.statistics.type}", "{sicknotes.statistics.certificate}"});
        verify(csvWriter).writeNext(new String[]{"42", "personOneFirstName", "personOneLastName", "Here, There", null, null, null, null});
        verify(csvWriter).writeNext(new String[]{null, null, null, null, "01.01.2022", "02.01.2022", null, ""});
        verify(csvWriter).writeNext(new String[]{null, null, null, null, "04.01.2022", "05.01.2022", null, "04.01.2022-05.01.2022"});
    }

    private void addMessageSource(String key) {
        when(messageSource.getMessage(eq(key), any(), any())).thenReturn(String.format("{%s}", key));
    }
}
