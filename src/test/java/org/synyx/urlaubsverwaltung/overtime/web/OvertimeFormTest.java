package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.person.Person;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static java.math.BigDecimal.ONE;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

class OvertimeFormTest {

    @Test
    void ensureCanBeInitializedWithPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final OvertimeForm overtimeForm = new OvertimeForm(person);

        assertThat(overtimeForm.getPerson()).isEqualTo(person);
        assertThat(overtimeForm.getId()).isNull();
        assertThat(overtimeForm.getStartDate()).isNull();
        assertThat(overtimeForm.getEndDate()).isNull();
        assertThat(overtimeForm.getHours()).isNull();
        assertThat(overtimeForm.getMinutes()).isNull();
        assertThat(overtimeForm.getComment()).isNull();
    }

    @Test
    void ensureCanConstructAnOvertimeObject() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final OvertimeForm overtimeForm = new OvertimeForm(person);
        overtimeForm.setStartDate(LocalDate.now(UTC));
        overtimeForm.setEndDate(ZonedDateTime.now(UTC).plusDays(1).toLocalDate());
        overtimeForm.setHours(BigDecimal.ONE);
        overtimeForm.setMinutes(15);
        overtimeForm.setComment("Lorem ipsum");

        final Overtime overtime = overtimeForm.generateOvertime();

        assertThat(overtime.getPerson()).isEqualTo(overtimeForm.getPerson());
        assertThat(overtime.getStartDate()).isEqualTo(overtimeForm.getStartDate());
        assertThat(overtime.getEndDate()).isEqualTo(overtimeForm.getEndDate());
        assertThat(overtime.getDuration()).isEqualTo(Duration.ofMinutes(75));
        assertThat(overtimeForm.getId()).isNull();
    }

    @Test
    void ensureEmptyStartDateValidFromIsoValue() {

        final Person person = new Person();
        final OvertimeForm overtimeForm = new OvertimeForm(person);

        overtimeForm.setStartDate(null);

        assertThat(overtimeForm.getStartDateIsoValue()).isEmpty();
    }

    @Test
    void ensureStartDateValidFromIsoValue() {

        final Person person = new Person();
        final OvertimeForm overtimeForm = new OvertimeForm(person);

        overtimeForm.setStartDate(LocalDate.parse("2020-10-30"));

        assertThat(overtimeForm.getStartDateIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void ensureEmptyEndDateValidFromIsoValue() {

        final Person person = new Person();
        final OvertimeForm overtimeForm = new OvertimeForm(person);

        overtimeForm.setEndDate(null);

        assertThat(overtimeForm.getEndDateIsoValue()).isEmpty();
    }

    @Test
    void ensureEndDateValidFromIsoValue() {

        final Person person = new Person();
        final OvertimeForm overtimeForm = new OvertimeForm(person);

        overtimeForm.setEndDate(LocalDate.parse("2020-10-30"));

        assertThat(overtimeForm.getEndDateIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void ensureCanBeInitializedWithExistentOvertime() throws IllegalAccessException {

        // Simulate existing overtime record
        final Overtime overtime = TestDataCreator.createOvertimeRecord();
        overtime.setDuration(Duration.ofMinutes(75));

        final Field idField = ReflectionUtils.findField(Overtime.class, "id");
        idField.setAccessible(true);
        idField.set(overtime, 42L);

        final OvertimeForm overtimeForm = new OvertimeForm(overtime);

        assertThat(overtimeForm.getId()).isEqualTo(overtime.getId());
        assertThat(overtimeForm.getPerson()).isEqualTo(overtime.getPerson());
        assertThat(overtimeForm.getStartDate()).isEqualTo(overtime.getStartDate());
        assertThat(overtimeForm.getEndDate()).isEqualTo(overtime.getEndDate());
        assertThat(overtimeForm.getHours()).isEqualTo(BigDecimal.ONE);
        assertThat(overtimeForm.getMinutes()).isEqualTo(15);
        assertThat(overtimeForm.getComment()).isNull();
    }


    @Test
    void ensureCanUpdateOvertime() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final OvertimeForm overtimeForm = new OvertimeForm(person);
        overtimeForm.setStartDate(LocalDate.now(UTC));
        overtimeForm.setEndDate(ZonedDateTime.now(UTC).plusDays(1).toLocalDate());
        overtimeForm.setHours(BigDecimal.valueOf(1.5));
        overtimeForm.setMinutes(15);
        overtimeForm.setComment("Lorem ipsum");

        final Overtime overtime = TestDataCreator.createOvertimeRecord();

        overtimeForm.updateOvertime(overtime);

        assertThat(overtime.getPerson()).isEqualTo(overtimeForm.getPerson());
        assertThat(overtime.getStartDate()).isEqualTo(overtimeForm.getStartDate());
        assertThat(overtime.getEndDate()).isEqualTo(overtimeForm.getEndDate());
        assertThat(overtime.getDuration()).isEqualTo(Duration.ofMinutes(105));
        assertThat(overtimeForm.getId()).isNull();
    }

    @Test
    void ensureNegativeNumberOfHours() {
        final Overtime overtime = TestDataCreator.createOvertimeRecord();
        overtime.setDuration(Duration.ofMinutes(-2550));

        final OvertimeForm overtimeForm = new OvertimeForm(overtime);

        assertThat(overtimeForm.getHours()).isEqualTo(BigDecimal.valueOf(42));
        assertThat(overtimeForm.getMinutes()).isEqualTo(30);
        assertThat(overtimeForm.isReduce()).isTrue();
    }

    @Test
    void ensureRoundingOfNumberOfHoursToOneMinute() {
        final Overtime overtime = TestDataCreator.createOvertimeRecord();
        overtime.setDuration(Duration.ofMinutes(61));

        final OvertimeForm overtimeForm = new OvertimeForm(overtime);

        assertThat(overtimeForm.getHours()).isEqualTo(BigDecimal.ONE);
        assertThat(overtimeForm.getMinutes()).isEqualTo(1);
    }

    @Test
    void ensureRoundingOfNumberOfHoursTo59Minutes() {
        final Overtime overtime = TestDataCreator.createOvertimeRecord();
        overtime.setDuration(Duration.ofMinutes(119));

        final OvertimeForm overtimeForm = new OvertimeForm(overtime);

        assertThat(overtimeForm.getHours()).isEqualTo(BigDecimal.ONE);
        assertThat(overtimeForm.getMinutes()).isEqualTo(59);
    }

    @Test
    void ensureDurationOfHourDecimalValueAndMinutes() {
        assertThat(overtimeForm(BigDecimal.valueOf(1.1), 12).getDuration()).isEqualTo(Duration.ofMinutes(78));
        assertThat(overtimeForm(ONE, 15).getDuration()).isEqualTo(Duration.ofMinutes(75));
        assertThat(overtimeForm(BigDecimal.valueOf(1.25), 0).getDuration()).isEqualTo(Duration.ofMinutes(75));
        assertThat(overtimeForm(BigDecimal.valueOf(1.25), null).getDuration()).isEqualTo(Duration.ofMinutes(75));
        assertThat(overtimeForm(null, 75).getDuration()).isEqualTo(Duration.ofMinutes(75));
        assertThat(overtimeForm(null, null).getDuration()).isNull();
        assertThat(overtimeForm(ONE, 75).getDuration()).isEqualTo(Duration.ofMinutes(135));

    }

    private OvertimeForm overtimeForm(BigDecimal hours, Integer minutes) {
        final OvertimeForm overtimeForm = new OvertimeForm();
        overtimeForm.setHours(hours);
        overtimeForm.setMinutes(minutes);
        return overtimeForm;
    }
}
