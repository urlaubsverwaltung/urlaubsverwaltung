package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntity;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureImportService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersionEntity;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureVersionDTO;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupRestoreEnabled
class CompanyClosureRestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final CompanyClosureImportService companyClosureImportService;
    private final PersonService personService;

    CompanyClosureRestoreService(CompanyClosureImportService companyClosureImportService, PersonService personService) {
        this.companyClosureImportService = companyClosureImportService;
        this.personService = personService;
    }

    void restore(List<CompanyClosureDTO> companyClosures) {

        // default closures may already exist (created on initial settings), the restore must leave exactly the backup's closures
        companyClosureImportService.deleteAll();

        for (CompanyClosureDTO dto : companyClosures) {
            final CompanyClosureEntity closure = dto.toEntity(findLastModifier(dto.externalIdOfLastModifier()));
            final List<CompanyClosureVersionEntity> versions = dto.versions().stream().map(this::toEntity).toList();
            companyClosureImportService.importCompanyClosure(closure, versions);
        }

        LOG.info("Restored {} companyClosures.", companyClosures.size());
    }

    private CompanyClosureVersionEntity toEntity(CompanyClosureVersionDTO dto) {
        return dto.toEntity(findLastModifier(dto.externalIdOfLastModifier()));
    }

    /**
     * The person may be deleted or missing in the backup, the closure is restored without its last modifier then.
     */
    private Person findLastModifier(String externalId) {
        return Optional.ofNullable(externalId)
            .flatMap(personService::getPersonByUsername)
            .orElse(null);
    }
}
