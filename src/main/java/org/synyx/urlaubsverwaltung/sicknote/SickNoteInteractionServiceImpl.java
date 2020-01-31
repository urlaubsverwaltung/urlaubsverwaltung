package org.synyx.urlaubsverwaltung.sicknote;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSyncService;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.AbsenceType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.LocalDate;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.ZoneOffset.UTC;
import static org.slf4j.LoggerFactory.getLogger;


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
    private final SettingsService settingsService;

    @Autowired
    public SickNoteInteractionServiceImpl(SickNoteService sickNoteService, SickNoteCommentService commentService,
                                          ApplicationInteractionService applicationInteractionService, CalendarSyncService calendarSyncService,
                                          AbsenceMappingService absenceMappingService, SettingsService settingsService) {

        this.sickNoteService = sickNoteService;
        this.commentService = commentService;
        this.applicationInteractionService = applicationInteractionService;
        this.calendarSyncService = calendarSyncService;
        this.absenceMappingService = absenceMappingService;
        this.settingsService = settingsService;
    }

    @Override
    public SickNote create(SickNote sickNote, Person creator) {
        return this.create(sickNote, creator, null);
    }

    @Override
    public SickNote create(SickNote sickNote, Person creator, String comment) {

        sickNote.setStatus(SickNoteStatus.ACTIVE);
        saveSickNote(sickNote);

        commentService.create(sickNote, SickNoteAction.CREATED, creator, comment);

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

        sickNote.setStatus(SickNoteStatus.ACTIVE);
        saveSickNote(sickNote);

        commentService.create(sickNote, SickNoteAction.EDITED, editor, comment);

        LOG.info("Updated sick note: {}", sickNote);

        updateAbsence(sickNote);

        return sickNote;
    }

    @Override
    public SickNote convert(SickNote sickNote, Application application, Person converter) {

        // make sick note inactive
        sickNote.setStatus(SickNoteStatus.CONVERTED_TO_VACATION);
        saveSickNote(sickNote);

        commentService.create(sickNote, SickNoteAction.CONVERTED_TO_VACATION, converter);

        applicationInteractionService.createFromConvertedSickNote(application, converter);

        LOG.info("Converted sick note to vacation: {}", sickNote);

        Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(sickNote.getId(),
            AbsenceType.SICKNOTE);

        if (absenceMapping.isPresent()) {
            String eventId = absenceMapping.get().getEventId();
            CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();

            calendarSyncService.update(new Absence(application.getPerson(), application.getPeriod(),
                new AbsenceTimeConfiguration(calendarSettings)), eventId);
            absenceMappingService.delete(absenceMapping.get());
            absenceMappingService.create(application.getId(), AbsenceType.VACATION, eventId);
        }

        return sickNote;
    }


    @Override
    public SickNote cancel(SickNote sickNote, Person canceller) {

        sickNote.setStatus(SickNoteStatus.CANCELLED);
        saveSickNote(sickNote);

        commentService.create(sickNote, SickNoteAction.CANCELLED, canceller);

        LOG.info("Cancelled sick note: {}", sickNote);

        Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(sickNote.getId(),
            AbsenceType.SICKNOTE);

        if (absenceMapping.isPresent()) {
            calendarSyncService.deleteAbsence(absenceMapping.get().getEventId());
            absenceMappingService.delete(absenceMapping.get());
        }

        return sickNote;
    }

    private void updateAbsence(SickNote sickNote) {

        Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(sickNote.getId(),
            AbsenceType.SICKNOTE);

        if (absenceMapping.isPresent()) {
            CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
            AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);
            calendarSyncService.update(new Absence(sickNote.getPerson(), sickNote.getPeriod(),
                timeConfiguration), absenceMapping.get().getEventId());
        }
    }

    private void updateCalendar(SickNote sickNote) {

        CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);

        Optional<String> eventId = calendarSyncService.addAbsence(new Absence(sickNote.getPerson(),
            sickNote.getPeriod(), timeConfiguration));

        eventId.ifPresent(s -> absenceMappingService.create(sickNote.getId(), AbsenceType.SICKNOTE, s));
    }

    private void saveSickNote(SickNote sickNote) {

        sickNote.setLastEdited(LocalDate.now(UTC));

        sickNoteService.save(sickNote);
    }

}
