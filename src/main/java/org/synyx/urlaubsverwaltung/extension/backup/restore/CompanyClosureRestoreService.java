package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureImportService;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureVersionDTO;

import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupRestoreEnabled
class CompanyClosureRestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final CompanyClosureImportService companyClosureImportService;

    CompanyClosureRestoreService(CompanyClosureImportService companyClosureImportService) {
        this.companyClosureImportService = companyClosureImportService;
    }

    void restore(List<CompanyClosureDTO> companyClosures) {

        // default closures may already exist (created on initial settings), the restore must leave exactly the backup's closures
        companyClosureImportService.deleteAll();

        for (CompanyClosureDTO dto : companyClosures) {
            companyClosureImportService.importCompanyClosure(
                dto.toEntity(),
                dto.versions().stream().map(CompanyClosureVersionDTO::toEntity).toList()
            );
        }

        LOG.info("Restored {} companyClosures.", companyClosures.size());
    }
}
