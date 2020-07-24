package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.person.Person;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;


class OvertimeFormTest {

    @Test
    void ensureThrowsIfInitializedWithNullPerson() {
        assertThatIllegalArgumentException().isThrownBy(() -> new OvertimeForm((Person) null));
    }

    @Test
    void ensureCanBeInitializedWithPerson() {

        Person person = createPerson();

        OvertimeForm overtimeForm = new OvertimeForm(person);

        Assert.assertNotNull("Person should be set", overtimeForm.getPerson());
        Assert.assertEquals("Wrong person", person, overtimeForm.getPerson());

        // assert other attributes are null
        Assert.assertNull("Should be not set", overtimeForm.getId());
        Assert.assertNull("Should be not set", overtimeForm.getStartDate());
        Assert.assertNull("Should be not set", overtimeForm.getEndDate());
        Assert.assertNull("Should be not set", overtimeForm.getNumberOfHours());
        Assert.assertNull("Should be not set", overtimeForm.getComment());
    }

    @Test
    void ensureCanConstructAnOvertimeObject() {

        Person person = createPerson();

        OvertimeForm overtimeForm = new OvertimeForm(person);
        overtimeForm.setStartDate(LocalDate.now(UTC));
        overtimeForm.setEndDate(ZonedDateTime.now(UTC).plusDays(1).toLocalDate());
        overtimeForm.setNumberOfHours(BigDecimal.ONE);
        overtimeForm.setComment("Lorem ipsum");

        Overtime overtime = overtimeForm.generateOvertime();

        Assert.assertNotNull("Object should have been constructed successfully", overtime);

        Assert.assertEquals("Wrong person", overtimeForm.getPerson(), overtime.getPerson());
        Assert.assertEquals("Wrong start date", overtimeForm.getStartDate(), overtime.getStartDate());
        Assert.assertEquals("Wrong end date", overtimeForm.getEndDate(), overtime.getEndDate());
        Assert.assertEquals("Wrong number of hours", overtimeForm.getNumberOfHours(), overtime.getHours());

        Assert.assertNull("ID should be not set", overtimeForm.getId());
    }


    @Test
    void ensureThrowsIfGeneratingOvertimeWithoutCheckingFormAttributes() {
        assertThatIllegalArgumentException().isThrownBy(() -> new OvertimeForm(createPerson()).generateOvertime());
    }


    @Test
    void ensureThrowsIfInitializedWithNullOvertime() {
        assertThatIllegalArgumentException().isThrownBy(() -> new OvertimeForm((Overtime) null));
    }

    @Test
    void ensureCanBeInitializedWithExistentOvertime() throws IllegalAccessException {

        // Simulate existing overtime record
        Overtime overtime = DemoDataCreator.createOvertimeRecord();
        Field idField = ReflectionUtils.findField(Overtime.class, "id");
        idField.setAccessible(true);
        idField.set(overtime, 42);

        OvertimeForm overtimeForm = new OvertimeForm(overtime);

        Assert.assertEquals("Wrong ID", overtime.getId(), overtimeForm.getId());
        Assert.assertEquals("Wrong person", overtime.getPerson(), overtimeForm.getPerson());
        Assert.assertEquals("Wrong start date", overtime.getStartDate(), overtimeForm.getStartDate());
        Assert.assertEquals("Wrong end date", overtime.getEndDate(), overtimeForm.getEndDate());
        Assert.assertEquals("Wrong hours", overtime.getHours(), overtimeForm.getNumberOfHours());

        // assert other attributes are null
        Assert.assertNull("Comment should be empty", overtimeForm.getComment());
    }


    @Test
    void ensureCanUpdateOvertime() {

        Person person = createPerson();

        OvertimeForm overtimeForm = new OvertimeForm(person);
        overtimeForm.setStartDate(LocalDate.now(UTC));
        overtimeForm.setEndDate(ZonedDateTime.now(UTC).plusDays(1).toLocalDate());
        overtimeForm.setNumberOfHours(BigDecimal.ONE);
        overtimeForm.setComment("Lorem ipsum");

        Overtime overtime = DemoDataCreator.createOvertimeRecord();

        overtimeForm.updateOvertime(overtime);

        Assert.assertEquals("Wrong person", overtimeForm.getPerson(), overtime.getPerson());
        Assert.assertEquals("Wrong start date", overtimeForm.getStartDate(), overtime.getStartDate());
        Assert.assertEquals("Wrong end date", overtimeForm.getEndDate(), overtime.getEndDate());
        Assert.assertEquals("Wrong number of hours", overtimeForm.getNumberOfHours(), overtime.getHours());

        Assert.assertNull("ID should be not set", overtimeForm.getId());
    }
}
