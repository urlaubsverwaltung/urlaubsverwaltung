package org.synyx.urlaubsverwaltung.sicknote.settings;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "sick_note_settings")
public class SickNoteSettingsEntity {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Specifies the maximal period of sick pay in days.
     */
    private Integer maximumSickPayDays = 42;

    /**
     * Specifies when a notification about the end of sick pay should be sent to the affected person and office. (number
     * of days before the end of sick pay)
     */
    private Integer daysBeforeEndOfSickPayNotification = 7;

    public Integer getMaximumSickPayDays() {
        return maximumSickPayDays;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMaximumSickPayDays(Integer maximumSickPayDays) {
        this.maximumSickPayDays = maximumSickPayDays;
    }

    public Integer getDaysBeforeEndOfSickPayNotification() {
        return daysBeforeEndOfSickPayNotification;
    }

    public void setDaysBeforeEndOfSickPayNotification(Integer daysBeforeEndOfSickPayNotification) {
        this.daysBeforeEndOfSickPayNotification = daysBeforeEndOfSickPayNotification;
    }
}
