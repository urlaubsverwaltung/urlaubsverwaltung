package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.DepartmentEntity;
import org.synyx.urlaubsverwaltung.department.DepartmentImportService;
import org.synyx.urlaubsverwaltung.extension.backup.model.DepartmentDTO;

import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.HashMap.newHashMap;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupRestoreEnabled
class DepartmentRestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final DepartmentImportService departmentImportService;

    DepartmentRestoreService(DepartmentImportService departmentImportService) {
        this.departmentImportService = departmentImportService;
    }

    Map<Long, Long> restore(List<DepartmentDTO> departments) {

        final Map<Long, Long> newDepartmentIdByOldId = newHashMap(departments.size());

        for (DepartmentDTO dto : departments) {
            final DepartmentEntity toImport = dto.toDepartmentEntity();
            final DepartmentEntity importedEntity = departmentImportService.importDepartment(toImport);
            newDepartmentIdByOldId.put(dto.id(), importedEntity.getId());
        }

        LOG.info("Restored {} departments.", newDepartmentIdByOldId.size());

        return newDepartmentIdByOldId;
    }
}
