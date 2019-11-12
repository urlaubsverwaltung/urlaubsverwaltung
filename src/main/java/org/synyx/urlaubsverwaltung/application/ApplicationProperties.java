package org.synyx.urlaubsverwaltung.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Component
@ConfigurationProperties("uv.application")
@Validated
public class ApplicationProperties {

    /**
     * Checks remind date about waiting applications by default every day at 07:00 am
     */
    @NotEmpty
    private String reminderNotificationCron = "0 0 7 * * *";

    public String getReminderNotificationCron() {
        return reminderNotificationCron;
    }

    public void setReminderNotificationCron(String reminderNotificationCron) {
        this.reminderNotificationCron = reminderNotificationCron;
    }
}

