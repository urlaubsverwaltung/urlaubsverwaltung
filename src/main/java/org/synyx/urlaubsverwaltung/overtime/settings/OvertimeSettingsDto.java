package org.synyx.urlaubsverwaltung.overtime.settings;

public class OvertimeSettingsDto {

    private Long id;

    private boolean overtimeActive;

    private Integer maximumOvertime;

    private Integer minimumOvertime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isOvertimeActive() {
        return overtimeActive;
    }

    public void setOvertimeActive(boolean overtimeActive) {
        this.overtimeActive = overtimeActive;
    }

    public Integer getMaximumOvertime() {
        return maximumOvertime;
    }

    public void setMaximumOvertime(Integer maximumOvertime) {
        this.maximumOvertime = maximumOvertime;
    }

    public Integer getMinimumOvertime() {
        return minimumOvertime;
    }

    public void setMinimumOvertime(Integer minimumOvertime) {
        this.minimumOvertime = minimumOvertime;
    }
}
