package org.synyx.urlaubsverwaltung.overtime.web;

import org.synyx.urlaubsverwaltung.overtime.Overtime;

import java.time.Duration;
import java.util.List;

import static java.util.stream.Collectors.toList;

final class OvertimeListMapper {

    private OvertimeListMapper() {
        // ok
    }

    static OvertimeListDto mapToDto(List<Overtime> overtimes, Duration totalOvertime, Duration leftOvertime) {

        final List<OvertimeListRecordDto> recordDtos = overtimes.stream()
            .map(overtime -> new OvertimeListRecordDto(overtime.getId(), overtime.getStartDate(), overtime.getEndDate(), overtime.getDuration(), overtime.getLastModificationDate()))
            .collect(toList());

        return new OvertimeListDto(recordDtos, totalOvertime, leftOvertime);
    }
}
