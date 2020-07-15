package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateFormat;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.ZoneOffset.UTC;


/**
 * Entity representing a sick note with information about employee and period.
 */
@Entity
public class SickNote extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 8524575678589823089L;

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
    private Instant startDate;

    private Instant endDate;

    /**
     * Time of day for the sick note: morning, noon or full day
     *
     * @since 2.9.4
     */
    @Enumerated(EnumType.STRING)
    private DayLength dayLength;

    /**
     * Period of the AUB (Arbeitsunfähigkeitsbescheinigung), is optional.
     */
    private Instant aubStartDate;

    private Instant aubEndDate;

    private Instant lastEdited;

    @Enumerated(EnumType.STRING)
    private SickNoteStatus status;

    public SickNote() {

        this.lastEdited = Instant.now();
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


    public Instant getStartDate() {

        if (this.startDate == null) {
            return null;
        }

        return this.startDate;
    }


    public void setStartDate(Instant startDate) {

        this.startDate = startDate;
    }


    public Instant getEndDate() {

        if (this.endDate == null) {
            return null;
        }

        return this.endDate;
    }


    public void setEndDate(Instant endDate) {

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


    public Instant getAubStartDate() {

        if (this.aubStartDate == null) {
            return null;
        }

        return this.aubStartDate;
    }


    public void setAubStartDate(Instant aubStartDate) {

        this.aubStartDate = aubStartDate;
    }


    public Instant getAubEndDate() {

        if (this.aubEndDate == null) {
            return null;
        }

        return this.aubEndDate;
    }


    public void setAubEndDate(Instant aubEndDate) {

        this.aubEndDate = aubEndDate;
    }


    public Instant getLastEdited() {

        if (this.lastEdited == null) {
            return null;
        }

        return this.lastEdited;
    }


    public void setLastEdited(Instant lastEdited) {

        this.lastEdited = lastEdited;
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
    public void setId(Integer id) { // NOSONAR - make it public instead of protected

        super.setId(id);
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
            ", status=" + status +
            '}';
    }

    private Object formatNullable(Instant date) {
        return date != null ? DateTimeFormatter.ofPattern(DateFormat.PATTERN).format(date) : null;
    }
}
