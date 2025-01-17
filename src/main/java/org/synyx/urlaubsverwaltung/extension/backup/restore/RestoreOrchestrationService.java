package org.synyx.urlaubsverwaltung.extension.backup.restore;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.UrlaubsverwaltungBackupDTO;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Service to orchestrate the restore process of the application.
 * Use this service to restore the data from a backup.
 */
@Service
@ConditionalOnBackupRestoreEnabled
public class RestoreOrchestrationService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final RestoreConfigurationProperties restoreConfiguration;

    private final ResetUrlaubsverwaltungService resetUrlaubsverwaltungService;
    private final BackupVersionRestoreValidator backupVersionRestoreValidator;
    private final RestoreService restoreService;

    RestoreOrchestrationService(RestoreConfigurationProperties restoreConfiguration, ResetUrlaubsverwaltungService resetUrlaubsverwaltungService, BackupVersionRestoreValidator backupVersionRestoreValidator, RestoreService restoreService) {
        this.restoreConfiguration = restoreConfiguration;
        this.resetUrlaubsverwaltungService = resetUrlaubsverwaltungService;
        this.backupVersionRestoreValidator = backupVersionRestoreValidator;
        this.restoreService = restoreService;
    }

    public boolean restoreData(UrlaubsverwaltungBackupDTO backupToRestore) {

        if (!restoreConfiguration.dropData()) {
            LOG.info("Skip restoring data - dropData is disabled, but must be enabled!");
            return false;
        }

        if (!backupVersionRestoreValidator.isValidBackupVersion(backupToRestore.urlaubsverwaltungVersion())) {
            LOG.error("Could not restore data because the version={} of the backup is not compatible with the version of the application", backupToRestore.urlaubsverwaltungVersion());
            return false;
        }

        resetUrlaubsverwaltungService.resetData();

        restoreService.restoreData(backupToRestore);

        return true;
    }

}
