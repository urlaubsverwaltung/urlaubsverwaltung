package org.synyx.urlaubsverwaltung.sicknote;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.settings.TimeSettingsEntity;
import org.synyx.urlaubsverwaltung.absence.settings.TimeSettingsService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSyncService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.absence.AbsenceMappingType.SICKNOTE;
import static org.synyx.urlaubsverwaltung.absence.AbsenceMappingType.VACATION;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteCommentAction.EDITED;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.CONVERTED_TO_VACATION;

/**
 * Implementation for {@link org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionService}.
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
    private final TimeSettingsService timeSettingsService;
    private final Clock clock;

    @Autowired
    public SickNoteInteractionServiceImpl(SickNoteService sickNoteService, SickNoteCommentService commentService,
                                          ApplicationInteractionService applicationInteractionService, CalendarSyncService calendarSyncService,
                                          AbsenceMappingService absenceMappingService, TimeSettingsService timeSettingsService, Clock clock) {

        this.sickNoteService = sickNoteService;
        this.commentService = commentService;
        this.applicationInteractionService = applicationInteractionService;
        this.calendarSyncService = calendarSyncService;
        this.absenceMappingService = absenceMappingService;
        this.timeSettingsService = timeSettingsService;
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

        updateCalendar(sickNote);

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
        updateAbsence(sickNote);

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

        final Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(sickNote.getId(), SICKNOTE);
        if (absenceMapping.isPresent()) {
            final String eventId = absenceMapping.get().getEventId();
            final TimeSettingsEntity timeSettings = getTimeSettings();

            calendarSyncService.update(new Absence(application.getPerson(), application.getPeriod(),
                new AbsenceTimeConfiguration(timeSettings)), eventId);

            absenceMappingService.delete(absenceMapping.get());
            absenceMappingService.create(application.getId(), VACATION, eventId);
        }

        return sickNote;
    }

    @Override
    public SickNote cancel(SickNote sickNote, Person canceller) {

        sickNote.setStatus(CANCELLED);
        saveSickNote(sickNote);
        LOG.info("Cancelled sick note: {}", sickNote);

        commentService.create(sickNote, SickNoteCommentAction.CANCELLED, canceller);

        final Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(sickNote.getId(), SICKNOTE);
        if (absenceMapping.isPresent()) {
            calendarSyncService.deleteAbsence(absenceMapping.get().getEventId());
            absenceMappingService.delete(absenceMapping.get());
        }

        return sickNote;
    }

    private void updateAbsence(SickNote sickNote) {

        final Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(sickNote.getId(), SICKNOTE);
        if (absenceMapping.isPresent()) {
            final AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(getTimeSettings());
            calendarSyncService.update(new Absence(sickNote.getPerson(), sickNote.getPeriod(),
                timeConfiguration), absenceMapping.get().getEventId());
        }
    }

    private void updateCalendar(SickNote sickNote) {

        final AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(getTimeSettings());
        final Absence absence = new Absence(sickNote.getPerson(), sickNote.getPeriod(), timeConfiguration);

        final Optional<String> maybeEventId = calendarSyncService.addAbsence(absence);
        maybeEventId.ifPresent(eventId -> absenceMappingService.create(sickNote.getId(), SICKNOTE, eventId));
    }

    private TimeSettingsEntity getTimeSettings() {
        return timeSettingsService.getSettings();
    }

    private void saveSickNote(SickNote sickNote) {
        sickNote.setLastEdited(LocalDate.now(clock));
        sickNoteService.save(sickNote);
    }
}
