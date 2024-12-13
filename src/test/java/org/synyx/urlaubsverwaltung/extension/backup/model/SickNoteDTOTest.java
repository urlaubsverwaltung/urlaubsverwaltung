package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteEntity;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SickNoteDTOTest {

    @Test
    void happyPathDTOToSickNoteEntity() {
        SickNoteType sickNoteType = new SickNoteType();
        Person person = new Person();
        Person applier = new Person();
        LocalDate now = LocalDate.now();
        SickNoteDTO dto = new SickNoteDTO(1L, "externalIdOfPerson", "externalIdOfApplier", 1L, now, now.plusDays(5), DayLengthDTO.FULL, now, now.plusDays(5), now, now.plusDays(10), SickNoteStatusDTO.ACTIVE, List.of(), List.of());

        final SickNoteEntity entity = dto.toSickNoteEntity(sickNoteType, person, applier);

        assertThat(entity.getPerson()).isEqualTo(person);
        assertThat(entity.getApplier()).isEqualTo(applier);
        assertThat(entity.getSickNoteType()).isEqualTo(sickNoteType);
        assertThat(entity.getStartDate()).isEqualTo(dto.startDate());
        assertThat(entity.getEndDate()).isEqualTo(dto.endDate());
        assertThat(entity.getDayLength()).isEqualTo(dto.dayLength().toDayLength());
        assertThat(entity.getAubStartDate()).isEqualTo(dto.aubStartDate());
        assertThat(entity.getAubEndDate()).isEqualTo(dto.aubEndDate());
        assertThat(entity.getLastEdited()).isEqualTo(dto.lastEdited());
        assertThat(entity.getEndOfSickPayNotificationSend()).isEqualTo(dto.endOfSickPayNotificationSend());
        assertThat(entity.getStatus()).isEqualTo(dto.sickNoteStatus().toSickNoteStatus());
    }

}
