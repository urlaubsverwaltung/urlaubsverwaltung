package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Provides overtime record demo data.
 */
class OvertimeRecordDataProvider {

    private final OvertimeService overtimeService;
    private final SettingsService settingsService;

    OvertimeRecordDataProvider(OvertimeService overtimeService, SettingsService settingsService) {
        this.overtimeService = overtimeService;
        this.settingsService = settingsService;
    }

    void activateOvertime() {
        final Settings settings = settingsService.getSettings();
        settings.getOvertimeSettings().setOvertimeActive(true);
        settingsService.save(settings);
    }

    void createOvertimeRecord(Person person, LocalDate startDate, LocalDate endDate, Duration duration) {
        final PersonId personId = person.getIdAsPersonId();
        final DateRange dateRange = new DateRange(startDate, endDate);
        overtimeService.createOvertime(personId, dateRange, duration, personId, "Ich habe ganz viel gearbeitet");
    }

    boolean personHasNoOvertimes(Person person) {
        return overtimeService.getAllOvertimesByPersonId(person.getIdAsPersonId()).isEmpty();
    }
}
