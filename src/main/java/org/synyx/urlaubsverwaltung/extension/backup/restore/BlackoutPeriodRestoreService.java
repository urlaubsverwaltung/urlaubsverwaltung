package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodImportService;
import org.synyx.urlaubsverwaltung.extension.backup.model.BlackoutPeriodDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupRestoreEnabled
class BlackoutPeriodRestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final BlackoutPeriodImportService blackoutPeriodImportService;

    BlackoutPeriodRestoreService(BlackoutPeriodImportService blackoutPeriodImportService) {
        this.blackoutPeriodImportService = blackoutPeriodImportService;
    }

    void restore(Collection<BlackoutPeriodDTO> blackoutPeriodDTOs, Map<Long, Long> newDepartmentIdByOldId, Map<Long, Long> newVacationTypeIdByOldId) {

        for (BlackoutPeriodDTO dto : blackoutPeriodDTOs) {
            final List<Long> departmentIds = mapIds(dto.departmentIds(), newDepartmentIdByOldId, "department", dto);
            final List<Long> vacationTypeIds = mapIds(dto.vacationTypeIds(), newVacationTypeIdByOldId, "vacationType", dto);
            blackoutPeriodImportService.importBlackoutPeriod(dto.toEntity(departmentIds, vacationTypeIds));
        }

        LOG.info("Restored {} blackoutPeriods.", blackoutPeriodDTOs.size());
    }

    // unknown ids are dropped, like the blackout period itself ignores deleted departments and vacation types
    private static List<Long> mapIds(List<Long> oldIds, Map<Long, Long> newIdByOldId, String type, BlackoutPeriodDTO dto) {
        return oldIds.stream()
            .map(oldId -> {
                final Long newId = newIdByOldId.get(oldId);
                if (newId == null) {
                    LOG.warn("blackoutPeriod {} with old id={} not found - skip it for blackoutPeriod title={}", type, oldId, dto.title());
                }
                return newId;
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
