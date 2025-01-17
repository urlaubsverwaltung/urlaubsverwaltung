package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@ConditionalOnBackupCreateEnabled
@ConditionalOnProperty(prefix = "uv.backup.backup-configuration", name = "backup-on-app-ready", havingValue = "true")
@ConditionalOnSingleTenantMode
class ApplicationReadyBackupTrigger {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final BackupCreateService backupCreateService;
    private final BackupDataCollectionService backupDataCollectionService;

    ApplicationReadyBackupTrigger(BackupCreateService backupCreateService, BackupDataCollectionService backupDataCollectionService) {
        this.backupCreateService = backupCreateService;
        this.backupDataCollectionService = backupDataCollectionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    void createBackup() {
        LOG.info("Starting backup by ApplicationReadyEvent...");
        backupCreateService.backupData(backupDataCollectionService.collectData());
        LOG.info("Finished backup by ApplicationReadyEvent ...");
    }
}
