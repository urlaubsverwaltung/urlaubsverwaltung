package org.synyx.urlaubsverwaltung.overtime.web;

import java.math.BigDecimal;
import java.util.List;

public class OvertimeDetailsDto {

    private final OvertimeDetailRecordDto record;
    private final List<OvertimeCommentDto> comments;
    private final BigDecimal overtimeTotal;
    private final BigDecimal overtimeLeft;

    public OvertimeDetailsDto(OvertimeDetailRecordDto record, List<OvertimeCommentDto> comments, BigDecimal overtimeTotal, BigDecimal overtimeLeft) {
        this.record = record;
        this.comments = comments;
        this.overtimeTotal = overtimeTotal;
        this.overtimeLeft = overtimeLeft;
    }

    public OvertimeDetailRecordDto getRecord() {
        return record;
    }

    public List<OvertimeCommentDto> getComments() {
        return comments;
    }

    public BigDecimal getOvertimeTotal() {
        return overtimeTotal;
    }

    public BigDecimal getOvertimeLeft() {
        return overtimeLeft;
    }
}
