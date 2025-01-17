package org.synyx.urlaubsverwaltung.extension.backup.filesystem.restore;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.synyx.urlaubsverwaltung.extension.backup.model.UrlaubsverwaltungBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.restore.BackupRestoreService;
import org.synyx.urlaubsverwaltung.extension.backup.restore.RestoreOrchestrationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;


class FilesystemRestoreService implements BackupRestoreService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ObjectMapper objectMapper;
    private final RestoreOrchestrationService restoreOrchestrationService;
    private final FilesystemRestoreConfigurationProperties filesystemBackupConfigurationProperties;

    FilesystemRestoreService(ObjectMapper objectMapper, RestoreOrchestrationService restoreOrchestrationService, FilesystemRestoreConfigurationProperties filesystemBackupConfigurationProperties) {
        this.objectMapper = objectMapper;
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
            return Optional.of(objectMapper.readValue(backup.toFile(), UrlaubsverwaltungBackupDTO.class));
        } catch (IOException e) {
            LOG.warn("Could not read file={} to pojo", backup.toAbsolutePath(), e);
            return Optional.empty();
        }
    }
}
