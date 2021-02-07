package org.synyx.urlaubsverwaltung.overtime.web;

import java.time.Duration;
import java.util.List;

public class OvertimeListDto {

    private final List<OvertimeListRecordDto> records;
    private final Duration overtimeTotal;
    private final Duration overtimeLeft;

    public OvertimeListDto(List<OvertimeListRecordDto> records, Duration overtimeTotal, Duration overtimeLeft) {
        this.records = records;
        this.overtimeTotal = overtimeTotal;
        this.overtimeLeft = overtimeLeft;
    }

    public List<OvertimeListRecordDto> getRecords() {
        return records;
    }

    public Duration getOvertimeTotal() {
        return overtimeTotal;
    }

    public Duration getOvertimeLeft() {
        return overtimeLeft;
    }
}
