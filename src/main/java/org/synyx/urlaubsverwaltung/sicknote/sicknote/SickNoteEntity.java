package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

import static javax.persistence.EnumType.STRING;

/**
 * Entity representing a sick note with information about employee and period.
 */
@Entity
@Table(name = "sick_note")
class SickNoteEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GenericGenerator(
        name = "sick_note_id_seq",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "sick_note_id_seq"),
            @Parameter(name = "initial_value", value = "1"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "sick_note_id_seq")
    private Long id;

    /**
     * One person may have multiple sick notes.
     */
    @ManyToOne
    private Person person;

    /**
     * Person that created the sick-note.
     */
    @ManyToOne
    private Person applier;

    /**
     * Type of sick note.
     *
     * @since 2.15.0
     */
    @ManyToOne
    private SickNoteType sickNoteType;

    /**
     * Sick note period: start and end date of the period, the employee is sick.
     */
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Time of day for the sick note: morning, noon or full day
     *
     * @since 2.9.4
     */
    @Enumerated(STRING)
    private DayLength dayLength;

    /**
     * Period of the AUB (Arbeitsunfähigkeitsbescheinigung), is optional.
     */
    private LocalDate aubStartDate;
    private LocalDate aubEndDate;

    private LocalDate lastEdited;

    private LocalDate endOfSickPayNotificationSend;

    @Enumerated(STRING)
    private SickNoteStatus status;

    Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    Person getPerson() {
        return person;
    }

    void setPerson(Person person) {
        this.person = person;
    }

    Person getApplier() {
        return applier;
    }

    void setApplier(Person applier) {
        this.applier = applier;
    }

    SickNoteType getSickNoteType() {
        return sickNoteType;
    }

    void setSickNoteType(SickNoteType sickNoteType) {
        this.sickNoteType = sickNoteType;
    }

    LocalDate getStartDate() {
        return this.startDate;
    }

    void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    LocalDate getEndDate() {
        return this.endDate;
    }

    void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    DayLength getDayLength() {
        return dayLength;
    }

    void setDayLength(DayLength dayLength) {
        this.dayLength = dayLength;
    }

    LocalDate getAubStartDate() {
        return this.aubStartDate;
    }

    void setAubStartDate(LocalDate aubStartDate) {
        this.aubStartDate = aubStartDate;
    }

    LocalDate getAubEndDate() {
        return this.aubEndDate;
    }

    void setAubEndDate(LocalDate aubEndDate) {
        this.aubEndDate = aubEndDate;
    }

    LocalDate getLastEdited() {
        return this.lastEdited;
    }

    void setLastEdited(LocalDate lastEdited) {
        this.lastEdited = lastEdited;
    }

    LocalDate getEndOfSickPayNotificationSend() {
        return endOfSickPayNotificationSend;
    }

    void setEndOfSickPayNotificationSend(LocalDate endOfSickPayNotificationSend) {
        this.endOfSickPayNotificationSend = endOfSickPayNotificationSend;
    }

    SickNoteStatus getStatus() {
        return status;
    }

    void setStatus(SickNoteStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SickNote{" +
            "id=" + getId() +
            ", person=" + person +
            ", applier=" + applier +
            ", sickNoteType=" + sickNoteType +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", dayLength=" + dayLength +
            ", aubStartDate=" + aubStartDate +
            ", aubEndDate=" + aubEndDate +
            ", lastEdited=" + lastEdited +
            ", endOfSickPayNotificationSend=" + endOfSickPayNotificationSend +
            ", status=" + status +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SickNoteEntity that = (SickNoteEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
