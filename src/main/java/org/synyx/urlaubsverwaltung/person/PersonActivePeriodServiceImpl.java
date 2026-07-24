package org.synyx.urlaubsverwaltung.person;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.groupingBy;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class PersonActivePeriodServiceImpl implements PersonActivePeriodService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonActivePeriodRepository repository;

    PersonActivePeriodServiceImpl(PersonActivePeriodRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PersonActivePeriod> getActivePeriods(PersonId personId) {
        return repository.findAllByPersonIdOrderByValidFromAsc(personId.value()).stream()
            .map(PersonActivePeriodServiceImpl::toPersonActivePeriod)
            .toList();
    }

    @Override
    public Map<PersonId, List<PersonActivePeriod>> getActivePeriodsOverlapping(Collection<PersonId> personIds, Instant from, Instant to) {

        final List<Long> ids = personIds.stream().map(PersonId::value).toList();

        final Map<PersonId, List<PersonActivePeriod>> map = repository.findAllByPersonIdIsInAndOverlapping(ids, from, to).stream()
            .map(PersonActivePeriodServiceImpl::toPersonActivePeriod)
            .collect(groupingBy(PersonActivePeriod::personId));

        // ensure entries for all personIds, even if they have no overlapping active period
        personIds.forEach(id -> map.putIfAbsent(id, List.of()));

        return map;
    }

    @Override
    public List<PersonActivePeriod> getAllActivePeriods() {
        return repository.findAll().stream()
            .map(PersonActivePeriodServiceImpl::toPersonActivePeriod)
            .toList();
    }

    /**
     * Opens a new active period for the given person.
     *
     * @param personId the ID of the person becoming active
     * @param validFrom the point in time the person became active
     * @throws PersonActivePeriodInconsistentStateException if the person already has an open active period
     */
    void openPeriod(PersonId personId, Instant validFrom) {

        final Optional<PersonActivePeriodEntity> existingOpenPeriod = repository.findByPersonIdAndValidToIsNull(personId.value());
        if (existingOpenPeriod.isPresent()) {
            throw new PersonActivePeriodInconsistentStateException(
                "Person with id=%d already has an open active period starting at %s. Cannot open a new one at %s."
                    .formatted(personId.value(), existingOpenPeriod.get().getValidFrom(), validFrom));
        }

        final PersonActivePeriodEntity entity = new PersonActivePeriodEntity();
        entity.setPersonId(personId.value());
        entity.setValidFrom(validFrom);
        repository.save(entity);

        LOG.info("Opened active period for person with id={} starting at {}", personId.value(), validFrom);
    }

    /**
     * Closes the currently open active period of the given person.
     *
     * @param personId the ID of the person becoming inactive
     * @param validTo the point in time the person became inactive
     * @throws PersonActivePeriodInconsistentStateException if the person has no open active period
     */
    void closeOpenPeriod(PersonId personId, Instant validTo) {

        final Optional<PersonActivePeriodEntity> existingOpenPeriod = repository.findByPersonIdAndValidToIsNull(personId.value());
        if (existingOpenPeriod.isEmpty()) {
            throw new PersonActivePeriodInconsistentStateException(
                "Person with id=%d has no open active period to close at %s.".formatted(personId.value(), validTo));
        }

        final PersonActivePeriodEntity entity = existingOpenPeriod.get();
        entity.setValidTo(validTo);
        repository.save(entity);

        LOG.info("Closed active period for person with id={} at {}", personId.value(), validTo);
    }

    private static PersonActivePeriod toPersonActivePeriod(PersonActivePeriodEntity entity) {
        return new PersonActivePeriod(new PersonId(entity.getPersonId()), entity.getValidFrom(), Optional.ofNullable(entity.getValidTo()));
    }
}
