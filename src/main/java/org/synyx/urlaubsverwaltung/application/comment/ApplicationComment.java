package org.synyx.urlaubsverwaltung.application.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.comment.AbstractComment;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;

/**
 * This class describes the information that every step of an {@link Application}'s lifecycle contains.
 */
@Entity
public class ApplicationComment extends AbstractComment {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "application_comment_generator")
    @SequenceGenerator(name = "application_comment_generator", sequenceName = "application_comment_id_seq")
    private Long id;

    @ManyToOne
    private Application application;

    @Enumerated(STRING)
    private ApplicationCommentAction action;

    protected ApplicationComment() {
        super();
    }

    public ApplicationComment(Clock clock) {
        super(clock);
    }

    public ApplicationComment(Person person, Clock clock) {
        super(clock);
        super.setPerson(person);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public ApplicationCommentAction getAction() {
        return action;
    }

    public void setAction(ApplicationCommentAction action) {
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
        final ApplicationComment that = (ApplicationComment) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
