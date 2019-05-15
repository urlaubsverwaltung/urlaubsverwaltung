package org.synyx.urlaubsverwaltung.comment;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;


/**
 * Represents a basic comment.
 */
@MappedSuperclass
public abstract class AbstractComment extends AbstractPersistable<Integer> {

    // Who has written the comment?
    @ManyToOne
    private Person person;

    // When has the comment be written?
    @Column(nullable = false)
    private final LocalDate date;

    // What is the content of the comment?
    private String text;

    public AbstractComment() {
        this.date = LocalDate.now(UTC);
    }

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public LocalDate getDate() {

        if (date == null) {
            throw new IllegalStateException("Date of comment can never be null!");
        }

        return date;
    }


    public String getText() {

        return text;
    }


    public void setText(String text) {

        this.text = text;
    }
}
