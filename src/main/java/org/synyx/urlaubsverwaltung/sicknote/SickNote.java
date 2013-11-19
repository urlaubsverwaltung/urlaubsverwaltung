package org.synyx.urlaubsverwaltung.sicknote;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;

import java.util.Date;

import javax.persistence.Entity;
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

    // One person may have multiple sick notes
    @ManyToOne
    private Person person;

    // period of the illness
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

    private BigDecimal workDays;

    // aub = Arbeitsunfähigkeitsbescheinigung
    private boolean aubPresent;

    private String comment;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date lastEdited;

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
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


    public boolean isAubPresent() {

        return aubPresent;
    }


    public void setAubPresent(boolean aubPresent) {

        this.aubPresent = aubPresent;
    }


    public String getComment() {

        return comment;
    }


    public void setComment(String comment) {

        this.comment = comment;
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


    public BigDecimal getWorkDays() {

        return workDays;
    }


    public void setWorkDays(BigDecimal workDays) {

        this.workDays = workDays;
    }
}
