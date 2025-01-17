package org.synyx.urlaubsverwaltung.extension.backup.filesystem.restore;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.synyx.urlaubsverwaltung.extension.backup.restore.ConditionalOnBackupRestoreEnabled;
import org.synyx.urlaubsverwaltung.extension.backup.restore.RestoreOrchestrationService;

@Configuration
@EnableConfigurationProperties(FilesystemRestoreConfigurationProperties.class)
@ConditionalOnProperty(prefix = "uv.backup.restore-configuration", name = "restore-service", havingValue = "filesystem")
@ConditionalOnBackupRestoreEnabled
class FilesystemRestoreConfiguration {

    @Bean
    FilesystemRestoreService filesystemRestoreService(ObjectMapper objectMapper, RestoreOrchestrationService restoreOrchestrationService, FilesystemRestoreConfigurationProperties filesystemBackupConfigurationProperties) {
        return new FilesystemRestoreService(objectMapper, restoreOrchestrationService, filesystemBackupConfigurationProperties);
    }
}
