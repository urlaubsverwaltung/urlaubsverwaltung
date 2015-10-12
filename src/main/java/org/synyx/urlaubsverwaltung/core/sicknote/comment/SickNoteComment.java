package org.synyx.urlaubsverwaltung.core.sicknote.comment;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;


/**
 * Comment to a sick note containing detailed information like date of comment or commenting person.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Entity
public class SickNoteComment extends AbstractPersistable<Integer> {

    @ManyToOne
    private SickNote sickNote;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date date;

    @ManyToOne
    private Person person;

    private String text;

    @Enumerated(EnumType.STRING)
    private SickNoteCommentStatus status;

    public SickNote getSickNote() {

        return sickNote;
    }


    public void setSickNote(SickNote sickNote) {

        this.sickNote = sickNote;
    }


    public DateMidnight getDate() {

        if (this.date == null) {
            return null;
        }

        return new DateTime(this.date).toDateMidnight();
    }


    public void setDate(DateMidnight date) {

        if (date == null) {
            this.date = null;
        } else {
            this.date = date.toDate();
        }
    }


    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public SickNoteCommentStatus getStatus() {

        return status;
    }


    public void setStatus(SickNoteCommentStatus status) {

        this.status = status;
    }


    public String getText() {

        return text;
    }


    public void setText(String text) {

        this.text = text;
    }
}
