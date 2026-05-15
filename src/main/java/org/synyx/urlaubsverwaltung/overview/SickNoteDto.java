package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.math.BigDecimal;
import java.time.LocalDate;

final class SickNoteDto {

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


    SickNoteDto(Long id, LocalDate startDate, LocalDate endDate, DayLength dayLength, boolean isAubPresent, BigDecimal workDays, BigDecimal workDaysWithAub, SickNoteStatus status, SickNoteType sickNoteType, boolean allowedToEdit, boolean allowedToCancel) {
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

    public boolean isAllowedToEdit() {
        return allowedToEdit;
    }

    public SickNoteType getSickNoteType() {
        return sickNoteType;
    }

    public boolean isAllowedToCancel() {
        return allowedToCancel;
    }
}

