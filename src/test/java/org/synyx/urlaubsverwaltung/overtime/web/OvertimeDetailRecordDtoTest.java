package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeDetailRecordDtoTest {

    @Test
    void equals() {
        final OvertimeDetailPersonDto person = new OvertimeDetailPersonDto(1, "email@example.org", "niceName", "gravatarUrl", false);
        final OvertimeDetailRecordDto overtimeDetailRecordDto = new OvertimeDetailRecordDto(1, person, LocalDate.parse("2021-06-28"), LocalDate.parse("2021-06-29"), Duration.ofHours(2), LocalDate.parse("2021-06-28"));
        final OvertimeDetailRecordDto overtimeDetailRecordDtoOne = new OvertimeDetailRecordDto(1, person, LocalDate.parse("2021-06-28"), LocalDate.parse("2021-06-29"), Duration.ofHours(2), LocalDate.parse("2021-06-28"));

        final OvertimeDetailPersonDto personTwo = new OvertimeDetailPersonDto(1, "differentEmail@example.org", "niceName", "gravatarUrl", false);
        final OvertimeDetailRecordDto overtimeDetailRecordDtoTwo = new OvertimeDetailRecordDto(1, personTwo, LocalDate.parse("2021-06-28"), LocalDate.parse("2021-06-29"), Duration.ofHours(2), LocalDate.parse("2021-06-28"));
        final OvertimeDetailRecordDto overtimeDetailRecordDtoThree = new OvertimeDetailRecordDto(1, person, LocalDate.parse("2021-06-27"), LocalDate.parse("2021-06-29"), Duration.ofHours(2), LocalDate.parse("2021-06-28"));
        final OvertimeDetailRecordDto overtimeDetailRecordDtoFour = new OvertimeDetailRecordDto(1, person, LocalDate.parse("2021-06-28"), LocalDate.parse("2021-06-30"), Duration.ofHours(2), LocalDate.parse("2021-06-28"));
        final OvertimeDetailRecordDto overtimeDetailRecordDtoFive = new OvertimeDetailRecordDto(1, person, LocalDate.parse("2021-06-28"), LocalDate.parse("2021-06-28"), Duration.ofHours(4), LocalDate.parse("2021-06-28"));

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
        final OvertimeDetailPersonDto person = new OvertimeDetailPersonDto(1, "email@example.org", "niceName", "gravatarUrl", false);
        final OvertimeDetailRecordDto overtimeDetailRecordDto = new OvertimeDetailRecordDto(1, person, LocalDate.parse("2021-06-28"), LocalDate.parse("2021-06-29"), Duration.ofHours(2), LocalDate.parse("2021-06-28"));
        assertThat(overtimeDetailRecordDto.hashCode()).isEqualTo(38396037);
    }
}
