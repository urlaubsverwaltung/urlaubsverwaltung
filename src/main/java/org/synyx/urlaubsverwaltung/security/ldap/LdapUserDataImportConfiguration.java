package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.config.ScheduleLocking;

class LdapUserDataImportConfiguration implements SchedulingConfigurer {

    private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;
    private final LdapUserDataImporter ldapUserDataImporter;
    private final ScheduleLocking scheduleLocking;

    LdapUserDataImportConfiguration(DirectoryServiceSecurityProperties directoryServiceSecurityProperties, LdapUserDataImporter ldapUserDataImporter, ScheduleLocking scheduleLocking) {
        this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
        this.ldapUserDataImporter = ldapUserDataImporter;
        this.scheduleLocking = scheduleLocking;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addCronTask(
            scheduleLocking.withLock("LdapUserDataImporterSync", ldapUserDataImporter::sync),
            directoryServiceSecurityProperties.getSync().getCron()
        );
    }
}
