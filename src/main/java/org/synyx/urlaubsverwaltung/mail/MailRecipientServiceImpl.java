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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@Service
class MailRecipientServiceImpl implements MailRecipientService {

    private final ResponsiblePersonService responsiblePersonService;
    private final PersonService personService;
    private final DepartmentService departmentService;
    private final UserNotificationSettingsService userNotificationSettingsService;

    @Autowired
    MailRecipientServiceImpl(
        ResponsiblePersonService responsiblePersonService, PersonService personService,
        DepartmentService departmentService, UserNotificationSettingsService userNotificationSettingsService
    ) {
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
        return getRecipientsOfInterest(List.of(personOfInterest), mailNotification).get(personOfInterest);
    }

    @Override
    public Map<Person, List<Person>> getRecipientsOfInterest(List<Person> personsOfInterest, MailNotification mailNotification) {

        final List<Person> distinctPersonsOfInterest = personsOfInterest.stream().distinct().toList();
        if (distinctPersonsOfInterest.isEmpty()) {
            return Map.of();
        }

        final List<Person> officeAndBosses = new ArrayList<>();
        if (mailNotification.isValidWith(List.of(USER, OFFICE))) {
            officeAndBosses.addAll(getOfficeWith(mailNotification));
        }
        if (mailNotification.isValidWith(List.of(USER, BOSS, APPLICATION_CANCELLATION_REQUESTED, SICK_NOTE_ADD, SICK_NOTE_EDIT))) {
            officeAndBosses.addAll(getBossWith(mailNotification));
        }
        final Map<Person, List<Person>> interestedOfficeAndBosses = getOfficeBossWithDepartmentMatch(distinctPersonsOfInterest, officeAndBosses);

        final Map<Person, List<Person>> departmentHeads = mailNotification.isValidWith(List.of(USER, DEPARTMENT_HEAD, APPLICATION_CANCELLATION_REQUESTED, SICK_NOTE_ADD, SICK_NOTE_EDIT))
            ? getResponsibleDepartmentHeads(distinctPersonsOfInterest, mailNotification)
            : Map.of();
        final Map<Person, List<Person>> secondStageAuthorities = mailNotification.isValidWith(List.of(USER, SECOND_STAGE_AUTHORITY, APPLICATION_CANCELLATION_REQUESTED, SICK_NOTE_ADD, SICK_NOTE_EDIT))
            ? getResponsibleSecondStageAuthorities(distinctPersonsOfInterest, mailNotification)
            : Map.of();

        final Map<Person, List<Person>> recipientsByPersonOfInterest = new HashMap<>();
        for (Person personOfInterest : distinctPersonsOfInterest) {
            final List<Person> recipients = Stream.of(
                    interestedOfficeAndBosses.getOrDefault(personOfInterest, List.of()),
                    departmentHeads.getOrDefault(personOfInterest, List.of()),
                    secondStageAuthorities.getOrDefault(personOfInterest, List.of()))
                .flatMap(List::stream)
                .filter(recipient -> recipient.getNotifications().contains(mailNotification))
                .filter(not(isEqual(personOfInterest)))
                .distinct()
                .toList();
            recipientsByPersonOfInterest.put(personOfInterest, recipients);
        }

        return recipientsByPersonOfInterest;
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

    private Map<Person, List<Person>> getOfficeBossWithDepartmentMatch(List<Person> personsOfInterest, List<Person> officeAndBosses) {

        final List<Person> distinctOfficesAndBosses = officeAndBosses.stream().distinct().toList();

        if (noDepartmentsAvailable()) {
            return personsOfInterest.stream().collect(toMap(identity(), _ -> distinctOfficesAndBosses));
        }

        final List<PersonId> officeBossIds = distinctOfficesAndBosses.stream().map(Person::getId).map(PersonId::new).toList();
        final Set<PersonId> restrictedToDepartmentsIds = userNotificationSettingsService.findNotificationSettings(officeBossIds).values().stream()
            .filter(UserNotificationSettings::restrictToDepartments)
            .map(UserNotificationSettings::personId)
            .collect(toSet());

        final List<Person> restrictedToDepartments = distinctOfficesAndBosses.stream()
            .filter(person -> restrictedToDepartmentsIds.contains(person.getIdAsPersonId()))
            .toList();
        final Map<Person, List<Person>> restrictedWithDepartmentMatch = restrictedToDepartments.isEmpty()
            ? Map.of()
            : departmentService.getPersonsWithDepartmentMatch(restrictedToDepartments, personsOfInterest);

        return personsOfInterest.stream().collect(toMap(identity(), personOfInterest -> distinctOfficesAndBosses.stream()
            .filter(person -> !restrictedToDepartmentsIds.contains(person.getIdAsPersonId())
                || restrictedWithDepartmentMatch.getOrDefault(personOfInterest, List.of()).contains(person))
            .toList()));
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

    private Map<Person, List<Person>> getResponsibleSecondStageAuthorities(List<Person> personsOfInterest, MailNotification concerningMailNotification) {
        return withNotification(responsiblePersonService.getResponsibleSecondStageAuthorities(personsOfInterest), concerningMailNotification);
    }

    private Map<Person, List<Person>> getResponsibleDepartmentHeads(List<Person> personsOfInterest, MailNotification concerningMailNotification) {
        return withNotification(responsiblePersonService.getResponsibleDepartmentHeads(personsOfInterest), concerningMailNotification);
    }

    private static Map<Person, List<Person>> withNotification(Map<Person, List<Person>> managersByPerson, MailNotification concerningMailNotification) {
        final Map<Person, List<Person>> managersWithNotification = new HashMap<>();
        managersByPerson.forEach((person, managers) -> managersWithNotification.put(person, managers.stream()
            .filter(manager -> manager.getNotifications().contains(concerningMailNotification))
            .filter(manager -> concerningMailNotification.isValidWith(manager.getPermissions()))
            .toList()));
        return managersWithNotification;
    }

    private boolean noDepartmentsAvailable() {
        return departmentService.getNumberOfDepartments() <= 0;
    }
}
