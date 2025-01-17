package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RestoreConfigurationProperties.class})
@ConditionalOnBackupRestoreEnabled
public class RestoreConfiguration {
}
