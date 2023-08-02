package org.synyx.urlaubsverwaltung.overtime.web;


import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeListRecordDtoTest {


    @Test
    void getDurationByYearOverTwoYears() {
        final OvertimeListRecordDto sut = new OvertimeListRecordDto(1L, LocalDate.parse("2021-06-28"), LocalDate.parse("2022-06-29"), Duration.ofHours(4), Map.of(2021, Duration.ofHours(1), 2022, Duration.ofHours(3)), Duration.ofHours(4), "STATUS", "COLOR", "TYPE", true);

        final Map<Integer, Duration> durationByYear = sut.getDurationByYear(2021);

        assertThat(durationByYear).containsEntry(2022, Duration.ofHours(3L));
    }
}
