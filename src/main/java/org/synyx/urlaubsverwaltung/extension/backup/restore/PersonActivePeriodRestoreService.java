package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.PersonActivePeriodDTO;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonActivePeriodImportService;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnBackupRestoreEnabled
class PersonActivePeriodRestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonActivePeriodImportService personActivePeriodImportService;
    private final PersonService personService;

    PersonActivePeriodRestoreService(PersonActivePeriodImportService personActivePeriodImportService, PersonService personService) {
        this.personActivePeriodImportService = personActivePeriodImportService;
        this.personService = personService;
    }

    void restore(Collection<PersonActivePeriodDTO> personActivePeriodDTOs) {

        int restored = 0;
        int skipped = 0;

        final Map<String, Person> personByExternalId = personService.getAllPersons().stream()
            .collect(toMap(Person::getUsername, Function.identity()));

        for (PersonActivePeriodDTO dto : personActivePeriodDTOs) {
            final Person person = personByExternalId.get(dto.personExternalId());
            if (person == null) {
                LOG.warn("personActivePeriod person with externalId={} not found - skip importing personActivePeriod!", dto.personExternalId());
                skipped++;
            } else {
                personActivePeriodImportService.importPersonActivePeriod(dto.toEntity(person));
                restored++;
            }
        }

        LOG.info("Restored {} personActivePeriods. Skipped {} personActivePeriods.", restored, skipped);
    }
}
