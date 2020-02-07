package org.synyx.urlaubsverwaltung.calendar;

public class DepartmentCalendarDto {

    private int personId;
    private int departmentId;
    private String departmentName;
    private String calendarUrl;

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getCalendarUrl() {
        return calendarUrl;
    }

    public void setCalendarUrl(String calendarUrl) {
        this.calendarUrl = calendarUrl;
    }
}
