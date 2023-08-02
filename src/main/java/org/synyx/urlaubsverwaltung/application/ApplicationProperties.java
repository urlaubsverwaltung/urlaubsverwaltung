package org.synyx.urlaubsverwaltung.application;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.synyx.urlaubsverwaltung.validation.CronExpression;

@Component
@ConfigurationProperties("uv.application")
@Validated
public class ApplicationProperties {

    /*
     * Checks every day at 07:00 am if remind mails for upcoming replacement applications can be sent
     */
    @Valid
    private ReminderNotification upcomingHolidayReplacementNotification = new ReminderNotification();

    /*
     * Checks every day at 07:00 am if remind mails for upcoming applications can be sent
     */
    @Valid
    private ReminderNotification upcomingNotification = new ReminderNotification();

    /*
     * Checks remind date about waiting applications by default every day at 07:00 am
     */
    @Valid
    private ReminderNotification reminderNotification = new ReminderNotification();

    public ReminderNotification getUpcomingHolidayReplacementNotification() {
        return upcomingHolidayReplacementNotification;
    }

    public void setUpcomingHolidayReplacementNotification(ReminderNotification upcomingHolidayReplacementNotification) {
        this.upcomingHolidayReplacementNotification = upcomingHolidayReplacementNotification;
    }

    public ReminderNotification getUpcomingNotification() {
        return upcomingNotification;
    }

    public void setUpcomingNotification(ReminderNotification upcomingNotification) {
        this.upcomingNotification = upcomingNotification;
    }

    public ReminderNotification getReminderNotification() {
        return reminderNotification;
    }

    public void setReminderNotification(ReminderNotification reminderNotification) {
        this.reminderNotification = reminderNotification;
    }

    public static class ReminderNotification {

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

