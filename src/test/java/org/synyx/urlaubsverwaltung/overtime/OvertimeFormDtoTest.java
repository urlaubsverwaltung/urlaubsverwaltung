package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.person.Person;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static java.math.BigDecimal.ONE;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

class OvertimeFormDtoTest {

    @Test
    void ensureCanBeInitializedWithPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(person);

        assertThat(overtimeFormDto.getPerson()).isEqualTo(person);
        assertThat(overtimeFormDto.getId()).isNull();
        assertThat(overtimeFormDto.getStartDate()).isNull();
        assertThat(overtimeFormDto.getEndDate()).isNull();
        assertThat(overtimeFormDto.getHours()).isNull();
        assertThat(overtimeFormDto.getMinutes()).isNull();
        assertThat(overtimeFormDto.getComment()).isNull();
    }

    @Test
    void ensureCanConstructAnOvertimeObject() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(person);
        overtimeFormDto.setStartDate(LocalDate.now(UTC));
        overtimeFormDto.setEndDate(ZonedDateTime.now(UTC).plusDays(1).toLocalDate());
        overtimeFormDto.setHours(BigDecimal.ONE);
        overtimeFormDto.setMinutes(15);
        overtimeFormDto.setComment("Lorem ipsum");

        final OvertimeEntity overtime = overtimeFormDto.generateOvertime();

        assertThat(overtime.getPerson()).isEqualTo(overtimeFormDto.getPerson());
        assertThat(overtime.getStartDate()).isEqualTo(overtimeFormDto.getStartDate());
        assertThat(overtime.getEndDate()).isEqualTo(overtimeFormDto.getEndDate());
        assertThat(overtime.getDuration()).isEqualTo(Duration.ofMinutes(75));
        assertThat(overtimeFormDto.getId()).isNull();
    }

    @Test
    void ensureEmptyStartDateValidFromIsoValue() {

        final Person person = new Person();
        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(person);

        overtimeFormDto.setStartDate(null);

        assertThat(overtimeFormDto.getStartDateIsoValue()).isEmpty();
    }

    @Test
    void ensureStartDateValidFromIsoValue() {

        final Person person = new Person();
        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(person);

        overtimeFormDto.setStartDate(LocalDate.parse("2020-10-30"));

        assertThat(overtimeFormDto.getStartDateIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void ensureEmptyEndDateValidFromIsoValue() {

        final Person person = new Person();
        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(person);

        overtimeFormDto.setEndDate(null);

        assertThat(overtimeFormDto.getEndDateIsoValue()).isEmpty();
    }

    @Test
    void ensureEndDateValidFromIsoValue() {

        final Person person = new Person();
        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(person);

        overtimeFormDto.setEndDate(LocalDate.parse("2020-10-30"));

        assertThat(overtimeFormDto.getEndDateIsoValue()).isEqualTo("2020-10-30");
    }

    @Test
    void ensureCanBeInitializedWithExistentOvertime() throws IllegalAccessException {

        // Simulate existing overtime record
        final OvertimeEntity overtime = TestDataCreator.createOvertimeRecord();
        overtime.setDuration(Duration.ofMinutes(75));

        final Field idField = ReflectionUtils.findField(OvertimeEntity.class, "id");
        idField.setAccessible(true);
        idField.set(overtime, 42L);

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(overtime);

        assertThat(overtimeFormDto.getId()).isEqualTo(overtime.getId());
        assertThat(overtimeFormDto.getPerson()).isEqualTo(overtime.getPerson());
        assertThat(overtimeFormDto.getStartDate()).isEqualTo(overtime.getStartDate());
        assertThat(overtimeFormDto.getEndDate()).isEqualTo(overtime.getEndDate());
        assertThat(overtimeFormDto.getHours()).isEqualTo(BigDecimal.ONE);
        assertThat(overtimeFormDto.getMinutes()).isEqualTo(15);
        assertThat(overtimeFormDto.getComment()).isNull();
    }


    @Test
    void ensureCanUpdateOvertime() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(person);
        overtimeFormDto.setStartDate(LocalDate.now(UTC));
        overtimeFormDto.setEndDate(ZonedDateTime.now(UTC).plusDays(1).toLocalDate());
        overtimeFormDto.setHours(BigDecimal.valueOf(1.5));
        overtimeFormDto.setMinutes(15);
        overtimeFormDto.setComment("Lorem ipsum");

        final OvertimeEntity overtime = TestDataCreator.createOvertimeRecord();

        overtimeFormDto.updateOvertime(overtime);

        assertThat(overtime.getPerson()).isEqualTo(overtimeFormDto.getPerson());
        assertThat(overtime.getStartDate()).isEqualTo(overtimeFormDto.getStartDate());
        assertThat(overtime.getEndDate()).isEqualTo(overtimeFormDto.getEndDate());
        assertThat(overtime.getDuration()).isEqualTo(Duration.ofMinutes(105));
        assertThat(overtimeFormDto.getId()).isNull();
    }

    @Test
    void ensureNegativeNumberOfHours() {
        final OvertimeEntity overtime = TestDataCreator.createOvertimeRecord();
        overtime.setDuration(Duration.ofMinutes(-2550));

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(overtime);

        assertThat(overtimeFormDto.getHours()).isEqualTo(BigDecimal.valueOf(42));
        assertThat(overtimeFormDto.getMinutes()).isEqualTo(30);
        assertThat(overtimeFormDto.isReduce()).isTrue();
    }

    @Test
    void ensureRoundingOfNumberOfHoursToOneMinute() {
        final OvertimeEntity overtime = TestDataCreator.createOvertimeRecord();
        overtime.setDuration(Duration.ofMinutes(61));

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(overtime);

        assertThat(overtimeFormDto.getHours()).isEqualTo(BigDecimal.ONE);
        assertThat(overtimeFormDto.getMinutes()).isEqualTo(1);
    }

    @Test
    void ensureRoundingOfNumberOfHoursTo59Minutes() {
        final OvertimeEntity overtime = TestDataCreator.createOvertimeRecord();
        overtime.setDuration(Duration.ofMinutes(119));

        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto(overtime);

        assertThat(overtimeFormDto.getHours()).isEqualTo(BigDecimal.ONE);
        assertThat(overtimeFormDto.getMinutes()).isEqualTo(59);
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

    private OvertimeFormDto overtimeForm(BigDecimal hours, Integer minutes) {
        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto();
        overtimeFormDto.setHours(hours);
        overtimeFormDto.setMinutes(minutes);
        return overtimeFormDto;
    }
}
