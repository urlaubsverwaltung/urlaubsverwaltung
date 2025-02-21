package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.extension.backup.model.AccountDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.DayLengthDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.FederalStateDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.MailNotificationDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.PersonBaseDataDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.PersonDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.RoleDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.ThemeDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.UserNotificationSettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.UserPaginationSettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.UserSettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.WorkingTimeDTO;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettingsService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.user.UserSettings;
import org.synyx.urlaubsverwaltung.user.UserSettingsService;
import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettingsSupplier;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.util.List;

@Service
@ConditionalOnBackupCreateEnabled
class PersonDataCollectionService {

    private final PersonBasedataService personBasedataService;
    private final WorkingTimeService workingTimeService;
    private final UserNotificationSettingsService userNotificationSettingsService;
    private final UserPaginationSettingsSupplier userPaginationSettingsSupplier;
    private final UserSettingsService userSettingsService;
    private final AccountService accountService;

    PersonDataCollectionService(PersonBasedataService personBasedataService, AccountService accountService, WorkingTimeService workingTimeService, UserNotificationSettingsService userNotificationSettingsService, UserPaginationSettingsSupplier userPaginationSettingsSupplier, UserSettingsService userSettingsService) {
        this.personBasedataService = personBasedataService;
        this.accountService = accountService;
        this.workingTimeService = workingTimeService;
        this.userNotificationSettingsService = userNotificationSettingsService;
        this.userPaginationSettingsSupplier = userPaginationSettingsSupplier;
        this.userSettingsService = userSettingsService;
    }

    List<PersonDTO> collectPersons(List<Person> persons) {
        return persons.stream().map(person -> {

            final List<RoleDTO> permissions = person.getPermissions().stream().map(role -> RoleDTO.valueOf(role.name())).toList();
            final List<MailNotificationDTO> mailNotificationDTOS = person.getNotifications().stream().map(notification -> MailNotificationDTO.valueOf(notification.name())).toList();
            final PersonBaseDataDTO personBaseDataDTO = personBasedataService.getBasedataByPersonId(person.getId()).map(personBasedata -> new PersonBaseDataDTO(personBasedata.personnelNumber(), personBasedata.additionalInformation())).orElse(null);

            final List<AccountDTO> accountDTOS = accountService.getHolidaysAccountsByPerson(person).stream().map(account -> new AccountDTO(account.getValidFrom(), account.getValidTo(), account.isDoRemainingVacationDaysExpireLocally(), account.isDoRemainingVacationDaysExpireGlobally(), account.getExpiryDateLocally(), account.getExpiryDateGlobally(), account.getExpiryNotificationSentDate(), account.getAnnualVacationDays(), account.getActualVacationDays(), account.getRemainingVacationDays(), account.getRemainingVacationDaysNotExpiring(), account.getComment())).toList();
            final List<WorkingTimeDTO> workingTimeDTOS = workingTimeService.getByPerson(person).stream().map(workingTime -> new WorkingTimeDTO(DayLengthDTO.valueOf(workingTime.getMonday().name()), DayLengthDTO.valueOf(workingTime.getTuesday().name()), DayLengthDTO.valueOf(workingTime.getWednesday().name()), DayLengthDTO.valueOf(workingTime.getThursday().name()), DayLengthDTO.valueOf(workingTime.getFriday().name()), DayLengthDTO.valueOf(workingTime.getSaturday().name()), DayLengthDTO.valueOf(workingTime.getSunday().name()), workingTime.getValidFrom(), FederalStateDTO.valueOf(workingTime.getFederalState().name()), workingTime.isDefaultFederalState())).toList();

            final UserNotificationSettingsDTO userNotificationSettingsDTO = new UserNotificationSettingsDTO(userNotificationSettingsService.findNotificationSettings(new PersonId(person.getId())).restrictToDepartments());
            final UserPaginationSettingsDTO userPaginationSettingsDTO = new UserPaginationSettingsDTO(userPaginationSettingsSupplier.getUserPaginationSettings(new PersonId(person.getId())).getDefaultPageSize());
            final UserSettings userSettingsForPerson = userSettingsService.getUserSettingsForPerson(person);
            final UserSettingsDTO userSettings = new UserSettingsDTO(ThemeDTO.valueOf(userSettingsForPerson.theme().name()), userSettingsForPerson.locale().orElse(null), userSettingsForPerson.localeBrowserSpecific().orElse(null), userNotificationSettingsDTO, userPaginationSettingsDTO);

            return new PersonDTO(person.getId(), person.getUsername(), person.getFirstName(), person.getLastName(), person.getEmail(), person.isActive(), permissions, mailNotificationDTOS, personBaseDataDTO, accountDTOS, workingTimeDTOS, userSettings);
        }).toList();
    }

}
