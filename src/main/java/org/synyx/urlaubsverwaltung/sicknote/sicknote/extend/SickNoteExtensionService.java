package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.ACCEPTED;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.SUBMITTED;

@Service
class SickNoteExtensionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SickNoteExtensionRepository repository;
    private final SickNoteService sickNoteService;
    private final SickNoteExtensionPreviewService previewService;
    private final Clock clock;

    SickNoteExtensionService(SickNoteExtensionRepository repository, SickNoteService sickNoteService,
                             SickNoteExtensionPreviewService previewService, Clock clock) {
        this.repository = repository;
        this.sickNoteService = sickNoteService;
        this.previewService = previewService;
        this.clock = clock;
    }

    /**
     * Creates a {@linkplain SickNoteExtension} linked to the given {@linkplain SickNote}.
     *
     * <p>
     * This method does not handle authorization. You have to check this upfront!
     * Whether the currently logged-in user is allowed to create an extension for the sickNote or not, for instance.
     *
     * @param sickNoteId id of the referenced {@linkplain SickNote}
     * @param newEndDate new endDate of the {@linkplain SickNote} when extension is accepted
     * @param isAub whether AUB exists or not
     * @return the created {@linkplain SickNoteExtension} with status {@linkplain SickNoteExtensionStatus#SUBMITTED submitted}.
     */
    SickNoteExtension createSickNoteExtension(Long sickNoteId, LocalDate newEndDate, boolean isAub) {

        final SickNoteExtensionEntity extensionEntity = new SickNoteExtensionEntity();
        extensionEntity.setSickNoteId(sickNoteId);
        extensionEntity.setNewEndDate(newEndDate);
        extensionEntity.setAub(isAub);
        extensionEntity.setCreatedAt(Instant.now(clock));
        extensionEntity.setStatus(SUBMITTED);

        final SickNoteExtensionEntity saved = repository.save(extensionEntity);
        final SickNoteExtension submittedExtension = toSickNoteExtension(saved);

        LOG.info("created sickNoteExtension id={} of sickNote id={}", submittedExtension.id(), submittedExtension.sickNoteId());

        return submittedExtension;
    }

    /**
     * Updates the referenced {@linkplain SickNote} to match the desired extension and set the status of the
     * {@linkplain SickNoteExtension} to {@linkplain SickNoteExtensionStatus#SUBMITTED submitted}.
     *
     * <p>
     * This method does not handle authorization. You have to check this upfront!
     * Whether the currently logged-in user is allowed to accept the extension for the sickNote or not, for instance.
     *
     * @param sickNoteId id of the {@linkplain SickNote} to update
     * @return the updated {@linkplain SickNote}
     */
    SickNote acceptSubmittedExtension(Long sickNoteId) {

        final SickNote sickNote = sickNoteService.getById(sickNoteId)
            .orElseThrow(() -> new IllegalStateException("could not find sickNote with id=" + sickNoteId));

        final SickNoteExtensionPreview extensionPreview = previewService.findExtensionPreviewOfSickNote(sickNoteId)
            .orElseThrow(() -> new IllegalStateException("could not find extension of sickNote id=" + sickNote));

        LOG.debug("update sickNote id={} to match accepted sickNoteExtension id={}", sickNoteId, extensionPreview.id());

        // aub is not in scope currently
        // will be handled with https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/4551
        final SickNote updatedSickNote = sickNoteService.save(
            SickNote.builder(sickNote).endDate(extensionPreview.endDate()).build()
        );

        LOG.debug("update sickNoteExtension status.");
        updateSickNoteExtensionStatusToSubmitted(sickNoteId);

        LOG.info("Accepted sick note extension: {}", updatedSickNote);

        return updatedSickNote;
    }

    private void updateSickNoteExtensionStatusToSubmitted(Long sickNoteId) {
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