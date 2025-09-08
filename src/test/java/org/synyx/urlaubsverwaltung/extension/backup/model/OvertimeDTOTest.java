package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeEntity;
import org.synyx.urlaubsverwaltung.overtime.OvertimeId;
import org.synyx.urlaubsverwaltung.overtime.OvertimeType;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeDTOTest {

    @Test
    void happyPathOvertimeToDTO() {

        final Person person = new Person();
        person.setId(1L);

        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = LocalDate.now().plusDays(1);
        final Overtime overtime = new Overtime(new OvertimeId(1L), person.getIdAsPersonId(), new DateRange(startDate, endDate), Duration.ofHours(8), OvertimeType.UV_INTERNAL, Instant.now());

        String externalIdOfOwner = "ownerId";
        List<OvertimeCommentDTO> overtimeComments = List.of();

        final OvertimeDTO dto = OvertimeDTO.of(overtime, externalIdOfOwner, overtimeComments);

        assertThat(dto.id()).isEqualTo(overtime.id().value());
        assertThat(dto.externalIdOfOwner()).isEqualTo(externalIdOfOwner);
        assertThat(dto.startDate()).isEqualTo(overtime.startDate());
        assertThat(dto.endDate()).isEqualTo(overtime.endDate());
        assertThat(dto.duration()).isEqualTo(overtime.duration());
        assertThat(dto.lastModificationDate()).isEqualTo(LocalDate.ofInstant(overtime.lastModification(), ZoneId.of("Europe/Berlin")));
        assertThat(dto.overtimeComments()).isEqualTo(overtimeComments);
    }


    @Test
    void happyPathDTOToOvertime() {
        LocalDate now = LocalDate.now();
        OvertimeDTO dto = new OvertimeDTO(1L, "ownerId", now, now.plusDays(1), Duration.ofHours(8), now, false, Collections.emptyList());
        Person person = new Person();

        final OvertimeEntity overtime = dto.toOverTime(person);

        assertThat(overtime.getId()).isNull();
        assertThat(overtime.getPerson()).isEqualTo(person);
        assertThat(overtime.getStartDate()).isEqualTo(dto.startDate());
        assertThat(overtime.getEndDate()).isEqualTo(dto.endDate());
        assertThat(overtime.getDuration()).isEqualTo(dto.duration());
        assertThat(overtime.getLastModificationDate()).isEqualTo(dto.lastModificationDate());
    }

}
