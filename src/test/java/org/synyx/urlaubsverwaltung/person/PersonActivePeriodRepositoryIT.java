package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PersonActivePeriodRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private PersonActivePeriodRepository sut;

    @Autowired
    private PersonRepository personRepository;

    @Nested
    class FindByPersonIdAndValidToIsNull {

        @Test
        void ensureReturnsTheOpenPeriod() {

            final Person person = createPerson();

            final PersonActivePeriodEntity closedPeriod = newEntity(person.getId(), Instant.now().minus(Duration.ofDays(20)), Instant.now().minus(Duration.ofDays(10)));
            final PersonActivePeriodEntity openPeriod = newEntity(person.getId(), Instant.now().minus(Duration.ofDays(5)), null);
            sut.saveAll(List.of(closedPeriod, openPeriod));

            final Optional<PersonActivePeriodEntity> actual = sut.findByPersonIdAndValidToIsNull(person.getId());
            assertThat(actual).contains(openPeriod);
        }

        @Test
        void ensureEmptyWhenNoOpenPeriodExists() {

            final Person person = createPerson();

            final PersonActivePeriodEntity closedPeriod = newEntity(person.getId(), Instant.now().minus(Duration.ofDays(20)), Instant.now().minus(Duration.ofDays(10)));
            sut.save(closedPeriod);

            assertThat(sut.findByPersonIdAndValidToIsNull(person.getId())).isEmpty();
        }
    }

    @Nested
    class FindAllByPersonIdIsInAndOverlapping {

        @Test
        void ensureReturnsPeriodsOverlappingTheGivenRange() {

            final Person person = createPerson();

            final Instant from = Instant.now().minus(Duration.ofDays(10));
            final Instant to = Instant.now();

            final PersonActivePeriodEntity overlapping = newEntity(person.getId(), Instant.now().minus(Duration.ofDays(15)), null);
            final PersonActivePeriodEntity endedBeforeRange = newEntity(person.getId(), Instant.now().minus(Duration.ofDays(30)), Instant.now().minus(Duration.ofDays(20)));
            sut.saveAll(List.of(overlapping, endedBeforeRange));

            final List<PersonActivePeriodEntity> actual = sut.findAllByPersonIdIsInAndOverlapping(List.of(person.getId()), from, to);
            assertThat(actual).containsExactly(overlapping);
        }

        @Test
        void ensureDoesNotReturnPeriodsStartingAfterTheGivenRange() {

            final Person person = createPerson();

            final Instant from = Instant.now().minus(Duration.ofDays(10));
            final Instant to = Instant.now();

            final PersonActivePeriodEntity startingAfterRange = newEntity(person.getId(), Instant.now().plus(Duration.ofDays(5)), null);
            sut.save(startingAfterRange);

            final List<PersonActivePeriodEntity> actual = sut.findAllByPersonIdIsInAndOverlapping(List.of(person.getId()), from, to);
            assertThat(actual).isEmpty();
        }
    }

    private Person createPerson() {
        return personRepository.save(new Person("max", "Mustermann", "Max", "mustermann@example.org"));
    }

    private PersonActivePeriodEntity newEntity(Long personId, Instant validFrom, Instant validTo) {
        final PersonActivePeriodEntity entity = new PersonActivePeriodEntity();
        entity.setPersonId(personId);
        entity.setValidFrom(validFrom);
        entity.setValidTo(validTo);
        return entity;
    }
}
