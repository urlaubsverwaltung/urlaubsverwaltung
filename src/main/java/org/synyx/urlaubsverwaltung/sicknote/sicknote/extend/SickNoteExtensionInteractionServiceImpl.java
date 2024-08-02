package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteUpdatedEvent;

import java.time.LocalDate;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_EDIT;
import static org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction.EXTENSION_ACCEPTED;

@Service
class SickNoteExtensionInteractionServiceImpl implements SickNoteExtensionInteractionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SickNoteExtensionService sickNoteExtensionService;
    private final SickNoteService sickNoteService;
    private final SickNoteCommentService commentService;
    private final ApplicationEventPublisher eventPublisher;

    SickNoteExtensionInteractionServiceImpl(SickNoteExtensionService sickNoteExtensionService,
                                            SickNoteService sickNoteService,
                                            SickNoteCommentService commentService,
                                            ApplicationEventPublisher eventPublisher) {
        this.sickNoteExtensionService = sickNoteExtensionService;
        this.sickNoteService = sickNoteService;
        this.commentService = commentService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public SickNoteExtension submitSickNoteExtension(Person submitter, Long sickNoteId, LocalDate newEndDate, boolean isAub) {

        final SickNote sickNote = getSickNote(sickNoteId);
        if (!sickNote.getPerson().equals(submitter)) {
            final String msg = "person id=%s is not allowed to submit sickNote extension for sickNote id=%s".formatted(submitter.getId(), sickNote.getId());
            throw new AccessDeniedException(msg);
        }

        // TODO do we have to do other stuff like sending an email?

        return sickNoteExtensionService.createSickNoteExtension(sickNoteId, newEndDate, isAub);
    }

    @Override
    public SickNote acceptSubmittedExtension(Person maintainer, Long sickNoteId) {

        if (!maintainer.isActive() || !maintainer.hasAnyRole(OFFICE, SICK_NOTE_EDIT)) {
            // isActive=false should not happen as this user cannot interact with the application
            // however, we are defensive since there is no context here, this is just a random person passed into this method.
            throw new AccessDeniedException("person id=%s is not authorized to accept submitted sickNoteExtension".formatted(maintainer.getId()));
        }

        final SickNote updatedSickNote = sickNoteExtensionService.acceptSubmittedExtension(sickNoteId);

        LOG.info("add extension accepted comment to sick note history.");
        commentService.create(updatedSickNote, EXTENSION_ACCEPTED, maintainer);

        // TODO think about publishing dedicated SickNoteExtensionAcceptedEvent
        LOG.info("publish sickNoteUpdatedEvent for accepted sick note extension.");
        eventPublisher.publishEvent(SickNoteUpdatedEvent.of(updatedSickNote));

        return updatedSickNote;
    }

    private SickNote getSickNote(Long id) {
        return sickNoteService.getById(id)
            .orElseThrow(() -> new IllegalStateException("could not find sickNote with id=" + id));
    }
}