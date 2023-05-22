package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.ResponsiblePersonService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Service
class MailRecipientServiceImpl implements MailRecipientService {

    private final ResponsiblePersonService responsiblePersonService;
    private final PersonService personService;
    private final DepartmentService departmentService;

    @Autowired
    MailRecipientServiceImpl(ResponsiblePersonService responsiblePersonService, PersonService personService, DepartmentService departmentService) {
        this.responsiblePersonService = responsiblePersonService;
        this.personService = personService;
        this.departmentService = departmentService;
    }

    @Override
    public List<Person> getResponsibleManagersOf(Person personOfInterest) {
        return responsiblePersonService.getResponsibleManagersOf(personOfInterest);
    }

    @Override
    public List<Person> getRecipientsOfInterest(Person personOfInterest, MailNotification mailNotification) {

        final List<Person> recipientsOfInterestForAll = new ArrayList<>();
        if (mailNotification.isValidWith(List.of(USER, OFFICE))) {
            recipientsOfInterestForAll.addAll(getOfficesWithApplicationManagementAllNotification(mailNotification));
        }

        if (mailNotification.isValidWith(List.of(USER, BOSS))) {
            recipientsOfInterestForAll.addAll(getBossWithApplicationManagementAllNotification(mailNotification));
        }

        Stream<Person> managementDepartmentPersons = Stream.of();
        if (departmentsAvailable()) {
            final List<Person> recipientsOfInterestForDepartment = new ArrayList<>();
            if (mailNotification.isValidWith(List.of(USER, DEPARTMENT_HEAD))) {
                recipientsOfInterestForDepartment.addAll(responsiblePersonService.getResponsibleDepartmentHeads(personOfInterest));
            }

            if (mailNotification.isValidWith(List.of(USER, SECOND_STAGE_AUTHORITY))) {
                recipientsOfInterestForDepartment.addAll(responsiblePersonService.getResponsibleSecondStageAuthorities(personOfInterest));
            }

            managementDepartmentPersons = recipientsOfInterestForDepartment.stream()
                .distinct()
                .filter(person -> person.getNotifications().contains(mailNotification));
        }

        return Stream.concat(recipientsOfInterestForAll.stream(), managementDepartmentPersons)
            .distinct()
            .collect(toList());
    }

    @Override
    public List<Person> getColleagues(Person personOfInterest, MailNotification mailNotification) {

        if (!departmentsAvailable()) {
            return personService.getActivePersons().stream()
                .filter(person -> person.getNotifications().contains(mailNotification))
                .filter(not(isEqual(personOfInterest)))
                .collect(toList());
        }

        return departmentService.getAssignedDepartmentsOfMember(personOfInterest).stream()
            .flatMap(department -> department.getMembers().stream()
                .filter(Person::isActive)
                .filter(person -> person.getNotifications().contains(mailNotification))
                .filter(person -> !department.getDepartmentHeads().contains(person))
                .filter(person -> !department.getSecondStageAuthorities().contains(person)))
            .filter(not(isEqual(personOfInterest)))
            .distinct()
            .collect(toList());
    }

    private List<Person> getBossWithApplicationManagementAllNotification(MailNotification concerningMailNotification) {
        return personService.getActivePersonsByRole(BOSS).stream()
            .filter(boss -> boss.getNotifications().contains(concerningMailNotification))
            .collect(toList());
    }

    private List<Person> getOfficesWithApplicationManagementAllNotification(MailNotification concerningMailNotification) {
        return personService.getActivePersonsByRole(OFFICE).stream()
            .filter(office -> office.getNotifications().contains(concerningMailNotification))
            .collect(toList());
    }

    private boolean departmentsAvailable() {
        return departmentService.getNumberOfDepartments() > 0;
    }
}
