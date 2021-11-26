package org.synyx.urlaubsverwaltung.absence;


import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DateRangeTest {

    static Stream<Arguments> overlappingDateRanges() {
        return Stream.of(
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-10", "2020-10-15"),
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-12", "2020-10-13"),
            Arguments.of("2020-10-12", "2020-10-13", "2020-10-10", "2020-10-15"),
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-14", "2020-10-16"),
            Arguments.of("2020-10-14", "2020-10-16", "2020-10-10", "2020-10-15"),
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-09", "2020-10-11"),
            Arguments.of("2020-10-09", "2020-10-11", "2020-10-10", "2020-10-15"),

            Arguments.of("2020-10-10", "2020-10-10", "2020-10-10", "2020-10-10"),
            Arguments.of("2020-10-09", "2020-10-10", "2020-10-10", "2020-10-10"),
            Arguments.of("2020-10-10", "2020-10-10", "2020-10-09", "2020-10-10"),
            Arguments.of("2020-10-10", "2020-10-10", "2020-10-10", "2020-10-11"),
            Arguments.of("2020-10-10", "2020-10-11", "2020-10-10", "2020-10-10")
        );
    }

    @ParameterizedTest
    @MethodSource("overlappingDateRanges")
    void isOverlapping(LocalDate startDateOne, LocalDate endDateOne, LocalDate startDateTwo, LocalDate endDateTwo) {
        final DateRange rangeOne = new DateRange(startDateOne, endDateOne);
        final DateRange rangeTwo = new DateRange(startDateTwo, endDateTwo);

        final boolean overlapping = rangeOne.isOverlapping(rangeTwo);
        assertThat(overlapping).isTrue();
    }

    static Stream<Arguments> notOverlappingDateRanges() {
        return Stream.of(
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-08", "2020-10-09"),
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-16", "2020-10-17"),
            Arguments.of("2020-10-08", "2020-10-09", "2020-10-10", "2020-10-15"),
            Arguments.of("2020-10-16", "2020-10-17", "2020-10-10", "2020-10-15"),
            Arguments.of("2020-10-16", "2020-10-17", "2020-10-15", "2020-10-15"),
            Arguments.of("2020-10-15", "2020-10-15", "2020-10-16", "2020-10-17"),
            Arguments.of("2020-10-16", "2020-10-17", "2020-10-18", "2020-10-18"),
            Arguments.of("2020-10-18", "2020-10-18", "2020-10-16", "2020-10-17"),
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-16", "2020-10-17"),
            Arguments.of("2020-10-16", "2020-10-17", "2020-10-10", "2020-10-15"),
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-07", "2020-10-08"),
            Arguments.of("2020-10-07", "2020-10-08", "2020-10-10", "2020-10-15")
        );
    }

    @ParameterizedTest
    @MethodSource("notOverlappingDateRanges")
    void notOverlapping(LocalDate startDateOne, LocalDate endDateOne, LocalDate startDateTwo, LocalDate endDateTwo) {
        final DateRange rangeOne = new DateRange(startDateOne, endDateOne);
        final DateRange rangeTwo = new DateRange(startDateTwo, endDateTwo);

        final boolean overlapping = rangeOne.isOverlapping(rangeTwo);
        assertThat(overlapping).isFalse();
    }

    static Stream<Arguments> overlapDateRanges() {
        return Stream.of(
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-10", "2020-10-15", "2020-10-10", "2020-10-15"),
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-12", "2020-10-13", "2020-10-12", "2020-10-13"),
            Arguments.of("2020-10-12", "2020-10-13", "2020-10-10", "2020-10-15", "2020-10-12", "2020-10-13"),
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-14", "2020-10-16", "2020-10-14", "2020-10-15"),
            Arguments.of("2020-10-14", "2020-10-16", "2020-10-10", "2020-10-15", "2020-10-14", "2020-10-15"),
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-09", "2020-10-11", "2020-10-10", "2020-10-11"),
            Arguments.of("2020-10-09", "2020-10-11", "2020-10-10", "2020-10-15", "2020-10-10", "2020-10-11"),

            Arguments.of("2020-10-10", "2020-10-10", "2020-10-10", "2020-10-10", "2020-10-10", "2020-10-10"),
            Arguments.of("2020-10-09", "2020-10-10", "2020-10-10", "2020-10-10", "2020-10-10", "2020-10-10"),
            Arguments.of("2020-10-10", "2020-10-10", "2020-10-09", "2020-10-10", "2020-10-10", "2020-10-10"),
            Arguments.of("2020-10-10", "2020-10-10", "2020-10-10", "2020-10-11", "2020-10-10", "2020-10-10"),
            Arguments.of("2020-10-10", "2020-10-11", "2020-10-10", "2020-10-10", "2020-10-10", "2020-10-10")
        );
    }

    @ParameterizedTest
    @MethodSource("overlapDateRanges")
    void withOverlap(LocalDate startDateOne, LocalDate endDateOne, LocalDate startDateTwo, LocalDate endDateTwo, LocalDate overlapStartDate, LocalDate overlapEndDate) {
        final DateRange rangeOne = new DateRange(startDateOne, endDateOne);
        final DateRange rangeTwo = new DateRange(startDateTwo, endDateTwo);

        final DateRange overlap = rangeOne.overlap(rangeTwo);
        assertThat(overlap.getStartDate()).isEqualTo(overlapStartDate);
        assertThat(overlap.getEndDate()).isEqualTo(overlapEndDate);
    }

    static Stream<Arguments> gapDateRanges() {
        return Stream.of(
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-08", "2020-10-09", null, null),
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-16", "2020-10-17", null, null),
            Arguments.of("2020-10-08", "2020-10-09", "2020-10-10", "2020-10-15", null, null),
            Arguments.of("2020-10-16", "2020-10-17", "2020-10-10", "2020-10-15", null, null),
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-07", "2020-10-08", "2020-10-09", "2020-10-09"),
            Arguments.of("2020-10-07", "2020-10-08", "2020-10-10", "2020-10-15", "2020-10-09", "2020-10-09"),
            Arguments.of("2020-10-10", "2020-10-15", "2020-10-07", "2020-10-07", "2020-10-08", "2020-10-09"),
            Arguments.of("2020-10-07", "2020-10-07", "2020-10-10", "2020-10-15", "2020-10-08", "2020-10-09")
        );
    }

    @ParameterizedTest
    @MethodSource("gapDateRanges")
    void gap(LocalDate startDateOne, LocalDate endDateOne, LocalDate startDateTwo, LocalDate endDateTwo, LocalDate gapStartDate, LocalDate gapEndDate) {
        final DateRange rangeOne = new DateRange(startDateOne, endDateOne);
        final DateRange rangeTwo = new DateRange(startDateTwo, endDateTwo);

        final DateRange gap = rangeOne.gap(rangeTwo);
        assertThat(gap.getStartDate()).isEqualTo(gapStartDate);
        assertThat(gap.getEndDate()).isEqualTo(gapEndDate);
    }

    @Test
    void dateRangeisEmpty() {
        final boolean isEmpty = new DateRange(null, null).isEmpty();
        assertThat(isEmpty).isTrue();
    }

    @Test
    void dateRangeIsNotEmpty() {
        final boolean isEmpty = new DateRange(LocalDate.MIN, LocalDate.MAX).isEmpty();
        assertThat(isEmpty).isFalse();
    }
}
