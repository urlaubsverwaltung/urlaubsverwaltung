package org.synyx.urlaubsverwaltung.person;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.DepartmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

@Service
class ResponsiblePersonServiceImpl implements ResponsiblePersonService {

    private final PersonService personService;
    private final DepartmentService departmentService;

    ResponsiblePersonServiceImpl(PersonService personService, DepartmentService departmentService) {
        this.personService = personService;
        this.departmentService = departmentService;
    }

    @Override
    public List<Person> getResponsibleManagersOf(Person personOfInterest) {
        final List<Person> managementDepartmentPersons = new ArrayList<>();
        if (departmentsAvailable()) {
            managementDepartmentPersons.addAll(getResponsibleDepartmentHeads(personOfInterest));
            managementDepartmentPersons.addAll(getResponsibleSecondStageAuthorities(personOfInterest));
        }

        final List<Person> bosses = personService.getActivePersonsByRole(BOSS);
        return Stream.concat(managementDepartmentPersons.stream(), bosses.stream())
            .distinct()
            .toList();
    }

    @Override
    public List<Person> getResponsibleDepartmentHeads(Person personOfInterest) {
        return personService.getActivePersonsByRole(DEPARTMENT_HEAD)
            .stream()
            .filter(departmentHead -> departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, personOfInterest))
            .filter(without(personOfInterest))
            .toList();
    }

    @Override
    public List<Person> getResponsibleSecondStageAuthorities(Person personOfInterest) {
        return personService.getActivePersonsByRole(SECOND_STAGE_AUTHORITY)
            .stream()
            .filter(secondStageAuthority -> departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, personOfInterest))
            .filter(without(personOfInterest))
            .toList();
    }

    private boolean departmentsAvailable() {
        return departmentService.getNumberOfDepartments() > 0;
    }

    private static Predicate<Person> without(Person personOfInterest) {
        return person -> !person.equals(personOfInterest);
    }
}
