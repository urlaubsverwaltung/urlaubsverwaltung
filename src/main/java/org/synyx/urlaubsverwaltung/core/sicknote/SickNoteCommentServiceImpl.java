package org.synyx.urlaubsverwaltung.core.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;
import java.util.Optional;


/**
 * Implementation for {@link SickNoteCommentService}.
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
    public SickNoteComment create(SickNote sickNote, SickNoteAction action, Optional<String> text, Person author) {

        SickNoteComment comment = new SickNoteComment();

        comment.setSickNote(sickNote);
        comment.setAction(action);
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
