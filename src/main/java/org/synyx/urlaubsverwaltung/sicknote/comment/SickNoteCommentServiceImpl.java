package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

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
    public SickNoteCommentEntity create(SickNote sickNote, SickNoteCommentAction action, Person author) {
        return this.create(sickNote, action, author, null);
    }

    @Override
    public SickNoteCommentEntity create(SickNote sickNote, SickNoteCommentAction action, Person author, String text) {

        final SickNoteCommentEntity comment = new SickNoteCommentEntity(clock);
        comment.setSickNote(sickNote);
        comment.setAction(action);
        comment.setPerson(author);
        comment.setText(requireNonNullElse(text, ""));

        return sickNoteCommentRepository.save(comment);
    }

    @Override
    public List<SickNoteCommentEntity> getCommentsBySickNote(SickNote sickNote) {
        return sickNoteCommentRepository.findBySickNote(sickNote);
    }
}
