package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

/**
 * @deprecated this class has been used for the client side rendered vacation overview which is obsolete now.
 */
@Deprecated(since = "4.0.0", forRemoval = true)
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
