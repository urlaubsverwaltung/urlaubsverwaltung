package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

class SickNoteTest {

    @Test
    void ensureGetWorkDays() {

        final Map<LocalDate, DayLength> workingTimes = buildWorkingTimeByDate(LocalDate.of(2022, JUNE, 1), LocalDate.of(2022, JUNE, 30), (date) -> FULL);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimes);

        final SickNote sickNote = SickNote.builder()
                .dayLength(FULL)
                .startDate(LocalDate.of(2022, JUNE, 13))
                .endDate(LocalDate.of(2022, JUNE, 24))
                .workingTimeCalendar(workingTimeCalendar)
                .build();

        final BigDecimal actual = sickNote.getWorkDays(LocalDate.of(2022, JUNE, 1), LocalDate.of(2022, JUNE, 30));

        assertThat(actual).isEqualTo(BigDecimal.valueOf(12));
    }

    @ParameterizedTest
    @EnumSource(value = DayLength.class, names = { "MORNING", "NOON" })
    void ensureGetWorkDaysHalfDay(DayLength givenDayLength) {

        final Map<LocalDate, DayLength> workingTimes = buildWorkingTimeByDate(LocalDate.of(2022, JUNE, 1), LocalDate.of(2022, JUNE, 30), (date) -> FULL);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimes);

        final SickNote sickNote = SickNote.builder()
                .dayLength(givenDayLength)
                .startDate(LocalDate.of(2022, JUNE, 2))
                .endDate(LocalDate.of(2022, JUNE, 2))
                .workingTimeCalendar(workingTimeCalendar)
                .build();

        final BigDecimal actual = sickNote.getWorkDays(LocalDate.of(2022, JUNE, 1), LocalDate.of(2022, JUNE, 30));

        assertThat(actual).isEqualTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void ensureGetWorkDaysWhenDateRangeIsAfterAubDate() {

        final SickNote sickNote = SickNote.builder()
                .startDate(LocalDate.of(2022, JUNE, 13))
                .endDate(LocalDate.of(2022, JUNE, 24))
                .build();

        final BigDecimal actual = sickNote.getWorkDays(LocalDate.of(2022, DECEMBER, 1), LocalDate.of(2022, DECEMBER, 31));

        assertThat(actual).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void ensureGetWorkDaysWhenDateRangeIsBeforeAubDate() {

        final SickNote sickNote = SickNote.builder()
                .startDate(LocalDate.of(2022, JUNE, 13))
                .endDate(LocalDate.of(2022, JUNE, 24))
                .build();

        final BigDecimal actual = sickNote.getWorkDays(LocalDate.of(2022, JANUARY, 1), LocalDate.of(2022, JANUARY, 31));

        assertThat(actual).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void ensureGetWorkDaysWithAub() {

        final Map<LocalDate, DayLength> workingTimes = buildWorkingTimeByDate(LocalDate.of(2022, JUNE, 1), LocalDate.of(2022, JUNE, 30), (date) -> FULL);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimes);

        final SickNote sickNote = SickNote.builder()
                .dayLength(FULL)
                .startDate(LocalDate.of(2022, JUNE, 13))
                .endDate(LocalDate.of(2022, JUNE, 24))
                .aubStartDate(LocalDate.of(2022, JUNE, 20))
                .aubEndDate(LocalDate.of(2022, JUNE, 24))
                .workingTimeCalendar(workingTimeCalendar)
                .build();

        final BigDecimal actual = sickNote.getWorkDaysWithAub(LocalDate.of(2022, JUNE, 1), LocalDate.of(2022, JUNE, 30));

        assertThat(actual).isEqualTo(BigDecimal.valueOf(5));
    }

    @ParameterizedTest
    @EnumSource(value = DayLength.class, names = { "MORNING", "NOON" })
    void ensureGetWorkDaysWithAubHalfDay(DayLength givenDayLength) {

        final Map<LocalDate, DayLength> workingTimes = buildWorkingTimeByDate(LocalDate.of(2022, JUNE, 1), LocalDate.of(2022, JUNE, 30), (date) -> FULL);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimes);

        final SickNote sickNote = SickNote.builder()
                .dayLength(givenDayLength)
                .startDate(LocalDate.of(2022, JUNE, 2))
                .endDate(LocalDate.of(2022, JUNE, 2))
                .aubStartDate(LocalDate.of(2022, JUNE, 2))
                .aubEndDate(LocalDate.of(2022, JUNE, 2))
                .workingTimeCalendar(workingTimeCalendar)
                .build();

        final BigDecimal actual = sickNote.getWorkDaysWithAub(LocalDate.of(2022, JUNE, 1), LocalDate.of(2022, JUNE, 30));

        assertThat(actual).isEqualTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void ensureGetWorkDaysWithAubWhenAubNotPresent() {

        final SickNote sickNote = SickNote.builder().build();
        final BigDecimal actual = sickNote.getWorkDaysWithAub(LocalDate.of(2022, DECEMBER, 1), LocalDate.of(2022, DECEMBER, 31));

        assertThat(actual).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void ensureGetWorkDaysWithAubWhenDateRangeIsAfterAubDate() {

        final SickNote sickNote = SickNote.builder()
                .startDate(LocalDate.of(2022, JUNE, 13))
                .endDate(LocalDate.of(2022, JUNE, 24))
                .aubStartDate(LocalDate.of(2022, JUNE, 20))
                .aubEndDate(LocalDate.of(2022, JUNE, 24))
                .build();

        final BigDecimal actual = sickNote.getWorkDaysWithAub(LocalDate.of(2022, DECEMBER, 1), LocalDate.of(2022, DECEMBER, 31));

        assertThat(actual).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void ensureGetWorkDaysWithAubWhenDateRangeIsBeforeAubDate() {

        final SickNote sickNote = SickNote.builder()
                .startDate(LocalDate.of(2022, JUNE, 13))
                .endDate(LocalDate.of(2022, JUNE, 24))
                .aubStartDate(LocalDate.of(2022, JUNE, 20))
                .aubEndDate(LocalDate.of(2022, JUNE, 24))
                .build();

        final BigDecimal actual = sickNote.getWorkDaysWithAub(LocalDate.of(2022, JANUARY, 1), LocalDate.of(2022, JANUARY, 31));

        assertThat(actual).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void ensureAUBIsPresentIfAUBStartDateAndAUBEndDateAreSet() {

        final SickNote sickNote = SickNote.builder()
                .aubStartDate(LocalDate.now(UTC))
                .aubEndDate(LocalDate.now(UTC))
                .build();

        assertThat(sickNote.isAubPresent()).isTrue();
    }

    @Test
    void ensureAUBIsNotPresentIfOnlyAUBStartDateIsSet() {

        final SickNote sickNote = SickNote.builder()
                .aubStartDate(LocalDate.now(UTC))
                .build();

        assertThat(sickNote.isAubPresent()).isFalse();
    }

    @Test
    void ensureAUBIsNotPresentIfOnlyAUBEndDateIsSet() {

        final SickNote sickNote = SickNote.builder()
                .aubEndDate(LocalDate.now(UTC))
                .build();

        assertThat(sickNote.isAubPresent()).isFalse();
    }

    @Test
    void ensureAUBIsNotPresentIfNoAUBPeriodIsSet() {
        assertThat(SickNote.builder().build().isAubPresent()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = SickNoteStatus.class, names = {"CANCELLED", "CONVERTED_TO_VACATION"})
    void ensureIsNotActiveForInactiveStatus(SickNoteStatus status) {
        final SickNote sickNote = SickNote.builder().status(status).build();
        assertThat(sickNote.isActive()).isFalse();
    }

    @Test
    void ensureIsActiveForActiveStatus() {
        final SickNote sickNote = SickNote.builder().status(SickNoteStatus.ACTIVE).build();
        assertThat(sickNote.isActive()).isTrue();
    }

    @Test
    void ensureGetPeriodReturnsCorrectPeriod() {

        final LocalDate startDate = LocalDate.now(UTC);
        final LocalDate endDate = startDate.plusDays(2);

        final SickNote sickNote = SickNote.builder()
                .startDate(startDate)
                .endDate(endDate)
                .dayLength(FULL)
                .build();

        final Period period = sickNote.getPeriod();
        assertThat(period).isNotNull();
        assertThat(period.getStartDate()).isEqualTo(startDate);
        assertThat(period.getEndDate()).isEqualTo(endDate);
        assertThat(period.getDayLength()).isEqualTo(FULL);
    }

    @Test
    void nullsafeToString() {
        final SickNote sickNote = SickNote.builder().lastEdited(null).build();
        assertThat(sickNote).hasToString("SickNote{id=null, person=null, applier=null, " +
            "sickNoteType=null, startDate=null," +
            " endDate=null, dayLength=null, aubStartDate=null, aubEndDate=null, lastEdited=null," +
            " endOfSickPayNotificationSend=null, status=null, workDays=0, workDaysWithAub=0}");
    }

    @Test
    void toStringTest() {
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        sickNoteType.setMessageKey("messageKey");

        final Person person = new Person();
        person.setId(1L);

        final Person applier = new Person();
        applier.setId(2L);

        final Map<LocalDate, DayLength> workingTimes = buildWorkingTimeByDate(LocalDate.of(2022, JANUARY, 1), LocalDate.of(2022, DECEMBER, 31), (date) -> FULL);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimes);

        final SickNote sickNote = SickNote.builder()
                .id(1L)
                .sickNoteType(sickNoteType)
                .startDate(LocalDate.of(2022, JANUARY, 1))
                .endDate(LocalDate.of(2022, JANUARY, 31))
                .status(SickNoteStatus.ACTIVE)
                .dayLength(FULL)
                .aubStartDate(LocalDate.of(2022, JANUARY, 17))
                .aubEndDate(LocalDate.of(2022, JANUARY, 21))
                .lastEdited(LocalDate.EPOCH)
                .endOfSickPayNotificationSend(LocalDate.EPOCH)
                .person(person)
                .applier(applier)
                .workingTimeCalendar(workingTimeCalendar)
                .build();

        assertThat(sickNote).hasToString("SickNote{id=1, person=Person{id='1'}, " +
            "applier=Person{id='2'}, sickNoteType=SickNoteType{category=SICK_NOTE, messageKey='messageKey'}, startDate=2022-01-01, " +
            "endDate=2022-01-31, dayLength=FULL, aubStartDate=2022-01-17, aubEndDate=2022-01-21," +
            " lastEdited=1970-01-01, endOfSickPayNotificationSend=1970-01-01, status=ACTIVE, workDays=31, workDaysWithAub=5}");
    }

    private Map<LocalDate, DayLength> buildWorkingTimeByDate(LocalDate from, LocalDate to, Function<LocalDate, DayLength> dayLengthProvider) {
        Map<LocalDate, DayLength> map = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            map.put(date, dayLengthProvider.apply(date));
        }
        return map;
    }
}
