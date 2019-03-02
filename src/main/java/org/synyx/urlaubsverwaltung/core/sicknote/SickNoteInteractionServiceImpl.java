package org.synyx.urlaubsverwaltung.core.sicknote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.CalendarSyncService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceType;
import org.synyx.urlaubsverwaltung.core.sync.absence.EventType;

import java.util.Optional;


/**
 * Implementation for {@link org.synyx.urlaubsverwaltung.core.sicknote.SickNoteInteractionService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Transactional
public class SickNoteInteractionServiceImpl implements SickNoteInteractionService {

    private static final Logger LOG = LoggerFactory.getLogger(SickNoteInteractionServiceImpl.class);

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

        sickNote.setStatus(SickNoteStatus.ACTIVE);
        sickNote.setLastEdited(DateMidnight.now());

        sickNoteService.save(sickNote);
        commentService.create(sickNote, SickNoteAction.CREATED, Optional.<String>empty(), creator);

        LOG.info("Created sick note: " + sickNote.toString());

        CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);

        Optional<String> eventId = calendarSyncService.addAbsence(new Absence(sickNote.getPerson(),
                    sickNote.getPeriod(), EventType.SICKNOTE, timeConfiguration));

        if (eventId.isPresent()) {
            absenceMappingService.create(sickNote.getId(), AbsenceType.SICKNOTE, eventId.get());
        }

        return sickNote;
    }


    @Override
    public SickNote update(SickNote sickNote, Person editor) {

        sickNote.setStatus(SickNoteStatus.ACTIVE);
        sickNote.setLastEdited(DateMidnight.now());

        sickNoteService.save(sickNote);
        commentService.create(sickNote, SickNoteAction.EDITED, Optional.<String>empty(), editor);

        LOG.info("Updated sick note: " + sickNote.toString());

        Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(sickNote.getId(),
                AbsenceType.SICKNOTE);

        if (absenceMapping.isPresent()) {
            CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
            AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);
            calendarSyncService.update(new Absence(sickNote.getPerson(), sickNote.getPeriod(), EventType.SICKNOTE,
                    timeConfiguration), absenceMapping.get().getEventId());
        }

        return sickNote;
    }


    @Override
    public SickNote convert(SickNote sickNote, Application application, Person converter) {

        // make sick note inactive
        sickNote.setStatus(SickNoteStatus.CONVERTED_TO_VACATION);
        sickNote.setLastEdited(DateMidnight.now());

        sickNoteService.save(sickNote);
        commentService.create(sickNote, SickNoteAction.CONVERTED_TO_VACATION, Optional.<String>empty(), converter);

        applicationInteractionService.createFromConvertedSickNote(application, converter);

        LOG.info("Converted sick note to vacation: " + sickNote.toString());

        Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(sickNote.getId(),
                AbsenceType.SICKNOTE);

        if (absenceMapping.isPresent()) {
            String eventId = absenceMapping.get().getEventId();
            CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();

            calendarSyncService.update(new Absence(application.getPerson(), application.getPeriod(),
                    EventType.ALLOWED_APPLICATION, new AbsenceTimeConfiguration(calendarSettings)), eventId);
            absenceMappingService.delete(absenceMapping.get());
            absenceMappingService.create(application.getId(), AbsenceType.VACATION, eventId);
        }

        return sickNote;
    }


    @Override
    public SickNote cancel(SickNote sickNote, Person canceller) {

        sickNote.setStatus(SickNoteStatus.CANCELLED);
        sickNote.setLastEdited(DateMidnight.now());

        sickNoteService.save(sickNote);
        commentService.create(sickNote, SickNoteAction.CANCELLED, Optional.<String>empty(), canceller);

        LOG.info("Cancelled sick note: " + sickNote.toString());

        Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(sickNote.getId(),
                AbsenceType.SICKNOTE);

        if (absenceMapping.isPresent()) {
            calendarSyncService.deleteAbsence(absenceMapping.get().getEventId());
            absenceMappingService.delete(absenceMapping.get());
        }

        return sickNote;
    }
}
