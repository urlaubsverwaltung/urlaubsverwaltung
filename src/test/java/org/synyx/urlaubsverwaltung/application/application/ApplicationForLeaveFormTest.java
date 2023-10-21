package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

class ApplicationForLeaveFormTest {

    @Test
    void ensureSettingNullHourDurationWithBuilderDoesNotSetHoursAndMinutes() {
        final ApplicationForLeaveForm form = new ApplicationForLeaveForm.Builder()
            .hoursAndMinutes(null)
            .build();

        assertThat(form.getHours()).isNull();
        assertThat(form.getMinutes()).isNull();
    }

    @Test
    void ensureBuilderSetsAllPropertiesCorrectly() {

        final Person person = new Person();

        final LocalDate startDate = LocalDate.now().minusDays(10);
        final LocalTime startTime = LocalTime.now().minusHours(5);

        final LocalDate endDate = LocalDate.now().minusDays(2);
        final LocalTime endTime = LocalTime.now().minusHours(7);

        final HolidayReplacementDto holidayReplacementDto = new HolidayReplacementDto();

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("vacation type label");
        vacationTypeDto.setCategory(HOLIDAY);

        final ApplicationForLeaveForm form = new ApplicationForLeaveForm.Builder()
            .person(person)
            .startDate(startDate)
            .startTime(startTime)
            .endDate(endDate)
            .endTime(endTime)
            .vacationType(vacationTypeDto)
            .dayLength(DayLength.ZERO)
            .hoursAndMinutes(Duration.ofMinutes(75))
            .reason("Good one.")
            .holidayReplacements(List.of(holidayReplacementDto))
            .address("Gartenstrasse 67")
            .teamInformed(true)
            .comment("Welcome!")
            .build();

        assertThat(form.getPerson()).isEqualTo(person);
        assertThat(form.getStartDate()).isEqualTo(startDate);
        assertThat(form.getStartTime()).isEqualTo(startTime);
        assertThat(form.getEndDate()).isEqualTo(endDate);
        assertThat(form.getEndTime()).isEqualTo(endTime);
        assertThat(form.getVacationType()).isSameAs(vacationTypeDto);
        assertThat(form.getDayLength()).isEqualTo(DayLength.ZERO);
        assertThat(form.getHours()).isEqualTo(ONE);
        assertThat(form.getMinutes()).isEqualTo(15);
        assertThat(form.getReason()).isEqualTo("Good one.");
        assertThat(form.getHolidayReplacements()).contains(holidayReplacementDto);
        assertThat(form.getAddress()).isEqualTo("Gartenstrasse 67");
        assertThat(form.isTeamInformed()).isTrue();
        assertThat(form.getComment()).isEqualTo("Welcome!");
    }

    @Test
    void ensureEmptyStartDateIsoValue() {

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();

        applicationForLeaveForm.setStartDate(null);

        assertThat(applicationForLeaveForm.getStartDateIsoValue()).isEmpty();
    }

    @Test
    void ensureStartDateIsoValue() {

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();

        applicationForLeaveForm.setStartDate(LocalDate.parse("2020-10-30"));

        assertThat(applicationForLeaveForm.getStartDateIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void ensureEmptyEndDateIsoValue() {

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();

        applicationForLeaveForm.setEndDate(null);

        assertThat(applicationForLeaveForm.getEndDateIsoValue()).isEmpty();
    }

    @Test
    void ensureEndDateIsoValue() {

        final ApplicationForLeaveForm applicationForLeaveForm = new ApplicationForLeaveForm();

        applicationForLeaveForm.setEndDate(LocalDate.parse("2020-10-30"));

        assertThat(applicationForLeaveForm.getEndDateIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void ensureCorrectDurationCalculation() {
        assertThat(formWithOvertime(ONE, 15).getOvertimeReduction()).isEqualTo(Duration.ofMinutes(75));
        assertThat(formWithOvertime(BigDecimal.valueOf(1.25), 0).getOvertimeReduction()).isEqualTo(Duration.ofMinutes(75));
        assertThat(formWithOvertime(BigDecimal.valueOf(1.25), null).getOvertimeReduction()).isEqualTo(Duration.ofMinutes(75));
        assertThat(formWithOvertime(null, 75).getOvertimeReduction()).isEqualTo(Duration.ofMinutes(75));
        assertThat(formWithOvertime(null, null).getOvertimeReduction()).isNull();
        assertThat(formWithOvertime(ONE, 75).getOvertimeReduction()).isEqualTo(Duration.ofMinutes(135));
    }

    @Test
    void toStringTest() {

        final Person person = new Person();
        final Person holidayReplacement = new Person();

        final LocalDate startDate = LocalDate.of(2022, 1, 2);
        final LocalTime startTime = LocalTime.of(11, 10, 0);

        final LocalDate endDate = LocalDate.of(2022, 2, 2);
        final LocalTime endTime = LocalTime.of(12, 10, 0);

        final HolidayReplacementDto replacementDto = new HolidayReplacementDto();
        replacementDto.setPerson(holidayReplacement);

        final ApplicationForLeaveFormVacationTypeDto vacationTypeDto = new ApplicationForLeaveFormVacationTypeDto();
        vacationTypeDto.setId(1L);
        vacationTypeDto.setLabel("vacation type label");
        vacationTypeDto.setCategory(HOLIDAY);

        final ApplicationForLeaveForm form = new ApplicationForLeaveForm.Builder()
            .person(person)
            .startDate(startDate)
            .startTime(startTime)
            .endDate(endDate)
            .endTime(endTime)
            .vacationType(vacationTypeDto)
            .dayLength(DayLength.ZERO)
            .hoursAndMinutes(Duration.ZERO)
            .reason("Reason")
            .holidayReplacements(List.of(replacementDto))
            .address("Address")
            .teamInformed(true)
            .comment("Comment")
            .build();

        assertThat(form).hasToString("ApplicationForLeaveForm{person=Person{id='null'}, startDate=2022-01-02," +
            " startTime=11:10, endDate=2022-02-02, endTime=12:10, vacationType=ApplicationForLeaveFormVacationTypeDto{id=1," +
            " label='vacation type label', category=HOLIDAY}, dayLength=ZERO, hours=0, minutes=0, " +
            "holidayReplacements=[HolidayReplacementDto{person=Person{id='null'}, departments=null}], teamInformed=true}");
    }

    private ApplicationForLeaveForm formWithOvertime(BigDecimal hours, Integer minutes) {
        final ApplicationForLeaveForm form = new ApplicationForLeaveForm();
        form.setHours(hours);
        form.setMinutes(minutes);
        return form;
    }
}
