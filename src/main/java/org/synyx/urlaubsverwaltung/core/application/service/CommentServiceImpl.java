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

    private final CommentDAO commentDAO;

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

        comment.setStatus(application.getStatus());
        comment.setPerson(person);
        comment.setApplication(application);
        comment.setDateOfComment(DateMidnight.now());

        commentDAO.save(comment);
    }


    /**
     * @see  CommentService#getCommentsByApplication(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public List<Comment> getCommentsByApplication(Application a) {

        return commentDAO.getCommentsByApplication(a);
    }
}
