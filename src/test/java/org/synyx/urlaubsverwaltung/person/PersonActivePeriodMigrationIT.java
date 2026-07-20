package org.synyx.urlaubsverwaltung.person;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the backfill SQL of {@code changelog-6.5.0-person-active-period.xml} against persons that already
 * existed before the {@code person_active_period} table was introduced (i.e. persons created without going
 * through {@link PersonService}, so no active period row exists for them yet).
 */
@SpringBootTest
@Transactional
class PersonActivePeriodMigrationIT extends SingleTenantTestContainersBase {

    // kept in sync with the "add-person-active-period--migrate-existing-persons" changeset
    private static final String BACKFILL_SQL = """
        INSERT INTO person_active_period (
          tenant_id,
          id,
          person_id,
          valid_from,
          valid_to
        )
        SELECT
          person.tenant_id,
          nextval('person_active_period_id_seq') as id,
          person.id as person_id,
          first_working_time.valid_from::timestamptz as valid_from,
          CASE WHEN inactive_person.person_id IS NOT NULL THEN now() ELSE NULL END as valid_to
        FROM person
        JOIN (
          SELECT person_id, MIN(valid_from) as valid_from
          FROM working_time
          GROUP BY person_id
        ) first_working_time ON first_working_time.person_id = person.id
        LEFT JOIN (
          SELECT DISTINCT person_id
          FROM person_permissions
          WHERE permissions = 'INACTIVE'
        ) inactive_person ON inactive_person.person_id = person.id;
        """;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private WorkingTimeWriteService workingTimeWriteService;

    @Autowired
    private PersonActivePeriodRepository sut;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ensureBackfillOpensPeriodFromFirstWorkingTimeForActiveLegacyPerson() {

        final Person transientPerson = new Person("legacyActive", "Mustermann", "Legacy", "legacy-active@example.org");
        transientPerson.setPermissions(new ArrayList<>(List.of(Role.USER)));
        final Person person = personRepository.save(transientPerson);

        workingTimeWriteService.touch(List.of(1, 2, 3, 4, 5), LocalDate.of(2018, 3, 1), person);
        workingTimeWriteService.touch(List.of(1, 2, 3, 4, 5), LocalDate.of(2020, 1, 1), person);

        entityManager.flush();
        jdbcTemplate.update(BACKFILL_SQL);

        final List<PersonActivePeriodEntity> periods = sut.findAllByPersonIdOrderByValidFromAsc(person.getId());
        assertThat(periods).hasSize(1);
        assertThat(periods.get(0).getValidTo()).isNull();

        // compare as a date (not an absolute instant) to stay independent of the DB session's timezone
        final LocalDate validFromDate = jdbcTemplate.queryForObject(
            "SELECT valid_from::date FROM person_active_period WHERE person_id = ?", LocalDate.class, person.getId());
        assertThat(validFromDate).isEqualTo(LocalDate.of(2018, 3, 1));
    }

    @Test
    void ensureBackfillClosesPeriodForInactiveLegacyPerson() {

        final Person transientPerson = new Person("legacyInactive", "Mustermann", "Legacy", "legacy-inactive@example.org");
        transientPerson.setPermissions(new ArrayList<>(List.of(Role.USER, Role.INACTIVE)));
        final Person person = personRepository.save(transientPerson);

        workingTimeWriteService.touch(List.of(1, 2, 3, 4, 5), LocalDate.of(2015, 6, 1), person);

        entityManager.flush();
        jdbcTemplate.update(BACKFILL_SQL);

        final List<PersonActivePeriodEntity> periods = sut.findAllByPersonIdOrderByValidFromAsc(person.getId());
        assertThat(periods).hasSize(1);
        assertThat(periods.get(0).getValidTo()).isNotNull();
    }
}
