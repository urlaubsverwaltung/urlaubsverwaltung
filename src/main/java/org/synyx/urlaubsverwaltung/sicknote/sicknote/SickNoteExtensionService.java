package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteExtensionStatus.SUBMITTED;

@Service
class SickNoteExtensionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SickNoteExtensionRepository repository;
    private final SickNoteService sickNoteService;
    private final SickNoteInteractionService sickNoteInteractionService;
    private final Clock clock;

    SickNoteExtensionService(SickNoteExtensionRepository repository,
                             SickNoteService sickNoteService,
                             SickNoteInteractionService sickNoteInteractionService,
                             Clock clock) {
        this.repository = repository;
        this.sickNoteService = sickNoteService;
        this.sickNoteInteractionService = sickNoteInteractionService;
        this.clock = clock;
    }

    /**
     * Submits a {@linkplain SickNoteExtension} that have to be accepted by a privileged person.
     *
     * <p>
     * This sick note extension has to be {@linkplain SickNoteExtensionService#acceptSickNoteExtension(Long, Person) accepted}
     * by a privileged person afterward.
     *
     * @param submitter {@linkplain Person} who submits the sick note extension
     * @param sickNoteId id of a {@linkplain SickNote} that should be extended
     * @param newEndDate new end date of the {@linkplain SickNote}
     * @param isAub whether AUB exists or not
     * @return the created {@linkplain SickNoteExtension} with status {@linkplain SickNoteExtensionStatus#SUBMITTED SUBMITTED}.
     * @throws AccessDeniedException when submitter is not allowed to extend the sick note
     * @throws IllegalStateException when sick note does not exist
     */
    SickNoteExtension submitSickNoteExtension(Person submitter, Long sickNoteId, LocalDate newEndDate, boolean isAub) {

        final SickNote sickNote = sickNoteService.getById(sickNoteId)
            .orElseThrow(() -> new IllegalStateException("could not find sickNote id=" + sickNoteId));

        if (!sickNote.getPerson().equals(submitter)) {
            final String msg = "person id=%s is not allowed to submit sickNote extension for sickNote id=%s".formatted(submitter.getId(), sickNote.getId());
            throw new AccessDeniedException(msg);
        }

        final SickNoteExtensionEntity extensionEntity = new SickNoteExtensionEntity();
        extensionEntity.setSickNoteId(sickNoteId);
        extensionEntity.setNewEndDate(newEndDate);
        extensionEntity.setAub(isAub);
        extensionEntity.setCreatedAt(Instant.now(clock));
        extensionEntity.setStatus(SUBMITTED);

        LOG.debug("submit extension of sickNote id={}", sickNoteId);

        final SickNoteExtensionEntity saved = repository.save(extensionEntity);
        final SickNoteExtension submittedExtension = toSickNoteExtension(saved);

        LOG.info("successfully submitted sickNoteExtension id={} of sickNote id={}", submittedExtension.id(), submittedExtension.sickNoteId());

        return submittedExtension;
    }

    private SickNoteExtension toSickNoteExtension(SickNoteExtensionEntity entity) {
        return new SickNoteExtension(entity.getId(), entity.getSickNoteId(), entity.getNewEndDate(), entity.isAub(), entity.getStatus());
    }
}
