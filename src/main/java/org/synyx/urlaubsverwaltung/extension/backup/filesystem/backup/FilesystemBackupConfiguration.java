package org.synyx.urlaubsverwaltung.extension.backup.filesystem.backup;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.synyx.urlaubsverwaltung.extension.backup.backup.ConditionalOnBackupCreateEnabled;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableConfigurationProperties(FilesystemBackupConfigurationProperties.class)
@ConditionalOnBackupCreateEnabled
@ConditionalOnProperty(prefix = "uv.backup.backup-configuration", name = "backup-service", havingValue = "filesystem")
class FilesystemBackupConfiguration {

    @Bean
    FilesystemBackupCreateService filesystemBackupCreateService(JsonMapper jsonMapper, FilesystemBackupConfigurationProperties filesystemBackupConfigurationProperties) {
        return new FilesystemBackupCreateService(jsonMapper, filesystemBackupConfigurationProperties);
    }
}
