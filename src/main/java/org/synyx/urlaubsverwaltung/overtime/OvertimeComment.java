package org.synyx.urlaubsverwaltung.overtime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.comment.AbstractComment;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;

/**
 * Recorded comment after executed an overtime action, e.g. create a new overtime record.
 *
 * @since 2.11.0
 */
@Entity
public class OvertimeComment extends AbstractComment {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "overtime_comment_generator")
    @SequenceGenerator(name = "overtime_comment_generator", sequenceName = "overtime_comment_id_seq")
    private Long id;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OvertimeComment that = (OvertimeComment) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
