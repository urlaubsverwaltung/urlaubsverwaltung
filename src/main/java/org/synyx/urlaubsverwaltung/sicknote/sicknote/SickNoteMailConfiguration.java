package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.config.ScheduleLocking;

@Configuration
class SickNoteMailConfiguration implements SchedulingConfigurer {

    private final SickNoteProperties sickNoteProperties;
    private final SickNoteMailService sickNoteMailService;
    private final ScheduleLocking scheduleLocking;

    @Autowired
    SickNoteMailConfiguration(SickNoteProperties sickNoteProperties, SickNoteMailService sickNoteMailService, ScheduleLocking scheduleLocking) {
        this.sickNoteProperties = sickNoteProperties;
        this.sickNoteMailService = sickNoteMailService;
        this.scheduleLocking = scheduleLocking;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addCronTask(
            scheduleLocking.withLock("EndOfSickPayNotification", sickNoteMailService::sendEndOfSickPayNotification),
            sickNoteProperties.getEndOfPayNotification().getCron()
        );
    }
}
