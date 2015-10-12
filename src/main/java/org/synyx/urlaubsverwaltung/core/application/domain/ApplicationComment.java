package org.synyx.urlaubsverwaltung.core.application.domain;

import org.synyx.urlaubsverwaltung.core.comment.AbstractComment;
import org.synyx.urlaubsverwaltung.core.person.Person;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;


/**
 * This class describes the information that every step of an {@link Application}'s lifecycle contains.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Entity
public class ApplicationComment extends AbstractComment {

    private static final long serialVersionUID = 8908423789423089L;

    @ManyToOne
    private Application application;

    @Enumerated(EnumType.STRING)
    private ApplicationCommentStatus status;

    ApplicationComment() {

        // needed for Hibernate
    }


    public ApplicationComment(Person person) {

        super.setPerson(person);
    }

    public Application getApplication() {

        return application;
    }


    public void setApplication(Application application) {

        this.application = application;
    }


    public ApplicationCommentStatus getStatus() {

        return status;
    }


    public void setStatus(ApplicationCommentStatus status) {

        this.status = status;
    }
}
