package org.synyx.urlaubsverwaltung.domain;

/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public enum ApplicationStatus {

    WAITING("state.waiting", 0),
    ALLOWED("state.allowed", 1),
    REJECTED("state.rejected", 2),
    CANCELLED("state.cancelled", 3);

    private String state;

    private int number;

    private ApplicationStatus(String state, int number) {

        this.state = state;
        this.number = number;
    }

    public String getState() {

        return this.state;
    }


    public int getNumber() {

        return number;
    }


    public void setNumber(int number) {

        this.number = number;
    }
}
