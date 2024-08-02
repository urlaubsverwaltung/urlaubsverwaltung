package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.ACCEPTED;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.SUBMITTED;

@Service
class SickNoteExtensionServiceImpl implements SickNoteExtensionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SickNoteExtensionRepository repository;
    private final SickNoteService sickNoteService;
    private final Clock clock;

    SickNoteExtensionServiceImpl(SickNoteExtensionRepository repository, SickNoteService sickNoteService, Clock clock) {
        this.repository = repository;
        this.sickNoteService = sickNoteService;
        this.clock = clock;
    }

    @Override
    public Optional<SickNoteExtensionPreview> findExtensionPreviewOfSickNote(Long sickNoteId) {

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

    @Override
    public SickNoteExtension submitSickNoteExtension(Person submitter, Long sickNoteId, LocalDate newEndDate, boolean isAub) {

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

    @Override
    public void acceptSubmittedExtension(Long sickNoteId) {
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
