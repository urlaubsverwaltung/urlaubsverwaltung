package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingImportService;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarIntegrationSettingsImportService;
import org.synyx.urlaubsverwaltung.extension.backup.model.AbsenceMappingDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CalendarIntegrationBackupDTO;

import java.util.List;

@Service
@ConditionalOnBackupRestoreEnabled
class CalendarIntegrationRestoreService {

    private final CalendarIntegrationSettingsImportService calendarIntegrationSettingsImportService;
    private final AbsenceMappingImportService absenceMappingImportService;

    CalendarIntegrationRestoreService(CalendarIntegrationSettingsImportService calendarIntegrationSettingsImportService,
                                      AbsenceMappingImportService absenceMappingImportService) {
        this.calendarIntegrationSettingsImportService = calendarIntegrationSettingsImportService;
        this.absenceMappingImportService = absenceMappingImportService;
    }

    private static Long resolveIdOfImportedAbsence(List<ImportedIdTuple> tuplesOfCreatedEntities, AbsenceMappingDTO absenceMapping, AbsenceMappingType vacation) {
        return tuplesOfCreatedEntities.stream()
            .filter(tuple -> tuple.idOfBackup().equals(absenceMapping.absenceId()))
            .map(ImportedIdTuple::idOfRestore)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No absence with type=%s for id=%s found - going to skip importing absenceMapping!".formatted(vacation.name(), absenceMapping.absenceId())));
    }

    void restore(CalendarIntegrationBackupDTO calendarIntegration, List<ImportedIdTuple> createdApplications, List<ImportedIdTuple> createdSicknotes) {
        calendarIntegrationSettingsImportService.importCalendarIntegrationSettings(calendarIntegration.calendarIntegrationSettings().toCalendarSettings());
        importAbsenceMappings(createdApplications, createdSicknotes, calendarIntegration.absenceMappings());
    }

    private void importAbsenceMappings(List<ImportedIdTuple> createdApplications, List<ImportedIdTuple> createdSicknotes, List<AbsenceMappingDTO> absenceMappingDTOs) {
        absenceMappingDTOs.forEach(absenceMapping -> {
            switch (absenceMapping.absenceMappingType()) {
                case VACATION -> importAbsenceMapping(createdApplications, absenceMapping);
                case SICKNOTE -> importAbsenceMapping(createdSicknotes, absenceMapping);
            }
        });
    }

    private void importAbsenceMapping(List<ImportedIdTuple> tuplesOfCreatedEntities, AbsenceMappingDTO absenceMapping) {
        final Long absenceIdOfCreatedAbsence = resolveIdOfImportedAbsence(tuplesOfCreatedEntities, absenceMapping, absenceMapping.absenceMappingType().toAbsenceMappingType());
        absenceMappingImportService.importAbsenceMapping(absenceMapping.toAbsenceMapping(absenceIdOfCreatedAbsence));
    }
}
