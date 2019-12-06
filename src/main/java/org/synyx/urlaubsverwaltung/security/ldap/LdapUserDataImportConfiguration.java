package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

class LdapUserDataImportConfiguration implements SchedulingConfigurer {

    private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;
    private final LdapUserDataImporter ldapUserDataImporter;

    LdapUserDataImportConfiguration(DirectoryServiceSecurityProperties directoryServiceSecurityProperties, LdapUserDataImporter ldapUserDataImporter) {

        this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
        this.ldapUserDataImporter = ldapUserDataImporter;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addCronTask(ldapUserDataImporter::sync, directoryServiceSecurityProperties.getSync().getCron());
    }
}
