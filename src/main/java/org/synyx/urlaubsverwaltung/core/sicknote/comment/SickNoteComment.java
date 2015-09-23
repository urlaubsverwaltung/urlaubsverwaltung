package org.synyx.urlaubsverwaltung.core.sicknote.comment;

import lombok.Data;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import javax.persistence.*;
import java.util.Date;


/**
 * Comment to a sick note containing detailed information like date of comment or commenting person.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Entity
@Data
public class SickNoteComment extends AbstractPersistable<Integer> {

    @ManyToOne
    private SickNote sickNote;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date date;

    @ManyToOne
    private Person person;

    private String text;

    @Enumerated(EnumType.STRING)
    private SickNoteStatus status;

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
}
