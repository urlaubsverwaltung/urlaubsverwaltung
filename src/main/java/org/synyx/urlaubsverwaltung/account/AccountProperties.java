package org.synyx.urlaubsverwaltung.account;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.synyx.urlaubsverwaltung.validation.CronExpression;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties("uv.account")
@Validated
public class AccountProperties {

    @NotNull
    @Min(-1)
    @Max(366)
    @Deprecated(since = "4.4.0", forRemoval = true)
    private Integer defaultVacationDays = 20;

    @Valid
    private Update update = new Update();

    @Valid
    private AccountProperties.VacationDaysReminder vacationDaysReminder = new VacationDaysReminder();

    @Deprecated(since = "4.4.0", forRemoval = true)
    public Integer getDefaultVacationDays() {
        return defaultVacationDays;
    }

    @Deprecated(since = "4.4.0", forRemoval = true)
    public void setDefaultVacationDays(Integer defaultVacationDays) {
        this.defaultVacationDays = defaultVacationDays;
    }

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
         * Update remaining vacation days for each account by default on 1st January at 05:00 am
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
         * Remind for remaining vacation days by default on 1st January at 06:00 am
         */
        @CronExpression
        private String turnOfTheYearCron = "0 0 6 1 1 *";

        /**
         * Remind for expired remaining vacation days by default on 1st April at 06:00 am
         */
        @CronExpression
        private String expiredRemainingVacationDaysCron = "0 0 6 1 4 *";

        public String getTurnOfTheYearCron() {
            return turnOfTheYearCron;
        }

        public void setTurnOfTheYearCron(String turnOfTheYearCron) {
            this.turnOfTheYearCron = turnOfTheYearCron;
        }

        public String getExpiredRemainingVacationDaysCron() {
            return expiredRemainingVacationDaysCron;
        }

        public void setExpiredRemainingVacationDaysCron(String expiredRemainingVacationDaysCron) {
            this.expiredRemainingVacationDaysCron = expiredRemainingVacationDaysCron;
        }

        public String getVacationDaysLeftCron() {
            return vacationDaysLeftCron;
        }

        public void setVacationDaysLeftCron(String vacationDaysLeftCron) {
            this.vacationDaysLeftCron = vacationDaysLeftCron;
        }
    }
}
