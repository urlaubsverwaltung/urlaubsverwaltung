package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public record OvertimeDTO(Long id, String externalIdOfOwner, LocalDate startDate, LocalDate endDate, Duration duration,
                          LocalDate lastModificationDate, List<OvertimeCommentDTO> overtimeComments) {

    public static OvertimeDTO of(Overtime overtime, String externalIdOfOwner, List<OvertimeCommentDTO> overtimeCommentDTOs) {
        return new OvertimeDTO(overtime.getId(), externalIdOfOwner, overtime.getStartDate(), overtime.getEndDate(), overtime.getDuration(), overtime.getLastModificationDate(), overtimeCommentDTOs);
    }

    public Overtime toOverTime(Person person) {
        return new Overtime(person, startDate, endDate, duration, lastModificationDate);
    }

}
