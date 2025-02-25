package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeDetailPersonDtoTest {

    @Test
    void equals() {
        final OvertimeDetailPersonDto personDto = new OvertimeDetailPersonDto(1L, "mail@example.org", "niceName", "N", "gravatarUrl", false);
        final OvertimeDetailPersonDto personDtoOne = new OvertimeDetailPersonDto(1L, "mail@example.org", "niceName", "N", "gravatarUrl", false);

        final OvertimeDetailPersonDto personDtoTwo = new OvertimeDetailPersonDto(1L, "differentMail@example.org", "niceName", "N", "gravatarUrl", false);

        assertThat(personDto)
            .isEqualTo(personDto)
            .isEqualTo(personDtoOne)
            .isNotEqualTo(personDtoTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final OvertimeDetailPersonDto personDtoOne = new OvertimeDetailPersonDto(1L, "mail@example.org", "niceName", "N", "gravatarUrl", false);
        assertThat(personDtoOne.hashCode()).isEqualTo(-754489482);
    }
}
