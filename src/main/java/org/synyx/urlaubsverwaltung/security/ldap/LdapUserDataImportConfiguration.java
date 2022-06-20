package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.config.ScheduleLocking;

class LdapUserDataImportConfiguration implements SchedulingConfigurer {

    private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;
    private final LdapUserDataImporter ldapUserDataImporter;
    private final ScheduleLocking scheduleLocking;
    private final ThreadPoolTaskScheduler taskScheduler;

    LdapUserDataImportConfiguration(DirectoryServiceSecurityProperties directoryServiceSecurityProperties, LdapUserDataImporter ldapUserDataImporter, ScheduleLocking scheduleLocking, ThreadPoolTaskScheduler taskScheduler) {
        this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
        this.ldapUserDataImporter = ldapUserDataImporter;
        this.scheduleLocking = scheduleLocking;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler);
        taskRegistrar.addCronTask(
            scheduleLocking.withLock("LdapUserDataImporterSync", ldapUserDataImporter::sync),
            directoryServiceSecurityProperties.getSync().getCron()
        );
    }
}
