package org.synyx.urlaubsverwaltung.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("uv.application")
public class ApplicationProperties {

    /**
     * Checks remind date about waiting applications by default every day at 07:00 am
     */
    private String reminderNotificationCron = "0 0 7 * * *";

    public String getReminderNotificationCron() {
        return reminderNotificationCron;
    }

    public void setReminderNotificationCron(String reminderNotificationCron) {
        this.reminderNotificationCron = reminderNotificationCron;
    }
}

