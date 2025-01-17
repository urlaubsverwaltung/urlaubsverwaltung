package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.synyx.urlaubsverwaltung.extension.backup.model.UrlaubsverwaltungBackupDTO;

/**
 * Service for creating backups of the application data.
 * Implement this interface to receive the data that should be backed up,
 * and you're responsible for storing it somewhere
 */
public interface BackupCreateService {

    void backupData(UrlaubsverwaltungBackupDTO backup);
}
