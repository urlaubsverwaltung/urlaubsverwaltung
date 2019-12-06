package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.comment.AbstractComment;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;


/**
 * Recorded comment after executed an overtime action, e.g. create a new overtime record.
 *
 * @since 2.11.0
 */
@Entity
public class OvertimeComment extends AbstractComment {

    @ManyToOne
    private Overtime overtime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OvertimeAction action;

    OvertimeComment() {

        // OK
    }


    public OvertimeComment(Person author, Overtime overtime, OvertimeAction action) {

        Assert.notNull(author, "Author must be given.");
        Assert.notNull(overtime, "Overtime record must be given.");
        Assert.notNull(action, "Action must be given.");

        super.setPerson(author);

        this.overtime = overtime;
        this.action = action;
    }

    public Overtime getOvertime() {

        return overtime;
    }


    public void setOvertime(Overtime overtime) {

        this.overtime = overtime;
    }


    public OvertimeAction getAction() {

        return action;
    }


    public void setAction(OvertimeAction action) {

        this.action = action;
    }
}
