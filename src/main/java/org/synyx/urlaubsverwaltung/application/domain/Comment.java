package org.synyx.urlaubsverwaltung.application.domain;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
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
    
    private String progress;

    @ManyToOne
    private Application application;

    private String nameOfCommentingPerson;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateOfComment;
    
    // each application may only have one comment to each ApplicationStatus
    private ApplicationStatus status;

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


    public String getReason() {

        return reason;
    }


    public void setReason(String reason) {

        this.reason = reason;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }
    
    
    
    
}
