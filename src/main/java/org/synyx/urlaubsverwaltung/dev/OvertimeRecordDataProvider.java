package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;


/**
 * Provides overtime record demo data.
 */
class OvertimeRecordDataProvider {

    private final OvertimeService overtimeService;

    OvertimeRecordDataProvider(OvertimeService overtimeService, SettingsService settingsService) {

        this.overtimeService = overtimeService;

        // Activate overtime management for development purpose
        Settings settings = settingsService.getSettings();
        settings.getWorkingTimeSettings().setOvertimeActive(true);
        settingsService.save(settings);
    }

    void createOvertimeRecord(Person person, LocalDate startDate, LocalDate endDate, BigDecimal hours) {

        final Overtime overtime = new Overtime(person, startDate, endDate, hours);
        overtimeService.record(overtime, Optional.of("Ich habe ganz viel gearbeitet"), person);
    }
}
