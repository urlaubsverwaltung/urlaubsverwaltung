package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class WorkingTimeCalendarTest {

    @Test
    void ensureWorkingTimeForApplicationOverTwoDaysWhenWorkingFull() {

        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, DayLength> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> DayLength.FULL);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        final Application application = new Application();
        application.setStartDate(from.plusDays(1));
        application.setEndDate(from.plusDays(2));
        application.setDayLength(DayLength.FULL);

        final BigDecimal actual = sut.workingTime(application);

        assertThat(actual).isEqualTo(BigDecimal.valueOf(2));
    }

    @Test
    void ensureWorkingTimeForApplicationHalfDayWhenWorkingFull() {

        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, DayLength> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> DayLength.FULL);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        final Application application = new Application();
        application.setStartDate(from.plusDays(1));
        application.setEndDate(from.plusDays(1));
        application.setDayLength(DayLength.NOON);

        final BigDecimal actual = sut.workingTime(application);

        assertThat(actual).isEqualTo(BigDecimal.valueOf(0.5));
    }

    @ParameterizedTest
    @EnumSource(value = DayLength.class, names = {"MORNING", "NOON"})
    void ensureWorkingTimeForApplicationFullDayWhenWorkingWith(DayLength workingDayLength) {

        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, DayLength> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> workingDayLength);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        final Application application = new Application();
        application.setStartDate(from.plusDays(1));
        application.setEndDate(from.plusDays(1));
        application.setDayLength(DayLength.FULL);

        assertThat(sut.workingTime(application)).isEqualTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void ensureWorkingTimeForApplicationWithStartInDateRange() {

        final LocalDate applicationFrom = LocalDate.of(2023, 3, 31);
        final LocalDate applicationTo = LocalDate.of(2023, 4, 2);
        final DateRange marchDateRange = new DateRange(LocalDate.of(2023, 3, 1), LocalDate.of(2023, 3, 31));


        final Map<LocalDate, DayLength> workingTimeByDate = buildWorkingTimeByDate(applicationFrom, applicationTo, date -> DayLength.FULL);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        final Application application = new Application();
        application.setStartDate(applicationFrom);
        application.setEndDate(applicationTo);
        application.setDayLength(DayLength.FULL);

        final BigDecimal actual = sut.workingTimeInDateRage(application, marchDateRange);

        assertThat(actual).isEqualTo(BigDecimal.valueOf(1));
    }

    @Test
    void ensureWorkingTimeForApplicationWithEndInDateRange() {

        final LocalDate applicationFrom = LocalDate.of(2023, 3, 31);
        final LocalDate applicationTo = LocalDate.of(2023, 4, 2);
        final DateRange aprilDateRange = new DateRange(LocalDate.of(2023, 4, 1), LocalDate.of(2023, 4, 30));

        final Map<LocalDate, DayLength> workingTimeByDate = buildWorkingTimeByDate(applicationFrom, applicationTo, date -> DayLength.FULL);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        final Application application = new Application();
        application.setStartDate(applicationFrom);
        application.setEndDate(applicationTo);
        application.setDayLength(DayLength.FULL);

        final BigDecimal actual = sut.workingTimeInDateRage(application, aprilDateRange);

        assertThat(actual).isEqualTo(BigDecimal.valueOf(2));
    }

    @Test
    void ensureWorkingTimeForLocalDateWhenWorkingFull() {
        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, DayLength> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> DayLength.FULL);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        final Optional<BigDecimal> actual = sut.workingTime(LocalDate.of(2022, 8, 10));

        assertThat(actual).hasValue(BigDecimal.valueOf(1));
    }

    @Test
    void ensureWorkingTimeForLocalDateReturnsEmptyOptionalForDateOutOfRange() {
        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, DayLength> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> DayLength.FULL);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        assertThat(sut.workingTime(LocalDate.of(2022, 7, 31))).isEmpty();
        assertThat(sut.workingTime(LocalDate.of(2022, 9, 1))).isEmpty();
    }

    @Test
    void ensureWorkingTimeForDateRangeWhenWorkingFull() {
        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, DayLength> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> DayLength.FULL);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        assertThat(sut.workingTime(from, to)).isEqualTo(BigDecimal.valueOf(31));
        assertThat(sut.workingTime(from.plusDays(10), to)).isEqualTo(BigDecimal.valueOf(21));
    }

    @Test
    void ensureWorkingTimeForDateRangeForFalsyDateRangeWhenWorkingFull() {
        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, DayLength> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> DayLength.FULL);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        assertThat(sut.workingTime(to, from)).isEqualTo(BigDecimal.ZERO);
    }

    @ParameterizedTest
    @EnumSource(value = DayLength.class, names = {"MORNING", "NOON"})
    void ensureWorkingTimeForDateRangeWhenWorkingNotFull(DayLength workingDayLength) {
        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, DayLength> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> workingDayLength);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        assertThat(sut.workingTime(from, to)).isEqualTo(BigDecimal.valueOf(15.5));
        assertThat(sut.workingTime(from.plusDays(10), to)).isEqualTo(BigDecimal.valueOf(10.5));
    }

    @ParameterizedTest
    @EnumSource(value = DayLength.class, names = {"MORNING", "NOON"})
    void ensureWorkingTimeForDateRangeForFalsyDateRangeWhenWorkingNotFull(DayLength workingDayLength) {
        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, DayLength> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> workingDayLength);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        assertThat(sut.workingTime(to, from)).isEqualTo(BigDecimal.ZERO);
    }

    private Map<LocalDate, DayLength> buildWorkingTimeByDate(LocalDate from, LocalDate to, Function<LocalDate, DayLength> dayLengthProvider) {
        Map<LocalDate, DayLength> map = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            map.put(date, dayLengthProvider.apply(date));
        }
        return map;
    }
}
