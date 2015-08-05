package org.synyx.urlaubsverwaltung.core.sync;

/**
 * Syncs vacations and sick notes with calendar providers like Exchange or Google Calendar.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface CalendarSyncService {

    /**
     * Add a person's absence to calendar.
     *
     * @param  absence  represents the absence of a person
     */
    void addAbsence(Absence absence);
}
