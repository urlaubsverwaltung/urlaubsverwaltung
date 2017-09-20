package org.synyx.urlaubsverwaltung.core.application.domain;

import lombok.Data;
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
@Data
public class ApplicationComment extends AbstractComment {

    private static final long serialVersionUID = 8908423789423089L;

    @ManyToOne
    private Application application;

    @Enumerated(EnumType.STRING)
    private ApplicationAction action;

    ApplicationComment() {

        // needed for Hibernate
    }


    public ApplicationComment(Person person) {

        super.setPerson(person);
    }
}
