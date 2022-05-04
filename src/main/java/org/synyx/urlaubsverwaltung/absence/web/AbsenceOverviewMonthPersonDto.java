package org.synyx.urlaubsverwaltung.absence.web;

import java.util.List;

public class AbsenceOverviewMonthPersonDto {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final String gravatarUrl;
    private final List<AbsenceOverviewPersonRowCellDto> days;
    private final List<AbsenceOverviewPersonPublicHolidayCellDto> publicHolidays;

    AbsenceOverviewMonthPersonDto(String firstName, String lastName, String email, String gravatarUrl, List<AbsenceOverviewPersonRowCellDto> days, List<AbsenceOverviewPersonPublicHolidayCellDto> publicHolidays) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gravatarUrl = gravatarUrl;
        this.days = days;
        this.publicHolidays = publicHolidays;
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

    public List<AbsenceOverviewPersonRowCellDto> getDays() {
        return days;
    }

    public List<AbsenceOverviewPersonPublicHolidayCellDto> getPublicHolidays() {
        return publicHolidays;
    }
}
