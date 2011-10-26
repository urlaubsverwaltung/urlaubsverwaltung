package org.synyx.urlaubsverwaltung.domain;

/**
 * @author  johannes
 */
public enum State {

    WARTEND("wartend"),
    GENEHMIGT("genehmigt"),
    ABGELEHNT("abgelehnt"),
    STORNIERT("storniert");

    private String stateName;

    private State(String stateName) {

        this.stateName = stateName;
    }

    public String getStateName() {

        return this.stateName;
    }
}
