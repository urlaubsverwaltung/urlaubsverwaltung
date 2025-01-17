package org.synyx.urlaubsverwaltung.extension.backup.filesystem.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.synyx.urlaubsverwaltung.extension.backup.backup.ConditionalOnBackupCreateEnabled;

@Configuration
@EnableConfigurationProperties(FilesystemBackupConfigurationProperties.class)
@ConditionalOnBackupCreateEnabled
@ConditionalOnProperty(prefix = "uv.backup.backup-configuration", name = "backup-service", havingValue = "filesystem")
class FilesystemBackupConfiguration {

    @Bean
    FilesystemBackupCreateService filesystemBackupCreateService(ObjectMapper objectMapper, FilesystemBackupConfigurationProperties filesystemBackupConfigurationProperties) {
        return new FilesystemBackupCreateService(objectMapper, filesystemBackupConfigurationProperties);
    }
}
