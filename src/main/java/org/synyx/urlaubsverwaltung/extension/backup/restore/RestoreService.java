package org.synyx.urlaubsverwaltung.extension.backup.restore;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.UrlaubsverwaltungBackupDTO;

import java.util.List;
import java.util.Map;

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
    private final DepartmentMembershipRestoreService departmentMembershipRestoreService;
    private final ApplicationRestoreService applicationRestoreService;
    private final CalendarIntegrationRestoreService calendarIntegrationRestoreService;
    private final PersonActivePeriodRestoreService personActivePeriodRestoreService;
    private final BlackoutPeriodRestoreService blackoutPeriodRestoreService;

    RestoreService(
        SettingsRestoreService settingsRestoreService,
        PersonRestoreService personRestoreService,
        OvertimeRestoreService overtimeRestoreService,
        SickNoteRestoreService sickNoteRestoreService,
        CalendarsRestoreService calendarsRestoreService,
        DepartmentRestoreService departmentRestoreService,
        DepartmentMembershipRestoreService departmentMembershipRestoreService,
        ApplicationRestoreService applicationRestoreService,
        CalendarIntegrationRestoreService calendarIntegrationRestoreService,
        PersonActivePeriodRestoreService personActivePeriodRestoreService,
        BlackoutPeriodRestoreService blackoutPeriodRestoreService
    ) {
        this.settingsRestoreService = settingsRestoreService;
        this.personRestoreService = personRestoreService;
        this.overtimeRestoreService = overtimeRestoreService;
        this.sickNoteRestoreService = sickNoteRestoreService;
        this.calendarsRestoreService = calendarsRestoreService;
        this.departmentRestoreService = departmentRestoreService;
        this.departmentMembershipRestoreService = departmentMembershipRestoreService;
        this.applicationRestoreService = applicationRestoreService;
        this.calendarIntegrationRestoreService = calendarIntegrationRestoreService;
        this.personActivePeriodRestoreService = personActivePeriodRestoreService;
        this.blackoutPeriodRestoreService = blackoutPeriodRestoreService;
    }

    void restoreData(UrlaubsverwaltungBackupDTO backupToRestore) {
        LOG.info("Starting to restore data...");
        settingsRestoreService.restore(backupToRestore.settings());
        personRestoreService.restore(backupToRestore.persons());
        personActivePeriodRestoreService.restore(backupToRestore.personActivePeriods());
        overtimeRestoreService.restore(backupToRestore.overtimes());

        final ApplicationRestoreService.RestoredApplications restoredApplications = applicationRestoreService.restore(backupToRestore.applications());
        final List<ImportedIdTuple> createdSicknotes = sickNoteRestoreService.restore(backupToRestore.sickNotes());
        calendarIntegrationRestoreService.restore(backupToRestore.calendarIntegration(), restoredApplications.createdApplications(), createdSicknotes);

        final Map<Long, Long> newDepartmentIdByOldId = departmentRestoreService.restore(backupToRestore.departments());
        departmentMembershipRestoreService.restore(backupToRestore.departmentMemberships(), newDepartmentIdByOldId);
        blackoutPeriodRestoreService.restore(backupToRestore.blackoutPeriods(), newDepartmentIdByOldId, restoredApplications.newVacationTypeIdByOldId());

        calendarsRestoreService.restore(backupToRestore.calendars(), backupToRestore.departments());

        LOG.info("Finished restoring data");
    }

}
