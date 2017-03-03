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
     * Depending on application issuer role the recipients for allow/remind mail are generated.
     *
     * <p>USER -> DEPARTMENT_HEAD DEPARTMENT_HEAD -> SECOND_STAGE_AUTHORITY, BOSS SECOND_STAGE_AUTHORITY -> BOSS</p>
     *
     * @param  application  to find out recipients for
     *
     * @return  list of recipients for the given application allow/remind request
     */
    List<Person> getRecipientsForAllowAndRemind(Application application) {

        List<Person> bosses = personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);

        Person applicationPerson = application.getPerson();

        if (applicationPerson.hasRole(Role.SECOND_STAGE_AUTHORITY)) {
            return bosses;
        }

        if (applicationPerson.hasRole(Role.DEPARTMENT_HEAD)) {
            List<Person> secondStageAuthorities = personService.getPersonsWithNotificationType(
                        MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY)
                    .stream()
                    .filter(secondStageAuthority ->
                            departmentService.isSecondStageAuthorityOfPerson(secondStageAuthority, applicationPerson))
                    .collect(Collectors.toList());

            return Stream.concat(bosses.stream(), secondStageAuthorities.stream()).collect(Collectors.toList());
        }

        /**
         * NOTE:
         *
         * It's not possible that someone has both roles,
         * {@link Role.BOSS} and
         * {@link Role.DEPARTMENT_HEAD}.
         *
         * Thus no need to use a {@link java.util.Set} to avoid person duplicates within the returned list.
         */
        List<Person> departmentHeads = personService.getPersonsWithNotificationType(
                    MailNotification.NOTIFICATION_DEPARTMENT_HEAD)
                .stream()
                .filter(departmentHead -> departmentService.isDepartmentHeadOfPerson(departmentHead, applicationPerson))
                .collect(Collectors.toList());

        return Stream.concat(bosses.stream(), departmentHeads.stream()).collect(Collectors.toList());
    }


    /**
     * Get all second stage authorities that must be notified about the given temporary allowed application.
     *
     * @param  application  that has been allowed temporary
     *
     * @return  list of recipients for the given temporary allowed application
     */
    List<Person> getRecipientsForTemporaryAllow(Application application) {

        List<Person> secondStageAuthorities = personService.getPersonsWithNotificationType(
                MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY);

        return secondStageAuthorities.stream()
            .filter(person -> departmentService.isSecondStageAuthorityOfPerson(person, application.getPerson()))
            .collect(Collectors.toList());
    }
}
