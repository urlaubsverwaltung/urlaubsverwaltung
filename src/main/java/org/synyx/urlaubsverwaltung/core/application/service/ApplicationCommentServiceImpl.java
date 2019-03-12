/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationCommentDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationAction;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;
import java.util.Optional;


/**
 * Implementation of interface {@link ApplicationCommentService}.
 *
 * @author  Aljona Murygina
 */
@Service
@Transactional
class ApplicationCommentServiceImpl implements ApplicationCommentService {

    private final ApplicationCommentDAO commentDAO;

    @Autowired
    ApplicationCommentServiceImpl(ApplicationCommentDAO commentDAO) {

        this.commentDAO = commentDAO;
    }

    @Override
    public ApplicationComment create(Application application, ApplicationAction action, Optional<String> text,
        Person author) {

        ApplicationComment comment = new ApplicationComment(author);

        comment.setAction(action);
        comment.setApplication(application);

        text.ifPresent(comment::setText);

        commentDAO.save(comment);

        return comment;
    }


    @Override
    public List<ApplicationComment> getCommentsByApplication(Application application) {

        return commentDAO.getCommentsByApplication(application);
    }
}
