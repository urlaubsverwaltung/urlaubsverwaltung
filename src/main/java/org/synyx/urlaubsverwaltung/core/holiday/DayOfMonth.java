package org.synyx.urlaubsverwaltung.core.holiday;

public class DayOfMonth {

    public enum TypeOfDay {
        WORKDAY, WEEKEND
    }

    private Integer dayNumber;
    private String dayText;
    private TypeOfDay typeOfDay;

    public String getDayText() {
        return dayText;
    }

    public void setDayText(String day) {
        this.dayText = day;
    }

    public Integer getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }

    public TypeOfDay getTypeOfDay() {
        return typeOfDay;
    }

    public void setTypeOfDay(TypeOfDay typeOfDay) {
        this.typeOfDay = typeOfDay;
    }
}
