package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;

import java.time.Clock;
import java.time.LocalDate;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction.EDITED;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.CONVERTED_TO_VACATION;

/**
 * Implementation for {@link SickNoteInteractionService}.
 */
@Service
@Transactional
class SickNoteInteractionServiceImpl implements SickNoteInteractionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SickNoteService sickNoteService;
    private final SickNoteCommentService commentService;
    private final ApplicationInteractionService applicationInteractionService;
    private final Clock clock;

    @Autowired
    SickNoteInteractionServiceImpl(SickNoteService sickNoteService, SickNoteCommentService commentService,
                                   ApplicationInteractionService applicationInteractionService, Clock clock) {

        this.sickNoteService = sickNoteService;
        this.commentService = commentService;
        this.applicationInteractionService = applicationInteractionService;
        this.clock = clock;
    }

    @Override
    public SickNote create(SickNote sickNote, Person creator) {
        return this.create(sickNote, creator, null);
    }

    @Override
    public SickNote create(SickNote sickNote, Person creator, String comment) {

        sickNote.setStatus(ACTIVE);
        saveSickNote(sickNote);

        commentService.create(sickNote, SickNoteCommentAction.CREATED, creator, comment);
        LOG.info("Created sick note: {}", sickNote);

        return sickNote;
    }

    @Override
    public SickNote update(SickNote sickNote, Person editor) {
        return this.update(sickNote, editor, null);
    }

    @Override
    public SickNote update(SickNote sickNote, Person editor, String comment) {

        sickNote.setStatus(ACTIVE);
        saveSickNote(sickNote);
        LOG.info("Updated sick note: {}", sickNote);

        commentService.create(sickNote, EDITED, editor, comment);

        return sickNote;
    }

    @Override
    public SickNote convert(SickNote sickNote, Application application, Person converter) {

        // make sick note inactive
        sickNote.setStatus(CONVERTED_TO_VACATION);
        saveSickNote(sickNote);

        commentService.create(sickNote, SickNoteCommentAction.CONVERTED_TO_VACATION, converter);
        applicationInteractionService.createFromConvertedSickNote(application, converter);
        LOG.info("Converted sick note to vacation: {}", sickNote);

        return sickNote;
    }

    @Override
    public SickNote cancel(SickNote sickNote, Person canceller) {

        sickNote.setStatus(CANCELLED);
        saveSickNote(sickNote);
        LOG.info("Cancelled sick note: {}", sickNote);

        commentService.create(sickNote, SickNoteCommentAction.CANCELLED, canceller);

        return sickNote;
    }

    private void saveSickNote(SickNote sickNote) {
        sickNote.setLastEdited(LocalDate.now(clock));
        sickNoteService.save(sickNote);
    }
}
