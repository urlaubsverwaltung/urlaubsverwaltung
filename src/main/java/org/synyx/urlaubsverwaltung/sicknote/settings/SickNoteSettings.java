package org.synyx.urlaubsverwaltung.sicknote.settings;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Settings concerning absence of persons because of vacation or sick days.
 */
@Embeddable
public class SickNoteSettings implements Serializable {

    /**
     * Specifies the maximal period of sick pay in days.
     */
    private Integer maximumSickPayDays = 42;

    /**
     * Specifies when a notification about the end of sick pay should be sent to the affected person and office. (number
     * of days before the end of sick pay)
     */
    private Integer daysBeforeEndOfSickPayNotification = 7;

    /**
     * Allows users to submit sicknotes for themselves
     */
    private boolean userIsAllowedToSubmitSickNotes = false;

    public Integer getMaximumSickPayDays() {
        return maximumSickPayDays;
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

    public boolean getUserIsAllowedToSubmitSickNotes() {
        return userIsAllowedToSubmitSickNotes;
    }

    public void setUserIsAllowedToSubmitSickNotes(boolean userIsAllowedToSubmitSickNotes) {
        this.userIsAllowedToSubmitSickNotes = userIsAllowedToSubmitSickNotes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SickNoteSettings that = (SickNoteSettings) o;
        return userIsAllowedToSubmitSickNotes == that.userIsAllowedToSubmitSickNotes
            && Objects.equals(maximumSickPayDays, that.maximumSickPayDays)
            && Objects.equals(daysBeforeEndOfSickPayNotification, that.daysBeforeEndOfSickPayNotification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maximumSickPayDays, daysBeforeEndOfSickPayNotification, userIsAllowedToSubmitSickNotes);
    }
}
