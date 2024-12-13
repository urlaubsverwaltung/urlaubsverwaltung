package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeDTOTest {

    @Test
    void happyPathOvertimeToDTO() {
        Overtime overtime = new Overtime(new Person(), LocalDate.now(), LocalDate.now().plusDays(1), Duration.ofHours(8), LocalDate.now());
        String externalIdOfOwner = "ownerId";
        List<OvertimeCommentDTO> overtimeComments = List.of();

        final OvertimeDTO dto = OvertimeDTO.of(overtime, externalIdOfOwner, overtimeComments);

        assertThat(dto.id()).isEqualTo(overtime.getId());
        assertThat(dto.externalIdOfOwner()).isEqualTo(externalIdOfOwner);
        assertThat(dto.startDate()).isEqualTo(overtime.getStartDate());
        assertThat(dto.endDate()).isEqualTo(overtime.getEndDate());
        assertThat(dto.duration()).isEqualTo(overtime.getDuration());
        assertThat(dto.lastModificationDate()).isEqualTo(overtime.getLastModificationDate());
        assertThat(dto.overtimeComments()).isEqualTo(overtimeComments);
    }


    @Test
    void happyPathDTOToOvertime() {
        LocalDate now = LocalDate.now();
        OvertimeDTO dto = new OvertimeDTO(1L, "ownerId", now, now.plusDays(1), Duration.ofHours(8), now, Collections.emptyList());
        Person person = new Person();

        final Overtime overtime = dto.toOverTime(person);

        assertThat(overtime.getId()).isNull();
        assertThat(overtime.getPerson()).isEqualTo(person);
        assertThat(overtime.getStartDate()).isEqualTo(dto.startDate());
        assertThat(overtime.getEndDate()).isEqualTo(dto.endDate());
        assertThat(overtime.getDuration()).isEqualTo(dto.duration());
        assertThat(overtime.getLastModificationDate()).isEqualTo(dto.lastModificationDate());
    }

}
