package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.config.ScheduleLocking;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Configuration
@ConditionalOnSingleTenantMode
class SickNoteMailConfiguration implements SchedulingConfigurer {

    private final SickNoteProperties sickNoteProperties;
    private final SickNoteMailService sickNoteMailService;
    private final ScheduleLocking scheduleLocking;
    private final TaskScheduler taskScheduler;

    @Autowired
    SickNoteMailConfiguration(SickNoteProperties sickNoteProperties, SickNoteMailService sickNoteMailService, ScheduleLocking scheduleLocking, TaskScheduler taskScheduler) {
        this.sickNoteProperties = sickNoteProperties;
        this.sickNoteMailService = sickNoteMailService;
        this.scheduleLocking = scheduleLocking;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler);
        taskRegistrar.addCronTask(
            scheduleLocking.withLock("EndOfSickPayNotification", sickNoteMailService::sendEndOfSickPayNotification),
            sickNoteProperties.getEndOfPayNotification().getCron()
        );
    }
}
