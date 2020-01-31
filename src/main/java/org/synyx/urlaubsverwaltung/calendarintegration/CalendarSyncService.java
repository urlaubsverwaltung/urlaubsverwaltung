package org.synyx.urlaubsverwaltung.calendarintegration;

import org.synyx.urlaubsverwaltung.absence.Absence;

import java.util.Optional;


/**
 * Sync absences with all activated and configured calendar providers.
 */
public interface CalendarSyncService {

    /**
     * Add a person's absence to calendar.
     *
     * @param absence represents the absence of a person
     * @return id of added absence event, may be empty if an error occurred during the calendar sync
     */
    Optional<String> addAbsence(Absence absence);


    /**
     * Updates a given event with absence content.
     *
     * @param absence represents the updated absence
     * @param eventId id of event to be updated
     */
    void update(Absence absence, String eventId);


    /**
     * Deletes a person's absence in calendar.
     *
     * @param eventId id of absence event, which should be deleted.
     */
    void deleteAbsence(String eventId);


    /**
     * Check the settings for calendar sync. (only if sync is active)
     */
    void checkCalendarSyncSettings();
}
