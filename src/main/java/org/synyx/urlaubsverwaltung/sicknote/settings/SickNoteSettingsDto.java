package org.synyx.urlaubsverwaltung.sicknote.settings;

public class SickNoteSettingsDto {

    private Long id;
    private Integer maximumSickPayDays = 42;
    private Integer daysBeforeEndOfSickPayNotification = 7;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
