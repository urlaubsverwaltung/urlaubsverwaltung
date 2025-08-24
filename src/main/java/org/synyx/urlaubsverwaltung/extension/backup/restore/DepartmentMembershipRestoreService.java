package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.DepartmentMembershipEntity;
import org.synyx.urlaubsverwaltung.department.DepartmentMembershipImportService;
import org.synyx.urlaubsverwaltung.extension.backup.model.DepartmentMembershipDTO;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupRestoreEnabled
class DepartmentMembershipRestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final DepartmentMembershipImportService departmentMembershipImportService;
    private final PersonService personService;

    DepartmentMembershipRestoreService(
        DepartmentMembershipImportService departmentMembershipImportService,
        PersonService personService
    ) {
        this.departmentMembershipImportService = departmentMembershipImportService;
        this.personService = personService;
    }

    void restore(Collection<DepartmentMembershipDTO> departmentMembershipDTOs, Map<Long, Long> newDepartmentIdByOldId) {

        int restored = 0;
        int skipped = 0;

        final Map<String, Person> personByExternalId = personService.getAllPersons().stream()
            .collect(toMap(Person::getUsername, Function.identity()));

        for (DepartmentMembershipDTO membershipDTO : departmentMembershipDTOs) {
            final Person person = personByExternalId.get(membershipDTO.personExternalId());
            final Long newDepartmentId = newDepartmentIdByOldId.get(membershipDTO.departmentId());
            if (person == null) {
                LOG.warn("departmentMembership owner with externalId={} no found - skip importing departmentMembership!", membershipDTO.personExternalId());
                skipped++;
            } else if (newDepartmentId == null) {
                LOG.warn("departmentMembership department with old id={} not found - skip importing departmentMembership!", membershipDTO.departmentId());
                skipped++;
            } else {
                restoreDepartmentMembership(membershipDTO, person, newDepartmentId);
                restored++;
            }
        }

        LOG.info("Restored {} departmentMemberships. Skipped {} departmentMemberships.", restored, skipped);
    }

    void restoreDepartmentMembership(DepartmentMembershipDTO dto, Person person, Long departmentId) {

        final DepartmentMembershipEntity entity = dto.toEntity(person);
        entity.setDepartmentId(departmentId);

        departmentMembershipImportService.importDepartmentMembership(entity);
    }
}
