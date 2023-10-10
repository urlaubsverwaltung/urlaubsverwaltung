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
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
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
            officeAndBosses.addAll(getOfficesWithApplicationManagementAllNotification(mailNotification));
        }
        if (mailNotification.isValidWith(List.of(USER, BOSS))) {
            officeAndBosses.addAll(getBossWithApplicationManagementAllNotification(mailNotification));
        }

        final List<Person> interestedOfficeAndBosses = getOfficeBossWithDepartmentMatch(personOfInterest, officeAndBosses);

        final List<Person> recipientsOfInterestForDepartment = new ArrayList<>();
        if (mailNotification.isValidWith(List.of(USER, DEPARTMENT_HEAD))) {
            recipientsOfInterestForDepartment.addAll(responsiblePersonService.getResponsibleDepartmentHeads(personOfInterest));
        }
        if (mailNotification.isValidWith(List.of(USER, SECOND_STAGE_AUTHORITY))) {
            recipientsOfInterestForDepartment.addAll(responsiblePersonService.getResponsibleSecondStageAuthorities(personOfInterest));
        }

        return Stream.concat(interestedOfficeAndBosses.stream(), recipientsOfInterestForDepartment.stream())
            .distinct()
            .filter(recipient -> recipient.getNotifications().contains(mailNotification))
            .collect(toList());
    }

    @Override
    public List<Person> getColleagues(Person personOfInterest, MailNotification mailNotification) {

        if (noDepartmentsAvailable()) {
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

    private List<Person> getOfficeBossWithDepartmentMatch(Person personOfInterest, List<Person> officeAndBosses) {

        final List<Person> distinctOfficesAndBosses = officeAndBosses.stream().distinct().toList();

        if (noDepartmentsAvailable()) {
            return distinctOfficesAndBosses;
        }

        final Map<PersonId, Person> byPersonId = distinctOfficesAndBosses.stream().collect(toMap(person -> new PersonId(person.getId()), identity(), (person, person2) -> person));
        final List<PersonId> officeBossIds = distinctOfficesAndBosses.stream().map(Person::getId).map(PersonId::new).toList();
        final Predicate<PersonId> departmentMatch = personId -> departmentService.hasDepartmentMatch(byPersonId.get(personId), personOfInterest);

        final List<PersonId> notInterestedIds = userNotificationSettingsService.findNotificationSettings(officeBossIds).values().stream()
            .filter(UserNotificationSettings::isRestrictToDepartments)
            .map(UserNotificationSettings::getPersonId)
            .filter(not(departmentMatch))
            .toList();

        return distinctOfficesAndBosses.stream()
            .filter(not(person -> notInterestedIds.contains(new PersonId(person.getId()))))
            .toList();
    }

    private List<Person> getBossWithApplicationManagementAllNotification(MailNotification concerningMailNotification) {
        return personService.getActivePersonsByRole(BOSS).stream()
            .filter(boss -> boss.getNotifications().contains(concerningMailNotification))
            .toList();
    }

    private List<Person> getOfficesWithApplicationManagementAllNotification(MailNotification concerningMailNotification) {
        return personService.getActivePersonsByRole(OFFICE).stream()
            .filter(office -> office.getNotifications().contains(concerningMailNotification))
            .toList();
    }

    private boolean noDepartmentsAvailable() {
        return departmentService.getNumberOfDepartments() <= 0;
    }
}
