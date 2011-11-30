package org.synyx.urlaubsverwaltung.domain;

import org.joda.time.DateMidnight;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.OneToOne;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Entity
public class Comment extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 8908423789423089L;

    private String text;

    @OneToOne
    private Person person;

    private DateMidnight dateOfComment;

    public DateMidnight getDateOfComment() {

        return dateOfComment;
    }


    public void setDateOfComment(DateMidnight dateOfComment) {

        this.dateOfComment = dateOfComment;
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
