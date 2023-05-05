package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Service
class MailRecipientServiceImpl implements MailRecipientService {

    private final PersonService personService;
    private final DepartmentService departmentService;

    @Autowired
    MailRecipientServiceImpl(PersonService personService, DepartmentService departmentService) {
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
            .collect(toList());
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
                recipientsOfInterestForDepartment.addAll(getResponsibleDepartmentHeads(personOfInterest));
            }

            if (mailNotification.isValidWith(List.of(USER, SECOND_STAGE_AUTHORITY))) {
                recipientsOfInterestForDepartment.addAll(getResponsibleSecondStageAuthorities(personOfInterest));
            }

            managementDepartmentPersons = recipientsOfInterestForDepartment.stream()
                .distinct()
                .filter(person -> person.getNotifications().contains(mailNotification));
        }

        return Stream.concat(recipientsOfInterestForAll.stream(), managementDepartmentPersons)
            .distinct()
            .collect(toList());
    }

    private List<Person> getResponsibleSecondStageAuthorities(Person personOfInterest) {
        return personService.getActivePersonsByRole(SECOND_STAGE_AUTHORITY)
            .stream()
            .filter(secondStageAuthority -> departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, personOfInterest))
            .filter(without(personOfInterest))
            .collect(toList());
    }

    private List<Person> getResponsibleDepartmentHeads(Person personOfInterest) {
        return personService.getActivePersonsByRole(DEPARTMENT_HEAD)
            .stream()
            .filter(departmentHead -> departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, personOfInterest))
            .filter(without(personOfInterest))
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

    private static Predicate<Person> without(Person personOfInterest) {
        return person -> !person.equals(personOfInterest);
    }

    private boolean departmentsAvailable() {
        return departmentService.getNumberOfDepartments() > 0;
    }
}
