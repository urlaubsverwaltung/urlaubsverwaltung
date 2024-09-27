package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteInteractionService;
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
    private final SickNoteInteractionService sickNoteInteractionService;
    private final SickNoteCommentService commentService;
    private final ApplicationEventPublisher eventPublisher;

    SickNoteExtensionInteractionServiceImpl(SickNoteExtensionService sickNoteExtensionService,
                                            SickNoteService sickNoteService,
                                            SickNoteInteractionService sickNoteInteractionService,
                                            SickNoteCommentService commentService,
                                            ApplicationEventPublisher eventPublisher) {
        this.sickNoteExtensionService = sickNoteExtensionService;
        this.sickNoteService = sickNoteService;
        this.sickNoteInteractionService = sickNoteInteractionService;
        this.commentService = commentService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void submitSickNoteExtension(Person submitter, Long sickNoteId, LocalDate newEndDate) {

        final SickNote sickNote = getSickNote(sickNoteId);
        if (!sickNote.getPerson().equals(submitter)) {
            final String msg = "person id=%s is not allowed to submit sickNote extension for sickNote id=%s".formatted(submitter.getId(), sickNote.getId());
            throw new AccessDeniedException(msg);
        }

        if (sickNote.isSubmitted()) {
            // a not yet accepted or cancelled sickNote can be edited right now
            final SickNote extendedSickNote = SickNote.builder(sickNote).endDate(newEndDate).build();
            // TODO shall we add a comment? which language?
            sickNoteInteractionService.update(extendedSickNote, submitter, "");
        } else if (sickNote.isActive()) {
            // while an active sickNote has to be extended with a request
            sickNoteExtensionService.createSickNoteExtension(sickNote, newEndDate);
        } else {
            throw new IllegalStateException("Cannot submit sickNoteExtension for sickNote id=%s with status=%s".formatted(sickNoteId, sickNote.getStatus()));
        }
    }

    @Override
    public SickNote acceptSubmittedExtension(Person maintainer, Long sickNoteId, String comment) {

        if (!maintainer.hasAnyRole(OFFICE, SICK_NOTE_EDIT)) {
            throw new AccessDeniedException("person id=%s is not authorized to accept submitted sickNoteExtension".formatted(maintainer.getId()));
        }

        final SickNote updatedSickNote = sickNoteExtensionService.acceptSubmittedExtension(sickNoteId);

        LOG.info("add extension accepted comment to sick note history.");
        commentService.create(updatedSickNote, EXTENSION_ACCEPTED, maintainer, comment);

        LOG.info("publish sickNoteUpdatedEvent for accepted sick note extension.");
        eventPublisher.publishEvent(SickNoteUpdatedEvent.of(updatedSickNote));

        return updatedSickNote;
    }

    private SickNote getSickNote(Long id) {
        return sickNoteService.getById(id)
            .orElseThrow(() -> new IllegalStateException("could not find sickNote with id=" + id));
    }
}
