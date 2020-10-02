package org.synyx.urlaubsverwaltung.settings;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TimeSettings {

    @Column(name = "timezoneid")
    private String timeZoneId;

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }
}
