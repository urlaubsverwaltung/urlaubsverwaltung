package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.extension.backup.model.PersonActivePeriodDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.PersonDTO;
import org.synyx.urlaubsverwaltung.person.PersonActivePeriodService;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@ConditionalOnBackupCreateEnabled
class PersonActivePeriodDataCollectionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonActivePeriodService personActivePeriodService;

    PersonActivePeriodDataCollectionService(PersonActivePeriodService personActivePeriodService) {
        this.personActivePeriodService = personActivePeriodService;
    }

    List<PersonActivePeriodDTO> collectPersonActivePeriods(Function<PersonId, PersonDTO> personDtoById) {
        return personActivePeriodService.getAllActivePeriods().stream()
            .map(period -> {
                final PersonDTO person = personDtoById.apply(period.personId());
                if (person == null) {
                    LOG.warn("Skip collecting personActivePeriod because could not find person id={} for {}", period.personId(), period);
                    return null;
                } else {
                    return new PersonActivePeriodDTO(person.externalId(), period.validFrom(), period.validTo().orElse(null));
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
