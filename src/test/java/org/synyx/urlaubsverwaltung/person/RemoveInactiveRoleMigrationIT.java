package org.synyx.urlaubsverwaltung.person;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the SQL of {@code changelog-6.14.0-remove-inactive-role.xml} against persons that were deactivated with
 * the former {@code INACTIVE} role. An inactive person is a person without the role {@link Role#USER} now.
 */
@SpringBootTest
@Transactional
class RemoveInactiveRoleMigrationIT extends SingleTenantTestContainersBase {

    // kept in sync with the "remove-inactive-role" changeset
    private static final String MIGRATION_SQL = """
        DELETE FROM person_permissions
        WHERE person_id IN (SELECT person_id FROM person_permissions WHERE permissions = 'INACTIVE');
        """;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ensureMigrationRemovesAllRolesOfInactivePerson() {

        final Person person = savePerson("legacyInactive", List.of(Role.USER, Role.OFFICE));
        jdbcTemplate.update("INSERT INTO person_permissions (person_id, permissions) VALUES (?, 'INACTIVE')", person.getId());

        jdbcTemplate.update(MIGRATION_SQL);

        assertThat(permissionsOf(person)).isEmpty();
    }

    @Test
    void ensureMigrationKeepsRolesOfActivePerson() {

        final Person person = savePerson("legacyActive", List.of(Role.USER, Role.OFFICE));

        jdbcTemplate.update(MIGRATION_SQL);

        assertThat(permissionsOf(person)).containsExactlyInAnyOrder("USER", "OFFICE");
    }

    private Person savePerson(String username, List<Role> permissions) {
        final Person transientPerson = new Person(username, "Mustermann", "Legacy", username + "@example.org");
        transientPerson.setPermissions(new ArrayList<>(permissions));
        transientPerson.setCreatedAt(Instant.now());
        final Person person = personRepository.save(transientPerson);
        entityManager.flush();
        return person;
    }

    private List<String> permissionsOf(Person person) {
        return jdbcTemplate.queryForList("SELECT permissions FROM person_permissions WHERE person_id = ?", String.class, person.getId());
    }
}
