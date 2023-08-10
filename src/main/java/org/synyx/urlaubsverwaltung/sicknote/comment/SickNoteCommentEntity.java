package org.synyx.urlaubsverwaltung.sicknote.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.synyx.urlaubsverwaltung.comment.AbstractComment;

import java.time.Clock;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

/**
 * Comment to a sick note containing detailed information like date of comment or commenting person.
 */
@Entity(name = "sick_note_comment")
public class SickNoteCommentEntity extends AbstractComment {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "sick_note_comment_generator")
    @SequenceGenerator(name = "sick_note_comment_generator", sequenceName = "sick_note_comment_id_seq")
    private Long id;

    @NotNull
    @Column(name = "sick_note_id")
    @OnDelete(action = CASCADE)
    private Long sickNoteId;

    @Enumerated(STRING)
    private SickNoteCommentAction action;

    protected SickNoteCommentEntity() {
        super();
    }

    public SickNoteCommentEntity(Clock clock) {
        super(clock);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSickNoteId() {
        return sickNoteId;
    }

    public void setSickNoteId(Long sickNoteId) {
        this.sickNoteId = sickNoteId;
    }

    public SickNoteCommentAction getAction() {
        return action;
    }

    public void setAction(SickNoteCommentAction action) {
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SickNoteCommentEntity that = (SickNoteCommentEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
