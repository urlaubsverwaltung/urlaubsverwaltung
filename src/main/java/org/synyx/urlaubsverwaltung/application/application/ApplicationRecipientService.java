package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_DEPARTMENTS;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
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
     * Depending on application issuer role the recipients for allowed/remind/rejected/revoked/cancelled mail are
     * generated.
     * <p>
     * without DEPARTMENTS:
     * USER -> BOSS with NOTIFICATION_BOSS_ALL
     * <p>
     * with DEPARTMENTS (no SECOND_STAGE_AUTHORITY):
     * USER -> DEPARTMENT_HEAD and (BOSS with NOTIFICATION_BOSS_ALL or NOTIFICATION_BOSS_DEPARTMENTS)
     * DEPARTMENT_HEAD -> OTHER DEPARTMENT_HEADs and (BOSS with NOTIFICATION_BOSS_ALL or NOTIFICATION_BOSS_DEPARTMENTS)
     * <p>
     * with DEPARTMENTS (with SECOND_STAGE_AUTHORITY):
     * USER -> DEPARTMENT_HEAD and (BOSS with NOTIFICATION_BOSS_ALL or NOTIFICATION_BOSS_DEPARTMENTS)
     * DEPARTMENT_HEAD -> OTHER DEPARTMENT_HEADs and SECOND_STAGE_AUTHORITY and (BOSS with NOTIFICATION_BOSS_ALL or NOTIFICATION_BOSS_DEPARTMENTS)
     * SECOND_STAGE_AUTHORITY -> (BOSS with NOTIFICATION_BOSS_ALL or NOTIFICATION_BOSS_DEPARTMENTS)
     *
     * @param application to find out recipients for
     * @return list of recipients for the given application allow/remind request
     */
    List<Person> getRecipientsOfInterest(Application application) {

        /*
         * NOTE:
         *
         * It's not possible that someone has both roles,
         * {@link Role.BOSS} and ({@link Role.DEPARTMENT_HEAD} or {@link Role.SECOND_STAGE_AUTHORITY})
         *
         * Thus no need to use a {@link java.util.Set} to avoid person duplicates within the returned list.
         */

        final Person applicationPerson = application.getPerson();
        final List<Person> bosses = personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL);

        final List<Person> relevantBosses =
            personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_DEPARTMENTS).stream()
                .filter(bossesForDepartmentOf(applicationPerson))
                .collect(toList());

        if (applicationPerson.hasRole(SECOND_STAGE_AUTHORITY)) {
            return concat(bosses, relevantBosses);
        }

        if (applicationPerson.hasRole(DEPARTMENT_HEAD)) {
            List<Person> secondStageAuthorities = getResponsibleSecondStageAuthorities(applicationPerson);
            List<Person> responsibleDepartmentHeads = getResponsibleDepartmentHeads(applicationPerson);
            return concat(bosses, relevantBosses, secondStageAuthorities, responsibleDepartmentHeads);
        }

        // boss and user
        List<Person> responsibleDepartmentHeads = getResponsibleDepartmentHeads(applicationPerson);
        return concat(bosses, relevantBosses, responsibleDepartmentHeads);
    }

    /**
     * Get all persons with the office notification enabled.
     *
     * @return list of recipients with NOTIFICATION_OFFICE
     */
    List<Person> getRecipientsWithOfficeNotifications() {
        return personService.getPersonsWithNotificationType(NOTIFICATION_OFFICE);
    }

    private Predicate<Person> bossesForDepartmentOf(Person applicationPerson) {
        return boss ->
            departmentService.getAssignedDepartmentsOfMember(applicationPerson).stream()
                .anyMatch(depOfAssignedMember -> departmentService.getAssignedDepartmentsOfMember(boss).contains(depOfAssignedMember));
    }

    private static List<Person> concat(List<Person> list1, List<Person> list2) {
        return Stream.concat(list1.stream(), list2.stream()).collect(toList());
    }

    private static List<Person> concat(List<Person> list1, List<Person> list2, List<Person> list3) {
        return concat(concat(list1, list2), list3);
    }

    private static List<Person> concat(List<Person> list1, List<Person> list2, List<Person> list3, List<Person> list4) {
        return concat(concat(list1, list2), concat(list3, list4));
    }

    private List<Person> getResponsibleSecondStageAuthorities(Person applicationPerson) {
        Predicate<Person> responsibleSecondStageAuthority = secondStageAuthority ->
            departmentService.isSecondStageAuthorityOfPerson(secondStageAuthority, applicationPerson);

        return personService.getPersonsWithNotificationType(NOTIFICATION_SECOND_STAGE_AUTHORITY)
            .stream()
            .filter(responsibleSecondStageAuthority)
            .filter(without(applicationPerson))
            .collect(toList());
    }

    private static Predicate<Person> without(Person applicationPerson) {
        return person -> !person.equals(applicationPerson);
    }

    private List<Person> getResponsibleDepartmentHeads(Person applicationPerson) {
        Predicate<Person> responsibleDepartmentHeads = departmentHead ->
            departmentService.isDepartmentHeadOfPerson(departmentHead, applicationPerson);

        return personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD)
            .stream()
            .filter(responsibleDepartmentHeads)
            .filter(without(applicationPerson))
            .collect(toList());
    }
}
