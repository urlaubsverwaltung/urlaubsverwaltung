package org.synyx.urlaubsverwaltung.overtime.settings;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "overtime_settings")
public class OvertimeSettingsEntity {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Activates or deactivates overtime settings.
     *
     * @since 2.15.0
     */
    @Column(name = "overtime_active")
    private boolean overtimeActive = false;

    /**
     * Defines the maximum number of overtime a person can have.
     *
     * @since 2.13.0
     */
    @Column(name = "overtime_maximum")
    private Integer maximumOvertime = 100;

    /**
     * Defines the minimum number of overtime a person can have. Minimum overtime means missing hours (equates to
     * negative)
     *
     * @since 2.15.0
     */
    @Column(name = "overtime_minimum")
    private Integer minimumOvertime = 5;

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
