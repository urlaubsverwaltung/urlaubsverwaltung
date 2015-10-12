package org.synyx.urlaubsverwaltung.core.application.domain;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;


/**
 * This class describes the information that every step of an {@link Application}'s lifecycle contains.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Entity
public class ApplicationComment extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 8908423789423089L;

    private String text;

    @ManyToOne
    private Application application;

    @ManyToOne
    private Person person;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date date;

    // each application may only have one comment to each ApplicationStatus
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    ApplicationComment() {

        // needed for Hibernate
    }


    public ApplicationComment(Person person) {

        this.person = person;
    }

    public Application getApplication() {

        return application;
    }


    public void setApplication(Application application) {

        this.application = application;
    }


    public DateMidnight getDate() {

        return date == null ? null : new DateTime(date).toDateMidnight();
    }


    public void setDate(DateMidnight date) {

        this.date = date == null ? null : date.toDate();
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


    public ApplicationStatus getStatus() {

        return status;
    }


    public void setStatus(ApplicationStatus status) {

        this.status = status;
    }
}
