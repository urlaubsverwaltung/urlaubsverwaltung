package org.synyx.urlaubsverwaltung.core.sicknote;

import lombok.Data;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.core.person.Person;

import javax.persistence.*;
import java.util.Date;


/**
 * Entity representing a sick note with information about employee and period.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Entity
@Data
public class SickNote extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 8524575678589823089L;

    // One person may have multiple sick notes
    @ManyToOne
    private Person person;

    @Enumerated(EnumType.STRING)
    private SickNoteType type;

    // period of the illness
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

    // period of the aub (Arbeitsunfähigkeitsbescheinigung)
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date aubStartDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date aubEndDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date lastEdited;

    // if sick note has been converted to vacation, it's not active
    private boolean active;


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


    @Override
    public void setId(Integer id) {

        super.setId(id);
    }
}
