package org.synyx.urlaubsverwaltung.extension.backup.restore;

/**
 * Service for restoring backups of the application data.
 * Implement this interface to restore the data that was backed up.
 * Pass the data to the {@link RestoreOrchestrationService} to restore the data.
 */
public interface BackupRestoreService {
    void restoreBackup();
}
