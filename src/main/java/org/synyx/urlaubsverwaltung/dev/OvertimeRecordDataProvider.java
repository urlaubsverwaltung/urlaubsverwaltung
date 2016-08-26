package org.synyx.urlaubsverwaltung.dev;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.stereotype.Component;

import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.settings.WorkingTimeSettings;

import java.math.BigDecimal;

import java.util.Optional;


/**
 * Provides overtime record test data.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
@ConditionalOnProperty("testdata.create")
class OvertimeRecordDataProvider {

    private final OvertimeService overtimeService;

    @Autowired
    OvertimeRecordDataProvider(OvertimeService overtimeService, SettingsService settingsService) {

        this.overtimeService = overtimeService;

        // Activate overtime management for development purpose
        Settings settings = settingsService.getSettings();
        WorkingTimeSettings workingTimeSettings = settings.getWorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(true);
        settingsService.save(workingTimeSettings);
    }

    Overtime createOvertimeRecord(Person person, DateMidnight startDate, DateMidnight endDate, BigDecimal hours) {

        Overtime overtime = new Overtime(person, startDate, endDate, hours);

        return overtimeService.record(overtime, Optional.of("Ich habe ganz viel gearbeitet"), person);
    }
}
