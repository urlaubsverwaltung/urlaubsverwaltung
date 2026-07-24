package org.synyx.urlaubsverwaltung.person;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class PersonActivePeriodImportService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonActivePeriodRepository repository;

    PersonActivePeriodImportService(PersonActivePeriodRepository repository) {
        this.repository = repository;
    }

    public void importPersonActivePeriod(PersonActivePeriodEntity entity) {
        repository.save(entity);
        LOG.debug("imported person active period: {}", entity);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
