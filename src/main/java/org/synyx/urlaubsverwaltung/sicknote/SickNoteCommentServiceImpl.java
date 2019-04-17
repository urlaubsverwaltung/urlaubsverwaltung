package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;


/**
 * Implementation for {@link SickNoteCommentService}.
 */
@Service
class SickNoteCommentServiceImpl implements SickNoteCommentService {

    private final SickNoteCommentDAO commentDAO;

    @Autowired
    public SickNoteCommentServiceImpl(SickNoteCommentDAO commentDAO) {

        this.commentDAO = commentDAO;
    }

    @Override
    public SickNoteComment create(SickNote sickNote, SickNoteAction action, Person author) {

        SickNoteComment comment = createSickNoteCommentBase(sickNote, action, author);

        commentDAO.save(comment);

        return comment;
    }

    @Override
    public SickNoteComment create(SickNote sickNote, SickNoteAction action, Person author, String text) {

        SickNoteComment comment = createSickNoteCommentBase(sickNote, action, author);

        comment.setText(text);

        commentDAO.save(comment);

        return comment;
    }


    @Override
    public List<SickNoteComment> getCommentsBySickNote(SickNote sickNote) {

        return commentDAO.getCommentsBySickNote(sickNote);
    }

    private SickNoteComment createSickNoteCommentBase(SickNote sickNote, SickNoteAction action, Person author) {
        SickNoteComment comment = new SickNoteComment();

        comment.setSickNote(sickNote);
        comment.setAction(action);
        comment.setPerson(author);
        return comment;
    }
}
