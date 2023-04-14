package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

@Service
class ApplicationRecipientService {

    private final PersonService personService;
    private final DepartmentService departmentService;

    @Autowired
    ApplicationRecipientService(PersonService personService, DepartmentService departmentService) {
        this.personService = personService;
        this.departmentService = departmentService;
    }

    /**
     * Returns all responsible managers of the given person.
     * Managers are:
     * <ul>
     *     <li>bosses</li>
     *     <li>department heads</li>
     *     <li>second stage authorities.</li>
     * </ul>
     *
     * @param personOfInterest person to get managers from
     * @return list of all responsible managers
     */
    List<Person> getResponsibleManagersOf(Person personOfInterest) {
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

    /**
     * Returns a list of recipients of interest for a given person based on
     * <ul>
     *     <li>is Office and {@link MailNotification#NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL} or the given mail notification is active</li>
     *     <li>is Boss and {@link MailNotification#NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL} or the given mail notification is active</li>
     *     <li>is responsible Department Head and the given mail notification is active</li>
     *     <li>is responsible Second Stage Authority and the given mail notification is active</li>
     *     <li>is Boss in the same Department and the given mail notification is active</li>
     * </ul>
     *
     * @param personOfInterest person to get recipients from
     * @param mailNotification given notification that one of must be active
     * @return list of recipients of interest
     */
    List<Person> getRecipientsOfInterest(Person personOfInterest, MailNotification mailNotification) {
        return getRecipientsOfInterest(personOfInterest, List.of(mailNotification));
    }

    /**
     * Returns a list of recipients of interest for a given person based on
     * <ul>
     *     <li>is Office and {@link MailNotification#NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL} or one of the given mail notifications is active</li>
     *     <li>is Boss and {@link MailNotification#NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL} or one of the given mail notifications is active</li>
     *     <li>is responsible Department Head and one of the given mail notifications is active</li>
     *     <li>is responsible Second Stage Authority and one of the given mail notifications is active</li>
     *     <li>is Boss in the same Department and one of the given mail notifications is active</li>
     * </ul>
     *
     * @param personOfInterest  person to get recipients from
     * @param mailNotifications given notifications that one of must be active
     * @return list of recipients of interest
     */
    List<Person> getRecipientsOfInterest(Person personOfInterest, List<MailNotification> mailNotifications) {

        final List<Person> recipientsOfInterestForAll = new ArrayList<>();
        recipientsOfInterestForAll.addAll(getOfficesWithApplicationManagementAllNotification(mailNotifications));
        recipientsOfInterestForAll.addAll(getBossWithApplicationManagementAllNotification(mailNotifications));

        Stream<Person> managementDepartmentPersons = Stream.of();
        if (departmentsAvailable()) {
            final List<Person> recipientsOfInterestForDepartment = new ArrayList<>();
            recipientsOfInterestForDepartment.addAll(getResponsibleDepartmentHeads(personOfInterest));
            recipientsOfInterestForDepartment.addAll(getResponsibleSecondStageAuthorities(personOfInterest));
            recipientsOfInterestForDepartment.addAll(getBossesWithDepartmentApplicationManagementNotification(personOfInterest));

            managementDepartmentPersons = recipientsOfInterestForDepartment.stream()
                .distinct()
                .filter(containsAny(mailNotifications));
        }

        return Stream.concat(recipientsOfInterestForAll.stream(), managementDepartmentPersons)
            .distinct()
            .collect(toList());
    }

    /**
     * Get all responsible second stage authorities that must be notified and
     * have the given mail notification activated
     *
     * @param personOfInterest to retrieve the responsible second stage authorities from
     * @return list of responsible second stage authorities
     */
    List<Person> getResponsibleSecondStageAuthorities(Person personOfInterest, MailNotification mailNotification) {
        return getResponsibleSecondStageAuthorities(personOfInterest).stream()
            .filter(containsAny(List.of(mailNotification)))
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

    private List<Person> getBossesWithDepartmentApplicationManagementNotification(Person personOfInterest) {
        final List<Department> applicationPersonDepartments = departmentService.getAssignedDepartmentsOfMember(personOfInterest);
        return personService.getActivePersonsByRole(BOSS).stream()
            .filter(boss -> {
                final List<Department> bossDepartments = departmentService.getAssignedDepartmentsOfMember(boss);
                return applicationPersonDepartments.stream().anyMatch(bossDepartments::contains);
            })
            .collect(toList());
    }

    private List<Person> getBossWithApplicationManagementAllNotification(List<MailNotification> concerningMailNotifications) {
        return personService.getActivePersonsByRole(BOSS).stream()
            .filter(boss -> boss.getNotifications().contains(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL) || boss.getNotifications().stream().anyMatch(concerningMailNotifications::contains))
            .collect(toList());
    }

    private List<Person> getOfficesWithApplicationManagementAllNotification(List<MailNotification> concerningMailNotifications) {
        return personService.getActivePersonsByRole(OFFICE).stream()
            .filter(office -> office.getNotifications().contains(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL) || office.getNotifications().stream().anyMatch(concerningMailNotifications::contains))
            .collect(toList());
    }

    private static Predicate<Person> without(Person personOfInterest) {
        return person -> !person.equals(personOfInterest);
    }

    private static Predicate<Person> containsAny(List<MailNotification> concerningMailNotifications) {
        return person -> person.getNotifications().stream().anyMatch(concerningMailNotifications::contains);
    }

    private boolean departmentsAvailable() {
        return departmentService.getNumberOfDepartments() > 0;
    }
}
