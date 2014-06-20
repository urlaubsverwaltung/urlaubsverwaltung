/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.application.dao.CommentDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;


/**
 * Implementation of interface {@link CommentService}.
 *
 * @author  Aljona Murygina
 */
@Service
@Transactional
class CommentServiceImpl implements CommentService {

    private static final String APPLIED = "progress.applied";
    private static final String ALLOWED = "progress.allowed";
    private static final String REJECTED = "progress.rejected";
    private static final String CANCELLED = "progress.cancelled";
    private static final String FORCED = "progress.forced";

    private CommentDAO commentDAO;

    @Autowired
    public CommentServiceImpl(CommentDAO commentDAO) {

        this.commentDAO = commentDAO;
    }

    /**
     * @see  CommentService#saveComment(org.synyx.urlaubsverwaltung.core.application.domain.Comment, org.synyx.urlaubsverwaltung.core.person.Person,
     *       org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public void saveComment(Comment comment, Person person, Application application) {

        ApplicationStatus status = application.getStatus();
        setProgressOfComment(comment, status);
        comment.setStatus(status);

        comment.setNameOfCommentingPerson(person.getNiceName());
        comment.setApplication(application);
        comment.setDateOfComment(DateMidnight.now());
        commentDAO.save(comment);
    }


    /**
     * dependent on {@link ApplicationStatus} the {@link Comment} gets its progress describing String.
     *
     * @param  comment {@link Comment}
     * @param  status {@link ApplicationStatus}
     */
    private void setProgressOfComment(Comment comment, ApplicationStatus status) {

        if (status == ApplicationStatus.WAITING) {
            comment.setProgress(APPLIED);
        }

        if (status == ApplicationStatus.ALLOWED) {
            comment.setProgress(ALLOWED);
        }

        if (status == ApplicationStatus.REJECTED) {
            comment.setProgress(REJECTED);
        }

        if (status == ApplicationStatus.CANCELLED) {
            comment.setProgress(CANCELLED);
        }
    }


    /**
     * @see  CommentService#getCommentByApplicationAndStatus(org.synyx.urlaubsverwaltung.core.application.domain.Application,
     *       org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus)
     */
    @Override
    public Comment getCommentByApplicationAndStatus(Application a, ApplicationStatus status) {

        return commentDAO.getCommentByApplicationAndStatus(a, status);
    }


    /**
     * @see  CommentService#getCommentsByApplication(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public List<Comment> getCommentsByApplication(Application a) {

        return commentDAO.getCommentsByApplication(a);
    }
}
