package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.DepartmentImportService;
import org.synyx.urlaubsverwaltung.extension.backup.model.DepartmentDTO;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnBackupRestoreEnabled
class DepartmentRestoreService {

    private final PersonService personService;
    private final DepartmentImportService departmentImportService;

    DepartmentRestoreService(PersonService personService, DepartmentImportService departmentImportService) {
        this.personService = personService;
        this.departmentImportService = departmentImportService;
    }

    void restore(List<DepartmentDTO> departments) {
        departments.stream().map(dto -> {
            final List<Person> departmentHeads = getPersons(dto.externalIdsOfDepartmentHeads());
            final List<Person> secondStageAuthorities = getPersons(dto.externalIdsOfSecondStageAuthorities());
            final List<Person> members = getPersons(dto.externalIdsOfMembers());
            return dto.toDepartmentEntity(departmentHeads, secondStageAuthorities, members);
        }).forEach(departmentImportService::importDepartment);
    }

    /**
     * Get persons by external ids aka usernames, when a person is not found it is not included in the result!
     *
     * @param externalIds list of external ids / usernames
     * @return
     */
    private List<Person> getPersons(List<String> externalIds) {
        return externalIds.stream()
            .map(personService::getPersonByUsername)
            .<Person>mapMulti(Optional::ifPresent)
            .toList();
    }
}
