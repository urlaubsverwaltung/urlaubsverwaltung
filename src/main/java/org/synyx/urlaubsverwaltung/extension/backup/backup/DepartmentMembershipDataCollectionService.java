package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.DepartmentMembershipService;
import org.synyx.urlaubsverwaltung.extension.backup.model.DepartmentMembershipDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.PersonDTO;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@ConditionalOnBackupCreateEnabled
class DepartmentMembershipDataCollectionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final DepartmentMembershipService departmentMembershipService;

    DepartmentMembershipDataCollectionService(DepartmentMembershipService departmentMembershipService) {
        this.departmentMembershipService = departmentMembershipService;
    }

    List<DepartmentMembershipDTO> collectDepartmentMemberships(Function<PersonId, PersonDTO> personDtoById) {
        return departmentMembershipService.getAllDepartmentMemberships().stream()
            .map(membership -> {
                final PersonDTO person = personDtoById.apply(membership.personId());
                if (person == null) {
                    LOG.warn("Skip collecting departmentMembership because could not find person id={} for {}", membership.personId(), membership);
                    return null;
                } else {
                    return new DepartmentMembershipDTO(
                        person.externalId(),
                        membership.departmentId(),
                        membership.membershipKind(),
                        membership.validFrom(),
                        membership.validTo().orElse(null)
                    );
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
