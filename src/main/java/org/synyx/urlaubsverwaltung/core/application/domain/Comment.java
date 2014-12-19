package org.synyx.urlaubsverwaltung.core.application.domain;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Date;

import javax.persistence.*;


/**
 * This class describes the information that every step of an {@link Application}'s lifecycle contains.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Entity
public class Comment extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 8908423789423089L;

    private String reason;

    // TODO: add an enum for progress instead of string
    private String progress;

    @ManyToOne
    private Application application;

    // TODO: use person reference instead of string
    private String nameOfCommentingPerson;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateOfComment;

    // each application may only have one comment to each ApplicationStatus
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    // Do not persist this information!
    @Transient
    private boolean isMandatory;

    public Comment() {

        // TODO: Should be replaced later, only constructor with person given should be used
    }


    public Comment(Person person) {

        this.nameOfCommentingPerson = person.getNiceName();
    }

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


    public boolean isMandatory() {

        return isMandatory;
    }


    public void setMandatory(boolean isMandatory) {

        this.isMandatory = isMandatory;
    }
}
