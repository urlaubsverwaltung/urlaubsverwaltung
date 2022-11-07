package org.synyx.urlaubsverwaltung.sicknote.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.synyx.urlaubsverwaltung.comment.AbstractComment;

import java.time.Clock;

import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

/**
 * Comment to a sick note containing detailed information like date of comment or commenting person.
 */
@Entity(name = "sick_note_comment")
public class SickNoteCommentEntity extends AbstractComment {

    @NotNull
    @Column(name = "sick_note_id")
    @OnDelete(action = CASCADE)
    private Integer sickNoteId;

    @Enumerated(STRING)
    private SickNoteCommentAction action;

    protected SickNoteCommentEntity() {
        super();
    }

    public SickNoteCommentEntity(Clock clock) {
        super(clock);
    }

    public Integer getSickNoteId() {
        return sickNoteId;
    }

    public void setSickNoteId(Integer sickNoteId) {
        this.sickNoteId = sickNoteId;
    }

    public SickNoteCommentAction getAction() {
        return action;
    }

    public void setAction(SickNoteCommentAction action) {
        this.action = action;
    }
}
