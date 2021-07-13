package org.synyx.urlaubsverwaltung.overtime.settings;

public class OvertimeSettingsDto {

    private Long id;

    private Boolean overtimeActive;

    private Boolean overtimeReductionWithoutApplicationActive;

    private Boolean overtimeWritePrivilegedOnly;

    private Integer maximumOvertime;

    private Integer minimumOvertime;

    private Integer minimumOvertimeReduction;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOvertimeActive(Boolean overtimeActive) {
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

    public Boolean getOvertimeActive() {
        return overtimeActive;
    }

    public Boolean getOvertimeReductionWithoutApplicationActive() {
        return overtimeReductionWithoutApplicationActive;
    }

    public void setOvertimeReductionWithoutApplicationActive(Boolean overtimeReductionWithoutApplicationActive) {
        this.overtimeReductionWithoutApplicationActive = overtimeReductionWithoutApplicationActive;
    }

    public Boolean getOvertimeWritePrivilegedOnly() {
        return overtimeWritePrivilegedOnly;
    }

    public void setOvertimeWritePrivilegedOnly(Boolean overtimeWritePrivilegedOnly) {
        this.overtimeWritePrivilegedOnly = overtimeWritePrivilegedOnly;
    }

    public Integer getMinimumOvertimeReduction() {
        return minimumOvertimeReduction;
    }

    public void setMinimumOvertimeReduction(Integer minimumOvertimeReduction) {
        this.minimumOvertimeReduction = minimumOvertimeReduction;
    }
}
