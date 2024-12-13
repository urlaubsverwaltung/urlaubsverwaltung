package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({BackupConfigurationProperties.class})
@ConditionalOnBackupCreateEnabled
public class BackupConfiguration {
}
