package org.synyx.urlaubsverwaltung.overtime;

import org.synyx.urlaubsverwaltung.comment.AbstractComment;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.time.Clock;

import static javax.persistence.EnumType.STRING;

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
    @Enumerated(STRING)
    private OvertimeCommentAction action;

    protected OvertimeComment() {
        super();
    }

    public OvertimeComment(Clock clock) {
        super(clock);
    }

    public OvertimeComment(Person author, Overtime overtime, OvertimeCommentAction action, Clock clock) {
        super(clock);
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

    public OvertimeCommentAction getAction() {
        return action;
    }

    public void setAction(OvertimeCommentAction action) {
        this.action = action;
    }
}
