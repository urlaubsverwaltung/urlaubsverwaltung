package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.extension.backup.model.BlackoutPeriodDTO;

import java.util.List;

@Service
@ConditionalOnBackupCreateEnabled
class BlackoutPeriodDataCollectionService {

    private final BlackoutPeriodService blackoutPeriodService;

    BlackoutPeriodDataCollectionService(BlackoutPeriodService blackoutPeriodService) {
        this.blackoutPeriodService = blackoutPeriodService;
    }

    List<BlackoutPeriodDTO> collectBlackoutPeriods() {
        return blackoutPeriodService.getAllBlackoutPeriods().stream()
            .map(blackoutPeriod -> new BlackoutPeriodDTO(
                blackoutPeriod.getTitle(),
                blackoutPeriod.getStartDate(),
                blackoutPeriod.getEndDate(),
                blackoutPeriod.getCreatedAt(),
                blackoutPeriod.getLastModification(),
                blackoutPeriod.isCompanyWide(),
                blackoutPeriod.appliesToAllVacationTypes(),
                blackoutPeriod.getDepartments().stream().map(Department::getId).sorted().toList(),
                blackoutPeriod.getVacationTypes().stream().map(VacationType::getId).sorted().toList()
            ))
            .toList();
    }
}
