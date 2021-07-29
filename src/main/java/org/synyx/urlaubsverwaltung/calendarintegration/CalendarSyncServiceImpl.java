package org.synyx.urlaubsverwaltung.calendarintegration;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.noop.NoopCalendarSyncProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsEntity;
import org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsService;

import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Implementation of {@link CalendarSyncService}.
 */
@Deprecated(since = "4.0.0", forRemoval = true)
@Service
public class CalendarSyncServiceImpl implements CalendarSyncService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final CalendarSettingsService settingsService;
    private final List<CalendarProvider> calendarProviders;

    @Autowired
    public CalendarSyncServiceImpl(CalendarSettingsService settingsService, List<CalendarProvider> calendarProviders) {
        this.settingsService = settingsService;
        this.calendarProviders = calendarProviders;

        LOG.info("The following calendar provider is configured: {}", getCalendarProvider().getClass());
    }

    @Override
    public Optional<String> addAbsence(Absence absence) {
        return getCalendarProvider().add(absence, getCalendarSettings());
    }

    @Override
    public void update(Absence absence, String eventId) {
        getCalendarProvider().update(absence, eventId, getCalendarSettings());
    }

    @Override
    public void deleteAbsence(String eventId) {
        getCalendarProvider().delete(eventId, getCalendarSettings());
    }

    @Override
    public void checkCalendarSyncSettings() {
        getCalendarProvider().checkCalendarSyncSettings(getCalendarSettings());
    }

    private CalendarProvider getCalendarProvider() {
        return getCalendarProviderClassByString(getCalendarSettings().getProvider());
    }

    private CalendarSettingsEntity getCalendarSettings() {
        return this.settingsService.getSettings();
    }

    private CalendarProvider getCalendarProviderClassByString(String provider) {
        return calendarProviders.stream()
            .filter(calendarProvider -> calendarProvider.getClass().getSimpleName().equals(provider))
            .findFirst()
            .orElse(new NoopCalendarSyncProvider());
    }
}
