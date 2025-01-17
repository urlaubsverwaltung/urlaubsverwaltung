package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.period.DayLength;

import static org.assertj.core.api.Assertions.assertThat;

class DayLengthTest {

    @ParameterizedTest
    @EnumSource(DayLength.class)
    void happyPathDayLengthToDTO(DayLength dayLength) {
        DayLengthDTO dayLengthDTO = DayLengthDTO.valueOf(dayLength.name());
        assertThat(dayLengthDTO).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(DayLengthDTO.class)
    void happyPathDTOToDayLength(DayLengthDTO dayLengthDTO) {
        DayLength dayLength = dayLengthDTO.toDayLength();
        assertThat(dayLength).isNotNull();
    }

}
