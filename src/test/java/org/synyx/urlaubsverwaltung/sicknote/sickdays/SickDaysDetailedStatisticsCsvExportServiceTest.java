package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import com.opencsv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.util.Locale.JAPANESE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.NO_WORKDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

@ExtendWith(MockitoExtension.class)
class SickDaysDetailedStatisticsCsvExportServiceTest {

    @Mock
    private MessageSource messageSource;

    private SickDaysDetailedStatisticsCsvExportService sut;

    @BeforeEach
    void setUp() {
        sut = new SickDaysDetailedStatisticsCsvExportService(messageSource);
    }

    @Test
    void getFileNameWithoutWhitespace() {

        final Locale locale = JAPANESE;

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage("action.sicknotes.download.filename", new String[]{}, locale)).thenReturn("test filename");

        final String fileName = sut.fileName(period, locale);
        assertThat(fileName).startsWith("test-filename_");
    }

    @Test
    void getFileNameForComplete2018() {

        final Locale locale = JAPANESE;

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage("action.sicknotes.download.filename", new String[]{}, locale)).thenReturn("test");

        final String fileName = sut.fileName(period, locale);
        assertThat(fileName).isEqualTo("test_2018-01-01_2018-12-31_ja.csv");
    }

    @Test
    void getFileNameForComplete2019() {

        final Locale locale = JAPANESE;

        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage(eq("action.sicknotes.download.filename"), any(), any())).thenReturn("test");

        final String fileName = sut.fileName(period, locale);
        assertThat(fileName).isEqualTo("test_2019-01-01_2019-12-31_ja.csv");
    }

    @Test
    void writeStatisticsForOnePerson() {

        final Locale locale = JAPANESE;

        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final Person person = new Person();
        person.setFirstName("personOneFirstName");
        person.setLastName("personOneLastName");

        final SickNoteType sickNoteTypeSick = new SickNoteType();
        sickNoteTypeSick.setCategory(SICK_NOTE);
        sickNoteTypeSick.setMessageKey("application.data.sicknotetype.sicknote");

        final SickNoteType sickNoteTypeSickChild = new SickNoteType();
        sickNoteTypeSickChild.setCategory(SICK_NOTE_CHILD);
        sickNoteTypeSickChild.setMessageKey("application.data.sicknotetype.sicknotechild");

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = workingTimeMondayToFriday(period.startDate(), period.endDate());
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);

        final SickNote sickNote = SickNote.builder()
            .dayLength(FULL)
            .startDate(startDate)
            .endDate(startDate.plusDays(1))
            .workingTimeCalendar(workingTimeCalendar)
            .sickNoteType(sickNoteTypeSick)
            .build();

        final SickNote sickNoteHalfDayMorning = SickNote.builder()
            .dayLength(MORNING)
            .startDate(startDate)
            .endDate(startDate)
            .workingTimeCalendar(workingTimeCalendar)
            .sickNoteType(sickNoteTypeSick)
            .build();

        final SickNote sickNoteHalfDayNoon = SickNote.builder()
            .dayLength(NOON)
            .startDate(startDate)
            .endDate(startDate)
            .workingTimeCalendar(workingTimeCalendar)
            .sickNoteType(sickNoteTypeSick)
            .build();

        final SickNote sickNoteWithAub = SickNote.builder()
            .dayLength(FULL)
            .startDate(startDate.plusDays(3))
            .endDate(startDate.plusDays(4))
            .workingTimeCalendar(workingTimeCalendar)
            .aubStartDate(startDate.plusDays(3))
            .aubEndDate(startDate.plusDays(4))
            .sickNoteType(sickNoteTypeSickChild)
            .build();

        final List<SickNote> sickNotes = List.of(sickNote, sickNoteWithAub, sickNoteHalfDayMorning, sickNoteHalfDayNoon);
        final List<String> departments = List.of("Here", "There");
        final SickDaysDetailedStatistics sickDaysDetailedStatistics = new
            SickDaysDetailedStatistics("42", person, sickNotes, departments);

        final List<SickDaysDetailedStatistics> statistics = List.of(sickDaysDetailedStatistics);

        addMessageSource("person.account.basedata.personnelNumber", locale);
        addMessageSource("person.data.firstName", locale);
        addMessageSource("person.data.lastName", locale);
        addMessageSource("sicknotes.statistics.departments", locale);
        addMessageSource("sicknotes.statistics.from", locale);
        addMessageSource("sicknotes.statistics.to", locale);
        addMessageSource("sicknotes.statistics.length", locale);
        addMessageSource("sicknotes.statistics.type", locale);
        addMessageSource("sicknotes.statistics.days", locale);
        addMessageSource("sicknotes.statistics.certificate.from", locale);
        addMessageSource("sicknotes.statistics.certificate.to", locale);
        addMessageSource("sicknotes.statistics.certificate.days", locale);
        addMessageSource("FULL", locale);
        addMessageSource("MORNING", locale);
        addMessageSource("NOON", locale);
        addMessageSource("application.data.sicknotetype.sicknote", locale);
        addMessageSource("application.data.sicknotetype.sicknotechild", locale);

        final CSVWriter csvWriter = mock(CSVWriter.class);
        sut.write(period, locale, statistics, csvWriter);

        verify(csvWriter).writeNext(new String[]{"{person.account.basedata.personnelNumber}", "{person.data.firstName}", "{person.data.lastName}", "{sicknotes.statistics.departments}", "{sicknotes.statistics.from}", "{sicknotes.statistics.to}", "{sicknotes.statistics.length}", "{sicknotes.statistics.days}", "{sicknotes.statistics.type}", "{sicknotes.statistics.certificate.from}", "{sicknotes.statistics.certificate.to}", "{sicknotes.statistics.certificate.days}"});
        verify(csvWriter).writeNext(new String[]{"42", "personOneFirstName", "personOneLastName", "Here, There", "2022/01/01", "2022/01/02", "{FULL}", "0", "{application.data.sicknotetype.sicknote}", null, null, null});
        verify(csvWriter).writeNext(new String[]{"42", "personOneFirstName", "personOneLastName", "Here, There", "2022/01/04", "2022/01/05", "{FULL}", "2", "{application.data.sicknotetype.sicknotechild}", "2022/01/04", "2022/01/05", "2"});
        verify(csvWriter).writeNext(new String[]{"42", "personOneFirstName", "personOneLastName", "Here, There", "2022/01/01", "2022/01/01", "{MORNING}", "0", "{application.data.sicknotetype.sicknote}", null, null, null});
        verify(csvWriter).writeNext(new String[]{"42", "personOneFirstName", "personOneLastName", "Here, There", "2022/01/01", "2022/01/01", "{NOON}", "0", "{application.data.sicknotetype.sicknote}", null, null, null});
    }

    private void addMessageSource(String key, Locale locale) {
        when(messageSource.getMessage(eq(key), any(), eq(locale))).thenReturn(String.format("{%s}", key));
    }

    private Map<LocalDate, WorkingDayInformation> workingTimeMondayToFriday(LocalDate from, LocalDate to) {
        return buildWorkingTimeByDate(from, to, date ->
            weekend(date)
                ? new WorkingDayInformation(ZERO, NO_WORKDAY, NO_WORKDAY)
                : new WorkingDayInformation(FULL, WORKDAY, WORKDAY)
        );
    }

    private boolean weekend(LocalDate date) {
        return date.getDayOfWeek().equals(SATURDAY) || date.getDayOfWeek().equals(SUNDAY);
    }

    private Map<LocalDate, WorkingDayInformation> buildWorkingTimeByDate(LocalDate from, LocalDate to, Function<LocalDate, WorkingDayInformation> dayLengthProvider) {
        Map<LocalDate, WorkingDayInformation> map = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            map.put(date, dayLengthProvider.apply(date));
        }
        return map;
    }
}
