package org.synyx.urlaubsverwaltung.companyclosure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.period.DayLength;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

/**
 * Verifies the data migration SQL of {@code changelog-6.22.0-add-company-closure.xml}: the working duration of the
 * settings is converted into the inverse closed day length.
 */
@SpringBootTest
@Transactional
class CompanyClosureMigrationIT extends SingleTenantTestContainersBase {

    // kept in sync with the "add-company-closure--migrate-settings" changeset
    private static final String MIGRATION_SQL = """
          INSERT INTO company_closure (tenant_id, id, month, day_of_month, name)
          SELECT tenant_id, nextval('company_closure_id_seq'), 12, 24, 'Heiligabend' FROM settings;

          INSERT INTO company_closure (tenant_id, id, month, day_of_month, name)
          SELECT tenant_id, nextval('company_closure_id_seq'), 12, 31, 'Silvester' FROM settings;

          INSERT INTO company_closure_version (tenant_id, id, company_closure_id, day_length, valid_from)
          SELECT
            company_closure.tenant_id,
            nextval('company_closure_version_id_seq'),
            company_closure.id,
            CASE
              WHEN company_closure.day_of_month = 24 THEN
                CASE settings.working_duration_for_christmas_eve WHEN 'FULL' THEN 'ZERO' WHEN 'NOON' THEN 'MORNING' WHEN 'ZERO' THEN 'FULL' ELSE 'NOON' END
              ELSE
                CASE settings.working_duration_for_new_years_eve WHEN 'FULL' THEN 'ZERO' WHEN 'NOON' THEN 'MORNING' WHEN 'ZERO' THEN 'FULL' ELSE 'NOON' END
            END,
            NULL
          FROM company_closure
          JOIN settings ON settings.tenant_id IS NOT DISTINCT FROM company_closure.tenant_id
          WHERE company_closure.month = 12 AND company_closure.day_of_month IN (24, 31);
        """;

    @Autowired
    private CompanyClosureRepository companyClosureRepository;

    @Autowired
    private CompanyClosureVersionRepository companyClosureVersionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void migratesWorkingDurationAsInverseClosure() {
        migrate("FULL", "ZERO");
        assertMigrated(ZERO, FULL);
    }

    @Test
    void migratesMorningToClosedNoon() {
        migrate("MORNING", "NOON");
        assertMigrated(NOON, MORNING);
    }

    @Test
    void migratesMissingValueToDefault() {
        migrate(null, null);
        assertMigrated(NOON, NOON);
    }

    private void migrate(String christmasEve, String newYearsEve) {
        // executed immediately, default rows would violate uq_company_closure_day once the inserts below run
        jdbcTemplate.update("DELETE FROM company_closure_version");
        jdbcTemplate.update("DELETE FROM company_closure");
        jdbcTemplate.update(
            "UPDATE settings SET working_duration_for_christmas_eve = ?, working_duration_for_new_years_eve = ?",
            christmasEve, newYearsEve);
        jdbcTemplate.execute(MIGRATION_SQL);
    }

    private void assertMigrated(DayLength christmasEve, DayLength newYearsEve) {
        assertThat(companyClosureRepository.findAll())
            .extracting(CompanyClosureEntity::getMonth, CompanyClosureEntity::getDayOfMonth, CompanyClosureEntity::getName)
            .containsExactlyInAnyOrder(
                tuple(12, 24, "Heiligabend"),
                tuple(12, 31, "Silvester"));

        assertThat(versionOf(24)).isEqualTo(christmasEve);
        assertThat(versionOf(31)).isEqualTo(newYearsEve);
    }

    private DayLength versionOf(int dayOfMonth) {
        final Long closureId = companyClosureRepository.findAll().stream()
            .filter(closure -> closure.getDayOfMonth() == dayOfMonth)
            .findFirst().orElseThrow().getId();
        final var versions = companyClosureVersionRepository.findAll().stream()
            .filter(version -> version.getCompanyClosureId().equals(closureId))
            .toList();
        assertThat(versions).hasSize(1);
        assertThat(versions.getFirst().getValidFrom()).isNull();
        return versions.getFirst().getDayLength();
    }
}
