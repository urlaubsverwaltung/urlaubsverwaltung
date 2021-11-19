package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

@Service
class ApplicationForLeaveStatisticsService {

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;
    private final VacationTypeService vacationTypeService;

    @Autowired
    ApplicationForLeaveStatisticsService(PersonService personService, DepartmentService departmentService,
                                         ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder, VacationTypeService vacationTypeService) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.applicationForLeaveStatisticsBuilder = applicationForLeaveStatisticsBuilder;
        this.vacationTypeService = vacationTypeService;
    }

    List<ApplicationForLeaveStatistics> getStatistics(FilterPeriod period) {
        final List<VacationType> activeVacationTypes = vacationTypeService.getActiveVacationTypes();
        return getRelevantPersons().stream()
            .map(person -> applicationForLeaveStatisticsBuilder.build(person, period.getStartDate(), period.getEndDate(), activeVacationTypes))
            .collect(toList());
    }

    private List<Person> getRelevantPersons() {

        final Person signedInUser = personService.getSignedInUser();

        if (signedInUser.hasRole(BOSS) || signedInUser.hasRole(OFFICE)) {
            return personService.getActivePersons();
        }

        final List<Person> relevantPersons = new ArrayList<>();
        if (signedInUser.hasRole(DEPARTMENT_HEAD)) {
            departmentService.getManagedMembersOfDepartmentHead(signedInUser).stream()
                .filter(person -> !person.hasRole(INACTIVE))
                .collect(toCollection(() -> relevantPersons));
        }

        if (signedInUser.hasRole(SECOND_STAGE_AUTHORITY)) {
            departmentService.getManagedMembersForSecondStageAuthority(signedInUser).stream()
                .filter(person -> !person.hasRole(INACTIVE))
                .collect(toCollection(() -> relevantPersons));
        }

       return relevantPersons.stream()
            .filter(distinctByKey(Person::getId))
            .collect(toList());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
