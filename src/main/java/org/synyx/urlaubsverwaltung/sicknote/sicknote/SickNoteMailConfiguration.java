package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.config.ScheduleLocking;

@Configuration
class SickNoteMailConfiguration implements SchedulingConfigurer {

    private final SickNoteProperties sickNoteProperties;
    private final SickNoteMailService sickNoteMailService;
    private final ScheduleLocking scheduleLocking;
    private final ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    SickNoteMailConfiguration(SickNoteProperties sickNoteProperties, SickNoteMailService sickNoteMailService, ScheduleLocking scheduleLocking, ThreadPoolTaskScheduler taskScheduler) {
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
