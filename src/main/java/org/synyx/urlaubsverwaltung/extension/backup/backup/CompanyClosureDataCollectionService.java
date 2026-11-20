package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntity;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureImportService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersionEntity;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureVersionDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.DayLengthDTO;

import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static org.synyx.urlaubsverwaltung.extension.backup.backup.PersonHelper.optionalExternalUserId;

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
            .sorted(comparing(CompanyClosureEntity::getId))
            .map(closure -> toDto(closure, versions))
            .toList();
    }

    private static CompanyClosureDTO toDto(CompanyClosureEntity closure, List<CompanyClosureVersionEntity> versions) {
        final List<CompanyClosureVersionDTO> versionDtos = versionsOf(closure, versions);
        final String externalIdOfLastModifier = optionalExternalUserId(closure.getLastModifiedBy());
        return new CompanyClosureDTO(closure.getMonth(), closure.getDayOfMonth(), closure.getMessageKey(), closure.getNameByLocale(),
            closure.getLastModifiedAt(), externalIdOfLastModifier, versionDtos);
    }

    private static List<CompanyClosureVersionDTO> versionsOf(CompanyClosureEntity closure, List<CompanyClosureVersionEntity> versions) {
        return versions.stream()
            .filter(version -> closure.getId().equals(version.getCompanyClosureId()))
            .sorted(comparing(CompanyClosureVersionEntity::getValidFrom, nullsFirst(naturalOrder())))
            .map(CompanyClosureDataCollectionService::toVersionDto)
            .toList();
    }

    private static CompanyClosureVersionDTO toVersionDto(CompanyClosureVersionEntity version) {
        final DayLengthDTO dayLength = DayLengthDTO.valueOf(version.getDayLength().name());
        final String externalIdOfLastModifier = optionalExternalUserId(version.getLastModifiedBy());
        return new CompanyClosureVersionDTO(dayLength, version.getValidFrom(), version.getLastModifiedAt(), externalIdOfLastModifier);
    }
}
