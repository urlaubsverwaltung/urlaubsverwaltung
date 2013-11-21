package org.synyx.urlaubsverwaltung.sicknote;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteComment;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

    // period of the aub (Arbeitsunfähigkeitsbescheinigung)
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date aubStartDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date aubEndDate;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<SickNoteComment> comments = new ArrayList<SickNoteComment>();

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


    public List<SickNoteComment> getComments() {

        return comments;
    }


    public void setComments(List<SickNoteComment> comments) {

        this.comments = comments;
    }


    public void addComment(SickNoteComment comment) {

        this.comments.add(comment);
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


    @Override
    public void setId(Integer id) {

        super.setId(id);
    }
}
