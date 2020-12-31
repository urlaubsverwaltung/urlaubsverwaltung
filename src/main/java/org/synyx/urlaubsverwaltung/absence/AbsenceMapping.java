package org.synyx.urlaubsverwaltung.absence;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;

import static javax.persistence.EnumType.STRING;


/**
 * Mapping object between absence (application for leave or sick note) and sync calendar event.
 */
@Entity
public class AbsenceMapping extends AbstractPersistable<Integer> {

    @Column(nullable = false)
    private Integer absenceId;

    @Enumerated(STRING)
    @Column(nullable = false)
    private AbsenceMappingType absenceMappingType;

    @Column(nullable = false)
    private String eventId;

    protected AbsenceMapping() {
        /* OK */
    }

    public AbsenceMapping(Integer absenceId, AbsenceMappingType absenceMappingType, String eventId) {
        this.absenceId = absenceId;
        this.absenceMappingType = absenceMappingType;
        this.eventId = eventId;
    }

    public Integer getAbsenceId() {
        return absenceId;
    }

    public void setAbsenceId(Integer absenceId) {
        this.absenceId = absenceId;
    }

    public AbsenceMappingType getAbsenceMappingType() {
        return absenceMappingType;
    }

    public void setAbsenceType(AbsenceMappingType absenceMappingType) {
        this.absenceMappingType = absenceMappingType;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
