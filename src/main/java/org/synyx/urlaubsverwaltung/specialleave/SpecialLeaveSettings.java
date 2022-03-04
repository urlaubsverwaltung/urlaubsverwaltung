package org.synyx.urlaubsverwaltung.specialleave;

public class SpecialLeaveSettings {

    private final Integer id;
    private final Integer ownWedding;
    private final Integer birthOfChild;
    private final Integer deathOfChild;
    private final Integer deathOfParent;
    private final Integer relocationForBusinessReasons;

    public SpecialLeaveSettings() {
        this.id = null;
        this.ownWedding = null;
        this.birthOfChild = null;
        this.deathOfChild = null;
        this.deathOfParent = null;
        this.relocationForBusinessReasons = null;
    }

    public SpecialLeaveSettings(Integer id, Integer ownWedding, Integer birthOfChild, Integer deathOfChild, Integer deathOfParent, Integer relocationForBusinessReasons) {
        this.id = id;
        this.ownWedding = ownWedding;
        this.birthOfChild = birthOfChild;
        this.deathOfChild = deathOfChild;
        this.deathOfParent = deathOfParent;
        this.relocationForBusinessReasons = relocationForBusinessReasons;
    }

    public Integer getId() {
        return id;
    }

    public Integer getOwnWedding() {
        return ownWedding;
    }

    public Integer getBirthOfChild() {
        return birthOfChild;
    }

    public Integer getDeathOfChild() {
        return deathOfChild;
    }

    public Integer getDeathOfParent() {
        return deathOfParent;
    }

    public Integer getRelocationForBusinessReasons() {
        return relocationForBusinessReasons;
    }
}
