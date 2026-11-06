package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntity;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureImportService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersionEntity;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureVersionDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.DayLengthDTO;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.nullsFirst;

@Service
@ConditionalOnBackupCreateEnabled
class CompanyClosureDataCollectionService {

    private final CompanyClosureImportService companyClosureImportService;

    CompanyClosureDataCollectionService(CompanyClosureImportService companyClosureImportService) {
        this.companyClosureImportService = companyClosureImportService;
    }

    List<CompanyClosureDTO> collectCompanyClosures() {
        final List<CompanyClosureVersionEntity> versions = companyClosureImportService.findAllVersions();
        return companyClosureImportService.findAllClosures().stream()
            .sorted(Comparator.comparing(CompanyClosureEntity::getId))
            .map(closure -> new CompanyClosureDTO(closure.getMonth(), closure.getDayOfMonth(), closure.getName(),
                versions.stream()
                    .filter(version -> closure.getId().equals(version.getCompanyClosureId()))
                    .sorted(Comparator.comparing(CompanyClosureVersionEntity::getValidFrom, nullsFirst(Comparator.naturalOrder())))
                    .map(version -> new CompanyClosureVersionDTO(DayLengthDTO.valueOf(version.getDayLength().name()), version.getValidFrom()))
                    .toList()))
            .toList();
    }
}
