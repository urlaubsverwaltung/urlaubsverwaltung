package org.synyx.urlaubsverwaltung.calendar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Component
@ConfigurationProperties("uv.ical")
@Validated
public class ICalProperties {

    /**
     * Defines how many days in past iCal calendar sharing should provide absences. Defaults to one year (356 days).
     */
    @Min(value = 0, message = "Number of days in past to sync absences with iCal calendar sharing must be positive")
    private Integer daysInPast = 356;

    public Integer getDaysInPast() {
        return daysInPast;
    }

    public void setDaysInPast(Integer daysInPast) {
        this.daysInPast = daysInPast;
    }
}

