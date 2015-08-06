package org.synyx.urlaubsverwaltung.core.sync;

import java.util.Optional;


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
     *
     * @return  id of added absence event, may be empty if an error occurred during the calendar sync
     */
    Optional<String> addAbsence(Absence absence);
}
