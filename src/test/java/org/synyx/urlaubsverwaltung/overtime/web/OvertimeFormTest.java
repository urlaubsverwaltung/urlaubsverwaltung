package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.person.Person;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


class OvertimeFormTest {

    @Test
    void ensureThrowsIfInitializedWithNullPerson() {
        assertThatIllegalArgumentException().isThrownBy(() -> new OvertimeForm((Person) null));
    }

    @Test
    void ensureCanBeInitializedWithPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final OvertimeForm overtimeForm = new OvertimeForm(person);

        assertThat(overtimeForm.getPerson()).isEqualTo(person);
        assertThat(overtimeForm.getId()).isNull();
        assertThat(overtimeForm.getStartDate()).isNull();
        assertThat(overtimeForm.getEndDate()).isNull();
        assertThat(overtimeForm.getNumberOfHours()).isNull();
        assertThat(overtimeForm.getComment()).isNull();
    }

    @Test
    void ensureCanConstructAnOvertimeObject() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final OvertimeForm overtimeForm = new OvertimeForm(person);
        overtimeForm.setStartDate(LocalDate.now(UTC));
        overtimeForm.setEndDate(ZonedDateTime.now(UTC).plusDays(1).toLocalDate());
        overtimeForm.setNumberOfHours(BigDecimal.ONE);
        overtimeForm.setComment("Lorem ipsum");

        final Overtime overtime = overtimeForm.generateOvertime();

        assertThat(overtime.getPerson()).isEqualTo(overtimeForm.getPerson());
        assertThat(overtime.getStartDate()).isEqualTo(overtimeForm.getStartDate());
        assertThat(overtime.getEndDate()).isEqualTo(overtimeForm.getEndDate());
        assertThat(overtime.getHours()).isEqualTo(overtimeForm.getNumberOfHours());
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
    void ensureThrowsIfGeneratingOvertimeWithoutCheckingFormAttributes() {
        assertThatIllegalArgumentException().isThrownBy(() -> new OvertimeForm(new Person("muster", "Muster", "Marlene", "muster@example.org")).generateOvertime());
    }


    @Test
    void ensureThrowsIfInitializedWithNullOvertime() {
        assertThatIllegalArgumentException().isThrownBy(() -> new OvertimeForm((Overtime) null));
    }

    @Test
    void ensureCanBeInitializedWithExistentOvertime() throws IllegalAccessException {

        // Simulate existing overtime record
        final Overtime overtime = TestDataCreator.createOvertimeRecord();
        final Field idField = ReflectionUtils.findField(Overtime.class, "id");
        idField.setAccessible(true);
        idField.set(overtime, 42);

        final OvertimeForm overtimeForm = new OvertimeForm(overtime);

        assertThat(overtimeForm.getId()).isEqualTo(overtime.getId());
        assertThat(overtimeForm.getPerson()).isEqualTo(overtime.getPerson());
        assertThat(overtimeForm.getStartDate()).isEqualTo(overtime.getStartDate());
        assertThat(overtimeForm.getEndDate()).isEqualTo(overtime.getEndDate());
        assertThat(overtimeForm.getNumberOfHours()).isEqualTo(overtime.getHours());
        assertThat(overtimeForm.getComment()).isNull();
    }


    @Test
    void ensureCanUpdateOvertime() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final OvertimeForm overtimeForm = new OvertimeForm(person);
        overtimeForm.setStartDate(LocalDate.now(UTC));
        overtimeForm.setEndDate(ZonedDateTime.now(UTC).plusDays(1).toLocalDate());
        overtimeForm.setNumberOfHours(BigDecimal.ONE);
        overtimeForm.setComment("Lorem ipsum");

        final Overtime overtime = TestDataCreator.createOvertimeRecord();

        overtimeForm.updateOvertime(overtime);

        assertThat(overtime.getPerson()).isEqualTo(overtimeForm.getPerson());
        assertThat(overtime.getStartDate()).isEqualTo(overtimeForm.getStartDate());
        assertThat(overtime.getEndDate()).isEqualTo(overtimeForm.getEndDate());
        assertThat(overtime.getHours()).isEqualTo(overtimeForm.getNumberOfHours());
        assertThat(overtimeForm.getId()).isNull();
    }
}
