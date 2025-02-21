package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettings;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettingsService;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.ResponsiblePersonService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Service
class MailRecipientServiceImpl implements MailRecipientService {

    private final ResponsiblePersonService responsiblePersonService;
    private final PersonService personService;
    private final DepartmentService departmentService;
    private final UserNotificationSettingsService userNotificationSettingsService;

    @Autowired
    MailRecipientServiceImpl(ResponsiblePersonService responsiblePersonService, PersonService personService,
                             DepartmentService departmentService, UserNotificationSettingsService userNotificationSettingsService) {

        this.responsiblePersonService = responsiblePersonService;
        this.personService = personService;
        this.departmentService = departmentService;
        this.userNotificationSettingsService = userNotificationSettingsService;
    }

    @Override
    public List<Person> getResponsibleManagersOf(Person personOfInterest) {
        return responsiblePersonService.getResponsibleManagersOf(personOfInterest);
    }

    @Override
    public List<Person> getRecipientsOfInterest(Person personOfInterest, MailNotification mailNotification) {

        final List<Person> officeAndBosses = new ArrayList<>();
        if (mailNotification.isValidWith(List.of(USER, OFFICE))) {
            officeAndBosses.addAll(getOfficeWith(mailNotification));
        }
        if (mailNotification.isValidWith(List.of(USER, BOSS, APPLICATION_CANCELLATION_REQUESTED, SICK_NOTE_ADD))) {
            officeAndBosses.addAll(getBossWith(mailNotification));
        }
        final List<Person> interestedOfficeAndBosses = getOfficeBossWithDepartmentMatch(personOfInterest, officeAndBosses);

        final List<Person> recipientsOfInterestForDepartment = new ArrayList<>();
        if (mailNotification.isValidWith(List.of(USER, DEPARTMENT_HEAD, APPLICATION_CANCELLATION_REQUESTED, SICK_NOTE_ADD))) {
            recipientsOfInterestForDepartment.addAll(getResponsibleDepartmentHeads(personOfInterest, mailNotification));
        }
        if (mailNotification.isValidWith(List.of(USER, SECOND_STAGE_AUTHORITY, APPLICATION_CANCELLATION_REQUESTED, SICK_NOTE_ADD))) {
            recipientsOfInterestForDepartment.addAll(getResponsibleSecondStageAuthorities(personOfInterest, mailNotification));
        }

        return Stream.concat(interestedOfficeAndBosses.stream(), recipientsOfInterestForDepartment.stream())
            .distinct()
            .filter(recipient -> recipient.getNotifications().contains(mailNotification))
            .toList();
    }

    @Override
    public List<Person> getColleagues(Person personOfInterest, MailNotification mailNotification) {

        if (noDepartmentsAvailable()) {
            return personService.getActivePersons().stream()
                .filter(person -> person.getNotifications().contains(mailNotification))
                .filter(not(isEqual(personOfInterest)))
                .toList();
        }

        return departmentService.getAssignedDepartmentsOfMember(personOfInterest).stream()
            .flatMap(department -> department.getMembers().stream()
                .filter(Person::isActive)
                .filter(person -> person.getNotifications().contains(mailNotification))
                .filter(person -> !department.getDepartmentHeads().contains(person))
                .filter(person -> !department.getSecondStageAuthorities().contains(person)))
            .filter(not(isEqual(personOfInterest)))
            .distinct()
            .toList();
    }

    private List<Person> getOfficeBossWithDepartmentMatch(Person personOfInterest, List<Person> officeAndBosses) {

        final List<Person> distinctOfficesAndBosses = officeAndBosses.stream().distinct().toList();

        if (noDepartmentsAvailable()) {
            return distinctOfficesAndBosses;
        }

        final Map<PersonId, Person> byPersonId = distinctOfficesAndBosses.stream().collect(toMap(person -> new PersonId(person.getId()), identity(), (person, person2) -> person));
        final List<PersonId> officeBossIds = distinctOfficesAndBosses.stream().map(Person::getId).map(PersonId::new).toList();
        final Predicate<PersonId> departmentMatch = personId -> departmentService.hasDepartmentMatch(byPersonId.get(personId), personOfInterest);

        final List<PersonId> notInterestedIds = userNotificationSettingsService.findNotificationSettings(officeBossIds).values().stream()
            .filter(UserNotificationSettings::restrictToDepartments)
            .map(UserNotificationSettings::personId)
            .filter(not(departmentMatch))
            .toList();

        return distinctOfficesAndBosses.stream()
            .filter(not(person -> notInterestedIds.contains(new PersonId(person.getId()))))
            .toList();
    }

    private List<Person> getOfficeWith(MailNotification concerningMailNotification) {
        return personService.getActivePersonsByRole(OFFICE).stream()
            .filter(office -> office.getNotifications().contains(concerningMailNotification))
            .filter(office -> concerningMailNotification.isValidWith(office.getPermissions()))
            .toList();
    }

    private List<Person> getBossWith(MailNotification concerningMailNotification) {
        return personService.getActivePersonsByRole(BOSS).stream()
            .filter(boss -> boss.getNotifications().contains(concerningMailNotification))
            .filter(boss -> concerningMailNotification.isValidWith(boss.getPermissions()))
            .toList();
    }

    private List<Person> getResponsibleSecondStageAuthorities(Person personOfInterest, MailNotification concerningMailNotification) {
        return responsiblePersonService.getResponsibleSecondStageAuthorities(personOfInterest).stream()
            .filter(departmentHead -> departmentHead.getNotifications().contains(concerningMailNotification))
            .filter(departmentHead -> concerningMailNotification.isValidWith(departmentHead.getPermissions()))
            .toList();
    }

    private List<Person> getResponsibleDepartmentHeads(Person personOfInterest, MailNotification concerningMailNotification) {
        return responsiblePersonService.getResponsibleDepartmentHeads(personOfInterest).stream()
            .filter(departmentHead -> departmentHead.getNotifications().contains(concerningMailNotification))
            .filter(departmentHead -> concerningMailNotification.isValidWith(departmentHead.getPermissions()))
            .toList();
    }

    private boolean noDepartmentsAvailable() {
        return departmentService.getNumberOfDepartments() <= 0;
    }
}
