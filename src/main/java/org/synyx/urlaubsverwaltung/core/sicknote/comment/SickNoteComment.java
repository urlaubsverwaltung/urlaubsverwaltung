package org.synyx.urlaubsverwaltung.core.sicknote.comment;

import org.synyx.urlaubsverwaltung.core.comment.AbstractComment;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;


/**
 * Comment to a sick note containing detailed information like date of comment or commenting person.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Entity
public class SickNoteComment extends AbstractComment {

    @ManyToOne
    private SickNote sickNote;

    @Enumerated(EnumType.STRING)
    private SickNoteCommentStatus status;

    public SickNoteComment() {

        super();
    }

    public SickNote getSickNote() {

        return sickNote;
    }


    public void setSickNote(SickNote sickNote) {

        this.sickNote = sickNote;
    }


    public SickNoteCommentStatus getStatus() {

        return status;
    }


    public void setStatus(SickNoteCommentStatus status) {

        this.status = status;
    }
}
