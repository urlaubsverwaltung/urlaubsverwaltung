package org.synyx.urlaubsverwaltung.extension.backup.filesystem.restore;


import org.slf4j.Logger;
import org.synyx.urlaubsverwaltung.extension.backup.model.UrlaubsverwaltungBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.restore.BackupRestoreService;
import org.synyx.urlaubsverwaltung.extension.backup.restore.RestoreOrchestrationService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;


class FilesystemRestoreService implements BackupRestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final JsonMapper jsonMapper;
    private final RestoreOrchestrationService restoreOrchestrationService;
    private final FilesystemRestoreConfigurationProperties filesystemBackupConfigurationProperties;

    FilesystemRestoreService(JsonMapper jsonMapper, RestoreOrchestrationService restoreOrchestrationService, FilesystemRestoreConfigurationProperties filesystemBackupConfigurationProperties) {
        this.jsonMapper = jsonMapper;
        this.restoreOrchestrationService = restoreOrchestrationService;
        this.filesystemBackupConfigurationProperties = filesystemBackupConfigurationProperties;
    }

    @Override
    public void restoreBackup() {
        final Path backupFile = Paths.get(this.filesystemBackupConfigurationProperties.backupFile());

        if (!Files.exists(backupFile)) {
            LOG.warn("Backup file={} does not exist - restore will be skipped!", backupFile.toAbsolutePath());
            return;
        }

        restoreBackup(backupFile);
    }

    private void restoreBackup(Path backup) {
        LOG.info("Going to restore data from file={}", backup.toAbsolutePath());
        readJsonFileToPojo(backup)
            .ifPresentOrElse(dataToRestore -> {
                restoreOrchestrationService.restoreData(dataToRestore);
                LOG.info("Finished restoring data from file={}", backup.toAbsolutePath());
            },() -> LOG.warn("Could not restore data from file={}", backup.toAbsolutePath()));
    }

    private Optional<UrlaubsverwaltungBackupDTO> readJsonFileToPojo(Path backup) {
        try {
            return Optional.of(jsonMapper.readValue(backup.toFile(), UrlaubsverwaltungBackupDTO.class));
        } catch (JacksonException e) {
            LOG.warn("Could not read file={} to pojo", backup.toAbsolutePath(), e);
            return Optional.empty();
        }
    }
}
