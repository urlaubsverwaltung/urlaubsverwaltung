package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Component
@ConditionalOnBackupRestoreEnabled
@ConditionalOnProperty(prefix = "uv.backup.restore-configuration", name = "restoreOnAppReady", havingValue = "true")
@ConditionalOnSingleTenantMode
class ApplicationReadyRestoreTrigger {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationReadyRestoreTrigger.class);

    private final BackupRestoreService backupRestoreService;

    ApplicationReadyRestoreTrigger(BackupRestoreService backupRestoreService) {
        this.backupRestoreService = backupRestoreService;
    }

    @EventListener(ApplicationReadyEvent.class)
    void restoreBackup() {
        LOG.info("Starting restore by ApplicationReadyEvent...");
        backupRestoreService.restoreBackup();
        LOG.info("Finished restore by ApplicationReadyEvent ...");
    }
}
