package org.synyx.urlaubsverwaltung.overtime.web;

import java.time.Duration;
import java.util.List;

public class OvertimeListDto {

    private final List<OvertimeListRecordDto> records;
    private final Duration overtimeTotal;
    private final Duration overtimeTotalLastYear;
    private final Duration overtimeLeft;

    OvertimeListDto(List<OvertimeListRecordDto> records, Duration overtimeTotal, Duration overtimeTotalLastYear, Duration overtimeLeft) {
        this.records = records;
        this.overtimeTotal = overtimeTotal;
        this.overtimeTotalLastYear = overtimeTotalLastYear;
        this.overtimeLeft = overtimeLeft;
    }

    public List<OvertimeListRecordDto> getRecords() {
        return records;
    }

    public Duration getOvertimeTotal() {
        return overtimeTotal;
    }

    public Duration getOvertimeTotalLastYear() {
        return overtimeTotalLastYear;
    }

    public Duration getOvertimeLeft() {
        return overtimeLeft;
    }
}
