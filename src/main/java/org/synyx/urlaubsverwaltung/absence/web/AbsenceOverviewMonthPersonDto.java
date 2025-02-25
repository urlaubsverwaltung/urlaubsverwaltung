package org.synyx.urlaubsverwaltung.absence.web;

import java.util.List;

public class AbsenceOverviewMonthPersonDto {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String initials;
    private final String gravatarUrl;
    private final List<AbsenceOverviewPersonDayDto> days;

    AbsenceOverviewMonthPersonDto(Long id, String firstName, String lastName, String initials, String gravatarUrl, List<AbsenceOverviewPersonDayDto> days) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.initials = initials;
        this.gravatarUrl = gravatarUrl;
        this.days = days;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getInitials() {
        return initials;
    }

    public String getGravatarUrl() {
        return gravatarUrl;
    }

    public List<AbsenceOverviewPersonDayDto> getDays() {
        return days;
    }
}
