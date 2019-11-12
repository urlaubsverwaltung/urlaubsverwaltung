package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Component
@ConfigurationProperties("uv.sick-note")
@Validated
public class SickNoteProperties {

    /**
     * Send notification about the end of sick pay by default every day at 06:00 am
     */
    @NotEmpty
    private String endOfPayNotificationCron = "0 0 6 * * *";

    public String getEndOfPayNotificationCron() {
        return endOfPayNotificationCron;
    }

    public void setEndOfPayNotificationCron(String endOfPayNotificationCron) {
        this.endOfPayNotificationCron = endOfPayNotificationCron;
    }
}

