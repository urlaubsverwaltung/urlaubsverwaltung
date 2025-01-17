package org.synyx.urlaubsverwaltung.extension.backup.restore;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.UrlaubsverwaltungBackupDTO;

import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupRestoreEnabled
class RestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SettingsRestoreService settingsRestoreService;
    private final PersonRestoreService personRestoreService;
    private final OvertimeRestoreService overtimeRestoreService;
    private final SickNoteRestoreService sickNoteRestoreService;
    private final CalendarsRestoreService calendarsRestoreService;
    private final DepartmentRestoreService departmentRestoreService;
    private final ApplicationRestoreService applicationRestoreService;
    private final CalendarIntegrationRestoreService calendarIntegrationRestoreService;

    RestoreService(SettingsRestoreService settingsRestoreService, PersonRestoreService personRestoreService, OvertimeRestoreService overtimeRestoreService, SickNoteRestoreService sickNoteRestoreService, CalendarsRestoreService calendarsRestoreService, DepartmentRestoreService departmentRestoreService, ApplicationRestoreService applicationRestoreService, CalendarIntegrationRestoreService calendarIntegrationRestoreService) {
        this.settingsRestoreService = settingsRestoreService;
        this.personRestoreService = personRestoreService;
        this.overtimeRestoreService = overtimeRestoreService;
        this.sickNoteRestoreService = sickNoteRestoreService;
        this.calendarsRestoreService = calendarsRestoreService;
        this.departmentRestoreService = departmentRestoreService;
        this.applicationRestoreService = applicationRestoreService;
        this.calendarIntegrationRestoreService = calendarIntegrationRestoreService;
    }

    void restoreData(UrlaubsverwaltungBackupDTO backupToRestore) {
        LOG.info("Starting to restore data...");
        settingsRestoreService.restore(backupToRestore.settings());
        personRestoreService.restore(backupToRestore.persons());
        overtimeRestoreService.restore(backupToRestore.overtimes());

        final List<ImportedIdTuple> createdApplications = applicationRestoreService.restore(backupToRestore.applications());
        final List<ImportedIdTuple> createdSicknotes = sickNoteRestoreService.restore(backupToRestore.sickNotes());
        calendarIntegrationRestoreService.restore(backupToRestore.calendarIntegration(), createdApplications, createdSicknotes);

        departmentRestoreService.restore(backupToRestore.departments());
        calendarsRestoreService.restore(backupToRestore.calendars(), backupToRestore.departments());
        LOG.info("Finished restoring data");
    }

}
