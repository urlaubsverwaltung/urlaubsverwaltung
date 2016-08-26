package org.synyx.urlaubsverwaltung.core.settings;

/**
 * Provides access to {@link org.synyx.urlaubsverwaltung.core.settings.Settings}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface SettingsService {

    /**
     * Persists the given absence settings.
     *
     * @param  absenceSettings  to be persisted
     */
    void save(AbsenceSettings absenceSettings);


    /**
     * Persists the given working time settings.
     *
     * @param  workingTimeSettings  to be persisted
     */
    void save(WorkingTimeSettings workingTimeSettings);


    /**
     * Persists the given mail settings.
     *
     * @param  mailSettings  to be persisted
     */
    void save(MailSettings mailSettings);


    /**
     * Persists the given calendar settings.
     *
     * @param  calendarSettings  to be persisted
     */
    void save(CalendarSettings calendarSettings);


    /**
     * @return  settings for the application
     */
    Settings getSettings();
}
