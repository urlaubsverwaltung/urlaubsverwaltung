package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class BlackoutPeriodImportService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final BlackoutPeriodRepository repository;

    BlackoutPeriodImportService(BlackoutPeriodRepository repository) {
        this.repository = repository;
    }

    public void importBlackoutPeriod(BlackoutPeriodEntity entity) {
        repository.save(entity);
        LOG.debug("imported blackout period: {}", entity);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
