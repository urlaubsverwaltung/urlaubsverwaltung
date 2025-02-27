package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeCommentDtoTest {

    @Test
    void equals() {
        final Instant now = Instant.now();

        final OvertimeCommentPersonDto personDto = new OvertimeCommentPersonDto(1L, "niceName", "NN","gravatarUrl");
        final OvertimeCommentDto overtimeCommentDtoOne = new OvertimeCommentDto(personDto, "CREATED", now, "text");
        final OvertimeCommentDto overtimeCommentDtoOneOne = new OvertimeCommentDto(personDto, "CREATED", now, "text");

        final OvertimeCommentPersonDto personDtoTwo = new OvertimeCommentPersonDto(2L, "niceNameDifferent", "NND","gravatarUrl");
        final OvertimeCommentDto overtimeCommentDtoTwo = new OvertimeCommentDto(personDtoTwo, "CREATED", now, "text");
        final OvertimeCommentDto overtimeCommentDtoThree = new OvertimeCommentDto(personDto, "EDITED", now, "text");
        final OvertimeCommentDto overtimeCommentDtoFour = new OvertimeCommentDto(personDto, "EDITED", now.minusSeconds(100), "text");
        final OvertimeCommentDto overtimeCommentDtoFive = new OvertimeCommentDto(personDto, "EDITED", now.minusSeconds(100), "differentText");

        assertThat(overtimeCommentDtoOne)
            .isEqualTo(overtimeCommentDtoOne)
            .isEqualTo(overtimeCommentDtoOneOne)
            .isNotEqualTo(overtimeCommentDtoTwo)
            .isNotEqualTo(overtimeCommentDtoThree)
            .isNotEqualTo(overtimeCommentDtoFour)
            .isNotEqualTo(overtimeCommentDtoFive)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final OvertimeCommentPersonDto personDto = new OvertimeCommentPersonDto(1L, "niceName", "NN","gravatarUrl");
        final OvertimeCommentDto overtimeCommentDto = new OvertimeCommentDto(personDto, "CREATED", Instant.parse("2021-06-28T00:00:00.00Z"), "text");
        assertThat(overtimeCommentDto.hashCode()).isEqualTo(-2074185642);
    }
}
