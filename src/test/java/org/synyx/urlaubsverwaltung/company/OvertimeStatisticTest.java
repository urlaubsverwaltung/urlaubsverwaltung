package org.synyx.urlaubsverwaltung.company;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeId;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeType.UV_INTERNAL;

class OvertimeStatisticTest {

    @Test
    void ensureNumberOfPersonsWithDurationBetweenCountsPersonsWithSummedDurationInRange() {

        final PersonId personLow = new PersonId(1L);
        final PersonId personInRange = new PersonId(2L);
        final PersonId personAtMin = new PersonId(3L);
        final PersonId personAtMaxExclusive = new PersonId(4L);

        final OvertimeStatistic sut = new OvertimeStatistic(Map.of(
            personLow, List.of(overtime(personLow, Duration.ofHours(2))),
            personInRange, List.of(overtime(personInRange, Duration.ofHours(3)), overtime(personInRange, Duration.ofHours(4))),
            personAtMin, List.of(overtime(personAtMin, Duration.ofHours(5))),
            personAtMaxExclusive, List.of(overtime(personAtMaxExclusive, Duration.ofHours(15)))
        ));

        final int actual = sut.numberOfPersonsWithDurationBetween(Duration.ofHours(5), Duration.ofHours(15));

        assertThat(actual).isEqualTo(2);
    }

    @Test
    void ensureNumberOfPersonsWithDurationBetweenCountsPersonWithoutOvertimeAsZero() {

        final PersonId personWithoutOvertime = new PersonId(1L);

        final OvertimeStatistic sut = new OvertimeStatistic(Map.of(personWithoutOvertime, List.of()));

        final int actual = sut.numberOfPersonsWithDurationBetween(Duration.ZERO, Duration.ofHours(5));

        assertThat(actual).isEqualTo(1);
    }

    @Test
    void ensureNumberOfPersonsWithDurationBetweenThrowsWhenMinIsGreaterThanMax() {

        final OvertimeStatistic sut = OvertimeStatistic.empty();

        assertThatThrownBy(() -> sut.numberOfPersonsWithDurationBetween(Duration.ofHours(15), Duration.ofHours(5)))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ensureNumberOfPersonsWithDurationGreaterOrEqualCountsPersonsWithSummedDurationAtOrAboveValue() {

        final PersonId personBelow = new PersonId(1L);
        final PersonId personAtValue = new PersonId(2L);
        final PersonId personAboveValue = new PersonId(3L);

        final OvertimeStatistic sut = new OvertimeStatistic(Map.of(
            personBelow, List.of(overtime(personBelow, Duration.ofHours(4))),
            personAtValue, List.of(overtime(personAtValue, Duration.ofHours(5))),
            personAboveValue, List.of(overtime(personAboveValue, Duration.ofHours(3)), overtime(personAboveValue, Duration.ofHours(10)))
        ));

        final int actual = sut.numberOfPersonsWithDurationGreaterOrEqual(Duration.ofHours(5));

        assertThat(actual).isEqualTo(2);
    }

    private static Overtime overtime(PersonId personId, Duration duration) {
        return new Overtime(
            new OvertimeId(1L),
            personId,
            new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)),
            duration,
            UV_INTERNAL,
            Instant.now()
        );
    }
}
