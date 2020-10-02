package org.synyx.urlaubsverwaltung.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.application.dao.ApplicationCommentRepository;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationAction;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.util.List;
import java.util.Optional;


/**
 * Implementation of interface {@link ApplicationCommentService}.
 */
@Service
@Transactional
class ApplicationCommentServiceImpl implements ApplicationCommentService {

    private final ApplicationCommentRepository commentRepository;
    private final Clock clock;

    @Autowired
    ApplicationCommentServiceImpl(ApplicationCommentRepository commentRepository, Clock clock) {

        this.commentRepository = commentRepository;
        this.clock = clock;
    }

    @Override
    public ApplicationComment create(Application application, ApplicationAction action, Optional<String> text,
                                     Person author) {

        final ApplicationComment comment = new ApplicationComment(author, clock);
        comment.setAction(action);
        comment.setApplication(application);
        text.ifPresent(comment::setText);

        return commentRepository.save(comment);
    }


    @Override
    public List<ApplicationComment> getCommentsByApplication(Application application) {

        return commentRepository.getCommentsByApplication(application);
    }
}
