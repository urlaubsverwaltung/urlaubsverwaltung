package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class LdapUserDataImportConfigurationTest {

    @Test
    void importsLdapUserDataWithGivenCronJobInterval() {

        final DirectoryServiceSecurityProperties properties = new DirectoryServiceSecurityProperties();
        final LdapUserDataImporter importer = mock(LdapUserDataImporter.class);
        final LdapUserDataImportConfiguration sut = new LdapUserDataImportConfiguration(properties, importer);

        final ScheduledTaskRegistrar taskRegistrar = new ScheduledTaskRegistrar();
        sut.configureTasks(taskRegistrar);

        final List<CronTask> cronTaskList = taskRegistrar.getCronTaskList();
        assertThat(cronTaskList).hasSize(1);

        final CronTask cronTask = cronTaskList.get(0);
        assertThat(cronTask.getExpression()).isEqualTo("0 0 1 * * ?");

        verifyNoInteractions(importer);

        cronTask.getRunnable().run();
        verify(importer).sync();
    }
}
