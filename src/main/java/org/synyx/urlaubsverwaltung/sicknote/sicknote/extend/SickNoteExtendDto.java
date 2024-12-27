package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

class SickNoteExtendDto {

    private Long sickNoteId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal workingDays;

    /**
     * This field is only required for error styling with {@code `th:errorclass`}.
     *
     * <p>
     * This has no value and does not have to be considered by us.
     */
    private String extendToDate;

    SickNoteExtendDto() {
        this(null, null, null, null);
    }

    SickNoteExtendDto(Long sickNoteId, LocalDate startDate) {
        this(sickNoteId, startDate, null, null);
    }

    SickNoteExtendDto(Long sickNoteId, LocalDate startDate, LocalDate endDate, BigDecimal workingDays) {
        this.sickNoteId = sickNoteId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.workingDays = workingDays;
    }

    SickNoteExtendDto(SickNoteExtendDto dto) {
        this.sickNoteId = dto.sickNoteId;
        this.startDate = dto.startDate;
        this.endDate = dto.endDate;
        this.workingDays = dto.workingDays;
    }

    public Long getSickNoteId() {
        return sickNoteId;
    }

    public void setSickNoteId(Long sickNoteId) {
        this.sickNoteId = sickNoteId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(BigDecimal workingDays) {
        this.workingDays = workingDays;
    }

    public String getExtendToDate() {
        return extendToDate;
    }

    public void setExtendToDate(String extendToDate) {
        this.extendToDate = extendToDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SickNoteExtendDto) obj;
        return Objects.equals(this.sickNoteId, that.sickNoteId) &&
            Objects.equals(this.startDate, that.startDate) &&
            Objects.equals(this.endDate, that.endDate) &&
            Objects.equals(this.workingDays, that.workingDays);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sickNoteId, startDate, endDate, workingDays);
    }

    @Override
    public String toString() {
        return "SickNoteExtendDto[" +
            "sickNoteId=" + sickNoteId + ", " +
            "startDate=" + startDate + ", " +
            "endDate=" + endDate + ", " +
            "workingDays=" + workingDays + ']';
    }
}
