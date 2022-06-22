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
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_DEPARTMENTS;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY;

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
     * <ul>
     * <li>
     * without DEPARTMENTS:
     *      <p>
     *      USER -> BOSS with NOTIFICATION_BOSS_ALL
     *      </p>
     * </li>
     * <li>
     * with DEPARTMENTS (no SECOND_STAGE_AUTHORITY):
     *      <p>
     *      USER -> DEPARTMENT_HEAD and (BOSS with NOTIFICATION_BOSS_ALL or NOTIFICATION_BOSS_DEPARTMENTS)
     *      DEPARTMENT_HEAD -> OTHER DEPARTMENT_HEADs and (BOSS with NOTIFICATION_BOSS_ALL or NOTIFICATION_BOSS_DEPARTMENTS)
     *      </p>
     * </li>
     * <li>
     * with DEPARTMENTS (with SECOND_STAGE_AUTHORITY):
     *     <p>
     *     USER -> DEPARTMENT_HEAD and (BOSS with NOTIFICATION_BOSS_ALL or NOTIFICATION_BOSS_DEPARTMENTS)
     *     DEPARTMENT_HEAD -> OTHER DEPARTMENT_HEADs and SECOND_STAGE_AUTHORITY and (BOSS with NOTIFICATION_BOSS_ALL or NOTIFICATION_BOSS_DEPARTMENTS)
     *     SECOND_STAGE_AUTHORITY -> (BOSS with NOTIFICATION_BOSS_ALL or NOTIFICATION_BOSS_DEPARTMENTS)
     *     </p>
     * </li>
     * </ul>
     *
     * @param application to find out recipients for
     * @return list of recipients for the given application allow/remind request
     */
    List<Person> getRecipientsOfInterest(Application application) {

        final List<Person> recipientsOfInterest = new ArrayList<>(personService.getActivePersonsWithNotificationType(NOTIFICATION_BOSS_ALL));

        final long numberOfDepartments = departmentService.getNumberOfDepartments();
        if (numberOfDepartments > 0) {
            final Person applicationPerson = application.getPerson();
            recipientsOfInterest.addAll(getBossesWithDepartmentNotification(applicationPerson));
            recipientsOfInterest.addAll(getResponsibleSecondStageAuthorities(applicationPerson));
            recipientsOfInterest.addAll(getResponsibleDepartmentHeads(applicationPerson));
        }

        return recipientsOfInterest.stream()
            .distinct()
            .collect(toList());
    }

    /**
     * Get all persons with the office notification enabled.
     *
     * @return list of recipients with NOTIFICATION_OFFICE
     */
    List<Person> getRecipientsWithOfficeNotifications() {
        return personService.getActivePersonsWithNotificationType(NOTIFICATION_OFFICE);
    }

    private List<Person> getResponsibleSecondStageAuthorities(Person applicationPerson) {
        return personService.getActivePersonsWithNotificationType(NOTIFICATION_SECOND_STAGE_AUTHORITY)
            .stream()
            .filter(secondStageAuthority -> departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, applicationPerson))
            .filter(without(applicationPerson))
            .collect(toList());
    }

    private static Predicate<Person> without(Person applicationPerson) {
        return person -> !person.equals(applicationPerson);
    }

    private List<Person> getResponsibleDepartmentHeads(Person applicationPerson) {
        return personService.getActivePersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD)
            .stream()
            .filter(departmentHead -> departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, applicationPerson))
            .filter(without(applicationPerson))
            .collect(toList());
    }

    private List<Person> getBossesWithDepartmentNotification(Person applicationPerson) {
        final List<Department> applicationPersonDepartments = departmentService.getAssignedDepartmentsOfMember(applicationPerson);

        return personService.getActivePersonsWithNotificationType(NOTIFICATION_BOSS_DEPARTMENTS).stream()
            .filter(boss -> {
                final List<Department> bossDepartments = departmentService.getAssignedDepartmentsOfMember(boss);
                return applicationPersonDepartments.stream().anyMatch(bossDepartments::contains);
            })
            .collect(toList());
    }
}
