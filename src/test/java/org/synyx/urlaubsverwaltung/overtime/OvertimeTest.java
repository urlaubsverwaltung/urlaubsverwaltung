package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class OvertimeTest {

    @Test
    void getDurationByYear() {

        final Overtime overtime = new Overtime(
            new OvertimeId(1L),
            new PersonId(1L),
            new DateRange(LocalDate.of(2022, 12, 30), LocalDate.of(2023, 1, 2)),
            Duration.ofHours(20),
            OvertimeType.UV_INTERNAL,
            Instant.now()
        );

        final Map<Integer, Duration> durationByYear = overtime.getDurationByYear();

        assertThat(durationByYear)
            .containsEntry(2022, Duration.ofHours(10))
            .containsEntry(2023, Duration.ofHours(10));
    }

    @Test
    void getTotalDurationBefore() {

        final Overtime overtime = new Overtime(
            new OvertimeId(1L),
            new PersonId(1L),
            new DateRange(LocalDate.of(2022, 12, 30), LocalDate.of(2023, 1, 2)),
            Duration.ofHours(20),
            OvertimeType.UV_INTERNAL,
            Instant.now()
        );

        assertThat(overtime.getTotalDurationBefore(2023)).isEqualTo(Duration.ofHours(10));
    }
}
