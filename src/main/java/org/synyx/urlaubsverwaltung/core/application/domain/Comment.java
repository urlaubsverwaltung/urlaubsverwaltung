package org.synyx.urlaubsverwaltung.core.application.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.core.person.Person;

import javax.persistence.*;
import java.util.Date;


/**
 * This class describes the information that every step of an {@link Application}'s lifecycle contains.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Entity
@Data
public class Comment extends AbstractPersistable<Integer> {

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

    Comment() {

        // needed for Hibernate
    }


    public Comment(Person person) {

        this.person = person;
    }

    public DateMidnight getDate() {

        return date == null ? null : new DateTime(date).toDateMidnight();
    }


    public void setDate(DateMidnight date) {

        this.date = date == null ? null : date.toDate();
    }

}
