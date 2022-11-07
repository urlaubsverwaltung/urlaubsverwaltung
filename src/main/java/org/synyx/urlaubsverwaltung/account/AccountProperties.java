package org.synyx.urlaubsverwaltung.account;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.synyx.urlaubsverwaltung.validation.CronExpression;

@Component
@ConfigurationProperties("uv.account")
@Validated
public class AccountProperties {

    @Valid
    private Update update = new Update();

    @Valid
    private AccountProperties.VacationDaysReminder vacationDaysReminder = new VacationDaysReminder();

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    public VacationDaysReminder getVacationDaysReminder() {
        return vacationDaysReminder;
    }

    public void setVacationDaysReminder(VacationDaysReminder vacationDaysReminder) {
        this.vacationDaysReminder = vacationDaysReminder;
    }

    public static class Update {

        /**
         * Creates or updates the users account for the new year by default on 1st January at 05:00 am
         */
        @CronExpression
        private String cron = "0 0 5 1 1 *";

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }
    }

    public static class VacationDaysReminder {

        /**
         * Remind for vacation days left by default on 1st October at 06:00 am
         */
        @CronExpression
        private String vacationDaysLeftCron = "0 0 6 1 10 *";

        /**
         * Remind for expired remaining vacation days by default every day at 06:00
         */
        @CronExpression
        private String expiredRemainingVacationDaysCron = "0 0 6 * * *";

        public String getVacationDaysLeftCron() {
            return vacationDaysLeftCron;
        }

        public void setVacationDaysLeftCron(String vacationDaysLeftCron) {
            this.vacationDaysLeftCron = vacationDaysLeftCron;
        }

        public String getExpiredRemainingVacationDaysCron() {
            return expiredRemainingVacationDaysCron;
        }

        public void setExpiredRemainingVacationDaysCron(String expiredRemainingVacationDaysCron) {
            this.expiredRemainingVacationDaysCron = expiredRemainingVacationDaysCron;
        }
    }
}
