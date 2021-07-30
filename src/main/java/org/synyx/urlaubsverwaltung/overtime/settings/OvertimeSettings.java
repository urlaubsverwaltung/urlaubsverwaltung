package org.synyx.urlaubsverwaltung.overtime.settings;

public class OvertimeSettings {

    private Long id;
    private boolean overtimeActive = false;
    private boolean overtimeReductionWithoutApplicationActive = true;
    private boolean overtimeWritePrivilegedOnly = false;
    private Integer maximumOvertime = 100;
    private Integer minimumOvertime = 5;
    private Integer minimumOvertimeReduction = 0;

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

    public boolean isOvertimeReductionWithoutApplicationActive() {
        return overtimeReductionWithoutApplicationActive;
    }

    public void setOvertimeReductionWithoutApplicationActive(boolean overtimeReductionWithoutApplicationActive) {
        this.overtimeReductionWithoutApplicationActive = overtimeReductionWithoutApplicationActive;
    }

    public boolean isOvertimeWritePrivilegedOnly() {
        return overtimeWritePrivilegedOnly;
    }

    public void setOvertimeWritePrivilegedOnly(boolean overtimeWritePrivilegedOnly) {
        this.overtimeWritePrivilegedOnly = overtimeWritePrivilegedOnly;
    }

    public Integer getMinimumOvertimeReduction() {
        return minimumOvertimeReduction;
    }

    public void setMinimumOvertimeReduction(Integer minimumOvertimeReduction) {
        this.minimumOvertimeReduction = minimumOvertimeReduction;
    }
}
