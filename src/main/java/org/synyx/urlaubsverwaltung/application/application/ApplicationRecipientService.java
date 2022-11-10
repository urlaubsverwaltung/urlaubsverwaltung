package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
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
        return getResponsibleSecondStageAuthorities(application.getPerson());
    }

    /**
     * Depending on application issuer role the recipients for allowed/remind/rejected/revoked/cancelled mail are generated.
     * If the person has the office role they will get a notification if they have
     * NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL
     * configured.
     * <p>
     * If the person has the boss role they will get a notification if they have
     * NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL or
     * NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT and is in the same department
     * configured.
     * <p>
     * If the person has the department head role they will get a notification if they have
     * NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT and does manage the application person
     * configured.
     * <p>
     * If the person has the second stage authority role they will get a notification if they have
     * NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT and does manage the application person
     * configured.
     *
     * @param application to find out recipients for
     * @return list of recipients for the given application allow/remind request
     */
    List<Person> getRecipientsOfInterest(Application application) {

        final List<Person> recipientsOfInterest = new ArrayList<>();
        recipientsOfInterest.addAll(getOfficesWithApplicationManagementAllNotification());
        recipientsOfInterest.addAll(getBossWithApplicationManagementAllNotification());

        final long numberOfDepartments = departmentService.getNumberOfDepartments();
        if (numberOfDepartments > 0) {
            final Person applicationPerson = application.getPerson();
            recipientsOfInterest.addAll(getResponsibleSecondStageAuthorities(applicationPerson));
            recipientsOfInterest.addAll(getResponsibleDepartmentHeads(applicationPerson));
            recipientsOfInterest.addAll(getBossesWithDepartmentApplicationManagementNotification(applicationPerson));
        }

        return recipientsOfInterest.stream()
            .distinct()
            .collect(toList());
    }

    private List<Person> getResponsibleSecondStageAuthorities(Person applicationPerson) {
        return personService.getActivePersonsByRole(SECOND_STAGE_AUTHORITY)
            .stream()
            .filter(secondStageAuthority -> secondStageAuthority.getNotifications().contains(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT))
            .filter(secondStageAuthority -> departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, applicationPerson))
            .filter(without(applicationPerson))
            .collect(toList());
    }

    private static Predicate<Person> without(Person applicationPerson) {
        return person -> !person.equals(applicationPerson);
    }

    private List<Person> getResponsibleDepartmentHeads(Person applicationPerson) {
        return personService.getActivePersonsByRole(DEPARTMENT_HEAD)
            .stream()
            .filter(departmentHead -> departmentHead.getNotifications().contains(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT))
            .filter(departmentHead -> departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, applicationPerson))
            .filter(without(applicationPerson))
            .collect(toList());
    }

    private List<Person> getBossesWithDepartmentApplicationManagementNotification(Person applicationPerson) {
        final List<Department> applicationPersonDepartments = departmentService.getAssignedDepartmentsOfMember(applicationPerson);

        return personService.getActivePersonsByRole(BOSS).stream()
            .filter(boss -> boss.getNotifications().contains(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_DEPARTMENT))
            .filter(boss -> {
                final List<Department> bossDepartments = departmentService.getAssignedDepartmentsOfMember(boss);
                return applicationPersonDepartments.stream().anyMatch(bossDepartments::contains);
            })
            .collect(toList());
    }

    private List<Person> getBossWithApplicationManagementAllNotification() {
        return personService.getActivePersonsByRole(BOSS).stream()
            .filter(office -> office.getNotifications().contains(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL))
            .collect(toList());
    }

    private List<Person> getOfficesWithApplicationManagementAllNotification() {
        return personService.getActivePersonsByRole(OFFICE).stream()
            .filter(office -> office.getNotifications().contains(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL))
            .collect(toList());
    }
}
