package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.synyx.urlaubsverwaltung.comment.AbstractComment;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.time.Clock;

import static javax.persistence.EnumType.STRING;

/**
 * Comment to a sick note containing detailed information like date of comment or commenting person.
 */
@Entity(name = "sick_note_comment")
public class SickNoteCommentEntity extends AbstractComment {

    @ManyToOne
    private SickNote sickNote;

    @Enumerated(STRING)
    private SickNoteCommentAction action;

    protected SickNoteCommentEntity() {
        super();
    }

    public SickNoteCommentEntity(Clock clock) {
        super(clock);
    }

    public SickNote getSickNote() {
        return sickNote;
    }

    public void setSickNote(SickNote sickNote) {
        this.sickNote = sickNote;
    }

    public SickNoteCommentAction getAction() {
        return action;
    }

    public void setAction(SickNoteCommentAction action) {
        this.action = action;
    }
}
