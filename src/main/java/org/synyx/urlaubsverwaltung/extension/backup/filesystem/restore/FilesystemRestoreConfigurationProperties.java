package org.synyx.urlaubsverwaltung.extension.backup.filesystem.restore;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the filesystem based restore.
 *
 * @param backupFile the file of an existing backup that will be restored - must be full qualified like /tmp/urlaubsverwaltung-1735854942.json
 */
@ConfigurationProperties("uv.backup.restore-configuration.filesystem")
record FilesystemRestoreConfigurationProperties(String backupFile) {
}
