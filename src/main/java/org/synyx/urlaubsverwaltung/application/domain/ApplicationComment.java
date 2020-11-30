package org.synyx.urlaubsverwaltung.application.domain;

import org.synyx.urlaubsverwaltung.comment.AbstractComment;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.time.Clock;

import static javax.persistence.EnumType.STRING;

/**
 * This class describes the information that every step of an {@link Application}'s lifecycle contains.
 */
@Entity
public class ApplicationComment extends AbstractComment {

    private static final long serialVersionUID = 8908423789423089L;

    @ManyToOne
    private Application application;

    @Enumerated(STRING)
    private ApplicationAction action;

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

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public ApplicationAction getAction() {
        return action;
    }

    public void setAction(ApplicationAction action) {
        this.action = action;
    }
}
