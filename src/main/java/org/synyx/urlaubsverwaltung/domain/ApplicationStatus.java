package org.synyx.urlaubsverwaltung.domain;

/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public enum ApplicationStatus {

    WAITING("state.wait"),
    ALLOWED("state.ok"),
    REJECTED("state.no"),
    CANCELLED("state.storniert");

    private String state;

    private ApplicationStatus(String state) {

        this.state = state;
    }

    public String getState() {

        return this.state;
    }
}
