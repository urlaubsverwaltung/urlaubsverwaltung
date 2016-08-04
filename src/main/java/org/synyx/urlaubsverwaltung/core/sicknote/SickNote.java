package org.synyx.urlaubsverwaltung.core.sicknote;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.period.Period;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.DateFormat;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;


/**
 * Entity representing a sick note with information about employee and period.
 *
 * @author  Aljona Murygina - murygina@synyx.de
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
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

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
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date aubStartDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date aubEndDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date lastEdited;

    @Enumerated(EnumType.STRING)
    private SickNoteStatus status;

    public SickNote() {

        this.lastEdited = DateTime.now().withTimeAtStartOfDay().toDate();
    }

    public final Person getPerson() {

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


    public final DateMidnight getStartDate() {

        if (this.startDate == null) {
            return null;
        }

        return new DateTime(this.startDate).toDateMidnight();
    }


    public void setStartDate(DateMidnight startDate) {

        if (startDate == null) {
            this.startDate = null;
        } else {
            this.startDate = startDate.toDate();
        }
    }


    public final DateMidnight getEndDate() {

        if (this.endDate == null) {
            return null;
        }

        return new DateTime(this.endDate).toDateMidnight();
    }


    public void setEndDate(DateMidnight endDate) {

        if (endDate == null) {
            this.endDate = null;
        } else {
            this.endDate = endDate.toDate();
        }
    }


    public final DayLength getDayLength() {

        return dayLength;
    }


    public void setDayLength(DayLength dayLength) {

        this.dayLength = dayLength;
    }


    public boolean isAubPresent() {

        return getAubStartDate() != null && getAubEndDate() != null;
    }


    public DateMidnight getAubStartDate() {

        if (this.aubStartDate == null) {
            return null;
        }

        return new DateTime(this.aubStartDate).toDateMidnight();
    }


    public void setAubStartDate(DateMidnight aubStartDate) {

        if (aubStartDate == null) {
            this.aubStartDate = null;
        } else {
            this.aubStartDate = aubStartDate.toDate();
        }
    }


    public DateMidnight getAubEndDate() {

        if (this.aubEndDate == null) {
            return null;
        }

        return new DateTime(this.aubEndDate).toDateMidnight();
    }


    public void setAubEndDate(DateMidnight aubEndDate) {

        if (aubEndDate == null) {
            this.aubEndDate = null;
        } else {
            this.aubEndDate = aubEndDate.toDate();
        }
    }


    public DateMidnight getLastEdited() {

        if (this.lastEdited == null) {
            return null;
        }

        return new DateTime(this.lastEdited).toDateMidnight();
    }


    public void setLastEdited(DateMidnight lastEdited) {

        if (lastEdited == null) {
            this.lastEdited = null;
        } else {
            this.lastEdited = lastEdited.toDate();
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
            .append("startDate", getStartDate().toString(DateFormat.PATTERN))
            .append("endDate", getEndDate().toString(DateFormat.PATTERN))
            .append("dayLength", getDayLength())
            .append("sickNoteType", getSickNoteType())
            .append("status", getStatus())
            .append("person", getPerson())
            .toString();
    }
}
