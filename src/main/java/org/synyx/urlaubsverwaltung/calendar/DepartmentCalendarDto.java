package org.synyx.urlaubsverwaltung.calendar;

public class DepartmentCalendarDto {

    private int personId;
    private int departmentId;
    private String departmentName;
    private String calendarUrl;

    /**
     * Whether this calendar is currently active/visible in the view or not.
     * Do not confuse this with app state like valid or invalid calendar.
     */
    private boolean active;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
