package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class WorkingTimeDTOTest {

    @Test
    void happyPathWithDefaultFederalState() {
        final WorkingTimeDTO dto = new WorkingTimeDTO(DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.ZERO, DayLengthDTO.ZERO, LocalDate.of(2023, 1, 1), FederalStateDTO.GERMANY_BADEN_WUERTTEMBERG, true);
        final Person person = new Person();

        final WorkingTimeEntity entity = dto.toWorkingTimeEntity(person);

        assertThat(entity.getPerson()).isEqualTo(person);
        assertThat(entity.getMonday()).isEqualTo(dto.monday().toDayLength());
        assertThat(entity.getTuesday()).isEqualTo(dto.tuesday().toDayLength());
        assertThat(entity.getWednesday()).isEqualTo(dto.wednesday().toDayLength());
        assertThat(entity.getThursday()).isEqualTo(dto.thursday().toDayLength());
        assertThat(entity.getFriday()).isEqualTo(dto.friday().toDayLength());
        assertThat(entity.getSaturday()).isEqualTo(dto.saturday().toDayLength());
        assertThat(entity.getSunday()).isEqualTo(dto.sunday().toDayLength());
        assertThat(entity.getValidFrom()).isEqualTo(dto.validFrom());
        assertThat(entity.getFederalStateOverride()).isNull();
    }

    @Test
    void happyPathWithoutDefaultFederalState() {
        final WorkingTimeDTO dto = new WorkingTimeDTO(DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.ZERO, DayLengthDTO.ZERO, LocalDate.of(2023, 1, 1), FederalStateDTO.GERMANY_BADEN_WUERTTEMBERG, false);
        final Person person = new Person();

        final WorkingTimeEntity entity = dto.toWorkingTimeEntity(person);

        assertThat(entity.getPerson()).isEqualTo(person);
        assertThat(entity.getMonday()).isEqualTo(dto.monday().toDayLength());
        assertThat(entity.getTuesday()).isEqualTo(dto.tuesday().toDayLength());
        assertThat(entity.getWednesday()).isEqualTo(dto.wednesday().toDayLength());
        assertThat(entity.getThursday()).isEqualTo(dto.thursday().toDayLength());
        assertThat(entity.getFriday()).isEqualTo(dto.friday().toDayLength());
        assertThat(entity.getSaturday()).isEqualTo(dto.saturday().toDayLength());
        assertThat(entity.getSunday()).isEqualTo(dto.sunday().toDayLength());
        assertThat(entity.getValidFrom()).isEqualTo(dto.validFrom());
        assertThat(entity.getFederalStateOverride()).isEqualTo(dto.federalState().toFederalState());
    }

}
