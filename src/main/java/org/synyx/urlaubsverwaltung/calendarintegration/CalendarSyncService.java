package org.synyx.urlaubsverwaltung.calendarintegration;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAllowedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAllowedTemporarilyEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAppliedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationCancelledEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationDeletedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationRejectedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationRevokedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationUpdatedEvent;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCancelledEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCreatedEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteDeletedEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteToApplicationConvertedEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteUpdatedEvent;

import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.SICKNOTE;
import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.VACATION;

@Service
class CalendarSyncService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SettingsService settingsService;
    private final CalendarSettingsService calendarSettingsService;
    private final CalendarProviderService calendarService;
    private final AbsenceMappingRepository absenceMappingRepository;

    @Autowired
    CalendarSyncService(
        SettingsService settingsService,
        CalendarSettingsService calendarSettingsService,
        CalendarProviderService calendarService,
        AbsenceMappingRepository absenceMappingRepository
    ) {
        this.settingsService = settingsService;
        this.calendarSettingsService = calendarSettingsService;
        this.calendarService = calendarService;
        this.absenceMappingRepository = absenceMappingRepository;
        LOG.debug("The following calendar provider is configured: {}", calendarService.getCalendarProvider().getClass());
    }

    @Async
    @EventListener
    public void consumeApplicationAppliedEvent(ApplicationAppliedEvent event) {
        addCalendarEntry(event.getApplication());
    }

    @Async
    @EventListener
    public void consumeApplicationUpdatedEvent(ApplicationAllowedTemporarilyEvent event) {
        update(event.getApplication());
    }

    @Async
    @EventListener
    public void consumeApplicationAllowedEvent(ApplicationAllowedEvent event) {
        update(event.getApplication());
    }

    @Async
    @EventListener
    public void consumeApplicationUpdatedEvent(ApplicationUpdatedEvent event) {
        update(event.getApplication());
    }

    @Async
    @EventListener
    public void consumeApplicationRejectedEvent(ApplicationRejectedEvent event) {
        deleteCalendarEntry(event.getApplication());
    }

    @Async
    @EventListener
    public void consumeApplicationRevokedEvent(ApplicationRevokedEvent event) {
        deleteCalendarEntry(event.getApplication());
    }

    @Async
    @EventListener
    public void consumeApplicationCancelledEvent(ApplicationCancelledEvent event) {
        deleteCalendarEntry(event.getApplication());
    }

    @Async
    @EventListener
    public void consumeApplicationDeletedEvent(ApplicationDeletedEvent event) {
        deleteCalendarEntry(event.getApplication());
    }

    @Async
    @EventListener
    public void consumeSickNoteCreatedEvent(SickNoteCreatedEvent event) {
        addCalendarEntry(event.getSickNote());
    }

    @Async
    @EventListener
    public void consumeSickNoteUpdatedEvent(SickNoteUpdatedEvent event) {
        update(event.getSickNote());
    }

    @Async
    @EventListener
    public void consumeSickNoteCancelledEvent(SickNoteCancelledEvent event) {
        deleteCalendarEntry(event.getSickNote());
    }

    @Async
    @EventListener
    public void consumeSickNoteDeletedEvent(SickNoteDeletedEvent event) {
        deleteCalendarEntry(event.getSickNote());
    }

    @Async
    @EventListener
    public void consumeSickNoteToApplicationConvertedEvent(SickNoteToApplicationConvertedEvent event) {
        deleteCalendarEntry(event.getSickNote());
        addCalendarEntry(event.getApplication());
    }

    private void addCalendarEntry(Application application) {
        calendarService.getCalendarProvider()
            .flatMap(calendarProvider -> calendarProvider.add(new Absence(application.getPerson(), application.getPeriod(), getAbsenceTimeConfiguration()), getCalendarSettings()))
            .ifPresent(eventId -> createCalendarEntryMapping(application.getId(), VACATION, eventId));
    }

    private void addCalendarEntry(SickNote sickNote) {
        calendarService.getCalendarProvider()
            .flatMap(calendarProvider -> calendarProvider.add(new Absence(sickNote.getPerson(), sickNote.getPeriod(), getAbsenceTimeConfiguration()), getCalendarSettings()))
            .ifPresent(eventId -> createCalendarEntryMapping(sickNote.getId(), SICKNOTE, eventId));
    }

    private void update(Application application) {
        getAbsenceByIdAndType(application.getId(), VACATION)
            .ifPresent(absenceMapping ->
                calendarService.getCalendarProvider()
                    .ifPresent(calendarProvider -> {
                        final Absence absence = new Absence(application.getPerson(), application.getPeriod(), getAbsenceTimeConfiguration());
                        calendarProvider.update(absence, absenceMapping.getEventId(), getCalendarSettings());
                    })
            );
    }

    private void update(SickNote sickNote) {
        getAbsenceByIdAndType(sickNote.getId(), VACATION)
            .ifPresent(absenceMapping ->
                calendarService.getCalendarProvider()
                    .ifPresent(calendarProvider -> {
                        final Absence absence = new Absence(sickNote.getPerson(), sickNote.getPeriod(), getAbsenceTimeConfiguration());
                        calendarProvider.update(absence, absenceMapping.getEventId(), getCalendarSettings());
                    })
            );
    }

    private void deleteCalendarEntry(Application application) {
        getAbsenceByIdAndType(application.getId(), VACATION)
            .flatMap(absenceMapping -> calendarService.getCalendarProvider()
                .flatMap(calendarProvider -> calendarProvider.delete(absenceMapping.getEventId(), getCalendarSettings())))
            .ifPresent(absenceMappingRepository::deleteByEventId);
    }

    private void deleteCalendarEntry(SickNote sickNote) {
        getAbsenceByIdAndType(sickNote.getId(), SICKNOTE)
            .flatMap(absenceMapping -> calendarService.getCalendarProvider()
                .flatMap(calendarProvider -> calendarProvider.delete(absenceMapping.getEventId(), getCalendarSettings())))
            .ifPresent(absenceMappingRepository::deleteByEventId);
    }

    void checkCalendarSyncSettings() {
        calendarService.getCalendarProvider()
            .ifPresent(calendarProvider -> calendarProvider.checkCalendarSyncSettings(getCalendarSettings()));
    }

    private void createCalendarEntryMapping(Long id, AbsenceMappingType absenceMappingType, String eventId) {
        absenceMappingRepository.save(new AbsenceMapping(id, absenceMappingType, eventId));
    }

    private Optional<AbsenceMapping> getAbsenceByIdAndType(Long id, AbsenceMappingType absenceMappingType) {
        return absenceMappingRepository.findAbsenceMappingByAbsenceIdAndAbsenceMappingType(id, absenceMappingType);
    }

    private CalendarSettings getCalendarSettings() {
        return calendarSettingsService.getCalendarSettings();
    }

    private AbsenceTimeConfiguration getAbsenceTimeConfiguration() {
        return new AbsenceTimeConfiguration(settingsService.getSettings().getTimeSettings());
    }
}
