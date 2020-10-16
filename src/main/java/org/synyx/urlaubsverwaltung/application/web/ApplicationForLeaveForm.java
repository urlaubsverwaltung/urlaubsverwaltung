package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;


/**
 * View class representing an application for leave.
 */
public class ApplicationForLeaveForm {

    // person of the application for leave
    private Person person;

    // period: date and time
    private LocalDate startDate;

    private Time startTime;

    private LocalDate endDate;

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

    private Integer id;

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


    public LocalDate getEndDate() {

        return endDate;
    }


    public void setEndDate(LocalDate endDate) {

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


    public LocalDate getStartDate() {

        return startDate;
    }


    public void setStartDate(LocalDate startDate) {

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

        applicationForLeave.setId(id);
        applicationForLeave.setPerson(person);

        applicationForLeave.setStartDate(startDate);
        applicationForLeave.setStartTime(startTime);

        applicationForLeave.setEndDate(endDate);
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
            ", holidayReplacement=" + holidayReplacement +
            ", address='" + address + '\'' +
            ", teamInformed=" + teamInformed +
            '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public static class Builder {

        private Person person;
        private LocalDate startDate;
        private Time startTime;
        private LocalDate endDate;
        private Time endTime;
        private VacationType vacationType;
        private DayLength dayLength;
        private BigDecimal hours;
        private String reason;
        private Person holidayReplacement;
        private String address;
        private boolean teamInformed;
        private String comment;
        private Integer id;

        public ApplicationForLeaveForm.Builder person(Person person) {
            this.person = person;
            return this;
        }

        public ApplicationForLeaveForm.Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public ApplicationForLeaveForm.Builder startTime(Time startTime) {
            this.startTime = startTime;
            return this;
        }

        public ApplicationForLeaveForm.Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public ApplicationForLeaveForm.Builder endTime(Time endTime) {
            this.endTime = endTime;
            return this;
        }

        public ApplicationForLeaveForm.Builder vacationType(VacationType vacationType) {
            this.vacationType = vacationType;
            return this;
        }

        public ApplicationForLeaveForm.Builder dayLength(DayLength dayLength) {
            this.dayLength = dayLength;
            return this;
        }

        public ApplicationForLeaveForm.Builder hours(BigDecimal hours) {
            this.hours = hours;
            return this;
        }

        public ApplicationForLeaveForm.Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public ApplicationForLeaveForm.Builder holidayReplacement(Person holidayReplacement) {
            this.holidayReplacement = holidayReplacement;
            return this;
        }

        public ApplicationForLeaveForm.Builder address(String address) {
            this.address = address;
            return this;
        }

        public ApplicationForLeaveForm.Builder teamInformed(boolean teamInformed) {
            this.teamInformed = teamInformed;
            return this;
        }

        public ApplicationForLeaveForm.Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public ApplicationForLeaveForm.Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public ApplicationForLeaveForm build() {

            final ApplicationForLeaveForm form = new ApplicationForLeaveForm();

            form.setPerson(person);
            form.setStartDate(startDate);
            form.setStartTime(startTime);
            form.setEndDate(endDate);
            form.setEndTime(endTime);
            form.setVacationType(vacationType);
            form.setDayLength(dayLength);
            form.setHours(hours);
            form.setReason(reason);
            form.setHolidayReplacement(holidayReplacement);
            form.setAddress(address);
            form.setTeamInformed(teamInformed);
            form.setComment(comment);
            form.setId(id);

            return form;
        }
    }
}
