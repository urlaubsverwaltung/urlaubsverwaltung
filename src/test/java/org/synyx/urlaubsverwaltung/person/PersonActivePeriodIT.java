package org.synyx.urlaubsverwaltung.person;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;

import java.time.Instant;
import java.util.ArrayList;
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

        person.setPermissions(new ArrayList<>(List.of(USER, INACTIVE)));
        personService.update(person);
        entityManager.flush();
        entityManager.clear();

        final List<PersonActivePeriod> afterDeactivation = sut.getActivePeriods(new PersonId(personId));
        assertThat(afterDeactivation).hasSize(1);
        assertThat(afterDeactivation.get(0).validTo()).isPresent();

        person.setPermissions(new ArrayList<>(List.of(USER)));
        personService.update(person);
        entityManager.flush();
        entityManager.clear();

        final List<PersonActivePeriod> afterReactivation = sut.getActivePeriods(new PersonId(personId));
        assertThat(afterReactivation).hasSize(2);
        assertThat(afterReactivation.get(0).validTo()).isPresent();
        assertThat(afterReactivation.get(1).validTo()).isEmpty();
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

        person.setPermissions(new ArrayList<>(List.of(USER, INACTIVE)));

        assertThatThrownBy(() -> personService.update(person))
            .isInstanceOf(PersonActivePeriodInconsistentStateException.class);
    }
}
