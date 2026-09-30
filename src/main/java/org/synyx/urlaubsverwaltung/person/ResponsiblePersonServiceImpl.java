package org.synyx.urlaubsverwaltung.person;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.DepartmentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        return getResponsibleDepartmentHeads(List.of(personOfInterest)).get(personOfInterest);
    }

    @Override
    public Map<Person, List<Person>> getResponsibleDepartmentHeads(List<Person> personsOfInterest) {
        final List<Person> departmentHeads = personService.getActivePersonsByRole(DEPARTMENT_HEAD);
        return withoutPersonOfInterest(departmentService.getDepartmentHeadsAllowedToManagePersons(departmentHeads, personsOfInterest));
    }

    @Override
    public List<Person> getResponsibleSecondStageAuthorities(Person personOfInterest) {
        return getResponsibleSecondStageAuthorities(List.of(personOfInterest)).get(personOfInterest);
    }

    @Override
    public Map<Person, List<Person>> getResponsibleSecondStageAuthorities(List<Person> personsOfInterest) {
        final List<Person> secondStageAuthorities = personService.getActivePersonsByRole(SECOND_STAGE_AUTHORITY);
        return withoutPersonOfInterest(departmentService.getSecondStageAuthoritiesAllowedToManagePersons(secondStageAuthorities, personsOfInterest));
    }

    private boolean departmentsAvailable() {
        return departmentService.getNumberOfDepartments() > 0;
    }

    private static Map<Person, List<Person>> withoutPersonOfInterest(Map<Person, List<Person>> responsiblesByPersonOfInterest) {
        final Map<Person, List<Person>> responsibles = new HashMap<>();
        responsiblesByPersonOfInterest.forEach((personOfInterest, responsiblesOfPerson) ->
            responsibles.put(personOfInterest, responsiblesOfPerson.stream().filter(without(personOfInterest)).toList()));
        return responsibles;
    }

    private static Predicate<Person> without(Person personOfInterest) {
        return person -> !person.equals(personOfInterest);
    }
}
