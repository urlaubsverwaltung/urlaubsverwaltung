package org.synyx.urlaubsverwaltung.calendarintegration.absence;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


/**
 * Mapping object between absence (application for leave or sick note) and sync calendar event.
 */
@Entity
public class AbsenceMapping extends AbstractPersistable<Integer> {

    @Column(nullable = false)
    private Integer absenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AbsenceType absenceType;

    @Column(nullable = false)
    private String eventId;

    public AbsenceMapping() {

        /* OK */
    }


    public AbsenceMapping(Integer absenceId, AbsenceType absenceType, String eventId) {

        this.absenceId = absenceId;
        this.absenceType = absenceType;
        this.eventId = eventId;
    }

    public Integer getAbsenceId() {

        return absenceId;
    }


    public void setAbsenceId(Integer absenceId) {

        this.absenceId = absenceId;
    }


    public AbsenceType getAbsenceType() {

        return absenceType;
    }


    public void setAbsenceType(AbsenceType absenceType) {

        this.absenceType = absenceType;
    }


    public String getEventId() {

        return eventId;
    }


    public void setEventId(String eventId) {

        this.eventId = eventId;
    }
}
