package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.synyx.urlaubsverwaltung.SingleTenantTestPostgreSQLContainer;
import org.synyx.urlaubsverwaltung.extension.backup.restore.BackupRestoreService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers(parallel = true)
@SpringBootTest(properties = {
    "uv.extensions.enabled=true",
    "uv.extensions.tenancy.tenant-id=this-is-a-tenant-id",
    "uv.backup.backup-configuration.enabled=true",
    "uv.backup.backup-configuration.backup-service=filesystem",
    "uv.backup.restore-configuration.enabled=true",
    "uv.backup.restore-configuration.drop-data=true",
    "uv.backup.restore-configuration.restore-service=filesystem",
    "uv.backup.restore-configuration.filesystem.backup-file=src/test/resources/urlaubsverwaltung-backup.json",})
@DirtiesContext
class BackupCreateServiceIT {

    private static final String EXISTING_UV_BACKUP_FILE = "src/test/resources/urlaubsverwaltung-backup.json";

    @TempDir
    private static Path tempDir;

    @Container
    private static final SingleTenantTestPostgreSQLContainer postgre = new SingleTenantTestPostgreSQLContainer();

    @Autowired
    private BackupRestoreService backupRestoreService;
    @Autowired
    private BackupCreateService backupCreateService;
    @Autowired
    private BackupDataCollectionService backupDataCollectionService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        postgre.configureSpringDataSource(registry);
        registry.add("uv.backup.backup-configuration.filesystem.backup-path", () -> tempDir.toString());
    }

    @Test
    void verifyActualBackupWithExistingBackup() throws IOException {
        // reset all sequences so the ids in the existing backup file
        // and the created backup file are the same
        resetAllSequences();

        // restore the existing backup file, so we have some data to backup
        backupRestoreService.restoreBackup();

        // create a fresh backup
        backupCreateService.backupData(backupDataCollectionService.collectData());

        // now verify that the created backup file is the same as the existing backup file
        final Optional<Path> backupFile = getCreatedBackupFile();

        assertThat(backupFile).isPresent();

        final String actualBackup = Files.readString(backupFile.get());
        final String expectedBackup = Files.readString(Paths.get(EXISTING_UV_BACKUP_FILE));

        assertThatJson(actualBackup).isEqualTo(expectedBackup);
    }

    private void resetAllSequences() {
        final String getSequencesSql = "SELECT schemaname, sequencename FROM pg_sequences WHERE schemaname = 'public'";
        final List<Map<String, Object>> sequences = jdbcTemplate.queryForList(getSequencesSql);

        sequences.forEach(sequence -> {
            final String schemaName = (String) sequence.get("schemaname");
            final String sequenceName = (String) sequence.get("sequencename");
            final String getMinValueSql = String.format("SELECT min_value FROM pg_sequences WHERE schemaname = '%s' AND sequencename = '%s'", schemaName, sequenceName);
            final Long minValue = jdbcTemplate.queryForObject(getMinValueSql, Long.class);
            final String resetSequenceSql = String.format("ALTER SEQUENCE %s.%s RESTART WITH %d", schemaName, sequenceName, minValue);
            jdbcTemplate.execute(resetSequenceSql);
        });
    }

    private static Optional<Path> getCreatedBackupFile() {
        try (var paths = Files.list(tempDir)) {
            return paths.filter(path -> path.getFileName().toString().matches("urlaubsverwaltung-\\d+\\.json")).findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
