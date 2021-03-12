package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.holidayreplacement.HolidayReplacementDto;
import org.synyx.urlaubsverwaltung.holidayreplacement.HolidayReplacementEntity;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import static java.util.Objects.requireNonNullElse;


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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Person> getHolidayReplacementPersons() {

        return ofNullable(holidayReplacements)
            .orElse(emptyList()).stream()
            .map(HolidayReplacementDto::getPerson)
            .collect(toList());
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

    public Application generateApplicationForLeave() {

        final List<HolidayReplacementEntity> replacementEntities = holidayReplacements.stream()
            .map(HolidayReplacementEntity::from)
            .collect(toList());

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
        applicationForLeave.setHolidayReplacements(replacementEntities);
        applicationForLeave.setAddress(address);
        applicationForLeave.setTeamInformed(teamInformed);

        if (VacationCategory.OVERTIME.equals(vacationType.getCategory())) {
            applicationForLeave.setHours(getOvertimeReduction());
        }

        return applicationForLeave;
    }

    /**
     * @return the hours and minutes fields mapped to a {@link Duration}
     */
    Duration getOvertimeReduction() {

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
            ", address='" + address + '\'' +
            ", teamInformed=" + teamInformed +
            '}';
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
        private Integer minutes;
        private String reason;
        private Person holidayReplacementToAdd;
        private List<HolidayReplacementDto> holidayReplacements;
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

        public ApplicationForLeaveForm.Builder id(Integer id) {
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
