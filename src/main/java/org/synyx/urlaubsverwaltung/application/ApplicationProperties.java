package org.synyx.urlaubsverwaltung.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.synyx.urlaubsverwaltung.validation.CronExpression;

import javax.validation.Valid;

@Component
@ConfigurationProperties("uv.application")
@Validated
public class ApplicationProperties {

    @Valid
    private ReminderNotification reminderNotification = new ReminderNotification();

    public ReminderNotification getReminderNotification() {
        return reminderNotification;
    }

    public void setReminderNotification(ReminderNotification reminderNotification) {
        this.reminderNotification = reminderNotification;
    }

    public static class ReminderNotification {

        /**
         * Checks remind date about waiting applications by default every day at 07:00 am
         */
        @CronExpression
        private String cron = "0 0 7 * * *";

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }
    }
}

