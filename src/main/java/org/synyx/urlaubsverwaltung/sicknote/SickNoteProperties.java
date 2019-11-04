package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("uv.sick-note")
public class SickNoteProperties {

    /**
     * Send notification about the end of sick pay by default every day at 06:00 am
     */
    private String endOfPayNotificationCron = "0 0 6 * * *";

    public String getEndOfPayNotificationCron() {
        return endOfPayNotificationCron;
    }

    public void setEndOfPayNotificationCron(String endOfPayNotificationCron) {
        this.endOfPayNotificationCron = endOfPayNotificationCron;
    }
}

