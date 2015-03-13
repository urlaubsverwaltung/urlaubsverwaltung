/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.application.service;

import com.google.common.base.Optional;

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

    @Override
    public Comment create(Application application, ApplicationStatus status, Optional<String> text, Person author) {

        Comment comment = new Comment(author);

        comment.setStatus(status);
        comment.setApplication(application);
        comment.setDateOfComment(DateMidnight.now());

        if (text.isPresent()) {
            comment.setReason(text.get());
        }

        commentDAO.save(comment);

        return comment;
    }


    @Override
    public List<Comment> getCommentsByApplication(Application application) {

        return commentDAO.getCommentsByApplication(application);
    }
}
