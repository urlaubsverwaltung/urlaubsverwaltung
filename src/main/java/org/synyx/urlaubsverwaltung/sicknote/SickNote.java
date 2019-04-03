package org.synyx.urlaubsverwaltung.sicknote;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateFormat;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.ZonedDateTime;
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
     * @since  2.15.0
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
     * @since  2.9.4
     */
    @Enumerated(EnumType.STRING)
    private DayLength dayLength;

    /**
     * Period of the AUB (Arbeitsunfähigkeitsbescheinigung), is optional.
     */
    private LocalDate aubStartDate;

    private LocalDate aubEndDate;

    private LocalDate lastEdited;

    @Enumerated(EnumType.STRING)
    private SickNoteStatus status;

    public SickNote() {

        this.lastEdited = ZonedDateTime.now(UTC).toLocalDate();
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

        if (this.startDate == null) {
            return null;
        }

        return this.startDate;
    }


    public void setStartDate(LocalDate startDate) {

        if (startDate == null) {
            this.startDate = null;
        } else {
            this.startDate = startDate;
        }
    }


    public LocalDate getEndDate() {

        if (this.endDate == null) {
            return null;
        }

        return this.endDate;
    }


    public void setEndDate(LocalDate endDate) {

        if (endDate == null) {
            this.endDate = null;
        } else {
            this.endDate = endDate;
        }
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

        if (this.aubStartDate == null) {
            return null;
        }

        return this.aubStartDate;
    }


    public void setAubStartDate(LocalDate aubStartDate) {

        if (aubStartDate == null) {
            this.aubStartDate = null;
        } else {
            this.aubStartDate = aubStartDate;
        }
    }


    public LocalDate getAubEndDate() {

        if (this.aubEndDate == null) {
            return null;
        }

        return this.aubEndDate;
    }


    public void setAubEndDate(LocalDate aubEndDate) {

        if (aubEndDate == null) {
            this.aubEndDate = null;
        } else {
            this.aubEndDate = aubEndDate;
        }
    }


    public LocalDate getLastEdited() {

        if (this.lastEdited == null) {
            return null;
        }

        return this.lastEdited;
    }


    public void setLastEdited(LocalDate lastEdited) {

        if (lastEdited == null) {
            this.lastEdited = null;
        } else {
            this.lastEdited = lastEdited;
        }
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

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getId())
            .append("startDate", getStartDate().format(DateTimeFormatter.ofPattern(DateFormat.PATTERN)))
            .append("endDate", getEndDate().format(DateTimeFormatter.ofPattern(DateFormat.PATTERN)))
            .append("dayLength", getDayLength())
            .append("sickNoteType", getSickNoteType())
            .append("status", getStatus())
            .append("person", getPerson())
            .toString();
    }
}
