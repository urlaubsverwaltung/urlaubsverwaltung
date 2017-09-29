package org.synyx.urlaubsverwaltung.core.sync.providers.noop;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.providers.CalendarProvider;

import java.util.Optional;

@ConditionalOnProperty(prefix = "uv.calendar", name = "provider", matchIfMissing = true)
@Service
public class NoopCalendarProvider implements CalendarProvider {

    private static final Logger LOG = Logger.getLogger(NoopCalendarProvider.class);

    @Override
    public Optional<String> add(Absence absence, CalendarSettings calendarSettings) {

        LOG.info(String.format("No calendar provider configured to add event: %s", absence));

        return Optional.empty();
    }

    @Override
    public void update(Absence absence, String eventId, CalendarSettings calendarSettings) {

        LOG.info(String.format("No calendar provider configured to update event: %s, eventId %s", absence, eventId));
    }

    @Override
    public void delete(String eventId, CalendarSettings calendarSettings) {
        LOG.info(String.format("No calendar provider configured to delete event '%s'", eventId));
    }

    @Override
    public void checkCalendarSyncSettings(CalendarSettings calendarSettings) {

    }
}
