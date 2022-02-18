package org.synyx.urlaubsverwaltung.overtime.web;

import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.overtime.Overtime;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.synyx.urlaubsverwaltung.overtime.web.OvertimeListRecordDto.OvertimeListRecordType.ABSENCE;
import static org.synyx.urlaubsverwaltung.overtime.web.OvertimeListRecordDto.OvertimeListRecordType.OVERTIME;

final class OvertimeListMapper {

    private OvertimeListMapper() {
        // ok
    }

    static OvertimeListDto mapToDto(List<Application> overtimeAbsences, List<Overtime> overtimes, Duration totalOvertime, Duration totalOvertimeLastYear, Duration leftOvertime) {

        final List<OvertimeListRecordDto> overtimeListRecords = overtimes.stream()
            .map(overtime -> new OvertimeListRecordDto(overtime.getId(), overtime.getStartDate(), overtime.getEndDate(), overtime.getDuration(), ApplicationStatus.ALLOWED, OVERTIME, overtime.getLastModificationDate()))
            .collect(toList());

        final List<OvertimeListRecordDto> overtimeAbsencesRecords = overtimeAbsences.stream()
            .map(application -> new OvertimeListRecordDto(application.getId(), application.getStartDate(), application.getEndDate(), application.getHours().negated(), application.getStatus(), ABSENCE, LocalDate.now()))
            .collect(toList());

        final List<OvertimeListRecordDto> allOvertimes = concat(overtimeListRecords.stream(), overtimeAbsencesRecords.stream())
            .sorted(comparing(OvertimeListRecordDto::getStartDate).reversed())
            .collect(toList());

        return new OvertimeListDto(allOvertimes, totalOvertime, totalOvertimeLastYear, leftOvertime);
    }
}
