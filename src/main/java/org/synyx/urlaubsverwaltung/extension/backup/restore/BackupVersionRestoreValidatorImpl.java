package org.synyx.urlaubsverwaltung.extension.backup.restore;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@ConditionalOnBackupRestoreEnabled
class BackupVersionRestoreValidatorImpl implements BackupVersionRestoreValidator {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final String applicationVersion;

    BackupVersionRestoreValidatorImpl(@Value("${info.app.version}") String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    @Override
    public boolean isValidBackupVersion(String backupVersion) {
        // to restore a backup the version of the backup must match the exact version of the running application!
        final boolean isValid = applicationVersion.equals(backupVersion);
        LOG.info("backup version={} is {} with application version={}", backupVersion, isValid ? "compatible" : "incompatible", applicationVersion);
        return isValid;
    }
}
