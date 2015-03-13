package org.synyx.urlaubsverwaltung.core.sicknote.comment;

import com.google.common.base.Optional;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.List;


/**
 * Implementation for {@link org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteCommentService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class SickNoteCommentServiceImpl implements SickNoteCommentService {

    private final SickNoteCommentDAO commentDAO;

    @Autowired
    public SickNoteCommentServiceImpl(SickNoteCommentDAO commentDAO) {

        this.commentDAO = commentDAO;
    }

    @Override
    public SickNoteComment create(SickNoteStatus status, Optional<String> text, Person author) {

        SickNoteComment comment = new SickNoteComment();

        comment.setDate(DateMidnight.now());
        comment.setStatus(status);
        comment.setPerson(author);

        if (text.isPresent()) {
            comment.setText(text.get());
        }

        commentDAO.save(comment);

        return comment;
    }


    @Override
    public List<SickNoteComment> getCommentsBySickNote(SickNote sickNote) {

        return commentDAO.getCommentsBySickNote(sickNote);
    }
}
