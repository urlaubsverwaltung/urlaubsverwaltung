package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteExtensionStatus.ACCEPTED;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteExtensionStatus.SUBMITTED;

@Service
class SickNoteExtensionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SickNoteExtensionRepository repository;
    private final SickNoteService sickNoteService;
    private final Clock clock;

    SickNoteExtensionService(SickNoteExtensionRepository repository,
                             SickNoteService sickNoteService,
                             Clock clock) {
        this.repository = repository;
        this.sickNoteService = sickNoteService;
        this.clock = clock;
    }

    /**
     * Find the newest {@linkplain SickNoteExtensionPreview} of the given {@linkplain SickNote} id.
     *
     * @param sickNoteId {@linkplain SickNote} id
     * @return Optional resolving to the newest {@linkplain SickNoteExtensionPreview}
     */
    Optional<SickNoteExtensionPreview> findExtensionPreviewOfSickNote(Long sickNoteId) {

        final SickNote sickNote = getSickNote(sickNoteId);
        final List<SickNoteExtensionEntity> extensions = repository.findAllBySickNoteIdOrderByCreatedAtDesc(sickNoteId);

        return extensions.stream()
            .findFirst()
            .filter(extension -> SUBMITTED.equals(extension.getStatus()))
            .map(extensionEntity -> new SickNoteExtensionPreview(
                extensionEntity.getId(),
                sickNote.getStartDate(),
                extensionEntity.getNewEndDate(),
                extensionEntity.isAub(),
                // TODO working days
                BigDecimal.valueOf(42L)
            ));
    }

    /**
     * Submits a {@linkplain SickNoteExtension} that have to be accepted by a privileged person.
     *
     * <p>
     * This sick note extension has to be {@linkplain SickNoteInteractionService#acceptSubmittedExtension(Long, Person) accepted}
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

        final SickNote sickNote = getSickNote(sickNoteId);
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

    /**
     * Updates the status of the last known submitted {@linkplain SickNoteExtension} to {@linkplain SickNoteExtensionStatus#ACCEPTED accepted}.
     *
     * <p>
     * Please note that you have to handle authorization of this action yourself.
     * Authorization is not considered here.
     *
     * @param sickNoteId if of the {@linkplain SickNote} to handle
     */
    void acceptSubmittedExtension(Long sickNoteId) {
        final List<SickNoteExtensionEntity> extensions = repository.findAllBySickNoteIdOrderByCreatedAtDesc(sickNoteId);
        extensions.stream()
            .findFirst()
            .filter(extension -> SUBMITTED.equals(extension.getStatus()))
            .ifPresentOrElse(
                extension -> {
                    extension.setStatus(ACCEPTED);
                    repository.save(extension);
                    LOG.info("updated status to 'ACCEPTED' of sickNoteExtension id={}", extension.getId());
                },
                () -> LOG.warn("not updating status of sickNoteExtensions for sickNote id={} since extension could not be found.", sickNoteId)
            );

    }

    private SickNote getSickNote(Long id) {
        return sickNoteService.getById(id)
            .orElseThrow(() -> new IllegalStateException("could not find referenced sickNote with id=" + id));
    }

    private SickNoteExtension toSickNoteExtension(SickNoteExtensionEntity entity) {
        return new SickNoteExtension(entity.getId(), entity.getSickNoteId(), entity.getNewEndDate(), entity.isAub(), entity.getStatus());
    }
}
