package org.synyx.urlaubsverwaltung.core.overtime;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 * @since  2.11.0
 */
@Service
public class OvertimeCommentServiceImpl implements OvertimeCommentService {

    private OvertimeCommentDAO commentDAO;

    @Autowired
    public OvertimeCommentServiceImpl(OvertimeCommentDAO commentDAO) {

        this.commentDAO = commentDAO;
    }

    @Override
    public OvertimeComment create(Overtime overtime, OvertimeAction action, Optional<String> text, Person author) {

        OvertimeComment comment = new OvertimeComment(author, overtime, action);

        text.ifPresent((content) -> comment.setText(content));

        commentDAO.save(comment);

        return comment;
    }
}
