package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.overtime.settings.OvertimeSettingsEntity;
import org.synyx.urlaubsverwaltung.overtime.settings.OvertimeSettingsService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Provides overtime record demo data.
 */
class OvertimeRecordDataProvider {

    private final OvertimeService overtimeService;
    private final OvertimeSettingsService overtimeSettingsService;

    OvertimeRecordDataProvider(OvertimeService overtimeService, OvertimeSettingsService overtimeSettingsService) {
        this.overtimeService = overtimeService;
        this.overtimeSettingsService = overtimeSettingsService;
    }

    void activateOvertime() {
        final OvertimeSettingsEntity settings = overtimeSettingsService.getSettings();
        settings.setOvertimeActive(true);
        overtimeSettingsService.save(settings);
    }

    void createOvertimeRecord(Person person, LocalDate startDate, LocalDate endDate, Duration duration) {
        final Overtime overtime = new Overtime(person, startDate, endDate, duration);
        overtimeService.record(overtime, Optional.of("Ich habe ganz viel gearbeitet"), person);
    }
}
