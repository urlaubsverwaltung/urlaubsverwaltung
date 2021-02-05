package org.synyx.urlaubsverwaltung.overtime.web;

import org.synyx.urlaubsverwaltung.overtime.Overtime;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

final class OvertimeListMapper {

    private OvertimeListMapper() {
    }

    static OvertimeListDto mapToDto(List<Overtime> records, Duration totalOvertime, Duration leftOvertime) {

        final List<OvertimeListRecordDto> recordDtos = records.stream()
            .map(e -> new OvertimeListRecordDto(e.getId(), e.getStartDate(), e.getEndDate(), e.getDuration(), e.getLastModificationDate()))
            .collect(Collectors.toList());

        return new OvertimeListDto(recordDtos,
            BigDecimal.valueOf(totalOvertime.toMinutes() / 60.0),
            BigDecimal.valueOf(leftOvertime.toMinutes() / 60.0));
    }
}
