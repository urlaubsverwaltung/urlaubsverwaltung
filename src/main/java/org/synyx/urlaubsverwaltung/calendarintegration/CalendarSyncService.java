package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
import org.synyx.urlaubsverwaltung.calendar.CalendarAbsence;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCancelledEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCreatedEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteDeletedEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteToApplicationConvertedEvent;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteUpdatedEvent;

import java.util.Optional;

import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.SICKNOTE;
import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.VACATION;

@Service
class CalendarSyncService {

    private final SettingsService settingsService;
    private final CalendarSettingsService calendarSettingsService;
    private final CalendarProviderService calendarProviderService;
    private final AbsenceMappingRepository absenceMappingRepository;

    @Autowired
    CalendarSyncService(
        SettingsService settingsService,
        CalendarSettingsService calendarSettingsService,
        CalendarProviderService calendarProviderService,
        AbsenceMappingRepository absenceMappingRepository
    ) {
        this.settingsService = settingsService;
        this.calendarSettingsService = calendarSettingsService;
        this.calendarProviderService = calendarProviderService;
        this.absenceMappingRepository = absenceMappingRepository;
    }

    @Async
    @EventListener
    public void consumeApplicationAppliedEvent(ApplicationAppliedEvent event) {
        addCalendarEntry(event.application());
    }

    @Async
    @EventListener
    public void consumeApplicationAllowedTemporarilyEvent(ApplicationAllowedTemporarilyEvent event) {
        updateCalendarEntry(event.application());
    }

    @Async
    @EventListener
    public void consumeApplicationAllowedEvent(ApplicationAllowedEvent event) {
        updateCalendarEntry(event.application());
    }

    @Async
    @EventListener
    public void consumeApplicationUpdatedEvent(ApplicationUpdatedEvent event) {
        updateCalendarEntry(event.application());
    }

    @Async
    @EventListener
    public void consumeApplicationRejectedEvent(ApplicationRejectedEvent event) {
        deleteCalendarEntry(event.application());
    }

    @Async
    @EventListener
    public void consumeApplicationRevokedEvent(ApplicationRevokedEvent event) {
        deleteCalendarEntry(event.application());
    }

    @Async
    @EventListener
    public void consumeApplicationCancelledEvent(ApplicationCancelledEvent event) {
        deleteCalendarEntry(event.application());
    }

    @Async
    @EventListener
    public void consumeApplicationDeletedEvent(ApplicationDeletedEvent event) {
        deleteCalendarEntry(event.application());
    }

    @Async
    @EventListener
    public void consumeSickNoteCreatedEvent(SickNoteCreatedEvent event) {
        addCalendarEntry(event.sickNote());
    }

    @Async
    @EventListener
    public void consumeSickNoteUpdatedEvent(SickNoteUpdatedEvent event) {
        updateCalendarEntry(event.sickNote());
    }

    @Async
    @EventListener
    public void consumeSickNoteCancelledEvent(SickNoteCancelledEvent event) {
        deleteCalendarEntry(event.sickNote());
    }

    @Async
    @EventListener
    public void consumeSickNoteDeletedEvent(SickNoteDeletedEvent event) {
        deleteCalendarEntry(event.sickNote());
    }

    @Async
    @EventListener
    public void consumeSickNoteToApplicationConvertedEvent(SickNoteToApplicationConvertedEvent event) {
        deleteCalendarEntry(event.sickNote());
        addCalendarEntry(event.application());
    }

    private void addCalendarEntry(Application application) {
        calendarProviderService.getCalendarProvider()
            .flatMap(calendarProvider -> calendarProvider.add(new CalendarAbsence(application.getPerson(), application.getPeriod(), getAbsenceTimeConfiguration()), getCalendarSettings()))
            .ifPresent(eventId -> createCalendarEntryMapping(application.getId(), VACATION, eventId));
    }

    private void addCalendarEntry(SickNote sickNote) {
        calendarProviderService.getCalendarProvider()
            .flatMap(calendarProvider -> calendarProvider.add(new CalendarAbsence(sickNote.getPerson(), sickNote.getPeriod(), getAbsenceTimeConfiguration()), getCalendarSettings()))
            .ifPresent(eventId -> createCalendarEntryMapping(sickNote.getId(), SICKNOTE, eventId));
    }

    private void updateCalendarEntry(Application application) {
        getAbsenceByIdAndType(application.getId(), VACATION)
            .ifPresent(absenceMapping ->
                calendarProviderService.getCalendarProvider()
                    .ifPresent(calendarProvider -> {
                        final CalendarAbsence absence = new CalendarAbsence(application.getPerson(), application.getPeriod(), getAbsenceTimeConfiguration());
                        calendarProvider.update(absence, absenceMapping.getEventId(), getCalendarSettings());
                    })
            );
    }

    private void updateCalendarEntry(SickNote sickNote) {
        getAbsenceByIdAndType(sickNote.getId(), VACATION)
            .ifPresent(absenceMapping -> calendarProviderService.getCalendarProvider()
                .ifPresent(calendarProvider -> {
                    final CalendarAbsence absence = new CalendarAbsence(sickNote.getPerson(), sickNote.getPeriod(), getAbsenceTimeConfiguration());
                    calendarProvider.update(absence, absenceMapping.getEventId(), getCalendarSettings());
                })
            );
    }

    private void deleteCalendarEntry(Application application) {
        getAbsenceByIdAndType(application.getId(), VACATION)
            .flatMap(absenceMapping -> calendarProviderService.getCalendarProvider()
                .flatMap(calendarProvider -> calendarProvider.delete(absenceMapping.getEventId(), getCalendarSettings())))
            .ifPresent(absenceMappingRepository::deleteByEventId);
    }

    private void deleteCalendarEntry(SickNote sickNote) {
        getAbsenceByIdAndType(sickNote.getId(), SICKNOTE)
            .flatMap(absenceMapping -> calendarProviderService.getCalendarProvider()
                .flatMap(calendarProvider -> calendarProvider.delete(absenceMapping.getEventId(), getCalendarSettings())))
            .ifPresent(absenceMappingRepository::deleteByEventId);
    }

    void checkCalendarSyncSettings() {
        calendarProviderService.getCalendarProvider()
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
