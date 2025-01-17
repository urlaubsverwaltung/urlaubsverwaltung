package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.account.AccountEntity;
import org.synyx.urlaubsverwaltung.account.AccountImportService;
import org.synyx.urlaubsverwaltung.extension.backup.model.AccountDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.PersonBaseDataDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.PersonDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.UserSettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.WorkingTimeDTO;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonImportService;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBaseDataImportService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeImportService;

import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupRestoreEnabled
class PersonRestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final PersonImportService personImportService;
    private final PersonBaseDataImportService personBaseDataImportService;
    private final UserSettingsRestoreService userSettingsRestoreService;
    private final WorkingTimeImportService workingTimeImportService;
    private final AccountImportService accountImportService;

    PersonRestoreService(PersonService personService, PersonImportService personImportService, PersonBaseDataImportService personBaseDataImportService, UserSettingsRestoreService userSettingsRestoreService, WorkingTimeImportService workingTimeImportService, AccountImportService accountImportService) {
        this.personService = personService;
        this.personImportService = personImportService;
        this.personBaseDataImportService = personBaseDataImportService;
        this.userSettingsRestoreService = userSettingsRestoreService;
        this.workingTimeImportService = workingTimeImportService;
        this.accountImportService = accountImportService;
    }

    private void restore(PersonDTO personToRestore) {
        personService.getPersonByMailAddress(personToRestore.email())
            .ifPresentOrElse(person -> LOG.warn("Person with email address {} already exists, skip restoring", personToRestore.email()),
                () -> {
                    Person person = personToRestore.toPerson();
                    LOG.info("Restoring person with username={}", person.getUsername());
                    Person importedPerson = personImportService.importPerson(person);
                    importAccounts(importedPerson, personToRestore.accounts());
                    importWorkingTimes(importedPerson, personToRestore.workingTimes());
                    importPersonBaseData(importedPerson.getId(), personToRestore.personBaseData());
                    importUserSettings(importedPerson.getId(), personToRestore.userSettings());
                });
    }

    private void importUserSettings(Long personId, UserSettingsDTO userSettingsDTO) {
        userSettingsRestoreService.importUserSettings(personId, userSettingsDTO);
    }

    private void importWorkingTimes(Person importedPerson, List<WorkingTimeDTO> workingTimeDTOS) {
        workingTimeImportService.importWorkingTimes(workingTimeDTOS.stream()
            .map(workingTimeDTO -> workingTimeDTO.toWorkingTimeEntity(importedPerson))
            .toList());
    }

    private void importAccounts(Person importedPerson, List<AccountDTO> accounts) {
        final List<AccountEntity> accountsToImport = accounts.stream()
            .map(accountToImport -> accountToImport.toAccountEntity(importedPerson))
            .toList();
        accountImportService.importAccounts(accountsToImport);
    }

    private void importPersonBaseData(Long personId, PersonBaseDataDTO personBaseDataDTO) {
        if (personBaseDataDTO == null) {
            return;
        }
        personBaseDataImportService.importPersonBaseData(personBaseDataDTO.toPersonBaseDataEntity(personId));
    }

    void restore(List<PersonDTO> personsToRestore) {
        personsToRestore.forEach(this::restore);
    }
}
