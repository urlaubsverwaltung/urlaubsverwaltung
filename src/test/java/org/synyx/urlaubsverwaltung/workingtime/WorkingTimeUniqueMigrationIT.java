package org.synyx.urlaubsverwaltung.workingtime;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the delete of {@code changelog-6.25.1-unique-working-time-per-person-and-date.xml} against duplicates
 * that were inserted before the unique constraint existed.
 */
@SpringBootTest
@Transactional
class WorkingTimeUniqueMigrationIT extends SingleTenantTestContainersBase {

    // kept in sync with the "unique-working-time-per-person-and-date" changeset
    private static final String DELETE_DUPLICATES_SQL = """
        DELETE FROM working_time duplicate
          USING working_time newer
          WHERE duplicate.person_id = newer.person_id
            AND duplicate.valid_from = newer.valid_from
            AND duplicate.id < newer.id;
        """;

    @Autowired
    private WorkingTimeRepository workingTimeRepository;

    @Autowired
    private PersonService personService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ensureDeleteKeepsTheLatestOfEachDuplicate() {

        // rolled back with the test transaction
        jdbcTemplate.execute("ALTER TABLE working_time DROP CONSTRAINT unique_working_time_by_person_and_valid_from");

        final Person person = personService.create("muster", "Marlene", "Muster", "muster@example.org");
        final Person other = personService.create("other", "Olaf", "Other", "other@example.org");
        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);

        final Long oldest = save(person, validFrom);
        final Long older = save(person, validFrom);
        final Long latest = save(person, validFrom);
        final Long otherDate = save(person, validFrom.plusYears(1));
        final Long otherPerson = save(other, validFrom);
        entityManager.flush();

        jdbcTemplate.update(DELETE_DUPLICATES_SQL);

        final List<Long> ids = jdbcTemplate.queryForList(
            "SELECT id FROM working_time WHERE person_id IN (?, ?)", Long.class, person.getId(), other.getId());
        assertThat(ids)
            .contains(latest, otherDate, otherPerson)
            .doesNotContain(oldest, older);
    }

    @Test
    void ensureIndexOnPersonIsDroppedInFavourOfTheUniqueConstraint() {
        final List<String> indexNames = jdbcTemplate.queryForList(
            "SELECT indexname FROM pg_indexes WHERE tablename = 'working_time'", String.class);
        assertThat(indexNames)
            .contains("unique_working_time_by_person_and_valid_from")
            .doesNotContain("working_time_person");
    }

    private Long save(Person person, LocalDate validFrom) {
        final WorkingTimeEntity workingTime = new WorkingTimeEntity();
        workingTime.setPerson(person);
        workingTime.setValidFrom(validFrom);
        return workingTimeRepository.save(workingTime).getId();
    }
}
