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

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT;
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
     * Get all second stage authorities that must be notified about the given temporary allowed application.
     *
     * @param application that has been allowed temporary
     * @return list of recipients for the given temporary allowed application
     */
    List<Person> getRecipientsForTemporaryAllow(Application application) {
        return getResponsibleSecondStageAuthorities(application.getPerson(), List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT));
    }

    /**
     * Returns a list of recipients of interest for a given person based on
     * <ul>
     *     <li>is Office and {@link MailNotification#NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL} or one of the given mail notifications is active</li>
     *     <li>is Boss and {@link MailNotification#NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL}</li>
     *     <li>is responsible Department Head and {@link MailNotification#NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT}</li>
     *     <li>is responsible Department Head and {@link MailNotification#NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT}</li>
     *     <li>is Boss in the same Department and {@link MailNotification#NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT}</li>
     * </ul>
     *
     * @param personOfInterest person to get recipients from
     * @return list of recipients of interest
     */
    List<Person> getRecipientsOfInterest(Person personOfInterest) {
        return getRecipientsOfInterest(personOfInterest, List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT));
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

        final List<Person> recipientsOfInterest = new ArrayList<>();
        recipientsOfInterest.addAll(getOfficesWithApplicationManagementAllNotification(mailNotifications));
        recipientsOfInterest.addAll(getBossWithApplicationManagementAllNotification(mailNotifications));

        final long numberOfDepartments = departmentService.getNumberOfDepartments();
        if (numberOfDepartments > 0) {
            recipientsOfInterest.addAll(getResponsibleSecondStageAuthorities(personOfInterest, mailNotifications));
            recipientsOfInterest.addAll(getResponsibleDepartmentHeads(personOfInterest, mailNotifications));
            recipientsOfInterest.addAll(getBossesWithDepartmentApplicationManagementNotification(personOfInterest, mailNotifications));
        }

        return recipientsOfInterest.stream()
            .distinct()
            .collect(toList());
    }

    private List<Person> getResponsibleSecondStageAuthorities(Person personOfInterest, List<MailNotification> concerningMailNotifications) {
        return personService.getActivePersonsByRole(SECOND_STAGE_AUTHORITY)
            .stream()
            .filter(containsAny(concerningMailNotifications))
            .filter(secondStageAuthority -> departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, personOfInterest))
            .filter(without(personOfInterest))
            .collect(toList());
    }

    private List<Person> getResponsibleDepartmentHeads(Person personOfInterest, List<MailNotification> concerningMailNotifications) {
        return personService.getActivePersonsByRole(DEPARTMENT_HEAD)
            .stream()
            .filter(containsAny(concerningMailNotifications))
            .filter(departmentHead -> departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, personOfInterest))
            .filter(without(personOfInterest))
            .collect(toList());
    }

    private List<Person> getBossesWithDepartmentApplicationManagementNotification(Person personOfInterest, List<MailNotification> concerningMailNotifications) {
        final List<Department> applicationPersonDepartments = departmentService.getAssignedDepartmentsOfMember(personOfInterest);

        return personService.getActivePersonsByRole(BOSS).stream()
            .filter(containsAny(concerningMailNotifications))
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
}
