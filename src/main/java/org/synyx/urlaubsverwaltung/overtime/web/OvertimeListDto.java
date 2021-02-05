package org.synyx.urlaubsverwaltung.overtime.web;

import java.math.BigDecimal;
import java.util.List;

public class OvertimeListDto {

    private final List<OvertimeListRecordDto> records;
    private final BigDecimal overtimeTotal;
    private final BigDecimal overtimeLeft;

    public OvertimeListDto(List<OvertimeListRecordDto> records, BigDecimal overtimeTotal, BigDecimal overtimeLeft) {
        this.records = records;
        this.overtimeTotal = overtimeTotal;
        this.overtimeLeft = overtimeLeft;
    }

    public List<OvertimeListRecordDto> getRecords() {
        return records;
    }

    public BigDecimal getOvertimeTotal() {
        return overtimeTotal;
    }

    public BigDecimal getOvertimeLeft() {
        return overtimeLeft;
    }
}
