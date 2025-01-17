package org.synyx.urlaubsverwaltung.extension.backup.backup;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("uv.backup.backup-configuration")
public record BackupConfigurationProperties(
    @DefaultValue("false") boolean enabled,
    @DefaultValue("false") boolean backupOnAppReady,
    @DefaultValue("filesystem") String backupService
) {
}
