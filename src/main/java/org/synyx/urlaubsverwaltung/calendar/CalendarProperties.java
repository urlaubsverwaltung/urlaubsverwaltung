package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;

@Component
@ConfigurationProperties("uv.calendar")
@Validated
public class CalendarProperties {

    /**
     * Adds the organizer to all iCal events that
     * where generated through the calender integration
     * feature
     */
    @Email
    private String organizer;

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }
}
