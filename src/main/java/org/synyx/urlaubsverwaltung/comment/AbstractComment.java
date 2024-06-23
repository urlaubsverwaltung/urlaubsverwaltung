package org.synyx.urlaubsverwaltung.comment;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

import java.time.Clock;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Optional.ofNullable;


/**
 * Represents a basic comment.
 */
@MappedSuperclass
public abstract class AbstractComment extends AbstractTenantAwareEntity {

    // Who has written the comment?
    @ManyToOne
    private Person person;

    // When has the comment be written?
    @Column(nullable = false)
    private Instant date;

    // What is the content of the comment?
    private String text;

    protected AbstractComment() {
        // needed for hibernate
    }

    protected AbstractComment(Clock clock) {
        final Clock c = ofNullable(clock).orElse(Clock.systemUTC());
        this.date = Instant.now(c).truncatedTo(DAYS);
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Instant getDate() {
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
