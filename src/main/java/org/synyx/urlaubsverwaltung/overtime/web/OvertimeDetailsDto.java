package org.synyx.urlaubsverwaltung.overtime.web;

import java.time.Duration;
import java.util.List;

public class OvertimeDetailsDto {

    private final OvertimeDetailRecordDto record;
    private final List<OvertimeCommentDto> comments;
    private final Duration overtimeTotal;
    private final Duration overtimeLeft;

    OvertimeDetailsDto(OvertimeDetailRecordDto record, List<OvertimeCommentDto> comments, Duration overtimeTotal, Duration overtimeLeft) {
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

    public Duration getOvertimeTotal() {
        return overtimeTotal;
    }

    public Duration getOvertimeLeft() {
        return overtimeLeft;
    }
}
