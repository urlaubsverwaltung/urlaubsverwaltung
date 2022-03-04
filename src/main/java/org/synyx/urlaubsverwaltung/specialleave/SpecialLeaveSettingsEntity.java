package org.synyx.urlaubsverwaltung.specialleave;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "special_leave_settings")
class SpecialLeaveSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer ownWedding;
    private Integer birthOfChild;
    private Integer deathOfChild;
    private Integer deathOfParent;
    private Integer relocationForBusinessReasons;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
