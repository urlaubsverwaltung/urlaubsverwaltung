package org.synyx.urlaubsverwaltung.account;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.synyx.urlaubsverwaltung.validation.CronExpression;

import javax.validation.Valid;

@Component
@ConfigurationProperties("uv.account")
@Validated
public class AccountProperties {

    @Valid
    private Update update = new Update();

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
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
}
