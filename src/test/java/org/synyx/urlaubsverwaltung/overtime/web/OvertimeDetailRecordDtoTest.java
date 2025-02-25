package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeDetailRecordDtoTest {

    @Test
    void getDurationByYearOverTwoYears() {
        final OvertimeDetailPersonDto person = new OvertimeDetailPersonDto(1L, "email@example.org", "niceName", "N","gravatarUrl", false);
        final OvertimeDetailRecordDto sut = new OvertimeDetailRecordDto(1L, person, LocalDate.parse("2021-06-28"), LocalDate.parse("2022-06-29"), Duration.ofHours(4), Map.of(2021, Duration.ofHours(1), 2022, Duration.ofHours(3)), LocalDate.parse("2021-06-28"));

        final Map<Integer, Duration> durationByYear = sut.getDurationByYear(2021);

        assertThat(durationByYear).containsEntry(2022, Duration.ofHours(3L));
    }

    @Test
    void equals() {
        final OvertimeDetailPersonDto person = new OvertimeDetailPersonDto(1L, "email@example.org", "niceName", "N","gravatarUrl", false);
        final OvertimeDetailRecordDto overtimeDetailRecordDto = new OvertimeDetailRecordDto(1L, person, LocalDate.parse("2021-06-28"), LocalDate.parse("2021-06-29"), Duration.ofHours(2), Map.of(2021, Duration.ofHours(2)), LocalDate.parse("2021-06-28"));
        final OvertimeDetailRecordDto overtimeDetailRecordDtoOne = new OvertimeDetailRecordDto(1L, person, LocalDate.parse("2021-06-28"), LocalDate.parse("2021-06-29"), Duration.ofHours(2), Map.of(2021, Duration.ofHours(2)), LocalDate.parse("2021-06-28"));

        final OvertimeDetailPersonDto personTwo = new OvertimeDetailPersonDto(1L, "differentEmail@example.org", "N","niceName", "gravatarUrl", false);
        final OvertimeDetailRecordDto overtimeDetailRecordDtoTwo = new OvertimeDetailRecordDto(1L, personTwo, LocalDate.parse("2021-06-28"), LocalDate.parse("2021-06-29"), Duration.ofHours(2), Map.of(2021, Duration.ofHours(2)), LocalDate.parse("2021-06-28"));
        final OvertimeDetailRecordDto overtimeDetailRecordDtoThree = new OvertimeDetailRecordDto(1L, person, LocalDate.parse("2021-06-27"), LocalDate.parse("2021-06-29"), Duration.ofHours(2), Map.of(2021, Duration.ofHours(2)), LocalDate.parse("2021-06-28"));
        final OvertimeDetailRecordDto overtimeDetailRecordDtoFour = new OvertimeDetailRecordDto(1L, person, LocalDate.parse("2021-06-28"), LocalDate.parse("2021-06-30"), Duration.ofHours(2), Map.of(2021, Duration.ofHours(2)), LocalDate.parse("2021-06-28"));
        final OvertimeDetailRecordDto overtimeDetailRecordDtoFive = new OvertimeDetailRecordDto(1L, person, LocalDate.parse("2021-06-28"), LocalDate.parse("2021-06-28"), Duration.ofHours(4), Map.of(2021, Duration.ofHours(4)), LocalDate.parse("2021-06-28"));

        assertThat(overtimeDetailRecordDto)
            .isEqualTo(overtimeDetailRecordDto)
            .isEqualTo(overtimeDetailRecordDtoOne)
            .isNotEqualTo(overtimeDetailRecordDtoTwo)
            .isNotEqualTo(overtimeDetailRecordDtoThree)
            .isNotEqualTo(overtimeDetailRecordDtoFour)
            .isNotEqualTo(overtimeDetailRecordDtoFive)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final OvertimeDetailPersonDto person = new OvertimeDetailPersonDto(1L, "email@example.org", "N","niceName", "gravatarUrl", false);
        final OvertimeDetailRecordDto overtimeDetailRecordDto = new OvertimeDetailRecordDto(1L, person, LocalDate.parse("2021-06-28"), LocalDate.parse("2021-06-29"), Duration.ofHours(2), Map.of(2021, Duration.ofHours(2)), LocalDate.parse("2021-06-28"));
        assertThat(overtimeDetailRecordDto.hashCode()).isEqualTo(-8943973);
    }
}
