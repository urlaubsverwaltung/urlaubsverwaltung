package org.synyx.urlaubsverwaltung.core.sicknote;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;

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

    @Enumerated(EnumType.STRING)
    private SickNoteType type;

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

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public SickNoteType getType() {

        return type;
    }


    public void setType(SickNoteType type) {

        this.type = type;
    }


    public DateMidnight getStartDate() {

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


    public DateMidnight getEndDate() {

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


    public DayLength getDayLength() {

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


    @Override
    public void setId(Integer id) {

        super.setId(id);
    }
}
