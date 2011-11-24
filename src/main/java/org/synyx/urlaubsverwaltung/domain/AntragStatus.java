package org.synyx.urlaubsverwaltung.domain;

/**
 * @author  johannes
 */
public enum AntragStatus {

    WARTEND("state.wait"),
    GENEHMIGT("state.ok"),
    ABGELEHNT("state.no"),
    STORNIERT("state.storniert");

    private String stateName;

    private AntragStatus(String stateName) {

        this.stateName = stateName;
    }

    public String getStateName() {

        return this.stateName;
    }
}
