package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.format.annotation.DateTimeFormat;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNullElse;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.HH_MM;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.HH_MM_SS;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

/**
 * View class representing an application for leave.
 */
public class ApplicationForLeaveForm {

    // person of the application for leave
    private Person person;

    // period: date and time
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate startDate;

    @DateTimeFormat(pattern = HH_MM, fallbackPatterns = HH_MM_SS)
    private LocalTime startTime;

    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate endDate;

    @DateTimeFormat(pattern = HH_MM, fallbackPatterns = HH_MM_SS)
    private LocalTime endTime;

    // Type of holiday, e.g. holiday, special leave, etc.
    private ApplicationForLeaveFormVacationTypeDto vacationType;

    // length of day: contains time of day (morning, noon or full day) and value (1.0 or 0.5 - as BigDecimal)
    private DayLength dayLength = FULL;

    // hours are relevant for overtime reduction, decimal input possible
    private BigDecimal hours;

    // minutes of overtime reduction
    private Integer minutes;

    // For special and unpaid leave a reason is required
    private String reason;

    private Person holidayReplacementToAdd;

    private List<HolidayReplacementDto> holidayReplacements = new ArrayList<>();

    // Address and phone number during holiday
    private String address;

    private boolean teamInformed;

    private String comment;

    private Long id;

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

    public String getEndDateIsoValue() {
        if (endDate == null) {
            return "";
        }

        return endDate.format(DateTimeFormatter.ISO_DATE);
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

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStartDateIsoValue() {
        if (startDate == null) {
            return "";
        }

        return startDate.format(DateTimeFormatter.ISO_DATE);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public ApplicationForLeaveFormVacationTypeDto getVacationType() {
        return vacationType;
    }

    public void setVacationType(ApplicationForLeaveFormVacationTypeDto vacationType) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<HolidayReplacementDto> getHolidayReplacements() {
        return holidayReplacements;
    }

    public void setHolidayReplacements(List<HolidayReplacementDto> holidayReplacements) {
        this.holidayReplacements = holidayReplacements;
    }

    public Person getHolidayReplacementToAdd() {
        return holidayReplacementToAdd;
    }

    public void setHolidayReplacementToAdd(Person holidayReplacementToAdd) {
        this.holidayReplacementToAdd = holidayReplacementToAdd;
    }

    /**
     * @return the hours and minutes fields mapped to a {@link Duration}
     */
    public Duration getOvertimeReduction() {

        if (hours == null && minutes == null) {
            return null;
        }

        final BigDecimal originalHours = requireNonNullElse(hours, BigDecimal.ZERO);
        final int originalMinutes = requireNonNullElse(minutes, 0);

        return Duration.ofMinutes(originalHours.multiply(BigDecimal.valueOf(60)).longValue() + originalMinutes);
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
            ", minutes=" + minutes +
            ", holidayReplacements=" + holidayReplacements +
            ", teamInformed=" + teamInformed +
            '}';
    }

    public static class Builder {

        private Person person;
        private LocalDate startDate;
        private LocalTime startTime;
        private LocalDate endDate;
        private LocalTime endTime;
        private ApplicationForLeaveFormVacationTypeDto vacationType;
        private DayLength dayLength;
        private BigDecimal hours;
        private Integer minutes;
        private String reason;
        private Person holidayReplacementToAdd;
        private List<HolidayReplacementDto> holidayReplacements;
        private String address;
        private boolean teamInformed;
        private String comment;
        private Long id;

        public ApplicationForLeaveForm.Builder person(Person person) {
            this.person = person;
            return this;
        }

        public ApplicationForLeaveForm.Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public ApplicationForLeaveForm.Builder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public ApplicationForLeaveForm.Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public ApplicationForLeaveForm.Builder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public ApplicationForLeaveForm.Builder vacationType(ApplicationForLeaveFormVacationTypeDto vacationType) {
            this.vacationType = vacationType;
            return this;
        }

        public ApplicationForLeaveForm.Builder dayLength(DayLength dayLength) {
            this.dayLength = dayLength;
            return this;
        }

        public ApplicationForLeaveForm.Builder hoursAndMinutes(Duration hours) {

            if (hours != null) {
                final BigDecimal overtimeReduction = BigDecimal.valueOf((double) hours.toMinutes() / 60);
                this.hours = overtimeReduction.setScale(0, RoundingMode.DOWN).abs();
                this.minutes = overtimeReduction.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_EVEN).abs().intValueExact();
            }

            return this;
        }

        public ApplicationForLeaveForm.Builder reason(String reason) {
            this.reason = reason;
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

        public ApplicationForLeaveForm.Builder id(Long id) {
            this.id = id;
            return this;
        }

        public ApplicationForLeaveForm.Builder holidayReplacements(List<HolidayReplacementDto> holidayReplacementDtos) {
            this.holidayReplacements = holidayReplacementDtos;
            return this;
        }

        public ApplicationForLeaveForm.Builder holidayReplacementToAdd(Person holidayReplacementToAdd) {
            this.holidayReplacementToAdd = holidayReplacementToAdd;
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
            form.setMinutes(minutes);
            form.setReason(reason);
            form.setHolidayReplacements(holidayReplacements);
            form.setAddress(address);
            form.setTeamInformed(teamInformed);
            form.setComment(comment);
            form.setId(id);
            form.setHolidayReplacements(holidayReplacements);
            form.setHolidayReplacementToAdd(holidayReplacementToAdd);

            return form;
        }
    }
}
