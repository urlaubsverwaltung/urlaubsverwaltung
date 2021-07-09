package org.synyx.urlaubsverwaltung.specialleave;

public class SpecialLeaveSettingsDto {

    private SpecialLeave birthOfCild;
    private Integer birthOfChildDays;

    private SpecialLeave ownWedding;
    private Integer ownWeddingDays;

    private SpecialLeave deathOfSpuseOrChild;
    private Integer deathOfSpuseOrChildDays;

    private SpecialLeave deathOfParent;
    private Integer deathOfParentDays;

    private SpecialLeave relocationForOperationalReasons;
    private Integer relocationForOperationalReasonsDays;

    public SpecialLeave getBirthOfCild() {
        return birthOfCild;
    }

    public void setBirthOfCild(SpecialLeave birthOfCild) {
        this.birthOfCild = birthOfCild;
    }

    public Integer getBirthOfChildDays() {
        return birthOfChildDays;
    }

    public void setBirthOfChildDays(Integer birthOfChildDays) {
        this.birthOfChildDays = birthOfChildDays;
    }

    public SpecialLeave getOwnWedding() {
        return ownWedding;
    }

    public void setOwnWedding(SpecialLeave ownWedding) {
        this.ownWedding = ownWedding;
    }

    public Integer getOwnWeddingDays() {
        return ownWeddingDays;
    }

    public void setOwnWeddingDays(Integer ownWeddingDays) {
        this.ownWeddingDays = ownWeddingDays;
    }

    public SpecialLeave getDeathOfSpuseOrChild() {
        return deathOfSpuseOrChild;
    }

    public void setDeathOfSpuseOrChild(SpecialLeave deathOfSpuseOrChild) {
        this.deathOfSpuseOrChild = deathOfSpuseOrChild;
    }

    public Integer getDeathOfSpuseOrChildDays() {
        return deathOfSpuseOrChildDays;
    }

    public void setDeathOfSpuseOrChildDays(Integer deathOfSpuseOrChildDays) {
        this.deathOfSpuseOrChildDays = deathOfSpuseOrChildDays;
    }

    public SpecialLeave getDeathOfParent() {
        return deathOfParent;
    }

    public void setDeathOfParent(SpecialLeave deathOfParent) {
        this.deathOfParent = deathOfParent;
    }

    public Integer getDeathOfParentDays() {
        return deathOfParentDays;
    }

    public void setDeathOfParentDays(Integer deathOfParentDays) {
        this.deathOfParentDays = deathOfParentDays;
    }

    public SpecialLeave getRelocationForOperationalReasons() {
        return relocationForOperationalReasons;
    }

    public void setRelocationForOperationalReasons(SpecialLeave relocationForOperationalReasons) {
        this.relocationForOperationalReasons = relocationForOperationalReasons;
    }

    public Integer getRelocationForOperationalReasonsDays() {
        return relocationForOperationalReasonsDays;
    }

    public void setRelocationForOperationalReasonsDays(Integer relocationForOperationalReasonsDays) {
        this.relocationForOperationalReasonsDays = relocationForOperationalReasonsDays;
    }
}
