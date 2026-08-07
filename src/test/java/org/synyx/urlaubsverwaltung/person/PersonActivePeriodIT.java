package org.synyx.urlaubsverwaltung.person;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class PersonActivePeriodIT extends SingleTenantTestContainersBase {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonActivePeriodService sut;

    @Autowired
    private PersonActivePeriodRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ensureActivePeriodIsOpenedOnPersonCreation() {

        final Person person = personService.create("max", "Max", "Mustermann", "mustermann@example.org");

        final List<PersonActivePeriod> activePeriods = sut.getActivePeriods(person.getIdAsPersonId());
        assertThat(activePeriods).hasSize(1);
        assertThat(activePeriods.get(0).validTo()).isEmpty();
    }

    @Test
    void ensureActivePeriodIsClosedOnDeactivationAndReopenedOnReactivation() {

        final Person person = personService.create("max", "Max", "Mustermann", "mustermann@example.org");
        final Long personId = person.getId();

        // detach from the persistence context, so that the following updates behave like separate requests would
        entityManager.flush();
        entityManager.clear();

        personService.update(new PersonId(personId), PersonUpdate.ofPermissions(List.of(USER, INACTIVE)));
        entityManager.flush();
        entityManager.clear();

        final List<PersonActivePeriod> afterDeactivation = sut.getActivePeriods(new PersonId(personId));
        assertThat(afterDeactivation).hasSize(1);
        assertThat(afterDeactivation.get(0).validTo()).isPresent();

        personService.update(new PersonId(personId), PersonUpdate.ofPermissions(List.of(USER)));
        entityManager.flush();
        entityManager.clear();

        final List<PersonActivePeriod> afterReactivation = sut.getActivePeriods(new PersonId(personId));
        assertThat(afterReactivation).hasSize(2);
        assertThat(afterReactivation.get(0).validTo()).isPresent();
        assertThat(afterReactivation.get(1).validTo()).isEmpty();
    }

    @Test
    void ensureActivePeriodIsClosedOnDeactivationWithinTheTransactionThePersonWasCreatedIn() {

        // no flush/clear on purpose: the person is updated within the very transaction it was created in,
        // like the demo data creation does on the PersonCreatedEvent
        final Person person = personService.create("max", "Max", "Mustermann", "mustermann@example.org");

        personService.update(person.getIdAsPersonId(), PersonUpdate.ofPermissions(List.of(USER, INACTIVE)));

        final List<PersonActivePeriod> activePeriods = sut.getActivePeriods(person.getIdAsPersonId());
        assertThat(activePeriods).hasSize(1);
        assertThat(activePeriods.getFirst().validTo()).isPresent();
    }

    @Test
    void ensureDeactivationThrowsInsteadOfSilentlyIgnoringAnAlreadyInconsistentActivePeriodState() {

        final Person person = personService.create("max", "Max", "Mustermann", "mustermann@example.org");
        final Long personId = person.getId();

        entityManager.flush();
        entityManager.clear();

        // simulate a corrupted/inconsistent state: the open period was already closed by something
        // other than PersonActivePeriodServiceImpl, so the person has no open period even though
        // PersonService still considers them active
        final PersonActivePeriodEntity openPeriod = repository.findByPersonIdAndValidToIsNull(personId).orElseThrow();
        openPeriod.setValidTo(Instant.now());
        repository.save(openPeriod);
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> personService.update(new PersonId(personId), PersonUpdate.ofPermissions(List.of(USER, INACTIVE))))
            .isInstanceOf(PersonActivePeriodInconsistentStateException.class);
    }
}
