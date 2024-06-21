package org.synyx.urlaubsverwaltung.application.application;

import java.math.BigDecimal;
import java.util.Objects;

public final class SickNoteDto {

    private final String id;
    private final BigDecimal workDays;
    private final SickNotePersonDto person;
    private final String type;
    private final String status;
    private final String durationOfAbsenceDescription;

    public SickNoteDto(
        String id,
        BigDecimal workDays,
        SickNotePersonDto person,
        String type,
        String status,
        String durationOfAbsenceDescription
    ) {
        this.id = id;
        this.workDays = workDays;
        this.person = person;
        this.type = type;
        this.status = status;
        this.durationOfAbsenceDescription = durationOfAbsenceDescription;
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

    public String getStatus() {
        return status;
    }

    public String getDurationOfAbsenceDescription() {
        return durationOfAbsenceDescription;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SickNoteDto) obj;
        return Objects.equals(this.id, that.id) &&
            Objects.equals(this.workDays, that.workDays) &&
            Objects.equals(this.person, that.person) &&
            Objects.equals(this.type, that.type) &&
            Objects.equals(this.status, that.status) &&
            Objects.equals(this.durationOfAbsenceDescription, that.durationOfAbsenceDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, workDays, person, type, status, durationOfAbsenceDescription);
    }

    @Override
    public String toString() {
        return "SickNoteDto[" +
            "id=" + id + ", " +
            "workDays=" + workDays + ", " +
            "person=" + person + ", " +
            "type=" + type + ", " +
            "status=" + status + ", " +
            "durationOfAbsenceDescription=" + durationOfAbsenceDescription + ']';
    }
}
