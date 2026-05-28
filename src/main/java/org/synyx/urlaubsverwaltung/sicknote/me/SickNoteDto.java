package org.synyx.urlaubsverwaltung.sicknote.me;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class SickNoteDto {
    private final Long id;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final DayLength dayLength;
    private final boolean isAubPresent;
    private final BigDecimal workDays;
    private final BigDecimal workDaysWithAub;
    private final SickNoteStatus status;
    private final SickNoteType sickNoteType;
    private final boolean allowedToEdit;
    private final boolean allowedToCancel;

    SickNoteDto(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        DayLength dayLength,
        boolean isAubPresent,
        BigDecimal workDays,
        BigDecimal workDaysWithAub,
        SickNoteStatus status,
        SickNoteType sickNoteType,
        boolean allowedToEdit,
        boolean allowedToCancel
    ) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dayLength = dayLength;
        this.isAubPresent = isAubPresent;
        this.workDays = workDays;
        this.workDaysWithAub = workDaysWithAub;
        this.status = status;
        this.sickNoteType = sickNoteType;
        this.allowedToEdit = allowedToEdit;
        this.allowedToCancel = allowedToCancel;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public boolean isAubPresent() {
        return isAubPresent;
    }

    public BigDecimal getWorkDays() {
        return workDays;
    }

    public BigDecimal getWorkDaysWithAub() {
        return workDaysWithAub;
    }

    public SickNoteStatus getStatus() {
        return status;
    }

    public SickNoteType getSickNoteType() {
        return sickNoteType;
    }

    public boolean isAllowedToEdit() {
        return allowedToEdit;
    }

    public boolean isAllowedToCancel() {
        return allowedToCancel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SickNoteDto) obj;
        return Objects.equals(this.id, that.id) &&
            Objects.equals(this.startDate, that.startDate) &&
            Objects.equals(this.endDate, that.endDate) &&
            Objects.equals(this.dayLength, that.dayLength) &&
            this.isAubPresent == that.isAubPresent &&
            Objects.equals(this.workDays, that.workDays) &&
            Objects.equals(this.workDaysWithAub, that.workDaysWithAub) &&
            Objects.equals(this.status, that.status) &&
            Objects.equals(this.sickNoteType, that.sickNoteType) &&
            this.allowedToEdit == that.allowedToEdit &&
            this.allowedToCancel == that.allowedToCancel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startDate, endDate, dayLength, isAubPresent, workDays, workDaysWithAub, status, sickNoteType, allowedToEdit, allowedToCancel);
    }

    @Override
    public String toString() {
        return "SickNoteDto[" +
            "id=" + id + ", " +
            "startDate=" + startDate + ", " +
            "endDate=" + endDate + ", " +
            "dayLength=" + dayLength + ", " +
            "isAubPresent=" + isAubPresent + ", " +
            "workDays=" + workDays + ", " +
            "workDaysWithAub=" + workDaysWithAub + ", " +
            "status=" + status + ", " +
            "sickNoteType=" + sickNoteType + ", " +
            "allowedToEdit=" + allowedToEdit + ", " +
            "allowedToCancel=" + allowedToCancel + ']';
    }

}

