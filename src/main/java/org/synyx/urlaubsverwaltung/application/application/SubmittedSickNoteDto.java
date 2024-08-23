package org.synyx.urlaubsverwaltung.application.application;

import jakarta.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents the view dto for submitted sick notes rendered on the application/absences page.
 */
public final class SubmittedSickNoteDto {

    private final String id;
    private final BigDecimal workDays;
    private final SickNotePersonDto person;
    private final String type;
    private final String durationOfAbsenceDescription;
    private final boolean extensionSubmitted;
    private final BigDecimal additionalWorkDays;
    private final String status = "SUBMITTED";

    public SubmittedSickNoteDto(
        String id,
        BigDecimal workDays,
        SickNotePersonDto person,
        String type,
        String durationOfAbsenceDescription,
        boolean extensionSubmitted,
        @Nullable BigDecimal additionalWorkDays
    ) {
        this.id = id;
        this.workDays = workDays;
        this.person = person;
        this.type = type;
        this.durationOfAbsenceDescription = durationOfAbsenceDescription;
        this.extensionSubmitted = extensionSubmitted;
        this.additionalWorkDays = additionalWorkDays;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getWorkDays() {
        return workDays;
    }

    public SickNotePersonDto getPerson() {
        return person;
    }

    public String getType() {
        return type;
    }

    public String getDurationOfAbsenceDescription() {
        return durationOfAbsenceDescription;
    }

    public boolean isExtensionSubmitted() {
        return extensionSubmitted;
    }

    public BigDecimal getAdditionalWorkDays() {
        return additionalWorkDays;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SubmittedSickNoteDto) obj;
        return Objects.equals(this.id, that.id) &&
            Objects.equals(this.workDays, that.workDays) &&
            Objects.equals(this.person, that.person) &&
            Objects.equals(this.type, that.type) &&
            Objects.equals(this.durationOfAbsenceDescription, that.durationOfAbsenceDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, workDays, person, type, durationOfAbsenceDescription);
    }

    @Override
    public String toString() {
        return "SickNoteDto[" +
            "id=" + id + ", " +
            "workDays=" + workDays + ", " +
            "person=" + person + ", " +
            "type=" + type + ", " +
            "durationOfAbsenceDescription=" + durationOfAbsenceDescription + ", " +
            "extensionSubmitted=" + extensionSubmitted + ", " +
            "additionalWorkDays=" + additionalWorkDays + ", " +
            "status=" + status + ']';
    }
}
