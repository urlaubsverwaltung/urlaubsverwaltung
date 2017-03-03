package org.synyx.urlaubsverwaltung.core.mail;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Provides functionality to get the correct mail recipients for different use cases.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
class RecipientService {

    private final PersonService personService;
    private final DepartmentService departmentService;

    @Autowired
    RecipientService(PersonService personService, DepartmentService departmentService) {

        this.personService = personService;
        this.departmentService = departmentService;
    }

    /**
     * Get all persons with the given notification type.
     *
     * @param  notification  to get all persons for
     *
     * @return  list of recipients with the given notification type
     */
    List<Person> getRecipientsWithNotificationType(MailNotification notification) {

        return personService.getPersonsWithNotificationType(notification);
    }

    /**
     * Get all second stage authorities that must be notified about the given temporary allowed application.
     *
     * @param  application  that has been allowed temporary
     *
     * @return  list of recipients for the given temporary allowed application
     */
    List<Person> getRecipientsForTemporaryAllow(Application application) {
        return getResponsibleSecondStageAuthorities(application.getPerson());
    }


    /**
     * Depending on application issuer role the recipients for allow/remind mail are generated.
     *
     * <p>USER -> DEPARTMENT_HEAD DEPARTMENT_HEAD -> SECOND_STAGE_AUTHORITY, BOSS SECOND_STAGE_AUTHORITY -> BOSS</p>
     *
     * @param  application  to find out recipients for
     *
     * @return  list of recipients for the given application allow/remind request
     */
    List<Person> getRecipientsForAllowAndRemind(Application application) {
        /**
         * NOTE:
         *
         * It's not possible that someone has both roles,
         * {@link Role.BOSS} and ({@link Role.DEPARTMENT_HEAD} or {@link Role.SECOND_STAGE_AUTHORITY})
         *
         * Thus no need to use a {@link java.util.Set} to avoid person duplicates within the returned list.
         */

        Person applicationPerson = application.getPerson();

        List<Person> bosses = personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);

        if (applicationPerson.hasRole(Role.SECOND_STAGE_AUTHORITY)) {
            return bosses;
        }

        if (applicationPerson.hasRole(Role.DEPARTMENT_HEAD)) {
            List<Person> secondStageAuthorities = getResponsibleSecondStageAuthorities(applicationPerson);
            List<Person> responsibleDepartmentHeads = getResponsibleDepartmentHeads(applicationPerson);
            return concat(bosses, secondStageAuthorities, responsibleDepartmentHeads);
        }

        //boss and user
        List<Person> responsibleDepartmentHeads = getResponsibleDepartmentHeads(applicationPerson);
        return concat(bosses, responsibleDepartmentHeads);
    }

    private static List<Person> concat(List<Person> list1, List<Person> list2) {
        return Stream.concat(list1.stream(), list2.stream()).collect(Collectors.toList());
    }

    private static List<Person> concat(List<Person> list1, List<Person> list2, List<Person> list3) {
        return concat(concat(list1, list2), list3);
    }

    private List<Person> getResponsibleSecondStageAuthorities(Person applicationPerson) {
        Predicate<Person> responsibleSecondStageAuthority = secondStageAuthority ->
                departmentService.isSecondStageAuthorityOfPerson(secondStageAuthority, applicationPerson);

        return personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY)
                .stream()
                .filter(responsibleSecondStageAuthority)
                .filter(without(applicationPerson))
                .collect(Collectors.toList());
    }

    private static Predicate<Person> without(Person applicationPerson) {
        return person -> !person.equals(applicationPerson);
    }

    private List<Person> getResponsibleDepartmentHeads(Person applicationPerson) {
        Predicate<Person> responsibleDepartmentHeads = departmentHead ->
                departmentService.isDepartmentHeadOfPerson(departmentHead, applicationPerson);

        return personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD)
                    .stream()
                    .filter(responsibleDepartmentHeads)
                    .filter(without(applicationPerson))
                    .collect(Collectors.toList());
    }

}
