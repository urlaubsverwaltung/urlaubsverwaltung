package org.synyx.urlaubsverwaltung.extension.backup.filesystem.backup;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the filesystem based backup.
 *
 * @param backupPath the path where backups will be stored - e.g. /tmp/ - the filename will be generated automatically!
 */
@ConfigurationProperties("uv.backup.backup-configuration.filesystem")
record FilesystemBackupConfigurationProperties(String backupPath) {
}
