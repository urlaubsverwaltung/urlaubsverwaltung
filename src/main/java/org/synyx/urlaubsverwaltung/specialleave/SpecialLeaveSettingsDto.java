package org.synyx.urlaubsverwaltung.specialleave;

public class SpecialLeaveSettingsDto {

    private Integer id;
    private Integer ownWedding;
    private Integer birthOfChild;
    private Integer deathOfChild;
    private Integer deathOfParent;
    private Integer relocationForBusinessReasons;


    public static SpecialLeaveSettingsDto mapToDto(SpecialLeaveSettings specialLeaveSettings) {
        final SpecialLeaveSettingsDto specialLeaveSettingsDto = new SpecialLeaveSettingsDto();
        specialLeaveSettingsDto.setId(specialLeaveSettings.getId());
        specialLeaveSettingsDto.setOwnWedding(specialLeaveSettings.getOwnWedding());
        specialLeaveSettingsDto.setBirthOfChild(specialLeaveSettings.getBirthOfChild());
        specialLeaveSettingsDto.setDeathOfChild(specialLeaveSettings.getDeathOfChild());
        specialLeaveSettingsDto.setDeathOfParent(specialLeaveSettings.getDeathOfParent());
        specialLeaveSettingsDto.setRelocationForBusinessReasons(specialLeaveSettings.getRelocationForBusinessReasons());
        return specialLeaveSettingsDto;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public Integer getOwnWedding() {
        return ownWedding;
    }

    public void setOwnWedding(Integer ownWedding) {
        this.ownWedding = ownWedding;
    }

    public Integer getBirthOfChild() {
        return birthOfChild;
    }

    public void setBirthOfChild(Integer birthOfChild) {
        this.birthOfChild = birthOfChild;
    }

    public Integer getDeathOfChild() {
        return deathOfChild;
    }

    public void setDeathOfChild(Integer deathOfChild) {
        this.deathOfChild = deathOfChild;
    }

    public Integer getDeathOfParent() {
        return deathOfParent;
    }

    public void setDeathOfParent(Integer deathOfParent) {
        this.deathOfParent = deathOfParent;
    }

    public Integer getRelocationForBusinessReasons() {
        return relocationForBusinessReasons;
    }

    public void setRelocationForBusinessReasons(Integer relocationForBusinessReasons) {
        this.relocationForBusinessReasons = relocationForBusinessReasons;
    }
}
