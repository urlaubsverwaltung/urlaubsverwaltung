package org.synyx.urlaubsverwaltung.core.comment;

import org.joda.time.DateMidnight;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;


/**
 * Represents a basic comment.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@MappedSuperclass
public abstract class AbstractComment extends AbstractPersistable<Integer> {

    // Who has written the comment?
    @ManyToOne
    private Person person;

    // When has the comment be written?
    @Column(nullable = false)
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date date;

    // What is the content of the comment?
    private String text;

    public AbstractComment() {

        this.date = DateMidnight.now().toDate();
    }

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public DateMidnight getDate() {

        if (date == null) {
            throw new IllegalStateException("Date of comment can never be null!");
        }

        return new DateMidnight(date.getTime());
    }


    public String getText() {

        return text;
    }


    public void setText(String text) {

        this.text = text;
    }
}
