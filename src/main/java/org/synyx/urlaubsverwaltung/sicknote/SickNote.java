package org.synyx.urlaubsverwaltung.sicknote;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;

import static java.time.ZoneOffset.UTC;
import static javax.persistence.EnumType.STRING;

/**
 * Entity representing a sick note with information about employee and period.
 */
@Entity
public class SickNote {

    @Id
    @GeneratedValue
    private Integer id;

    /**
     * One person may have multiple sick notes.
     */
    @ManyToOne
    private Person person;

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

    public SickNote() {
        this.lastEdited = LocalDate.now(UTC);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public SickNoteType getSickNoteType() {
        return sickNoteType;
    }

    public void setSickNoteType(SickNoteType sickNoteType) {
        this.sickNoteType = sickNoteType;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public void setDayLength(DayLength dayLength) {
        this.dayLength = dayLength;
    }

    public boolean isAubPresent() {
        return getAubStartDate() != null && getAubEndDate() != null;
    }

    public LocalDate getAubStartDate() {
        return this.aubStartDate;
    }

    public void setAubStartDate(LocalDate aubStartDate) {
        this.aubStartDate = aubStartDate;
    }

    public LocalDate getAubEndDate() {
        return this.aubEndDate;
    }

    public void setAubEndDate(LocalDate aubEndDate) {
        this.aubEndDate = aubEndDate;
    }

    public LocalDate getLastEdited() {
        return this.lastEdited;
    }

    public void setLastEdited(LocalDate lastEdited) {
        this.lastEdited = lastEdited;
    }

    public LocalDate getEndOfSickPayNotificationSend() {
        return endOfSickPayNotificationSend;
    }

    public void setEndOfSickPayNotificationSend(LocalDate endOfSickPayNotificationSend) {
        this.endOfSickPayNotificationSend = endOfSickPayNotificationSend;
    }

    public boolean isActive() {
        return SickNoteStatus.ACTIVE.equals(getStatus());
    }

    public SickNoteStatus getStatus() {
        return status;
    }

    public void setStatus(SickNoteStatus status) {
        this.status = status;
    }

    public Period getPeriod() {
        return new Period(getStartDate(), getEndDate(), getDayLength());
    }

    @Override
    public String toString() {
        return "SickNote{" +
            "id=" + getId() +
            ", person=" + person +
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
        final SickNote that = (SickNote) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
