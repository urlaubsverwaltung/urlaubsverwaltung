package org.synyx.urlaubsverwaltung.domain;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Entity
public class Comment extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 8908423789423089L;

    private String reason;

    @OneToOne
    private Application application;

    private String nameOfCommentingPerson;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateOfComment;

    public Application getApplication() {

        return application;
    }


    public void setApplication(Application application) {

        this.application = application;
    }


    public DateMidnight getDateOfComment() {

        return dateOfComment == null ? null : new DateTime(dateOfComment).toDateMidnight();
    }


    public void setDateOfComment(DateMidnight dateOfComment) {

        this.dateOfComment = dateOfComment == null ? null : dateOfComment.toDate();
    }


    public String getNameOfCommentingPerson() {

        return nameOfCommentingPerson;
    }


    public void setNameOfCommentingPerson(String nameOfCommentingPerson) {

        this.nameOfCommentingPerson = nameOfCommentingPerson;
    }


    public String getText() {

        return reason;
    }


    public void setText(String reason) {

        this.reason = reason;
    }
}
