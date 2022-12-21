package org.synyx.urlaubsverwaltung.application.export;

import org.synyx.urlaubsverwaltung.application.application.ApplicationForLeave;

import java.util.List;

public class ApplicationForLeaveExport {

    private final String personalNumber;
    private final String firstName;
    private final String lastName;
    private final List<ApplicationForLeave> applicationForLeaves;
    private final List<String> departments;

    ApplicationForLeaveExport(String personalNumber, String firstName, String lastName, List<ApplicationForLeave> applicationForLeaves, List<String> departments) {
        this.personalNumber = personalNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.applicationForLeaves = applicationForLeaves;
        this.departments = departments;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<ApplicationForLeave> getApplicationForLeaves() {
        return applicationForLeaves;
    }

    public List<String> getDepartments() {
        return departments;
    }
}
