package org.synyx.urlaubsverwaltung.core.sync.providers;

import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;

import java.util.Optional;


/**
 * Syncs vacations and sick notes with calendar providers like Exchange or Google Calendar.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface CalendarProvider {

    /**
     * Add a person's absence to calendar.
     *
     * @param  absence  represents the absence of a person
     * @param  calendarSettings  contains configuration for calendar provider
     *
     * @return  id of added absence event, may be empty if an error occurred during the calendar sync
     */
    Optional<String> add(Absence absence, CalendarSettings calendarSettings);


    /**
     * Updates a given event with absence content.
     *
     * @param  absence  represents the updated absence
     * @param  eventId  id of event to be updated
     * @param  calendarSettings  contains configuration for calendar provider
     */
    void update(Absence absence, String eventId, CalendarSettings calendarSettings);


    /**
     * Deletes a person's absence in calendar.
     *
     * @param  eventId  id of absence event, which should be deleted
     * @param  calendarSettings  contains configuration for calendar provider
     */
    void delete(String eventId, CalendarSettings calendarSettings);


    /**
     * Check the settings for calendar sync.
     *
     * @param  calendarSettings  to be checked, containing configuration for calendar provider
     */
    void checkCalendarSyncSettings(CalendarSettings calendarSettings);
}
