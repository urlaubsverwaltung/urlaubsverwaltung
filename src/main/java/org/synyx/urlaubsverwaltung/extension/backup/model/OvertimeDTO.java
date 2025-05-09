package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.overtime.OvertimeEntity;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public record OvertimeDTO(
    Long id, String externalIdOfOwner, LocalDate startDate, LocalDate endDate, Duration duration,
    LocalDate lastModificationDate, boolean external, List<OvertimeCommentDTO> overtimeComments
) {

    public static OvertimeDTO of(OvertimeEntity overtime, String externalIdOfOwner, List<OvertimeCommentDTO> overtimeCommentDTOs) {
        return new OvertimeDTO(overtime.getId(), externalIdOfOwner, overtime.getStartDate(), overtime.getEndDate(), overtime.getDuration(), overtime.getLastModificationDate(), overtime.isExternal(), overtimeCommentDTOs);
    }

    public OvertimeEntity toOverTime(Person person) {
        return new OvertimeEntity(person, startDate, endDate, duration, external, lastModificationDate);
    }
}
