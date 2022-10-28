package org.synyx.urlaubsverwaltung.absence.web;

import java.util.List;

public class AbsenceOverviewMonthPersonDto {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final String gravatarUrl;
    private final List<AbsenceOverviewPersonDayDto> days;

    AbsenceOverviewMonthPersonDto(String firstName, String lastName, String email, String gravatarUrl, List<AbsenceOverviewPersonDayDto> days) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gravatarUrl = gravatarUrl;
        this.days = days;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getGravatarUrl() {
        return gravatarUrl;
    }

    public List<AbsenceOverviewPersonDayDto> getDays() {
        return days;
    }
}
