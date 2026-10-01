package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.synyx.urlaubsverwaltung.validation.CronExpression;

@Component
@ConfigurationProperties("uv.sick-note")
@Validated
public class SickNoteProperties {

    @Valid
    private EndOfPayNotification endOfPayNotification = new EndOfPayNotification();

    @Valid
    private MissingAubNotification missingAubNotification = new MissingAubNotification();

    public EndOfPayNotification getEndOfPayNotification() {
        return endOfPayNotification;
    }

    public void setEndOfPayNotification(EndOfPayNotification endOfPayNotification) {
        this.endOfPayNotification = endOfPayNotification;
    }

    public MissingAubNotification getMissingAubNotification() {
        return missingAubNotification;
    }

    public void setMissingAubNotification(MissingAubNotification missingAubNotification) {
        this.missingAubNotification = missingAubNotification;
    }

    public static class EndOfPayNotification {

        /**
         * Send notification about the end of sick pay by default every day at 06:00 am
         */
        @CronExpression
        private String cron = "0 0 6 * * *";

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }
    }

    public static class MissingAubNotification {

        /**
         * Send notification about sick notes without AU-Bescheinigung reaching the configured work day by default every
         * day at 06:00 am
         */
        @CronExpression
        private String cron = "0 0 6 * * *";

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }
    }
}
