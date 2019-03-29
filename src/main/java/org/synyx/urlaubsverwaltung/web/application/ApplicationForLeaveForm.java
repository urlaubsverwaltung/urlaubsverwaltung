
package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.sql.Time;


/**
 * View class representing an application for leave.
 */
public class ApplicationForLeaveForm {

    // person of the application for leave
    private Person person;

    // period: date and time
    private DateMidnight startDate;

    private Time startTime;

    private DateMidnight endDate;

    private Time endTime;

    // Type of holiday, e.g. holiday, special leave, etc.
    private VacationType vacationType;

    // length of day: contains time of day (morning, noon or full day) and value (1.0 or 0.5 - as BigDecimal)
    private DayLength dayLength;

    // hours are relevant for overtime reduction
    private BigDecimal hours;

    // For special and unpaid leave a reason is required
    private String reason;

    // Stands in while the person is on holiday
    private Person holidayReplacement;

    // Address and phone number during holiday
    private String address;

    private boolean teamInformed;

    private String comment;

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public String getAddress() {

        return address;
    }


    public void setAddress(String address) {

        this.address = address;
    }


    public DateMidnight getEndDate() {

        return endDate;
    }


    public void setEndDate(DateMidnight endDate) {

        this.endDate = endDate;
    }


    public DayLength getDayLength() {

        return dayLength;
    }


    public void setDayLength(DayLength dayLength) {

        this.dayLength = dayLength;
    }


    public BigDecimal getHours() {

        return hours;
    }


    public void setHours(BigDecimal hours) {

        this.hours = hours;
    }


    public String getReason() {

        return reason;
    }


    public void setReason(String reason) {

        this.reason = reason;
    }


    public Person getHolidayReplacement() {

        return holidayReplacement;
    }


    public void setHolidayReplacement(Person holidayReplacement) {

        this.holidayReplacement = holidayReplacement;
    }


    public DateMidnight getStartDate() {

        return startDate;
    }


    public void setStartDate(DateMidnight startDate) {

        this.startDate = startDate;
    }


    public Time getStartTime() {

        return startTime;
    }


    public void setStartTime(Time startTime) {

        this.startTime = startTime;
    }


    public Time getEndTime() {

        return endTime;
    }


    public void setEndTime(Time endTime) {

        this.endTime = endTime;
    }


    public VacationType getVacationType() {

        return vacationType;
    }


    public void setVacationType(VacationType vacationType) {

        this.vacationType = vacationType;
    }


    public String getComment() {

        return comment;
    }


    public void setComment(String comment) {

        this.comment = comment;
    }


    public boolean isTeamInformed() {

        return teamInformed;
    }


    public void setTeamInformed(boolean teamInformed) {

        this.teamInformed = teamInformed;
    }


    public Application generateApplicationForLeave() {

        Application applicationForLeave = new Application();

        applicationForLeave.setPerson(person);

        applicationForLeave.setStartDate(startDate);
        applicationForLeave.setEndDate(endDate);
        applicationForLeave.setStartTime(startTime);
        applicationForLeave.setEndTime(endTime);

        applicationForLeave.setVacationType(vacationType);
        applicationForLeave.setDayLength(dayLength);
        applicationForLeave.setReason(reason);
        applicationForLeave.setHolidayReplacement(holidayReplacement);
        applicationForLeave.setAddress(address);
        applicationForLeave.setTeamInformed(teamInformed);

        if (VacationCategory.OVERTIME.equals(vacationType.getCategory())) {
            applicationForLeave.setHours(hours);
        }

        return applicationForLeave;
    }

    @Override
    public String toString() {
        return "ApplicationForLeaveForm{" +
                "person=" + person +
                ", startDate=" + startDate +
                ", startTime=" + startTime +
                ", endDate=" + endDate +
                ", endTime=" + endTime +
                ", vacationType=" + vacationType +
                ", dayLength=" + dayLength +
                ", hours=" + hours +
                ", reason='" + reason + '\'' +
                ", holidayReplacement=" + holidayReplacement +
                ", address='" + address + '\'' +
                ", teamInformed=" + teamInformed +
                ", comment='" + comment + '\'' +
                '}';
    }
}
