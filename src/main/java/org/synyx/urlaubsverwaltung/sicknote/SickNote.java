package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.person.Person;

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

    // aub = Arbeitsunfähigkeitsbescheinigung
    private boolean aubPresent;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date aubPresentedDate;

    private String comment;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date lastEdited;

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public Date getStartDate() {

        return startDate;
    }


    public void setStartDate(Date startDate) {

        this.startDate = startDate;
    }


    public Date getEndDate() {

        return endDate;
    }


    public void setEndDate(Date endDate) {

        this.endDate = endDate;
    }


    public boolean isAubPresent() {

        return aubPresent;
    }


    public void setAubPresent(boolean aubPresent) {

        this.aubPresent = aubPresent;
    }


    public Date getAubPresentedDate() {

        return aubPresentedDate;
    }


    public void setAubPresentedDate(Date aubPresentedDate) {

        this.aubPresentedDate = aubPresentedDate;
    }


    public String getComment() {

        return comment;
    }


    public void setComment(String comment) {

        this.comment = comment;
    }


    public Date getLastEdited() {

        return lastEdited;
    }


    public void setLastEdited(Date lastEdited) {

        this.lastEdited = lastEdited;
    }
}
