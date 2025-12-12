package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import static java.math.BigDecimal.ONE;
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
    void ensureDurationOfHourDecimalValueAndMinutes() {
        assertThat(overtimeForm(BigDecimal.valueOf(1.1), 12).getDuration()).isEqualTo(Duration.ofMinutes(78));
        assertThat(overtimeForm(ONE, 15).getDuration()).isEqualTo(Duration.ofMinutes(75));
        assertThat(overtimeForm(BigDecimal.valueOf(1.25), 0).getDuration()).isEqualTo(Duration.ofMinutes(75));
        assertThat(overtimeForm(BigDecimal.valueOf(1.25), null).getDuration()).isEqualTo(Duration.ofMinutes(75));
        assertThat(overtimeForm(null, 75).getDuration()).isEqualTo(Duration.ofMinutes(75));
        assertThat(overtimeForm(null, null).getDuration()).isNull();
        assertThat(overtimeForm(ONE, 75).getDuration()).isEqualTo(Duration.ofMinutes(135));
    }

    @Test
    void ensureThatLongOverflowWillBeCappedByMax() {
        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto();
        overtimeFormDto.setHours(new BigDecimal("2562047788015216"));
        overtimeFormDto.setMinutes(0);
        overtimeFormDto.setReduce(false);

        final Duration duration = overtimeFormDto.getDuration();
        assertThat(duration.toHours()).isEqualTo(2562047788015215L);
        assertThat(duration.isNegative()).isFalse();
    }

    @Test
    void ensureThatLongOverflowWillBeCappedByMin() {
        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto();
        overtimeFormDto.setHours(new BigDecimal("2562047788015216"));
        overtimeFormDto.setMinutes(0);
        overtimeFormDto.setReduce(true);

        final Duration duration = overtimeFormDto.getDuration();
        assertThat(duration.toHours()).isEqualTo(-2562047788015215L);
        assertThat(duration.isNegative()).isTrue();
    }

    private OvertimeFormDto overtimeForm(BigDecimal hours, Integer minutes) {
        final OvertimeFormDto overtimeFormDto = new OvertimeFormDto();
        overtimeFormDto.setHours(hours);
        overtimeFormDto.setMinutes(minutes);
        return overtimeFormDto;
    }
}
