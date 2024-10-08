package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.NO_WORKDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

class WorkingTimeCalendarTest {

    @Test
    void ensureNextWorkingFollowingTo() {

        final LocalDate from = LocalDate.of(2024, 7, 1);
        final LocalDate to = LocalDate.of(2024, 7, 31);

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> {
            if (List.of(WEDNESDAY, SATURDAY, SUNDAY).contains(date.getDayOfWeek())) {
                return emptyWorkingDayInformation();
            } else {
                return fullWorkingDayInformation();
            }
        });

        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        assertThat(sut.nextWorkingFollowingTo(LocalDate.of(2024, 6, 30))).hasValue(LocalDate.of(2024, 7, 1));
        assertThat(sut.nextWorkingFollowingTo(LocalDate.of(2024, 7, 15))).hasValue(LocalDate.of(2024, 7, 16));
        assertThat(sut.nextWorkingFollowingTo(LocalDate.of(2024, 7, 16))).hasValue(LocalDate.of(2024, 7, 18));
        assertThat(sut.nextWorkingFollowingTo(LocalDate.of(2024, 7, 17))).hasValue(LocalDate.of(2024, 7, 18));
        assertThat(sut.nextWorkingFollowingTo(LocalDate.of(2024, 7, 18))).hasValue(LocalDate.of(2024, 7, 19));
        assertThat(sut.nextWorkingFollowingTo(LocalDate.of(2024, 7, 19))).hasValue(LocalDate.of(2024, 7, 22));
        assertThat(sut.nextWorkingFollowingTo(LocalDate.of(2024, 7, 20))).hasValue(LocalDate.of(2024, 7, 22));
        assertThat(sut.nextWorkingFollowingTo(LocalDate.of(2024, 7, 21))).hasValue(LocalDate.of(2024, 7, 22));
    }

    @Test
    void ensureNextWorkingFollowingToReturnsEmptyOptionalWhenNotInWorkingDays() {

        final LocalDate from = LocalDate.of(2024, 7, 1);
        final LocalDate to = LocalDate.of(2024, 7, 31);

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        // 2024-06-30 would return 2024-07-01 -> use 2024-06-30 to assert empty value
        assertThat(sut.nextWorkingFollowingTo(LocalDate.of(2024, 6, 29))).isEmpty();
        assertThat(sut.nextWorkingFollowingTo(LocalDate.of(2024, 7, 31))).isEmpty();
    }

    @Test
    void ensureWorkingTimeForApplicationOverTwoDaysWhenWorkingFull() {

        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> fullWorkingDayInformation());
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

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        final Application application = new Application();
        application.setStartDate(from.plusDays(1));
        application.setEndDate(from.plusDays(1));
        application.setDayLength(DayLength.NOON);

        final BigDecimal actual = sut.workingTime(application);

        assertThat(actual).isEqualTo(BigDecimal.valueOf(0.5));
    }

    @ParameterizedTest
    @MethodSource("morningAndNoonWorkingTimeInformation")
    void ensureWorkingTimeForApplicationFullDayWhenWorkingWith(WorkingDayInformation workingDayInformation) {

        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> workingDayInformation);
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


        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(applicationFrom, applicationTo, date -> fullWorkingDayInformation());
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

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(applicationFrom, applicationTo, date -> fullWorkingDayInformation());
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

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        final Optional<BigDecimal> actual = sut.workingTime(LocalDate.of(2022, 8, 10));

        assertThat(actual).hasValue(BigDecimal.valueOf(1));
    }

    @Test
    void ensureWorkingTimeForLocalDateReturnsEmptyOptionalForDateOutOfRange() {
        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        assertThat(sut.workingTime(LocalDate.of(2022, 7, 31))).isEmpty();
        assertThat(sut.workingTime(LocalDate.of(2022, 9, 1))).isEmpty();
    }

    @Test
    void ensureWorkingTimeForDateRangeWhenWorkingFull() {
        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        assertThat(sut.workingTime(from, to)).isEqualTo(BigDecimal.valueOf(31));
        assertThat(sut.workingTime(from.plusDays(10), to)).isEqualTo(BigDecimal.valueOf(21));
    }

    @Test
    void ensureWorkingTimeForDateRangeForFalsyDateRangeWhenWorkingFull() {
        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        assertThat(sut.workingTime(to, from)).isEqualTo(BigDecimal.ZERO);
    }

    @ParameterizedTest
    @MethodSource("morningAndNoonWorkingTimeInformation")
    void ensureWorkingTimeForDateRangeWhenWorkingNotFull(WorkingDayInformation workingDayInformation) {
        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> workingDayInformation);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        assertThat(sut.workingTime(from, to)).isEqualTo(BigDecimal.valueOf(15.5));
        assertThat(sut.workingTime(from.plusDays(10), to)).isEqualTo(BigDecimal.valueOf(10.5));
    }

    @ParameterizedTest
    @MethodSource("morningAndNoonWorkingTimeInformation")
    void ensureWorkingTimeForDateRangeForFalsyDateRangeWhenWorkingNotFull(WorkingDayInformation workingDayInformation) {
        final LocalDate from = LocalDate.of(2022, 8, 1);
        final LocalDate to = LocalDate.of(2022, 8, 31);

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(from, to, date -> workingDayInformation);
        final WorkingTimeCalendar sut = new WorkingTimeCalendar(workingTimeByDate);

        assertThat(sut.workingTime(to, from)).isEqualTo(BigDecimal.ZERO);
    }

    static Stream<Arguments> morningAndNoonWorkingTimeInformation() {
        return Stream.of(
            Arguments.of(new WorkingDayInformation(MORNING, WORKDAY, NO_WORKDAY)),
            Arguments.of(new WorkingDayInformation(NOON, NO_WORKDAY, WORKDAY))
        );
    }

    private static WorkingDayInformation emptyWorkingDayInformation() {
        return new WorkingDayInformation(ZERO, NO_WORKDAY, NO_WORKDAY);
    }

    private static WorkingDayInformation fullWorkingDayInformation() {
        return new WorkingDayInformation(FULL, WORKDAY, WORKDAY);
    }

    private Map<LocalDate, WorkingDayInformation> buildWorkingTimeByDate(LocalDate from, LocalDate to, Function<LocalDate, WorkingDayInformation> dayLengthProvider) {
        Map<LocalDate, WorkingDayInformation> map = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            map.put(date, dayLengthProvider.apply(date));
        }
        return map;
    }
}
