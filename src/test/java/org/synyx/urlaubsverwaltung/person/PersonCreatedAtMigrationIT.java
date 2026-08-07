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
import java.util.List;

import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the backfill SQL of {@code changelog-6.7.0-add-created-at-to-person.xml} against a person that already
 * existed before the {@code person.created_at} column was introduced (i.e. a person for whom created_at was never
 * populated). The column is fully migrated (including its NOT NULL constraint) by the time this test runs, so the
 * legacy state is reproduced by dropping the constraint and nulling the column out just for this transaction.
 */
@SpringBootTest
@Transactional
class PersonCreatedAtMigrationIT extends SingleTenantTestContainersBase {

    // kept in sync with the "add-created-at-to-person--backfill" changeset
    private static final String BACKFILL_FROM_WORKING_TIME_SQL = """
        UPDATE person
        SET created_at = first_working_time.valid_from::timestamptz
        FROM (
          SELECT person_id, MIN(valid_from) as valid_from
          FROM working_time
          GROUP BY person_id
        ) first_working_time
        WHERE person.id = first_working_time.person_id
          AND person.created_at IS NULL;
        """;

    private static final String BACKFILL_UNKNOWN_SQL = """
        UPDATE person
        SET created_at = '1970-01-01T00:00:00Z'::timestamptz
        WHERE created_at IS NULL;
        """;

    @Autowired
    private PersonService personService;

    @Autowired
    private WorkingTimeWriteService workingTimeWriteService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ensureBackfillUsesFirstWorkingTimeValidFromAsCreatedAt() {

        final Person person = personService.create("legacy", "Legacy", "Mustermann", "legacy@example.org");

        workingTimeWriteService.touch(List.of(1, 2, 3, 4, 5), LocalDate.of(2018, MARCH, 1), person);
        workingTimeWriteService.touch(List.of(1, 2, 3, 4, 5), LocalDate.of(2020, JANUARY, 1), person);

        entityManager.flush();
        givenCreatedAtWasNeverPopulated(person);

        backfill();

        assertThat(createdAtDateOf(person)).isEqualTo(LocalDate.of(2018, MARCH, 1));
    }

    @Test
    void ensureBackfillUsesTheEpochAsCreatedAtWithoutAnyWorkingTime() {

        final Person person = personService.create("legacy", "Legacy", "Mustermann", "legacy@example.org");

        entityManager.flush();

        // a person without any working time has no proxy for their creation date at all
        jdbcTemplate.update("DELETE FROM working_time WHERE person_id = ?", person.getId());
        givenCreatedAtWasNeverPopulated(person);

        backfill();

        assertThat(createdAtDateOf(person)).isEqualTo(LocalDate.of(1970, JANUARY, 1));
    }

    @Test
    void ensureBackfillDoesNotTouchPersonsWithAKnownCreatedAt() {

        final Person person = personService.create("known", "Known", "Mustermann", "known@example.org");

        workingTimeWriteService.touch(List.of(1, 2, 3, 4, 5), LocalDate.of(2018, MARCH, 1), person);

        entityManager.flush();
        final LocalDate createdAtBeforeBackfill = createdAtDateOf(person);

        backfill();

        assertThat(createdAtDateOf(person)).isEqualTo(createdAtBeforeBackfill);
    }

    private void backfill() {
        jdbcTemplate.update(BACKFILL_FROM_WORKING_TIME_SQL);
        jdbcTemplate.update(BACKFILL_UNKNOWN_SQL);
    }

    private void givenCreatedAtWasNeverPopulated(Person person) {
        jdbcTemplate.execute("ALTER TABLE person ALTER COLUMN created_at DROP NOT NULL");
        jdbcTemplate.update("UPDATE person SET created_at = NULL WHERE id = ?", person.getId());
        entityManager.clear();
    }

    // compares as a date (not an absolute instant) to stay independent of the DB session's timezone
    private LocalDate createdAtDateOf(Person person) {
        return jdbcTemplate.queryForObject(
            "SELECT created_at::date FROM person WHERE id = ?", LocalDate.class, person.getId());
    }
}
