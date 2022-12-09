package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMapping;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSyncService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.SICKNOTE;
import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.VACATION;
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
    private final CalendarSyncService calendarSyncService;
    private final AbsenceMappingService absenceMappingService;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    SickNoteInteractionServiceImpl(SickNoteService sickNoteService, SickNoteCommentService commentService,
                                   ApplicationInteractionService applicationInteractionService, CalendarSyncService calendarSyncService,
                                   AbsenceMappingService absenceMappingService, SettingsService settingsService, Clock clock) {

        this.sickNoteService = sickNoteService;
        this.commentService = commentService;
        this.applicationInteractionService = applicationInteractionService;
        this.calendarSyncService = calendarSyncService;
        this.absenceMappingService = absenceMappingService;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @Override
    public SickNote create(SickNote sickNote, Person creator) {
        return this.create(sickNote, creator, null);
    }

    @Override
    public SickNote create(SickNote sickNote, Person applier, String comment) {

        final SickNote updatedSickNote = sickNoteService.save(SickNote.builder(sickNote).status(ACTIVE).build());

        commentService.create(updatedSickNote, SickNoteCommentAction.CREATED, applier, comment);
        LOG.info("Created sick note: {}", updatedSickNote);

        updateCalendar(updatedSickNote);

        return updatedSickNote;
    }

    @Override
    public SickNote update(SickNote sickNote, Person editor, String comment) {

        final SickNote updatedSickNote = sickNoteService.save(SickNote.builder(sickNote).status(ACTIVE).build());
        LOG.info("Updated sick note: {}", updatedSickNote);

        commentService.create(updatedSickNote, EDITED, editor, comment);
        updateAbsence(updatedSickNote);

        return updatedSickNote;
    }

    @Override
    public SickNote convert(SickNote sickNote, Application application, Person converter) {

        // make sick note inactive
        final SickNote updatedSickNote = sickNoteService.save(SickNote.builder(sickNote).status(CONVERTED_TO_VACATION).build());

        commentService.create(sickNote, SickNoteCommentAction.CONVERTED_TO_VACATION, converter);
        applicationInteractionService.createFromConvertedSickNote(application, converter);
        LOG.info("Converted sick note to vacation: {}", updatedSickNote);

        if (calendarSyncService.isRealProviderConfigured()) {
            final Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(updatedSickNote.getId(), SICKNOTE);
            if (absenceMapping.isPresent()) {
                final String eventId = absenceMapping.get().getEventId();
                final TimeSettings timeSettings = getTimeSettings();

                calendarSyncService.update(new Absence(application.getPerson(), application.getPeriod(),
                    new AbsenceTimeConfiguration(timeSettings)), eventId);

                absenceMappingService.delete(absenceMapping.get());
                absenceMappingService.create(application.getId(), VACATION, eventId);
            }
        }

        return updatedSickNote;
    }

    @Override
    public SickNote cancel(SickNote sickNote, Person canceller) {

        final SickNote savedSickNote = sickNoteService.save(SickNote.builder(sickNote).status(CANCELLED).build());
        LOG.info("Cancelled sick note: {}", savedSickNote);

        commentService.create(savedSickNote, SickNoteCommentAction.CANCELLED, canceller);

        final Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(savedSickNote.getId(), SICKNOTE);
        if (absenceMapping.isPresent()) {
            calendarSyncService.deleteAbsence(absenceMapping.get().getEventId());
            absenceMappingService.delete(absenceMapping.get());
        }

        return savedSickNote;
    }

    /**
     * Deletes all {@link SickNote} and {@link org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentEntity}
     * in the database of person.
     *
     * @param event the person which is deleted and whose sicknotes should be deleted
     */
    @EventListener
    void deleteAll(PersonDeletedEvent event) {
        final Person personToBeDeleted = event.getPerson();
        commentService.deleteAllBySickNotePerson(personToBeDeleted);
        commentService.deleteCommentAuthor(personToBeDeleted);
        final List<SickNote> deletedSickNotes = sickNoteService.deleteAllByPerson(personToBeDeleted);
        deletedSickNotes.forEach(sickNote -> absenceMappingService.getAbsenceByIdAndType(sickNote.getId(), SICKNOTE)
            .ifPresent(absenceMapping -> {
                calendarSyncService.deleteAbsence(absenceMapping.getEventId());
                absenceMappingService.delete(absenceMapping);
            })
        );
        sickNoteService.deleteSickNoteApplier(personToBeDeleted);
    }

    private void updateAbsence(SickNote sickNote) {

        if (calendarSyncService.isRealProviderConfigured()) {
            final Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(sickNote.getId(), SICKNOTE);
            if (absenceMapping.isPresent()) {
                final AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(getTimeSettings());
                calendarSyncService.update(new Absence(sickNote.getPerson(), sickNote.getPeriod(),
                    timeConfiguration), absenceMapping.get().getEventId());
            }
        }
    }

    private void updateCalendar(SickNote sickNote) {

        if (calendarSyncService.isRealProviderConfigured()) {
            final AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(getTimeSettings());
            final Absence absence = new Absence(sickNote.getPerson(), sickNote.getPeriod(), timeConfiguration);
            calendarSyncService.addAbsence(absence)
                .ifPresent(eventId -> absenceMappingService.create(sickNote.getId(), SICKNOTE, eventId));
        }
    }

    private TimeSettings getTimeSettings() {
        return settingsService.getSettings().getTimeSettings();
    }
}
