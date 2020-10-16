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

    private final SickNoteCommentRepository sickNoteCommentRepository;

    @Autowired
    public SickNoteCommentServiceImpl(SickNoteCommentRepository sickNoteCommentRepository) {
        this.sickNoteCommentRepository = sickNoteCommentRepository;
    }

    @Override
    public SickNoteComment create(SickNote sickNote, SickNoteAction action, Person author) {
        return this.create(sickNote, action, author, null);
    }

    @Override
    public SickNoteComment create(SickNote sickNote, SickNoteAction action, Person author, String text) {

        final SickNoteComment comment = new SickNoteComment();

        comment.setSickNote(sickNote);
        comment.setAction(action);
        comment.setPerson(author);

        if (text == null) {
            comment.setText("");
        } else {
            comment.setText(text);
        }

        return sickNoteCommentRepository.save(comment);
    }


    @Override
    public List<SickNoteComment> getCommentsBySickNote(SickNote sickNote) {
        return sickNoteCommentRepository.getCommentsBySickNote(sickNote);
    }
}
