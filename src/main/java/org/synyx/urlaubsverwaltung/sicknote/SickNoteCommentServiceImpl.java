package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.util.List;

import static java.util.Objects.requireNonNullElse;


/**
 * Implementation for {@link SickNoteCommentService}.
 */
@Service
class SickNoteCommentServiceImpl implements SickNoteCommentService {

    private final SickNoteCommentRepository sickNoteCommentRepository;
    private final Clock clock;

    @Autowired
    public SickNoteCommentServiceImpl(SickNoteCommentRepository sickNoteCommentRepository, Clock clock) {
        this.sickNoteCommentRepository = sickNoteCommentRepository;
        this.clock = clock;
    }

    @Override
    public SickNoteComment create(SickNote sickNote, SickNoteAction action, Person author) {
        return this.create(sickNote, action, author, null);
    }

    @Override
    public SickNoteComment create(SickNote sickNote, SickNoteAction action, Person author, String text) {

        final SickNoteComment comment = new SickNoteComment(clock);

        comment.setSickNote(sickNote);
        comment.setAction(action);
        comment.setPerson(author);
        comment.setText(requireNonNullElse(text, ""));

        return sickNoteCommentRepository.save(comment);
    }


    @Override
    public List<SickNoteComment> getCommentsBySickNote(SickNote sickNote) {
        return sickNoteCommentRepository.getCommentsBySickNote(sickNote);
    }
}
