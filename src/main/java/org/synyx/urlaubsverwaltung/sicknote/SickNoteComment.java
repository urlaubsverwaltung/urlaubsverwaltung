package org.synyx.urlaubsverwaltung.sicknote;

import org.synyx.urlaubsverwaltung.comment.AbstractComment;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.time.Clock;


/**
 * Comment to a sick note containing detailed information like date of comment or commenting person.
 */
@Entity
public class SickNoteComment extends AbstractComment {

    @ManyToOne
    private SickNote sickNote;

    @Enumerated(EnumType.STRING)
    private SickNoteAction action;

    private SickNoteComment() {
        super();
    }

    public SickNoteComment(Clock clock) {
        super(clock);
    }

    public SickNote getSickNote() {

        return sickNote;
    }


    public void setSickNote(SickNote sickNote) {

        this.sickNote = sickNote;
    }


    public SickNoteAction getAction() {

        return action;
    }


    public void setAction(SickNoteAction action) {

        this.action = action;
    }
}
