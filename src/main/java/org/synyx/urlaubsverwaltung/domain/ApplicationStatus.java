package org.synyx.urlaubsverwaltung.domain;

/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public enum ApplicationStatus {

    WAITING("state.waiting"),
    ALLOWED("state.allowed"),
    REJECTED("state.rejected"),
    CANCELLED("state.cancelled");

    private String state;

    private ApplicationStatus(String state) {

        this.state = state;
    }

    public String getState() {

        return this.state;
    }
}
