/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.application.service;

import java.util.List;
import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.application.dao.CommentDAO;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.Comment;
import org.synyx.urlaubsverwaltung.person.Person;


/**
 * @author  Aljona Murygina
 */
@Transactional
class CommentServiceImpl implements CommentService {

    private CommentDAO commentDAO;
    
    private static final String APPLIED = "progress.applied";
    private static final String ALLOWED = "progress.allowed";
    private static final String REJECTED = "progress.rejected";
    private static final String CANCELLED = "progress.cancelled";
    
    @Autowired
    public CommentServiceImpl(CommentDAO commentDAO) {

        this.commentDAO = commentDAO;
    }

    @Override
    public void saveComment(Comment comment, Person person, Application application) {

        String nameOfCommentingPerson = person.getFirstName() + " " + person.getLastName();

        ApplicationStatus status = application.getStatus();
        setProgressOfComment(comment, status);
        comment.setStatus(status);
        
        comment.setNameOfCommentingPerson(nameOfCommentingPerson);
        comment.setApplication(application);
        comment.setDateOfComment(DateMidnight.now());
        commentDAO.save(comment);
    }
    
    
    /**
     * dependent on ApplicationStatus the Comment gets its progress-String
     * @param comment
     * @param status 
     */
    private void setProgressOfComment(Comment comment, ApplicationStatus status) {
        
        if(status == ApplicationStatus.WAITING) {
            comment.setProgress(APPLIED);
        }
        
        if(status == ApplicationStatus.ALLOWED) {
            comment.setProgress(ALLOWED);
        }
        
        if(status == ApplicationStatus.REJECTED) {
            comment.setProgress(REJECTED);
        }
        
        if(status == ApplicationStatus.CANCELLED) {
            comment.setProgress(CANCELLED);
        }
    }

    @Override
    public Comment getCommentByApplicationAndStatus(Application a, ApplicationStatus status) {
        return commentDAO.getCommentByApplicationAndStatus(a, status);
    }

    @Override
    public List<Comment> getCommentsByApplication(Application a) {
        
        return commentDAO.getCommentsByApplication(a);
    }
}
