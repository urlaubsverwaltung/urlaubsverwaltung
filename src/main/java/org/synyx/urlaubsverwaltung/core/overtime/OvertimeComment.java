package org.synyx.urlaubsverwaltung.core.overtime;

import org.synyx.urlaubsverwaltung.core.comment.AbstractComment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;


/**
 * Recorded comment after executed an overtime action, e.g. create a new overtime record.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Entity
public class OvertimeComment extends AbstractComment {

    @ManyToOne
    private Overtime overtime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OvertimeAction action;

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
