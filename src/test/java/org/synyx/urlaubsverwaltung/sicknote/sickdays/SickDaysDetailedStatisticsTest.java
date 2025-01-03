package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Calendar.JUNE;
import static java.util.Calendar.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.WITH_AUB;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

class SickDaysDetailedStatisticsTest {

    @Test
    void ensureGetSickDays() {

        final Person person = new Person("username", "last name", "first name", "email@example.org");
        person.setId(1L);

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);

        final Map<LocalDate, WorkingDayInformation> workingTimes = buildWorkingTimeByDate(
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2022, 12, 31),
            date -> new WorkingDayInformation(FULL, WORKDAY, WORKDAY)
        );
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimes);

        final SickNote sickNoteOne = SickNote.builder()
            .id(1L)
            .person(person)
            .applier(person)
            .sickNoteType(sickNoteType)
            .startDate(LocalDate.of(2022, JUNE, 20))
            .endDate(LocalDate.of(2022, JUNE, 24))
            .dayLength(FULL)
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final SickNote sickNoteTwo = SickNote.builder()
            .id(2L)
            .person(person)
            .applier(person)
            .sickNoteType(sickNoteType)
            .startDate(LocalDate.of(2022, NOVEMBER, 7))
            .endDate(LocalDate.of(2022, NOVEMBER, 18))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2022, NOVEMBER, 14))
            .aubEndDate(LocalDate.of(2022, NOVEMBER, 18))
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final SickDaysDetailedStatistics sut =
            new SickDaysDetailedStatistics("0000001337", person, List.of(sickNoteOne, sickNoteTwo), List.of());

        final SickDays actual = sut.getSickDays(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31));

        assertThat(actual.getDays()).containsEntry(TOTAL.name(), BigDecimal.valueOf(17)); // 3 * 5days + 2weekend
        assertThat(actual.getDays()).containsEntry(WITH_AUB.name(), BigDecimal.valueOf(5));
    }

    @Test
    void ensureGetSickDaysIgnoresSickNotesOutOfDateRange() {

        final Person person = new Person("username", "last name", "first name", "email@example.org");
        person.setId(1L);

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .sickNoteType(sickNoteType)
            .startDate(LocalDate.of(2022, JUNE, 20))
            .endDate(LocalDate.of(2022, JUNE, 24))
            .build();

        final SickDaysDetailedStatistics sut =
            new SickDaysDetailedStatistics("0000001337", person, List.of(sickNote), List.of());

        final SickDays actual = sut.getSickDays(LocalDate.of(2022, 12, 1), LocalDate.of(2022, 12, 31));

        assertThat(actual.getDays()).containsEntry(TOTAL.name(), BigDecimal.ZERO);
        assertThat(actual.getDays()).containsEntry(WITH_AUB.name(), BigDecimal.ZERO);
    }

    @Test
    void ensureGetSickDaysIgnoresChildSickNotes() {

        final Person person = new Person("username", "last name", "first name", "email@example.org");
        person.setId(1L);

        final SickNoteType childSickNoteType = new SickNoteType();
        childSickNoteType.setId(1L);
        childSickNoteType.setCategory(SICK_NOTE_CHILD);

        final SickNote childSickNote = SickNote.builder()
            .id(1L)
            .sickNoteType(childSickNoteType)
            .startDate(LocalDate.of(2022, JUNE, 20))
            .endDate(LocalDate.of(2022, JUNE, 24))
            .build();

        final SickDaysDetailedStatistics sut =
            new SickDaysDetailedStatistics("0000001337", person, List.of(childSickNote), List.of());

        final SickDays actual = sut.getSickDays(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31));

        assertThat(actual.getDays()).containsEntry(TOTAL.name(), BigDecimal.ZERO);
        assertThat(actual.getDays()).containsEntry(WITH_AUB.name(), BigDecimal.ZERO);
    }

    @Test
    void ensureGetChildSickDays() {

        final Person person = new Person("username", "last name", "first name", "email@example.org");
        person.setId(1L);

        final SickNoteType childSickNoteType = new SickNoteType();
        childSickNoteType.setId(1L);
        childSickNoteType.setCategory(SICK_NOTE_CHILD);

        final Map<LocalDate, WorkingDayInformation> workingTimes = buildWorkingTimeByDate(
            LocalDate.of(2022, 1, 1),
            LocalDate.of(2022, 12, 31),
            date -> new WorkingDayInformation(FULL, WORKDAY, WORKDAY)
        );
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimes);

        final SickNote childSickNoteOne = SickNote.builder()
            .id(1L)
            .person(person)
            .applier(person)
            .sickNoteType(childSickNoteType)
            .startDate(LocalDate.of(2022, JUNE, 20))
            .endDate(LocalDate.of(2022, JUNE, 24))
            .dayLength(FULL)
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final SickNote childSickNoteTwo = SickNote.builder()
            .id(2L)
            .person(person)
            .applier(person)
            .sickNoteType(childSickNoteType)
            .startDate(LocalDate.of(2022, NOVEMBER, 7))
            .endDate(LocalDate.of(2022, NOVEMBER, 18))
            .dayLength(FULL)
            .aubStartDate(LocalDate.of(2022, NOVEMBER, 14))
            .aubEndDate(LocalDate.of(2022, NOVEMBER, 18))
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final SickDaysDetailedStatistics sut =
            new SickDaysDetailedStatistics("0000001337", person, List.of(childSickNoteOne, childSickNoteTwo), List.of());

        final SickDays actual = sut.getChildSickDays(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31));

        assertThat(actual.getDays()).containsEntry(TOTAL.name(), BigDecimal.valueOf(17)); // 3 * 5days + 2weekend
        assertThat(actual.getDays()).containsEntry(WITH_AUB.name(), BigDecimal.valueOf(5));
    }

    @Test
    void ensureGetChildSickDaysIgnoresSickNotesOutOfRange() {

        final Person person = new Person("username", "last name", "first name", "email@example.org");
        person.setId(1L);

        final SickNoteType childSickNoteType = new SickNoteType();
        childSickNoteType.setId(1L);
        childSickNoteType.setCategory(SICK_NOTE_CHILD);

        final SickNote childSickNote = SickNote.builder()
            .id(1L)
            .sickNoteType(childSickNoteType)
            .startDate(LocalDate.of(2022, JUNE, 20))
            .endDate(LocalDate.of(2022, JUNE, 24))
            .build();

        final SickDaysDetailedStatistics sut =
            new SickDaysDetailedStatistics("0000001337", person, List.of(childSickNote), List.of());

        final SickDays actual = sut.getChildSickDays(LocalDate.of(2022, 12, 1), LocalDate.of(2022, 12, 31));

        assertThat(actual.getDays()).containsEntry(TOTAL.name(), BigDecimal.ZERO);
        assertThat(actual.getDays()).containsEntry(WITH_AUB.name(), BigDecimal.ZERO);
    }

    @Test
    void ensureGetChildSickDaysIgnoresSickNotes() {

        final Person person = new Person("username", "last name", "first name", "email@example.org");
        person.setId(1L);

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(SICK_NOTE);

        final SickNote sickNote = SickNote.builder()
            .id(1L)
            .sickNoteType(sickNoteType)
            .startDate(LocalDate.of(2022, JUNE, 20))
            .endDate(LocalDate.of(2022, JUNE, 24))
            .build();

        final SickDaysDetailedStatistics sut =
            new SickDaysDetailedStatistics("0000001337", person, List.of(sickNote), List.of());

        final SickDays actual = sut.getChildSickDays(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31));

        assertThat(actual.getDays()).containsEntry(TOTAL.name(), BigDecimal.ZERO);
        assertThat(actual.getDays()).containsEntry(WITH_AUB.name(), BigDecimal.ZERO);
    }

    private Map<LocalDate, WorkingDayInformation> buildWorkingTimeByDate(LocalDate from, LocalDate to, Function<LocalDate, WorkingDayInformation> dayLengthProvider) {
        Map<LocalDate, WorkingDayInformation> map = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            map.put(date, dayLengthProvider.apply(date));
        }
        return map;
    }
}
