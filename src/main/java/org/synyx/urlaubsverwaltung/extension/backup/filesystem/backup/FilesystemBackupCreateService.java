package org.synyx.urlaubsverwaltung.extension.backup.filesystem.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.synyx.urlaubsverwaltung.extension.backup.backup.BackupCreateService;
import org.synyx.urlaubsverwaltung.extension.backup.model.UrlaubsverwaltungBackupDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

class FilesystemBackupCreateService implements BackupCreateService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ObjectMapper objectMapper;
    private final FilesystemBackupConfigurationProperties filesystemBackupConfigurationProperties;

    FilesystemBackupCreateService(ObjectMapper objectMapper, FilesystemBackupConfigurationProperties filesystemBackupConfigurationProperties) {
        this.objectMapper = objectMapper;
        this.filesystemBackupConfigurationProperties = filesystemBackupConfigurationProperties;
    }

    @Override
    public void backupData(UrlaubsverwaltungBackupDTO backup) {
        writeIntoFileSystem(backup);
    }

    void writeIntoFileSystem(UrlaubsverwaltungBackupDTO exportModel) {
        final Path filePath = getPath();
        try {
            Files.createDirectories(filePath.getParent());
            final File exportFile = filePath.toFile();
            LOG.info("Writing export file={}", exportFile.getAbsolutePath());
            objectMapper.writeValue(exportFile, exportModel);
            LOG.info("Export file written to file={}", exportFile.getAbsolutePath());
        } catch (IOException e) {
            LOG.warn("Could not write export file={}", filePath.toAbsolutePath(), e);
        }
    }

    private Path getPath() {
        final String backupPath = this.filesystemBackupConfigurationProperties.backupPath();
        final String filename = "urlaubsverwaltung-%s.json".formatted(Instant.now().getEpochSecond());
        return Paths.get(backupPath, filename);
    }
}
