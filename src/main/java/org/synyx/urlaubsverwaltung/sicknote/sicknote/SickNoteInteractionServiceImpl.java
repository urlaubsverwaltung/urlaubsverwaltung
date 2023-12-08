package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;

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
    private final SickNoteMailService sickNoteMailService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    SickNoteInteractionServiceImpl(SickNoteService sickNoteService,
                                   SickNoteCommentService commentService,
                                   ApplicationInteractionService applicationInteractionService,
                                   SickNoteMailService sickNoteMailService,
                                   ApplicationEventPublisher applicationEventPublisher
    ) {

        this.sickNoteService = sickNoteService;
        this.commentService = commentService;
        this.applicationInteractionService = applicationInteractionService;
        this.sickNoteMailService = sickNoteMailService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public SickNote create(SickNote sickNote, Person creator) {
        return this.create(sickNote, creator, null);
    }

    @Override
    public SickNote create(SickNote sickNote, Person applier, String comment) {

        final SickNote createdSickNote = sickNoteService.save(SickNote.builder(sickNote).status(ACTIVE).build());
        LOG.info("Created sick note: {}", createdSickNote);

        commentService.create(createdSickNote, SickNoteCommentAction.CREATED, applier, comment);

        sickNoteMailService.sendCreatedSickPerson(createdSickNote);
        sickNoteMailService.sendCreatedToColleagues(createdSickNote);

        applicationEventPublisher.publishEvent(SickNoteCreatedEvent.of(createdSickNote));

        return createdSickNote;
    }

    @Override
    public SickNote update(SickNote sickNote, Person editor, String comment) {

        final SickNote updatedSickNote = sickNoteService.save(SickNote.builder(sickNote).status(ACTIVE).build());
        LOG.info("Updated sick note: {}", updatedSickNote);

        commentService.create(updatedSickNote, EDITED, editor, comment);

        sickNoteMailService.sendEditedToSickPerson(updatedSickNote);

        applicationEventPublisher.publishEvent(SickNoteUpdatedEvent.of(updatedSickNote));

        return updatedSickNote;
    }

    @Override
    public SickNote convert(SickNote sickNote, Application application, Person converter) {

        // make sick note inactive
        final SickNote convertedSickNote = sickNoteService.save(SickNote.builder(sickNote).status(CONVERTED_TO_VACATION).build());

        commentService.create(convertedSickNote, SickNoteCommentAction.CONVERTED_TO_VACATION, converter);
        applicationInteractionService.createFromConvertedSickNote(application, converter);
        LOG.info("Converted sick note to vacation: {}", convertedSickNote);

        applicationEventPublisher.publishEvent(SickNoteToApplicationConvertedEvent.of(convertedSickNote, application));

        return convertedSickNote;
    }

    @Override
    public SickNote cancel(SickNote sickNote, Person canceller) {

        final SickNote cancelledSickNote = sickNoteService.save(SickNote.builder(sickNote).status(CANCELLED).build());
        LOG.info("Cancelled sick note: {}", cancelledSickNote);

        commentService.create(cancelledSickNote, SickNoteCommentAction.CANCELLED, canceller);

        sickNoteMailService.sendCancelledToSickPerson(cancelledSickNote);
        sickNoteMailService.sendCancelToColleagues(cancelledSickNote);

        applicationEventPublisher.publishEvent(SickNoteCancelledEvent.of(cancelledSickNote));
        return cancelledSickNote;
    }

    /**
     * Deletes all {@link SickNote} and {@link org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentEntity}
     * in the database of person.
     *
     * @param event the person which is deleted and whose sicknotes should be deleted
     */
    @EventListener
    void deleteAll(PersonDeletedEvent event) {
        final Person personToBeDeleted = event.person();
        commentService.deleteAllBySickNotePerson(personToBeDeleted);
        commentService.deleteCommentAuthor(personToBeDeleted);

        sickNoteService.deleteAllByPerson(personToBeDeleted)
            .forEach(sickNote -> applicationEventPublisher.publishEvent(SickNoteDeletedEvent.of(sickNote)));

        sickNoteService.deleteSickNoteApplier(personToBeDeleted);
    }
}
