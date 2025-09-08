package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeEntity;
import org.synyx.urlaubsverwaltung.overtime.OvertimeType;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public record OvertimeDTO(
    Long id, String externalIdOfOwner, LocalDate startDate, LocalDate endDate, Duration duration,
    LocalDate lastModificationDate, boolean external, List<OvertimeCommentDTO> overtimeComments
) {

    public static OvertimeDTO of(Overtime overtime, String externalIdOfOwner, List<OvertimeCommentDTO> overtimeCommentDTOs) {
        return new OvertimeDTO(
            overtime.id().value(),
            externalIdOfOwner,
            overtime.startDate(),
            overtime.endDate(),
            overtime.duration(),
            LocalDate.ofInstant(overtime.lastModification(), ZoneId.of("Europe/Berlin")),
            overtime.type().equals(OvertimeType.EXTERNAL),
            overtimeCommentDTOs
        );
    }

    public OvertimeEntity toOverTime(Person person) {
        return new OvertimeEntity(person, startDate, endDate, duration, external, lastModificationDate);
    }
}
