package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;


/**
 * Comment to a sick note containing detailled information like date of comment or commenting person.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Entity
public class SickNoteComment extends AbstractPersistable<Integer> {

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date date;

    @ManyToOne
    private Person person;

    private String text;

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


    public String getText() {

        return text;
    }


    public void setText(String text) {

        this.text = text;
    }
}
